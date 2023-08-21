// 로그인된 회원만 볼 수 있는 페이지
import React, { useState, useEffect } from 'react';
import { Card, Row, Col } from 'antd';
import { request } from '../../../hoc/request';

function Auth() {
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
    );
}

export default Auth;
