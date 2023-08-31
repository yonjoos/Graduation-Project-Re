import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';


/*
=======================================================================================================================================
=============================================  /  HOC file - auth.js  /  ==============================================================
=======================================================================================================================================

** HOC file : Redux 라이브러리를 사용함에 있어서 encapsulate를 위한 패턴이라고 GPT 씨가 말씀하심

** What HOC Does : 컴포넌트에 어떤 것을 추가해서 새로운 컴포넌트로 내보내는 역할
    ex >> 로그인 정보에 인증 정보를 추가해서 새로운 컴포넌트로 내보냄 (아닐 수도....)

** 이 프로젝트에서의 Auth 함수 :

    < 매개변수 >
    1) SpecificationComponent : Components such as '...Page'
     - Auth 함수의 로직을 통과하면 navigate 정보가 추가로 입혀짐.
     - 기존의 데이터(<Html> 소스코드) 에 무언가가 추가된(navigate + else) 새로운 컴포넌트로 내보내짐

    2)
    3)
    4)
     
     
    < return 값 >
        return값(Html코드 + else) 은 App.js의 <Rout>의 element 속성의 값으로 들어가게 됨

=======================================================================================================================================
*/

function Auth(SpecificComponent, option, adminRoute = null, hasPortfolio = null) {

    function AuthenticationCheck(props) {
        const isAuthenticated = useSelector(state => state.isAuthenticated);
        const userRole = useSelector(state => state.userRole);
        const userPortfolio = useSelector(state => state.userPortfolio);

        const navigate = useNavigate();

        useEffect(() => {
            console.log('isAuthenticated:', isAuthenticated);
            console.log('userRole:', userRole);
            console.log('userPortfolio:', userPortfolio);
            console.log('hasPortfolio:', hasPortfolio);

            // 로그아웃 유저에 대해
            if (isAuthenticated === null || isAuthenticated === "null" || isAuthenticated === false) {
                // 로그인한 유저만 접근할 수 있는 곳이나 ADMIN만 접근할 수 있는 곳에 접근할 때 접근 못하도록
                if (option === true || adminRoute) {
                    navigate('/login')
                }
            }
            // 로그인 유저 (USER, ADMIN)에 대해
            else {
                // userRole === 'USER"인 사람은 ADMIN만 접근할 수 있는 페이지에 접근 못하도록
                if (adminRoute && userRole !== 'ADMIN') {
                    navigate('/')
                } else {
                    // option === false이면 로그인한 유저가 들어갈 수 없는 페이지
                    if (option === false) {
                        navigate('/')
                    }

                    //user인데, 로그인이 되어있고, 포폴이 없는데, 포폴이 있어야만 접근 가능한 루트이고, 그 컴포넌트가 update또는 delete페이지이면 /portfolio로 이동(시홍)
                    else if ((!userPortfolio && hasPortfolio && SpecificComponent.name==="UpdatePortfolioPage")
                                || (!userPortfolio && hasPortfolio && SpecificComponent.name==="DeletePortfolioPage")) {
                        navigate('/portfolio')
                    }

                    //user인데, 로그인이 되어있고, 포폴이 있는데, 포폴이 없어야만 접근 가능한 루트이고, 그 컴포넌트가 upload페이지이면 /portfolio로 이동(시홍)
                    else if (userPortfolio && !hasPortfolio && SpecificComponent.name==="UploadPortfolioPage") {
                        navigate('/portfolio')
                    }
                }
            }
        }, [isAuthenticated, navigate, userRole, userPortfolio])

        return (
            <SpecificComponent {...props}/>
        );
    }

    return <AuthenticationCheck />
}

export default Auth;
