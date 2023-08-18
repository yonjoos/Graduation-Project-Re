import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Row, Col, Tabs, Input, Button } from 'antd';
import { request, setAuthHeader } from '../../../hoc/auth';

function Login(props) {
    const navigate = useNavigate();

    // 로그인 및 회원가입 폼의 상태를 관리할 state 변수들
    const [active, setActive] = useState('login'); // 로그인과 회원가입 탭을 전환하기 위한 활성 탭 상태
    const [firstName, setFirstName] = useState(''); // 이름
    const [lastName, setLastName] = useState('');   // 성
    const [login, setLogin] = useState('');         // 사용자명
    const [password, setPassword] = useState('');   // 비밀번호

    // 입력 필드 변경 시 호출되는 이벤트 핸들러
    const onChangeHandler = (event) => {
        const name = event.target.name;
        const value = event.target.value;
        
        // 입력 필드마다 해당하는 state 변수를 업데이트
        if (name === 'firstName') setFirstName(value);
        else if (name === 'lastName') setLastName(value);
        else if (name === 'login') setLogin(value);
        else if (name === 'password') setPassword(value);
    };

    // Login 컴포넌트 내에서 로그인 액션을 처리하는 함수를 정의
    const onLogin = (event, username, password) => {
        event.preventDefault();

        request('POST', '/login', {
            login: username,
            password: password
        })
            // 로그인 성공
            .then((response) => {
                setAuthHeader(response.data.token);         // 헤더에 토큰 설정
                props.setIsLogin(true);                      // 로그인 상태로 설정
                alert("로그인에 성공하였습니다.");
            })
            // 로그인 실패
            .catch((error) => {
                setAuthHeader(null);                        // 헤더에 토큰 지우기
                alert("로그인에 실패하였습니다.");
            });
    };

    // Login 컴포넌트 내에서 회원가입 액션을 처리하는 함수를 정의
    const onRegister = (event, firstName, lastName, username, password) => {
        event.preventDefault();

        request('POST', '/register', {
            firstName: firstName,
            lastName: lastName,
            login: username,
            password: password
        })
            // 회원가입 성공
            .then((response) => {
                setAuthHeader(response.data.token);     // 헤더에 토큰 설정
                alert("회원가입에 성공하였습니다.");
            })
            // 회원가입 실패
            .catch((error) => {
                setAuthHeader(null);                    // 헤더에 토큰 지우기
                alert("회원가입에 실패하였습니다.");
            });
    };

    // 로그인 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitLogin = (e) => {
        e.preventDefault();
        onLogin(e, login, password); // 부모 컴포넌트로부터 전달받은 onLogin 함수 호출
        navigate('/');
    };

    // 회원가입 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitRegister = (e) => {
        e.preventDefault();
        // 부모 컴포넌트로부터 전달받은 onRegister 함수 호출
        onRegister(e, firstName, lastName, login, password);
        navigate('/');
    };

    return (
        <Row justify="center">
            <Col span={12}>
                <Tabs activeKey={active} onChange={(key) => setActive(key)} centered>
                    <Tabs tab="Login" key="login">
                        <form onSubmit={onSubmitLogin}>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="login"
                                    placeholder="Username"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="password"
                                    name="password"
                                    placeholder="Password"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <Button type="primary" block htmlType="submit">Sign In</Button>
                        </form>
                    </Tabs>
                    <Tabs tab="Register" key="register">
                        <form onSubmit={onSubmitRegister}>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="firstName"
                                    placeholder="First Name"
                                    onChange={onChangeHandler}

                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="lastName"
                                    placeholder="Last Name"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="login"
                                    placeholder="Username"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="password"
                                    name="password"
                                    placeholder="Password"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <Button type="primary" block htmlType="submit">Sign Up</Button>
                        </form>
                    </Tabs>
                </Tabs>
            </Col>
        </Row>
    );
}

export default Login;