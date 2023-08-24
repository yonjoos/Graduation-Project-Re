//index.js 파일은 React 애플리케이션의 찐 진입점으로, Redux 스토어를 사용하여 애플리케이션을 렌더링하는 코드
//Redux의 Provider 컴포넌트는 앱의 최상위 컴포넌트를 감싸며, 스토어의 상태를 하위 컴포넌트에 전달하는 역할 수행

import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './components/App'; // components폴더의 App.js 가져오기
import reportWebVitals from './reportWebVitals';
import 'bootstrap/dist/css/bootstrap.min.css'; // 부트스트랩 import
import { Provider } from 'react-redux';
import store from './_reducers/store';
import { getAuthToken, getUserRole, getHasPortfolio } from './hoc/request';
import { loginSuccess } from './_actions/actions';

const storedAuthToken = getAuthToken(); //로컬스토리지에서 토큰이 있으면 가져옴
const userRole = getUserRole(); //로컬스토리지에서 해당 유저의 역할 가져옴
const hasPortfolio = getHasPortfolio(); // 로컬스토리지에서 해당 유저의 포트폴리오 유무 여부를 가져옴

const localStorageCleared = localStorage.getItem('localStorageCleared'); //로컬 스토리지에서 클리어되었는지 여부 가져옴

if (!localStorageCleared) { //만약 로컬 스토리지가 비워져있지 않다면(서버 재시작하면 클리어되어있지 않음),f5누르면 index.js로 다시오는데, 로그인 된 상태에서는 localStoragecleared가 true여도 storedToken이 있기 때문에 store에 로그인 상태로 다시 세팅 명령 시킬 수 있음
    
    // Clear localStorage
    localStorage.clear();  //로컬스토리지를 비우고
    // Mark localStorage as cleared to avoid repeating clearing
    localStorage.setItem('localStorageCleared', 'true'); //로컬 스토리지가 비워졌다고 명시
}

if (storedAuthToken) { //저장된 토큰이 있다면 로그인완료 상태로 디스패치 -> f5누르면 index.js로 다시 오는데, 이거 때문에 로그인 상태가 유지되는 것임

    // 저장된 토큰과 역할로 로그인 액션 디스패치
    store.dispatch(loginSuccess(storedAuthToken, userRole, hasPortfolio));
}
    
ReactDOM.createRoot(document.getElementById('root')).render(
    <Provider store={store}>
        <App />
    </Provider>
);


// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
