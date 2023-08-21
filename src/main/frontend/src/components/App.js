// 프론트앤드의 시작점

// app.js는 node module을 로딩하고 초기 initialize해야 하는 변수나 Object를 선언하고 Router에 유입이 이루어지는 그 유입점의 역할을 하는 JavaScript

import React/*, { useEffect }*/ from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout } from 'antd';
import Auth from '../hoc/auth';
import MyHeader from './views/MyHeader/MyHeader';
import AdminPage from './views/AdminPage/AdminPage';
import MyPage from './views/MyPage/MyPage';
import ScrapPage from './views/ScrapPage/ScrapPage';
import PortfolioPage from './views/PortfolioPage/PortfolioPage';
import GroupPage from './views/GroupPage/GroupPage';
import LandingPage from './views/LandingPage/LandingPage';
import LoginPage from './views/LoginPage/LoginPage';
import RecommendationPage from './views/RecommendationPage/RecommendationPage';
import ProjectPage from './views/ProjectPage/ProjectPage';
import StudyPage from './views/StudyPage/StudyPage';
import Footer from './views/Footer/Footer';
import './App.css';
/*import logo from '../logo.svg';
import logo1 from '../pickme.PNG';*/
import logo2 from '../pickme2.PNG'


const { Content } = Layout;

function App() {
    // 아래 코드의 문제점 : 
    // 1. useNavigate를 사용하지 않으면 로그아웃 된다.
    // 2. F5를 눌러 페이지 새로고침 시, 로그아웃 된다.
    // useEffect(() => {
    //     localStorage.clear(); //프론트엔드가 재구동되면 로컬스토리지(토큰이 있으면 지움)를 비우고 시작하게 세팅
    //     // 또는 특정 key를 지우고 싶다면 localStorage.removeItem('key')를 사용합니다.
    //   }, []); // 빈 배열을 넘겨주면 컴포넌트가 처음 마운트될 때 한 번만 실행됩니다.

    return (
        <Router>
            <Layout>
                {/** 로고 모양을 인자로 넘김 */}
                <MyHeader logoSrc={logo2} />
                <Content style={{ padding: '20px' }}>
                    <div style={{ paddingLeft : '15%', paddingRight : '15%' }}>
                    <Routes>
                        {
                            // higier order component, 즉 hoc(특별한 목적)인 Auth가 LandingPage 등을 감싸도록 한다.
                            // 즉, 인증 여부를 거쳐야 페이지를 사용할 수 있는 권한을 부여하는 것이다.
                            // Auth 함수는 auth.js와 관련 있는 함수다.
                            // 해당 유저가 어떤 사람인지 파악하여, null, true, false 값을 지정한다.
                            // null => 아무나 출입이 가능한 페이지
                            // true => 로그인한 유저만 출입이 가능한 페이지
                            // false => 로그인한 유저는 출입 불가능한 페이지
                            // LandingPage는 아무나 들어갈 수 있으므로 null
                            // LoginPage는 로그인한 유저는 출입할 수 없으므로 false
                            // RegisterPage도 로그인한 유저는 출입할 수 없으므로 false
                            // FavoritePage는 로그인한 유저만 볼 수 있으므로 true
                            // admin한 유저(관리자)만 들어갈 수 있는 페이지를 만들기 위해서는 다음과 같이 코딩한다.
                            // <Route  path="/" element={Auth(LandingPage, null, true)} />

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
                             */
                        }
                        <Route
                            path="/"
                            // element={<LandingPage/>}
                            element={Auth(LandingPage, null)}
                        />
                        <Route
                            path="/adminPage"
                            // element={<AdminPage/>}
                            element={Auth(AdminPage, null, true)}
                        />
                        <Route
                            path="/myPage"
                            // element={<MyPage/>}
                            element={Auth(MyPage, true)}
                        />
                        <Route
                            path="/scrap"
                            // element={<ScrapPage/>}
                            element={Auth(ScrapPage, true)}
                        />
                        <Route
                            path="/portfolio"
                            // element={<PortfolioPage/>}
                            element={Auth(PortfolioPage, true)}
                        />
                        <Route
                            path="/group"
                            // element={<GroupPage/>}
                            element={Auth(GroupPage, true)}
                        />
                        <Route
                            path="/login"
                            // element={<LoginPage/>}
                            element={Auth(LoginPage, false)}
                        />
                        <Route
                            path="/Recommendation"
                            // element={<RecommendationPage/>}
                            element={Auth(RecommendationPage, true)}
                        />
                        <Route
                            path="/Project"
                            // element={<ProjectPage/>}
                            element={Auth(ProjectPage, true)}
                        />
                        <Route
                            path="/Study"
                            // element={<StudyPage/>}
                            element={Auth(StudyPage, true)}
                        />
                    </Routes>
                    </div>
                </Content>
                <Footer style={{ paddingLeft : '15%', paddingRight : '15%' }} logoSrc={logo2}/>
            </Layout>
        </Router>
    );
}

// es6에서는 내보낼 단일객체를 위해 export를 사용하고, 그 이전 버전의 CommonJS에서는 module.exports를 사용한다.
// https://www.daleseo.com/js-module-import/
export default App;

// export default :
// 코딩 중 export할 파일소스 내 제일 처음 export default로 정의한 클래스(함수, 변수등 모든 정의되것들)로 가지고옴
// 같은 소스 내에 export default로 정의한 것들이 여러 개 있다 하더라도 제일 처음 정의한 것만 가능

// export : export할 파일소스내의 클래스(함수, 변수등 모든 정의되것들)들 중 “import {그안에 들어있는 것들중1, 것들중2,것들중3, 계속추가} from 파일.js” 처럼 특정해서 사용됨