import React, { useState } from 'react';
import classNames from 'classnames';
import { Row, Col, Tabs, Input, Button } from 'antd';

const { TabPane } = Tabs;

export default function Login(props) {
    const [active, setActive] = useState('login');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');

    const onChangeHandler = (event) => {
        const name = event.target.name;
        const value = event.target.value;
        // Using this method, we can save the updated value of the field to State.
        if (name === 'firstName') setFirstName(value);
        else if (name === 'lastName') setLastName(value);
        else if (name === 'login') setLogin(value);
        else if (name === 'password') setPassword(value);
    };

    const onSubmitLogin = (e) => {
        e.preventDefault();
        props.onLogin(e, login, password);
    };

    const onSubmitRegister = (e) => {
        e.preventDefault();
        props.onRegister(e, firstName, lastName, login, password);
    };

    return (
        <Row justify="center">
            <Col span={12}>
                <Tabs activeKey={active} onChange={(key) => setActive(key)} centered>
                    <TabPane tab="Login" key="login">
                        <form onSubmit={onSubmitLogin}>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="login"
                                    placeholder="Username"
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
                            <Button type="primary" block htmlType="submit">Sign In</Button>
                        </form>
                    </TabPane>
                    <TabPane tab="Register" key="register">
                        <form onSubmit={onSubmitRegister}>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="firstName"
                                    placeholder="First Name"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="lastName"
                                    placeholder="Last Name"
                                    onChange={onChangeHandler}
                                />
                            </div>
                            <div className="form-outline mb-4">
                                <Input
                                    type="text"
                                    name="login"
                                    placeholder="Username"
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
                            <Button type="primary" block htmlType="submit">Sign Up</Button>
                        </form>
                    </TabPane>
                </Tabs>
            </Col>
        </Row>
    );
}
