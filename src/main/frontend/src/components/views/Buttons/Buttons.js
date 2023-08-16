import React from 'react';
import { Row, Col, Button } from 'antd';

export default function Buttons(props) {
    return (
        <Row justify="center" style={{ marginTop: '30px' }}>
            <Col span={24} className="text-center">
                <Button type="primary" style={{ margin: '10px' }} onClick={props.login}>
                    Login
                </Button>
                <Button style={{ margin: '10px' }} onClick={props.logout}>
                    Logout
                </Button>
            </Col>
        </Row>
    );
}
