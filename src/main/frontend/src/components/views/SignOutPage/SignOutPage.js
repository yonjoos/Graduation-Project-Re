// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Button } from 'antd';
import { request, getAuthToken} from '../../../hoc/request';

function SignOutPage() {
    const [data, setData] = useState(null);
    const navigate = useNavigate();

 
    
  
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
                </h2>
            </div>

            
        </div>
    );
    
}

export default SignOutPage;