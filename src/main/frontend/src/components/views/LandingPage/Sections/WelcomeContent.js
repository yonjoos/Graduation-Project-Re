import React from 'react';
import { Row, Col, Typography, Card } from 'antd';

const { Title, Paragraph } = Typography;

export default function WelcomeContent() {
    return (
        <Row justify="center" style={{ marginTop: '20px' }}>
            <Col xs={24} sm={16} md={12} lg={8}>
                <Card>
                    <Typography>
                        <Title>Welcome</Title>
                        <Paragraph>Login to see protected content.</Paragraph>
                    </Typography>
                </Card>
            </Col>
        </Row>
    );
}
