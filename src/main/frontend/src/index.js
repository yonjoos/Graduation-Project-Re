import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './components/App'; // components폴더의 App.js 가져오기
import reportWebVitals from './reportWebVitals';
import 'bootstrap/dist/css/bootstrap.min.css'; // 부트스트랩 import
import { Provider } from 'react-redux';
import store from './components/_reducers/store';

ReactDOM.createRoot(document.getElementById('root')).render(
    <Provider store={store}>
        <App />
    </Provider>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
