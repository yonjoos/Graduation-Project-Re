import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';

function Auth(SpecificComponent, option, adminRoute = null) {

    function AuthenticationCheck(props) {
        const isAuthenticated = useSelector(state => state.isAuthenticated);
        const userRole = useSelector(state => state.userRole);

        const navigate = useNavigate();

        useEffect(() => {
            console.log("isAuthenticated : " + isAuthenticated);
            console.log("userRole : " + userRole);
            console.log("this : " + this);

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
                    if (option === false)
                        navigate('/')
                }
            }
        }, [isAuthenticated, navigate, userRole])

        return (
            <SpecificComponent {...props}/>
        );
    }

    return <AuthenticationCheck />
}

export default Auth;
