import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';

function Auth(SpecificComponent, option, adminRoute = null, hasPortfolio = null) {

    function AuthenticationCheck(props) {
        const isAuthenticated = useSelector(state => state.isAuthenticated);
        const userRole = useSelector(state => state.userRole);
        const userPortfolio = useSelector(state => state.userPortfolio);

        const getAuthToken = getAuthToken(); //로컬스토리지에서 토큰이 있으면 가져옴
        const getUserRole = getUserRole(); //로컬스토리지에서 해당 유저의 역할 가져옴
        const getHasPortfolio = getHasPortfolio(); // 로컬스토리지에서 해당 유저의 포트폴리오 유무 여부를 가져옴

        const navigate = useNavigate();

        useEffect(() => {
            console.log("===================================================");
            console.log("useSelector");
            console.log("---------------------------------------------------");
            console.log('isAuthenticated:', isAuthenticated);
            console.log('userRole:', userRole);
            console.log('userPortfolio:', userPortfolio);
            console.log('hasPortfolio:', hasPortfolio);
            console.log("---------------------------------------------------");
            console.log("getMethod");
            console.log("---------------------------------------------------");
            console.log('getAuthToken:', getAuthToken);
            console.log('getUserRole:', getUserRole);
            console.log('getHasPortfolio:', getHasPortfolio);
            console.log("===================================================");

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
                    // userRole === 'USER'인 사람 중, 포트폴리오를 작성한 사람은 접근할 수 없는 페이지
                    else if (userPortfolio && hasPortfolio) {
                        navigate('/portfolio')
                    }
                    // userRole === 'USER'인 사람 중, 포트폴리오를 작성하지 않은 사람은 접근할 수 없는 페이지
                    else if (!userPortfolio && !hasPortfolio) {
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
