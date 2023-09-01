// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { Card, Row, Col, Divider } from 'antd';
import { request } from '../../../hoc/request';
import { useNavigate } from 'react-router-dom';



function RecommendationPage() {
    const [data, setData] = useState([]);
    const navigate = useNavigate();



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

    const onClickHandler = () => {
        navigate('/portfolio');
      }
    
    const testfunction = () => {
        
        return (
            <div>
                <Row gutter={[16, 16]}>
                    <Col xs={24} sm={8}>
                        <Card onClick={onClickHandler} title = "hi">
                            <h2>TEST 1</h2>
                            <p>
                                as this is a test comp,
                                won't consider actual texts.
                                <br></br>
                                <br></br>

                                I apologize for any inconvenience.
                                sincerely, yonjoo.
                            </p>
                        </Card>
                    </Col>
                    <Col xs={24} sm={8}>
                        <Card title = "hi">
                            <h2>TEST 2</h2>
                            <p>
                                as this is a test comp,
                                won't consider actual texts.
                                <br></br>
                                <br></br>

                                I apologize for any inconvenience.
                                sincerely, yonjoo.
                            </p>
                        </Card>
                    </Col>
                    <Col xs={24} sm={8}>
                        <Card title = "hi">
                            <h3>TEST 3</h3>
                            <p>
                                as this is a test comp,
                                won't consider actual texts.
                                <br></br>
                                <br></br>

                                I apologize for any inconvenience.
                                sincerely, yonjoo.
                            </p>
                        </Card>
                    </Col>
                </Row>
            </div>
        )

    }

    return (
        <div>
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
            <Divider></Divider>
            {testfunction()}
        </div>
    );
}

export default RecommendationPage;
