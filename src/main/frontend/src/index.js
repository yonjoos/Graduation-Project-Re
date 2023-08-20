//index.js 파일은 React 애플리케이션의 찐 진입점으로, Redux 스토어를 사용하여 애플리케이션을 렌더링하는 코드
//Redux의 Provider 컴포넌트는 앱의 최상위 컴포넌트를 감싸며, 스토어의 상태를 하위 컴포넌트에 전달하는 역할 수행

import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './components/App'; // components폴더의 App.js 가져오기
import reportWebVitals from './reportWebVitals';
import 'bootstrap/dist/css/bootstrap.min.css'; // 부트스트랩 import
import { Provider } from 'react-redux';
import store from './components/_reducers/store';
import { getAuthToken, getUserRole } from './hoc/auth';
import { loginSuccess } from './components/_actions/actions';

const storedAuthToken = getAuthToken();
const userRole = getUserRole();

const localStorageCleared = localStorage.getItem('localStorageCleared');

if (!localStorageCleared) {
    // Clear localStorage
    localStorage.clear();
    // Mark localStorage as cleared to avoid repeating clearing
    localStorage.setItem('localStorageCleared', 'true');
}

if (storedAuthToken) {
    // 저장된 토큰과 역할로 로그인 액션 디스패치
    store.dispatch(loginSuccess(storedAuthToken, userRole));
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
