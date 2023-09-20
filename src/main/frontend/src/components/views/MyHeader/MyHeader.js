import { React, useState } from 'react';
import { Layout, /*Typography, */Button, Drawer, Card, message } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { setAuthHeader, setUserRole } from '../../../hoc/request';
import { useSelector, useDispatch } from 'react-redux';
import { request } from '../../../hoc/request';
import { logout } from '../../../_actions/actions'
import { lastVisitedEndpoint } from '../../../_actions/actions';
import { setLastVisitedEndpoint } from '../../../hoc/request';
import CustomDropdown from './Sections/CustomDropdown';
//import Notifications from '../../utils/Notifications';

const { Header } = Layout;
//const { Title } = Typography;

function MyHeader(props) { //여기서 props는 로고 모양을 app.js에서 가져오기 위함
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const location = useLocation();
    const currentEndpoint = location.pathname;
    const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
    const userRole = useSelector(state => state.auth.userRole);

    const [open, setOpen] = useState(false);
    const [notificationData, setNotificationData] = useState([]);

    // Notification 배너가 열리면 해당 회원의 모든 알림을 가져와서 렌더링
    const showDrawer = () => {
        setOpen(true);

        // 백엔드에 모든 알림 가져오기 요청
        request('GET', 'sse/getNotifications', {})
            .then((response) => {
                setNotificationData(response.data);
                console.log('알림', response.data);
            })
            .catch((error) => {
                console.log("Error fetching data:", error);
            })
    };

    // 알림 배너 닫기
    const onClose = () => {
        setOpen(false);
    };


    const handleScrap = () => {
        navigate('/scrap');
    }

    const handlePortfolio = () => {
        navigate('/portfolio');
    }

    const handleGroup = () => {
        navigate('/group');
    }

    //로그아웃 버튼을 클릭하면 호출되며, 로컬 스토리지의 토큰을 삭제하고 로그아웃 액션을 디스패치합니다.
    //즉 로그 아웃 버튼 누르면 로컬 스토리지의 'auth-token'필드를 null 로 채우고, action.js에 등록된 logout관련 액션을 수행하도록 dispatch(강제 명령) 날림. 그리고 그 상태 값이 store.js의 switch문에 의해 변경됨
    const handleLogout = () => {
        setAuthHeader(null); // Clear token in local storage(로컬 스토리지에서 토큰 지우는 건 인증받지 못한 사람은 api호출을 못하게 하기 위함)
        setUserRole(null); //로컬 스토리지에서 역할을 지우는건 역할 없는 사람은 api호출을 못하게 하기 위함
        localStorage.clear();   // 로컬스토리지 클리어 반드시 해주기!! 얘 안하면 로그아웃 상태에서 새로고침 시 랜딩페이지가 렌더링되지 않음!!
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

    // const handleSiteNameClick = () => { //사이트 이름 클릭하면 홈 화면으로 다시 라우팅
    //     console.log("go home by site name");
    //     navigate('/');
    // };

    // 알림 카드 각각을 클릭했을 때 동작
    const handleCardClick = (postId, postType) => {

        // /project/detail/${projectId}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /project로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /project로 설정
        dispatch(lastVisitedEndpoint(currentEndpoint));    // 전역에 상태 저장을 위한 

        setLastVisitedEndpoint(currentEndpoint);   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.

        const lowerType = postType.toLowerCase(); // 백엔드에서 받은 postType은 PROJECT , STUDY와 같은 형식이므로 navigate를 위해선 소문자로 바꿔줄 필요가 있음

        navigate(`${lowerType}/detail/${postId}`); // 알림에 해당하는 게시물로 navigate 걸어줌

    };

    // 알림 카드 닫기 버튼 클릭시 호출될 함수
    const handleCardClose = (notificationId) => (e) => {

        e.stopPropagation(); // 상위의 이벤트인 handleCardClick가 실행되는 걸 막음

        // 백엔드에 해당 알림 id를 delete 요청
        request('POST', `sse/deleteNotification/${notificationId}`, {})
            .then((response) => {
                message.success('알림이 삭제되었습니다.');
                // 알림을 삭제한 후, 상태를 업데이트하여 해당 알림을 화면에서 제거
                setNotificationData(prevData => prevData.filter(notification => notification.notificationId !== notificationId));
            })
            .catch((error) => {
                console.log("Error fetching data:", error);
                message.error('알림 삭제에 실패했습니다.');
            });

        // Drawer를 열었다가 닫지 않고, 업데이트된 알림 데이터를 사용하여 카드를 다시 렌더링
        setOpen(true);
    };

    return (
        <div>
            <Header className="App-header">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', height: '100%', paddingLeft: '15%', paddingRight: '15%' }}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <img
                            src={props.logoSrc}
                            className="App-logo"
                            alt="logo"
                            onClick={handleLogoClick}
                            style={{ cursor: 'pointer', maxWidth: '200px', maxHeight: '40px' }}
                        />
                        {/* <Title level={2} className="App-title" style={{color : 'whitesmoke'}} onClick={handleSiteNameClick}>
                            <div style={{ display: "inline-block", fontSize: "30px", textDecoration: "none", cursor: 'pointer' }}>
                                <div style={{color : 'white', fontWeight: 'bold'}}>P
                                <span style={{color : 'dodgerblue'}}>!</span>
                                ck Me</div>
                            </div>
                        </Title> */}
                    </div>
                    <div>
                        {/** 토글 형식, background: 'transparent' : 버튼 배경을 투명하게, padding: '20px 40px' : 각각 Top, Bottom 패딩 설정 */}
                        {isAuthenticated ? (
                            <div>
                                <Button type="text" value="large" style={{ color: 'black', background: 'transparent', fontSize: '18px', height: '10vh', }} onClick={showDrawer}>
                                    Notification
                                </Button>
                                <Drawer title="알림" width={520} closable={false} onClose={onClose} open={open}>
                                    <div>
                                        {/* 알림 데이터를 Card 컴포넌트로 렌더링 */}
                                        {notificationData.length > 0 && (
                                            // 알림 데이터를 Card 컴포넌트로 렌더링
                                            notificationData.map((notification, index) => {
                                                // 만약 알림 내용이 하나도 없으면 렌더링하지 않음
                                                if (
                                                    notification.postId === null &&
                                                    notification.notificationMessage === null &&
                                                    notification.postType === null &&
                                                    notification.notificationId === null
                                                ) {
                                                    return null; // postId, notificationMessage, postType, notificationId이 모두 null인 경우 카드를 렌더링하지 않음
                                                }
                                                return (
                                                    <Card
                                                        key={index}
                                                        onClick={() => handleCardClick(notification.postId, notification.postType)}
                                                        style={{ cursor: 'pointer', marginBottom: '10px' }}
                                                    >
                                                        <span
                                                            style={{ position: 'absolute', right: '4px', top: '4px', cursor: 'pointer', fontSize: '20px' }}
                                                            onClick={handleCardClose(notification.notificationId)}
                                                        >
                                                            &times;
                                                        </span>
                                                        {/* 여기에 알림 내용을 출력 */}
                                                        {notification.notificationMessage}
                                                    </Card>
                                                );
                                            })
                                        )}
                                    </div>
                                </Drawer>
                                <Button type="text" value="large" style={{ color: 'black', background: 'transparent', fontSize: '18px', height: '10vh', }} onClick={handleScrap}>
                                    Scrap
                                </Button>
                                <Button type="text" value="large" style={{ color: 'black', background: 'transparent', fontSize: '18px', height: '10vh', }} onClick={handlePortfolio}>
                                    Portfolio
                                </Button>
                                <Button type="text" value="large" style={{ color: 'black', background: 'transparent', fontSize: '18px', height: '10vh', }} onClick={handleGroup}>
                                    Group
                                </Button>
                                <CustomDropdown userRole={userRole} handleLogout={handleLogout}>
                                    Me
                                </CustomDropdown>
                            </div>
                        ) : (
                            <Button type="text" value="large" style={{ color: 'black', background: 'transparent', fontSize: '18px' }} onClick={login}>
                                Login
                            </Button>
                        )}
                    </div>
                </div>
            </Header>
        </div>
    );
}

export default MyHeader;
