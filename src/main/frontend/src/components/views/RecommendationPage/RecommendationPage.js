// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { Card, Row, Col } from 'antd';
import { request } from '../../../hoc/auth';

function RecommendationPage() {
    const isAuthenticated = useSelector(state => state.isAuthenticated);
    const [data, setData] = useState([]);
    

    useEffect(() => {
        request('GET', '/messages', {})
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
        {!isAuthenticated && (
            <div>
                <h2> This is a Recommendation Page </h2>
                <br/>
                <br/>
                <h3> You have to do Login </h3>
            </div>
        )}
        {isAuthenticated && (
            <Row justify="center" style={{ marginTop: '20px' }}>
                <Col xs={24} sm={16} md={12} lg={8}>
                    <Card title="Backend Response" style={{ width: '100%' }}>
                        <p>Content:</p>
                        <ul>
                            {data.map((line, index) => (
                                <li key={index}>{line}</li>
                            ))}
                        </ul>
                    </Card>
                </Col>
            </Row>
        )}
        </div>
    );
}

export default RecommendationPage;
