// 프론트앤드의 시작점

// app.js는 node module을 로딩하고 초기 initialize해야 하는 변수나 Object를 선언하고 Router에 유입이 이루어지는 그 유입점의 역할을 하는 JavaScript

import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout } from 'antd';
import MyHeader from './views/MyHeader/MyHeader';
import LandingPage from './views/LandingPage/LandingPage';
import LoginPage from './views/LoginPage/LoginPage';
import RecommendationPage from './views/RecommendationPage/RecommendationPage';
import ProjectPage from './views/ProjectPage/ProjectPage';
import StudyPage from './views/StudyPage/StudyPage';
import Footer from './views/Footer/Footer';
import './App.css';
import logo from '../logo.svg';
import logo1 from '../pickme.PNG';
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
                    <Routes>
                        <Route
                            path="/"
                            element={
                                <LandingPage />
                            }
                        />
                        <Route
                            path="/login"
                            element={
                                <LoginPage />
                            }
                        />
                        <Route
                            path="/Recommendation"
                            element={
                                <RecommendationPage />
                            }
                        />
                        <Route
                            path="/Project"
                            element={
                                <ProjectPage />
                            }
                        />
                        <Route
                            path="/Study"
                            element={
                                <StudyPage />
                            }
                        />
                    </Routes>
                </Content>
                <Footer logoSrc={logo2}/>
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