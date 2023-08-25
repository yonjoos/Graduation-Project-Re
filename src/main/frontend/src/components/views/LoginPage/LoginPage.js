import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Row, Col, Tabs, Input, Button, message } from 'antd';
import { request, setAuthHeader, setHasPortfolio, setUserRole } from '../../../hoc/request';
import { loginSuccess } from '../../../_actions/actions'

function LoginPage() {
    const navigate = useNavigate();
    const dispatch = useDispatch();

    // 로그인 및 회원가입 폼의 상태를 관리할 state 변수들
    const [active, setActive] = useState('login'); // 로그인과 회원가입 탭을 전환하기 위한 활성 탭 상태
    const [userName, setUserName] = useState(''); // 이름
    const [nickName, setNickName] = useState('');   // 성
    const [email, setEmail] = useState('');         // 사용자명
    const [password, setPassword] = useState('');   // 비밀번호

    // 입력 필드 변경 시 호출되는 이벤트 핸들러
    const onChangeHandler = (event) => {
        const name = event.target.name;
        const value = event.target.value;
        
        // 입력 필드마다 해당하는 state 변수를 업데이트
        if (name === 'userName') setUserName(value);
        else if (name === 'nickName') setNickName(value);
        else if (name === 'email') setEmail(value);
        else if (name === 'password') setPassword(value);
    };



    // 로그인 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitLogin = (e) => {
        e.preventDefault();

        if (!email) {
            message.warning('이메일을 입력해주세요.');
            return;
        }
        if (!password) {
            message.warning('비밀번호를 입력해주세요.');
            return;
        }

        onLogin(e, email, password); // 부모 컴포넌트로부터 전달받은 onLogin 함수 호출
        navigate('/');
    };

    const onLogin = (event, email, password) => {
        event.preventDefault();
    
        request('POST', '/login', {
            email: email,
            password: password
        })
        .then((response) => {
            const { token, role, isCreated } = response.data;
            dispatch(loginSuccess(token, role, isCreated)); // Dispatch login success action with role
            setAuthHeader(token); // Set token in local storage
            setUserRole(role);
            setHasPortfolio(isCreated);
            localStorage.setItem('localStorageCleared', 'true'); //로컬 스토리지가 비워졌다고 명시. F5문제를 위해 설정한 임시 방편
            alert("로그인에 성공하였습니다.");
        })
        .catch((error) => {
            alert("로그인에 실패하였습니다.");
        });
    };
    


    // 회원가입 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitRegister = (e) => {
        e.preventDefault();

        if (!userName) {
            message.warning('이름을 입력해주세요.');
            return;
        }
        if (!nickName) {
            message.warning('닉네임을 입력해주세요.');
            return;
        }
        if (!email) {
            message.warning('이메일을 입력해주세요.');
            return;
        }
        if (!password) {
            message.warning('비밀번호를 설정해주세요.');
            return;
        }

        // 부모 컴포넌트로부터 전달받은 onRegister 함수 호출
        onRegister(e, userName, nickName, email, password);
        navigate('/');
    };

    // Login 컴포넌트 내에서 회원가입 액션을 처리하는 함수를 정의
    const onRegister = (event, userName, nickName, email, password) => {
        event.preventDefault();

        request('POST', '/register', {
            userName: userName,
            nickName: nickName,
            email: email,
            password: password
        })
            // 회원가입 성공
            .then((response) => {
                //setAuthHeader(response.data.token);     // 헤더에 토큰 설정   -> 회원가입 시 토큰 설정 배제
                alert("회원가입에 성공하였습니다.");
            })
            // 회원가입 실패
            .catch((error) => {
                //setAuthHeader(null);                    // 헤더에 토큰 지우기 -> 회원가입 시 토큰 설정 배제
                alert("회원가입에 실패하였습니다.");
            });
    };


    return (
        <Row justify="center">
            <Col span={12}>
                <Tabs activeKey={active} onChange={(key) => setActive(key)} centered>
                    <Tabs tab="Login" key="login">
                        <form onSubmit={onSubmitLogin}>
                            {/** mb-4 : "margin Bottom 4"를 의미하며 요소 하단에 여백을 적용하는 데 사용 */}
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="email"
                                    placeholder="Email"
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
                                    name="userName"
                                    placeholder="User Name"
                                    onChange={onChangeHandler}

                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="nickName"
                                    placeholder="Nick Name"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="email"
                                    placeholder="Email"
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

export default LoginPage;