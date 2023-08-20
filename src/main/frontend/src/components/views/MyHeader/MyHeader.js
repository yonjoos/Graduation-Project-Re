import React from 'react';
import { Layout, Typography, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { setAuthHeader, setUserRole } from '../../../hoc/auth';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../../_actions/actions'

const { Header } = Layout;
const { Title } = Typography;

function MyHeader(props) { //여기서 props는 로고 모양을 app.js에서 가져오기 위함
    const navigate = useNavigate();
    const isAuthenticated = useSelector(state => state.isAuthenticated);
    const dispatch = useDispatch();

    //로그아웃 버튼을 클릭하면 호출되며, 로컬 스토리지의 토큰을 삭제하고 로그아웃 액션을 디스패치합니다.
    //즉 로그 아웃 버튼 누르면 로컬 스토리지의 'auth-token'필드를 null 로 채우고, action.js에 등록된 logout관련 액션을 수행하도록 dispatch(강제 명령) 날림. 그리고 그 상태 값이 store.js의 switch문에 의해 변경됨
    const handleLogout = () => { 
        setAuthHeader(null); // Clear token in local storage(로컬 스토리지에서 토큰 지우는 건 인증받지 못한 사람은 api호출을 못하게 하기 위함)
        setUserRole(null);
        dispatch(logout()); // Dispatch logout action(이걸 하는 이유는 프런트 전역적으로 이 사람이 인증받지 못한 사람이란 걸 인지하게 하기 위함) 

        //여기서 navigate(/)해줘야할듯? -> 로그아웃 버튼 누르면 홈페이지로 가는게 맞을 것 같다
        navigate('/');
    };    

    const login = () => {
        navigate('/login');
        console.log("login");
    };

    const handleLogoClick = () => { //로고 클릭하면 홈 화면으로 다시 라우팅
        console.log("go home by site logo");
        navigate('/');
    };

    const handleSiteNameClick = () => { //사이트 이름 클릭하면 홈 화면으로 다시 라우팅
        console.log("go home by site name");
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
                        <Title level={2} className="App-title" style={{color : 'whitesmoke'}} onClick={handleSiteNameClick}>
                            <div style={{ display: "inline-block", fontSize: "30px", textDecoration: "none", cursor: 'pointer' }}>
                                <div style={{color : 'white', fontWeight: 'bold'}}>P
                                <span style={{color : 'dodgerblue'}}>!</span>
                                ck Me</div>
                            </div>
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
