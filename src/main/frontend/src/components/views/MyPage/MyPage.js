// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Button, Menu } from 'antd';
import { request } from '../../../hoc/request';
import { useDispatch } from 'react-redux';
import { logout } from '../../../_actions/actions'
import { setAuthHeader, setUserRole } from '../../../hoc/request';
function MyPage() {
    const [data, setData] = useState(null); //업데이트랑 기존 정보 받아올 떄 동시사용, 업데이트 할떄는 data에 비밀번호까지 실어서 보내고, 다시 effect로 getUserInfo할때는 userDto로 받음(비밀번호 필드 누락)
    const navigate = useNavigate();
    const dispatch = useDispatch();

    const [selectedOption, setSelectedOption] = useState('info'); // Default selected option
    const [isUpdateButtonEnabled, setIsUpdateButtonEnabled] = useState(false);

    const handleMenuClick = (e) => {
        setSelectedOption(e.key);
    };

    const updateInfo = (updatedData) => {
        request('PUT', '/updateUserInfo', updatedData) // Adjust the endpoint and data accordingly
        .then((response) => {
            // Handle success, e.g., show a success message
            alert('정보가 업데이트되었습니다.');
            setData((prevData) => ({ ...prevData, ...updatedData, password: '' })); // Update password to empty string
            
            navigate('/myPage');
        })
        .catch((error) => {
            // Handle error, e.g., display an error message
            console.error("Error updating information:", error);
            alert('정보 업데이트에 실패했습니다. 기존의 비밀번호를 올바르게 입력하세요');
        });
    };

    const handleInputChange = (fieldName, value) => {
        setData((prevData) => ({ ...prevData, [fieldName]: value }));
    };

    useEffect(() => {
        // Enable the Update button only if required fields are filled
        const isRequiredFieldsFilled = data && data.nickName && data.password;
        setIsUpdateButtonEnabled(isRequiredFieldsFilled);
    }, [data]);

    const onClickHandler = () => {
          // Assuming request function structure: request(method, endpoint, data)
        request('POST', '/signOut', {}) // Adjust the endpoint accordingly
            .then((response) => {
                // Show success message
                alert('회원 탈퇴가 완료되었습니다.');
                setAuthHeader(null); // Clear token in local storage(로컬 스토리지에서 토큰 지우는 건 인증받지 못한 사람은 api호출을 못하게 하기 위함)
                setUserRole(null); //로컬 스토리지에서 역할을 지우는건 역할 없는 사람은 api호출을 못하게 하기 위함
                localStorage.clear();   // 로컬스토리지 클리어 반드시 해주기!! 얘 안하면 로그아웃 상태에서 새로고침 시 랜딩페이지가 렌더링되지 않음!!
                dispatch(logout()); // Dispatch login success action with role
                navigate('/'); // Redirect or perform any other action
            })
            .catch((error) => {
                // Handle error, e.g., display an error message
                console.error("Error signing out:", error);
                alert('회원 탈퇴에 실패했습니다.');
            });
    }

    useEffect(() => {
        request('GET', '/userInfo', {})
            .then((response) => {
                setData(response.data);
            })
            .catch((error) => {
                // Handle error, e.g., redirect to login or display an error message
                console.error("Error fetching data:", error);
            });
    }, []);


    return (
        <div>
            <div>
                <Row justify="center" style={{ marginTop: '20px' }}>
                    <Col xs={24} sm={16} md={12} lg={8}>
                        <Card title="Backend Response in Project Page" style={{ width: '100%' }}>
                            <div>현재 계정 정보</div>
                            {data && (
                                <ul>
                                    <li><strong>ID:</strong> {data.id}</li>
                                    <li><strong>User Name:</strong> {data.userName}</li>
                                    <li><strong>Nick Name:</strong> {data.nickName}</li>
                                    <li><strong>Email:</strong> {data.email}</li>
                                    {/* Add other properties as needed */}
                                </ul>
                            )}
                        </Card>
                    </Col>
                </Row>
            </div>     
            <div>
                <h2>
                    This is a My Page
                </h2>
            <div>
                <div style={{ display: 'flex', flexDirection: 'row' }}>
                    <div style={{ width: '25%' }}>
                        <Menu mode="vertical" selectedKeys={[selectedOption]} onClick={handleMenuClick}>
                            <Menu.Item key="info">정보 수정</Menu.Item>
                            <Menu.Item key="password">비밀번호 변경</Menu.Item>
                            <Menu.Item key="withdrawal">회원 탈퇴</Menu.Item>
                        </Menu>
                    </div>
                    <div style={{ width: '75%' }}>
                        {selectedOption === 'info' && (
                            <Card title="정보 수정" style={{ width: '100%' }}>
                                {data && (
                        <form>
                            <div>
                                <label>등록된 이메일 주소:</label>
                                <input
                                    type="email"
                                    value={data.email}
                                    readOnly
                                    disabled // Prevent interaction with the field
                                    style={{ backgroundColor: '#f0f0f0' }}
                                />
                            </div>
                            <div>
                                <label>닉네임:</label>
                                <input
                                    type="text"
                                    value={data.nickName}
                                    onChange={(e) => handleInputChange('nickName', e.target.value)}
                                />
                            </div>
                            <div>
                                <label>이름:</label>
                                <input
                                    type="text"
                                    value={data.userName}
                                    onChange={(e) => handleInputChange('userName', e.target.value)}
                                />
                            </div>
                            <div>
                                <label>패스워드:</label>
                                <input
                                    type="password"
                                    value={data.password || ''} //비밀번호는 백엔드에서 가져오지 못했으므로 빈칸으로 세팅
                                    onChange={(e) => handleInputChange('password', e.target.value)}
                                />
                            </div>
                            
                            <Button type="primary" onClick={() => updateInfo(data)}
                                    disabled={!isUpdateButtonEnabled}>
                                정보 업데이트
                            </Button>
                        </form>
                    )}
                            </Card>
                        )}
                        {selectedOption === 'password' && (
                            <Card title="비밀번호 변경" style={{ width: '100%' }}>
                                {/* Render your password change form here */}
                            </Card>
                        )}
                        {selectedOption === 'withdrawal' && (
                            <Card title="회원 탈퇴" style={{ width: '100%' }}>
                                {/* Render your membership withdrawal form here */}
                                <Button type="primary" onClick={onClickHandler}>
                                탈퇴하기
                                </Button>
                            </Card>
                        )}
                    </div>
                </div>
            </div>
            </div>

            
        </div>
    );
    
}

export default MyPage;