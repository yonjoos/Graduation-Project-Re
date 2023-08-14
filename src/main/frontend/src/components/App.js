// 프론트앤드의 시작점

// app.js는 node module을 로딩하고 초기 initialize해야 하는 변수나 Object를 선언하고 Router에 유입이 이루어지는 그 유입점의 역할을 하는 JavaScript


import React, { Suspense } from "react";
import {
    BrowserRouter as Router,
    // Switch는 v5버전이고, 현재는 v6이므로 Routes를 씀
    Routes,
    Route,
    Link,
} from "react-router-dom";

import './App.css';
import logo from '../logo.svg';
import MyHeader from './views/MyHeader/MyHeader';
import LandingPage from "./views/LandingPage/LandingPage";
import NavBar from "./views/NavBar/NavBar";
import Footer from "./views/Footer/Footer";

function App() {
    return (
        <div>
            <MyHeader pageTitle="Frontend authenticated with JWT" logoSrc={logo}/>
            <div className="container-fluid">
                <div className="row">
                    <div className="col">
                        <LandingPage/>
                    </div>
                </div>
            </div>
        </div>
    )
    // return (
    //   <Router>
    //       <Suspense fallback={<div>Loading...</div>}>
    //           <NavBar />
    //           <div style={{ paddingTop: "69px" }}></div>
    //           <div style={{ paddingTop: "200px" }}></div>
    //             <Footer />
    //       </Suspense>
    //   </Router>
    // );
}

export default App;
