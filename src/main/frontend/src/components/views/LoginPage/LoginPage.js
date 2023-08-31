import React, { useState, useEffect } from 'react';
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
    const [nicknameAvailability, setNicknameAvailability] = useState(null); //닉네임 중복 여부
    const [verificationCode, setVerificationCode] = useState(''); // 백엔드에서 받은 인증번호
    const [userInputCode, setUserInputCode] = useState(''); // 유저가 입력한 인증번호
    const [verificationSuccess, setVerificationSuccess] = useState(null); // 인증번호 확인 성공 여부
    

/*    useEffect(() => {
        if (userInputCode==='' || verificationCode==='')
        {
            setVerificationSuccess(null);
        }
        else if (userInputCode === verificationCode) {
            setVerificationSuccess(true);
        } else {
            setVerificationSuccess(false);
        }
    }, [userInputCode, verificationCode]);
*/

    // 입력 필드 변경 시 호출되는 이벤트 핸들러
    const onChangeHandler = (event) => {
        const name = event.target.name;
        const value = event.target.value;

        // 입력 필드마다 해당하는 state 변수를 업데이트
        if (name === 'userName') setUserName(value);
        else if (name === 'nickName') { //닉네임 필드를 입력할 때
            setNickName(value); //현재 입력 값을 닉네임 변수에 세팅
            setNicknameAvailability(null); //새로운 값을 입력할 떄는 닉네임 사용 가능 여부를 null로 초기화
        }
        else if (name === 'email') setEmail(value);
        else if (name === 'password') setPassword(value);
        else if (name === 'userInputCode') setUserInputCode(value);
    };

    // 중복 확인 버튼을 누르면 호출되는 이벤트 핸들러
    const handleDuplicateCheck = () => {
        request('GET', `/nicknameDuplicate?nickname=${nickName}`) //백엔드에 현재 입력받은 nickname을 가진 회원이 있는 지 찾고, 백엔드는 해당 닉네임으로 유저 생성 가능하면 available:true /불가능하면 available:false 반환
            .then((response) => {
                const isAvailable = response.data.available;
                setNicknameAvailability(isAvailable); //닉네임 사용 가능 여부 값을 상태변수에 저장
            })
            .catch((error) => {
                alert("잠시 후 다시 시도해보세요.");
            });
    };

    const handleSendVerificationCode = () => {
        if (!email) {
            message.warning('이메일을 입력해주세요.');
            return;
        }

        const emailParams = new URLSearchParams({ email: email });
        console.log('email',email);

        request('POST', `/mailConfirm?${emailParams}`)
            .then((response) => {
                message.success('해당 이메일로 인증코드가 발송되었습니다.');
                setVerificationCode(response.data.code);
            })
            .catch((error) => {
                alert("인증코드 발송에 실패했습니다. 다시 시도하세요.");
            });
    };

    const handleVerifyCode = () => {
        if (!userInputCode || userInputCode==='') {
            message.warning('인증코드를 입력해주세요');
            setVerificationSuccess(null);
            return;
        }

        console.log('입력한 인증번호: ',userInputCode );
        console.log('실제 인증번호: ',verificationCode);
        if(userInputCode===verificationCode)
        {
            setVerificationSuccess(true);
        }

        else{
            setVerificationSuccess(false);
        }

        console.log('둘이 같나? ',verificationSuccess);

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
        e.preventDefault(); //페이지에서 새로고침하면..

        if (!userName) {
            message.warning('이름을 입력해주세요.');
            return;
        }
        if (!nickName) {
            message.warning('닉네임을 입력해주세요.');
            return;
        }
        if (nicknameAvailability === false) { //만약 닉네임 중복인 걸 아는데도 불구하고 회원가입 버튼을 누르는 경우
            message.warning('이미 사용 중인 닉네임입니다. 닉네임 변경 후 다시 시도하세요.');
            return;
        }
        if (nicknameAvailability === null) {
            message.warning('닉네임 중복 확인을 먼저 해주세요.');
            return;
        }
        if (!email) {
            message.warning('이메일을 입력해주세요.');
            return;
        }
        if (verificationSuccess === null) {
            message.warning('이메일 인증 확인이 아직 되지 않았습니다. 이메일 인증 후 다시 시도해주세요.')
            return;
        }
        if (verificationSuccess === false) {
            message.warning('이메일 인증 코드가 제대로 입력되지 않았습니다. 인증 번호를 다시 입력해주세요.')
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
                            <Button type="primary" block htmlType="submit">Log In</Button>
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
                                <div style={{ display: "flex" }}>
                                    <Input
                                        type="text"
                                        name="nickName"
                                        placeholder="Nick Name"
                                        onChange={onChangeHandler}
                                    />
                                    <Button onClick={handleDuplicateCheck}>닉네임 중복 확인</Button>
                                </div>
                                {nicknameAvailability !== null && ( // 중복 확인 버튼 눌러서 중복 확인 여부를 알아왔을 때,
                                    // 사용 가능한 닉네임인 경우 초록색으로 아래에 사용 가능하단 문구를 렌더링
                                    // 사용 불가능한 닉네임인 경우 빨간색으로 아래에 사용 불가능하단 문구를 렌더링
                                    // 빈 문자열로 중복확인 한 경우 빨간색으로 다시 입력하라는 문구를 렌더링
                                    <div className={nicknameAvailability ? "verification-success" : "verification-failure"} style={{ color: (nickName === "" || !nicknameAvailability) ? "#ff4d4f" : "#00cc00" }}>
                                        {(() => {
                                            if (nickName === "") {
                                                return "빈 문자열로는 닉네임을 생성할 수 없습니다. 다시 입력하세요"
                                            }
                                            else if (!nicknameAvailability) {
                                                return "이미 사용 중인 닉네임입니다. 다른 닉네임을 입력하세요."
                                            }
                                            else {
                                                return "사용 가능한 닉네임입니다!"
                                            }
                                        })()}

                                    </div>


                                )}
                            </div>
                            <div className="form-outline mb-4">
                                <div style={{ display: "flex" }}>
                                    <Input
                                        type="text"
                                        name="email"
                                        placeholder="Email"
                                        onChange={onChangeHandler}
                                    />
                                    <Button onClick={handleSendVerificationCode}>인증코드 발송</Button>
                                </div>
                            </div>
                            <div className="form-outline mb-4">
                                <div style={{ display: "flex" }}>
                                <Input disabled={verificationSuccess} // 확인 버튼 눌렀을 때 이메일 인증번호가 맞으면 폼 자체가 disable됨
                                    type="text"
                                    name="userInputCode"
                                    placeholder="Verification Code"
                                    onChange={onChangeHandler}
                                />
                                <Button onClick={handleVerifyCode}>확인</Button>
                                </div>
                                <div className={verificationSuccess ? "verification-success" : "verification-failure"} style={{ color: verificationSuccess ? "#00cc00" : "#ff4d4f" }}>
                                {verificationSuccess !== null && (
                                    verificationSuccess ? "인증 완료되었습니다." : "인증코드가 맞지 않습니다. 다시 입력하세요"
                                )}
                            </div>
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