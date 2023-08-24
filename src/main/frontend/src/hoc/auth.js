import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';

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
