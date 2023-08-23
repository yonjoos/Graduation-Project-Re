// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Button } from 'antd';
import { request, getAuthToken} from '../../../hoc/request';
import { useDispatch } from 'react-redux';
import { logout } from '../../../_actions/actions'
import { setAuthHeader, setUserRole } from '../../../hoc/request';
function MyPage() {
    const [data, setData] = useState(null);
    const navigate = useNavigate();
    const dispatch = useDispatch();

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
            </div>

            {/** justifyContent: 'center'를 적용시키려면 display: 'flex'가 함께 있어야 한다. */}
            <div style = {{ display: 'flex', justifyContent: 'center', }}> 
            <Button type="primary" onClick={onClickHandler}>
                Sign Out
            </Button>
            </div>
        </div>
    );
    
}

export default MyPage;