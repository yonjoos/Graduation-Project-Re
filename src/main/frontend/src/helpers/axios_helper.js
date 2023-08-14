import axios from 'axios';

// JWT를 저장하고 프론트엔드의 다음 요청에 사용.
// 이를 위해 localstorage에서 JWT를 저장하고 읽도록 axios도우미 (axios_helper)를 다음과 같이 작성하였음.
// 이제 로그인 또는 회원가입이 완료되면, JWT를 저장함.

export const getAuthToken = () => {
    return window.localStorage.getItem('auth_token');
};

export const setAuthHeader = (token) => {
    window.localStorage.setItem('auth_token', token);
};

axios.defaults.baseURL = 'http://localhost:9090';
axios.defaults.headers.post['Content-Type'] = 'application/json';

export const request = (method, url, data) => {

    let headers = {};
    if (getAuthToken() !== null && getAuthToken() !== "null") {
        // Baerer 백틱 (`) 주의!
        headers = {'Authorization': `Bearer ${getAuthToken()}`};
    }

    return axios({
        method: method,
        url: url,
        headers: headers,
        data: data});
};