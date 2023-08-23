// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Button } from 'antd';
import { request, getAuthToken} from '../../../hoc/request';

function SignOutPage() {
    const [data, setData] = useState(null);
    const navigate = useNavigate();

    // const onClickHandler = () => {
    //     // Assuming request function structure: request(method, endpoint, data)
    //     request('POST', '/signOut', {}) // Adjust the endpoint accordingly
    //         .then((response) => {
    //             // Show success message
    //             message.success('회원 탈퇴가 완료되었습니다.');
    //             dispatch(logout()); // Dispatch login success action with role
    //             navigate('/'); // Redirect or perform any other action
    //         })
    //         .catch((error) => {
    //             // Handle error, e.g., display an error message
    //             console.error("Error signing out:", error);
    //             message.error('회원 탈퇴에 실패했습니다.');
    //         });
    //   }
    
  
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
                    This is a Sign Out Page

                    {/* <div style = {{ display: 'flex', justifyContent: 'center', }}> 
                        <Button type="primary" onClick={onClickHandler}>
                            Ok
                        </Button>
                    </div> */}
                </h2>
            </div>

            
        </div>
    );
    
}

export default SignOutPage;