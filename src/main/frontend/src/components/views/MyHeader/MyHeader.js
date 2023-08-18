import React from 'react';
import { Layout, Typography, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { setAuthHeader } from '../../../hoc/auth';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../../_actions/actions'

const { Header } = Layout;
const { Title } = Typography;

function MyHeader(props) {
    const navigate = useNavigate();
    const isAuthenticated = useSelector(state => state.isAuthenticated);
    const dispatch = useDispatch();

    const handleLogout = () => {
        setAuthHeader(null); // Clear token in local storage
        dispatch(logout()); // Dispatch logout action
    };    

    const login = () => {
        navigate('/login');
        console.log("login");
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
                        {/** 토글 형식 */}
                        {isAuthenticated ? (
                            <Button type="primary" onClick={handleLogout}>Logout</Button>
                        ) : (
                            <Button type="primary" onClick={login}>Login</Button>
                        )}
                    </div>
                </div>
            </Header>
        </div>
    );
}

export default MyHeader;
