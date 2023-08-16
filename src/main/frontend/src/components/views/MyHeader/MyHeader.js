import React from 'react';
import { Layout, Typography, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { setAuthHeader } from '../../../hoc/auth';

const { Header } = Layout;
const { Title } = Typography;

function MyHeader(props) {
    const navigate = useNavigate();

    const login = () => {
        navigate('/login');
        console.log("login");
    };

    const logout = () => {
        props.setIsLogin(false);
        setAuthHeader(null);
        console.log("logout");
    };

    const handleLogoClick = () => {
        console.log("go home");
        navigate('/');
    };

    return (
        <div>
            <Header className="App-header">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', height: '100%' }}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <img
                            src={props.logoSrc}
                            className="App-logo"
                            alt="logo"
                            onClick={handleLogoClick}
                            style={{ cursor: 'pointer' }}
                        />
                        <Title level={2} className="App-title" style={{color : 'whitesmoke'}}>
                            {props.pageTitle}
                        </Title>
                    </div>
                    <div>
                        <Button type="primary" onClick={login}>Login</Button>
                        <Button type="primary" onClick={logout}>Logout</Button>
                    </div>
                </div>
            </Header>
        </div>
    );
}

export default MyHeader;
