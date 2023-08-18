import axios from 'axios';

// JWT를 저장하고 프론트엔드의 다음 요청에 사용.
// 이를 위해 localstorage에서 JWT를 저장하고 읽도록 axios도우미 (axios_helper)를 다음과 같이 작성하였음.
// 이제 로그인 또는 회원가입이 완료되면, JWT를 저장함.

export const getAuthToken = () => {
    return window.localStorage.getItem('auth_token');
};

export const setAuthHeader = (token) => {
    if (token !== null && token !== "null") {
        window.localStorage.setItem('auth_token', token);
    } else {
        window.localStorage.removeItem('auth_token');
    }
};

axios.defaults.baseURL = 'http://localhost:9090';
axios.defaults.headers.post['Content-Type'] = 'application/json';

// 로그인 성공시, getAuthToken()을 통해 로그인 정보를 가져오고, request틀을 만들어 준다.
export const request = (method, url, data) => {

    // 로그인되지 않은 유저라면, 헤더에 토큰을 달아주지 않는다.
    let headers = {};

    const authToken = getAuthToken();
    console.log('Token:', authToken);

    // 로그인된 유저라면, 헤더에 토큰을 달아준다.
    if (getAuthToken() !== null && getAuthToken() !== "null" && getAuthToken() !== "undefined") {
        // Baerer 백틱 (`) 주의!
        headers = {'Authorization': `Bearer ${getAuthToken()}`};
    }

    return axios({
        method: method,
        url: url,
        headers: headers,
        data: data
    })
    .then(response => {
        console.log('Response:', response);
        return response;
    })
    .catch(error => {
        console.error('Error:', error);
        throw error;
    });
};