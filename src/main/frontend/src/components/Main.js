// 프론트앤드의 시작점

// app.js는 node module을 로딩하고 초기 initialize해야 하는 변수나 Object를 선언하고 Router에 유입이 이루어지는 그 유입점의 역할을 하는 JavaScript

import React/*, { useEffect }*/ from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import { Layout } from 'antd';
import Auth from '../hoc/auth';
import MyHeader from './views/MyHeader/MyHeader';
import AdminPage from './views/AdminPage/AdminPage';
import MyPage from './views/MyPage/MyPage';
import ScrapPage from './views/ScrapPage/ScrapPage';
import MyPortfolioPage from './views/PortfolioPage/MyPortfolioPage/MyPortfolioPage';
import UploadPortfolioPage from './views/PortfolioPage/UploadPortfolioPage/UploadPortfolioPage'
import UpdatePortfolioPage from './views/PortfolioPage/UpdatePortfolioPage/UpdatePortfolioPage';
import PortfolioPage from './views/PortfolioPage/PortfolioPage';
import GroupPage from './views/GroupPage/GroupPage';
import LandingPage from './views/LandingPage/LandingPage';
import LoginPage from './views/LoginPage/LoginPage';
import PortfolioCardPage from './views/PortfolioCardPage/PortfolioCardPage';
import ProjectPage from './views/ProjectPage/ProjectPage';
import DetailProjectPage from './views/ProjectPage/DetailProjectPage/DetailProjectPage';
import UploadProjectPage from './views/ProjectPage/UploadProjectPage/UploadProjectPage';
import UpdateProjectPage from './views/ProjectPage/UpdateProjectPage/UpdateProjectPage';
import StudyPage from './views/StudyPage/StudyPage';
import DetailStudyPage from './views/StudyPage/DetailStudyPage/DetailStudyPage';
import UploadStudyPage from './views/StudyPage/UploadStudyPage/UploadStudyPage';
import UpdateStudyPage from './views/StudyPage/UpdateStudyPage/UpdateStudyPage';
import DetailProjectNotifyPage from './views/ProjectPage/DetailProjectPage/DetailProjectNotifyPage';
import DetailStudyNotifyPage from './views/StudyPage/DetailStudyPage/DetailStudyNotifyPage';
import PortfolioNotifyPage from './views/PortfolioPage/PortfolioNotifyPage';
import SearchPortfolioListPage from './views/SearchListPage/SearchPortfolioListPage';
import SearchProjectListPage from './views/SearchListPage/SearchProjectListPage';
import SearchStudyListPage from './views/SearchListPage/SearchStudyListPage';
import About from './views/About/About';

import Footer from './views/Footer/Footer';
import './App.css';
import Notifications from './utils/Notifications';

const { Content } = Layout;

function Main() {
    // 아래 코드의 문제점 : 
    // 1. useNavigate를 사용하지 않으면 로그아웃 된다.
    // 2. F5를 눌러 페이지 새로고침 시, 로그아웃 된다.
    // useEffect(() => {
    //     localStorage.clear(); //프론트엔드가 재구동되면 로컬스토리지(토큰이 있으면 지움)를 비우고 시작하게 세팅
    //     // 또는 특정 key를 지우고 싶다면 localStorage.removeItem('key')를 사용합니다.
    //   }, []); // 빈 배열을 넘겨주면 컴포넌트가 처음 마운트될 때 한 번만 실행됩니다.

    const location = useLocation();

    // Header를 렌더링할지 여부를 결정하는 함수
    const renderHeader = () => {
        return (
            !location.pathname.includes('/project/detail/notify/') &&
            !location.pathname.includes('/study/detail/notify/')
        );
    };

    return (
        <Layout>
            {/** 로고 모양을 인자로 넘김 */}
            {renderHeader() && <MyHeader logoSrc={'https://storage.googleapis.com/hongik-pickme-bucket/P!ckMeLogo.png'} />}
            <Content style={{ width: '100%', backgroundColor: 'whitesmoke'}}>
                <div style={{ paddingLeft: '15%', paddingRight: '15%' }}>
                    <Routes>
                        {
                            /**
                             * 테스트 방법
                             * 
                             * Auth(페이지명, null) -> 누구나 들어갈 수 있는 페이지
                             * => 로그아웃, 로그인, 관리자 모두 들어가지나 확인해본다.
                             * 
                             * Auth(페이지명, true) -> 로그인한 유저(ADMIN, USER)만 들어갈 수 있는 페이지
                             * => 로그아웃 유저는 해당 EndPointer로 접근 불가. 로그인, 관리자는 해당 Endpointer로 접근 가능
                             * 
                             * Auth(페이지명, false) -> 로그인한 유저(ADMIN, USER)는 들어갈 수 없는 페이지
                             * => 로그아웃 유저만 해당 EndPointer로 접근 가능. 로그인, 관리자는 해당 Endpointer로 접근 불가
                             * 
                             * Auth(페이지명, null, true) -> 관리자(ADMIN)만 들어갈 수 있는 페이지
                             * => 로그아웃 유저 및 일반 유저는 해당 EndPointer로 접근 불가. 관리자만 해당 Endpointer로 접근 가능
                             * 
                             * Auth(페이지명, true, null, true) -> 유저(USER) 중 포트폴리오가 이미 작성된 사람이어야 들어갈 수 있는 페이지
                             * => 포트폴리오를 작성하지 않은 사람은 해당 Endpointer로 접근 불가
                             * 
                             * Auth(페이지명, true, null, false) -> 유저(USER) 중 포트폴리오가 없는 사람이어야 들어갈 수 있는 페이지
                             * => 포트폴리오를 작성한 사람은 해당 Endpointer로 접근 불가
                             * 
                             * 
                             * 
                             * 
                             * Auth(페이지명, null)인 페이지는 모든 사용자가 접근 가능하다.
                             * 따라서 isAuthenticated && userRole === 'ADMIN'을 구체적으로 명시해서, 각 사용자(비회원, 회원, 관리자)마다 보이는 화면이 다르도록 구분한다.
                             * 
                             * Auth(페이지명, true)인 페이지는 로그인한 유저만 접근 가능하다.
                             * 따라서 isAuthenticated && userRole === 'ADMIN'을 구체적으로 명시해서, 각 사용자(회원, 관리자)마다 보이는 화면이 다르도록 구분한다.
                             * 
                             * Auth(페이지명, false)인 페이지는 로그인한 유저가 접근 불가능하다.
                             * 따라서 비회원만 접근 가능하므로, 경우를 나누지 않고 코딩한다.
                             * 
                             * Auth(페이지명, null, true)인 페이지는 관리자만 접근 가능하다.
                             * 따라서 경우를 나누지 않고 코딩한다.
                             * 
                             * Auth(페이지명, true, null, true)인 페이지는 로그인한 유저 중 포트폴리오가 이미 작성된 사람만 들어갈 수 있다.
                             * 따라서 오직 포트폴리오를 작성하지 않은 유저는 접근 불가능하므로, 경우를 나누지 않고 코딩한다.
                             * 
                             * Auth(페이지명, true, null, false)인 페이지는 로그인한 유저 중 포트폴리오가 없는 사람만 들어갈 수 있다.
                             * 따라서 오직 포트폴리오를 작성한 유저는 접근 불가능하므로, 경우를 나누지 않고 코딩한다.
                             * 
                             */
                        }
                        <Route
                            path="/about"
                            element={Auth(About, null)}
                        />
                        <Route
                            path="/adminPage"
                            element={Auth(AdminPage, null, true)}
                        />
                        <Route
                            path="/myPage"
                            element={Auth(MyPage, true)}
                        />
                        <Route
                            path="/scrap"
                            element={Auth(ScrapPage, true)}
                        />
                        <Route
                            path="/portfolio"
                            element={Auth(MyPortfolioPage, true)}
                        />
                        <Route
                            // path 입력 시 / 빼먹는거 주의!! path="portfolio/upload" 아니라 path="/portfolio/upload"임!!
                            path="/portfolio/upload"
                            // User 중, Portfolio가 이미 작성되어있는 사람은 접근할 수 없는 페이지 : 윤식 comment
                            // 이쪽 해석할 때 option: true -> 로그인된 사람만 접근 가능한 페이지인가? adminRoute : true -> 관리자만 접근 가능한가?  hasPortfolio : true -> 포폴 있는 사람만 접근 가능한가? 로 해석 (시홍)
                            element={Auth(UploadPortfolioPage, true, null, false)} //로그인 되어있고, 역할 상관없이, 포폴 없는 사람만 접근 가능한 페이지
                        />
                        <Route
                            path="/portfolio/update"
                            // User 중, Portfolio가 없는 사람은 접근할 수 없는 페이지
                            element={Auth(UpdatePortfolioPage, true, null, true)} //로그인 되어있고, 역할 상관없이, 포폴 있는 사람만 접근 가능한 페이지(시홍)
                        />
                        <Route
                            path="/portfolio/:nickName"
                            element={Auth(PortfolioPage, true)}
                        />
                        <Route
                            path="/portfolio/notify/:nickName"
                            element={Auth(PortfolioNotifyPage, true)}
                        />
                        <Route
                            path="/group"
                            element={Auth(GroupPage, true)}
                        />
                        <Route
                            path="/login"
                            element={Auth(LoginPage, false)}
                        />
                        <Route
                            path="/portfoliocard"
                            element={Auth(PortfolioCardPage, null)}
                        />
                        <Route
                            path="/project"
                            element={Auth(ProjectPage, null)}
                        />
                        {/* <Route
                            path="/project/detail/:projectId"
                            element={Auth(DetailProjectPage, true)}
                        /> */}
                        <Route
                            path="/project/upload"
                            element={Auth(UploadProjectPage, true)}
                        />
                        <Route
                            path="/project/update/:projectId"
                            element={Auth(UpdateProjectPage, true)}
                        />
                        <Route
                            path="/study"
                            element={Auth(StudyPage, null)}
                        />
                        {/* <Route
                            path="/study/detail/:studyId"
                            element={Auth(DetailStudyPage, true)}
                        /> */}
                        <Route
                            path="/study/upload"
                            element={Auth(UploadStudyPage, true)}
                        />
                        <Route
                            path="/study/update/:studyId"
                            element={Auth(UpdateStudyPage, true)}
                        />
                        <Route
                            path="/search/portfoliocard/query/:searchTerm"
                            element={Auth(SearchPortfolioListPage, true)}
                        />
                        <Route
                            path="/search/project/query/:searchTerm"
                            element={Auth(SearchProjectListPage, true)}
                        />
                        <Route
                            path="/search/study/query/:searchTerm"
                            element={Auth(SearchStudyListPage, true)}
                        />
                    </Routes>
                    <Notifications />
                </div>
                <div>
                    <Routes>
                        <Route
                            path="/"
                            element={Auth(LandingPage, null)}
                        />
                        <Route
                            path="/project/detail/:projectId"
                            element={Auth(DetailProjectPage, true)}
                        />
                        <Route
                            path="/study/detail/:studyId"
                            element={Auth(DetailStudyPage, true)}
                        />

                        {/* <Route
                            path="/project/detail/notify/:projectId"
                            element={Auth(DetailProjectNotifyPage, true)}
                        />
                        <Route
                            path="/study/detail/notify/:studyId"
                            element={Auth(DetailStudyNotifyPage, true)}
                        /> */}

                    </Routes>
                </div>
            </Content>
            <Footer style={{ paddingLeft: '15%', paddingRight: '15%' }} logoSrc={'https://storage.googleapis.com/hongik-pickme-bucket/P!ckMeLogo.png'} />
        </Layout>
    );
}

// es6에서는 내보낼 단일객체를 위해 export를 사용하고, 그 이전 버전의 CommonJS에서는 module.exports를 사용한다.
// https://www.daleseo.com/js-module-import/
export default Main;

// export default :
// 코딩 중 export할 파일소스 내 제일 처음 export default로 정의한 클래스(함수, 변수등 모든 정의되것들)로 가지고옴
// 같은 소스 내에 export default로 정의한 것들이 여러 개 있다 하더라도 제일 처음 정의한 것만 가능

// export : export할 파일소스내의 클래스(함수, 변수등 모든 정의되것들)들 중 “import {그안에 들어있는 것들중1, 것들중2,것들중3, 계속추가} from 파일.js” 처럼 특정해서 사용됨