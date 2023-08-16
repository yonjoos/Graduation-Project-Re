// 프론트앤드의 시작점

// app.js는 node module을 로딩하고 초기 initialize해야 하는 변수나 Object를 선언하고 Router에 유입이 이루어지는 그 유입점의 역할을 하는 JavaScript


import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom"; // Import from React Router v6
import MyHeader from './views/MyHeader/MyHeader';
import LandingPage from "./views/LandingPage/LandingPage";
import LoginPage from "./views/LoginPage/LoginPage";

import './App.css';
import logo from '../logo.svg';

function App() {
    const [isLogin, setIsLogin] = useState(false);    // 로그인 여부를 통해, 컴포넌트를 표시할지 여부를 제어하는 state 변수를 정의

    return (
        <Router>
            <div>
                {/** 사이트 이름과 로고 모양, 을 인자로 넘김 */}
                <MyHeader pageTitle="P!ck Me" logoSrc={logo} setIsLogin={setIsLogin}/>
                <div className="container-fluid">
                    <div className="row">
                        <div className="col">
                            <Routes>
                                <Route path="/" element={<LandingPage isLogin={isLogin}/>} />
                                {/** isLogin === true이면 로그인 화면 안보이게, isLogin === false 이면 로그인 화면 보이게 */}
                                <Route path="/login" element={<LoginPage isLogin={isLogin}/>} />
                            </Routes>
                        </div>
                    </div>
                </div>
            </div>
        </Router>
    )
}

// es6에서는 내보낼 단일객체를 위해 export를 사용하고, 그 이전 버전의 CommonJS에서는 module.exports를 사용한다.
// https://www.daleseo.com/js-module-import/
export default App;

// export default :
// 코딩 중 export할 파일소스 내 제일 처음 export default로 정의한 클래스(함수, 변수등 모든 정의되것들)로 가지고옴
// 같은 소스 내에 export default로 정의한 것들이 여러 개 있다 하더라도 제일 처음 정의한 것만 가능

// export : export할 파일소스내의 클래스(함수, 변수등 모든 정의되것들)들 중 “import {그안에 들어있는 것들중1, 것들중2,것들중3, 계속추가} from 파일.js” 처럼 특정해서 사용됨