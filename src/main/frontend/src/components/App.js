// 프론트앤드의 시작점

// app.js는 node module을 로딩하고 초기 initialize해야 하는 변수나 Object를 선언하고 Router에 유입이 이루어지는 그 유입점의 역할을 하는 JavaScript

import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import Main from './Main';

function App() {

    return (
        <Router>
            <Main />
        </Router>
    );
}

export default App;