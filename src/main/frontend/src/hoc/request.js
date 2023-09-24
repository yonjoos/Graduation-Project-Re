import axios from 'axios';

// JWT를 저장하고 프론트엔드의 다음 요청에 사용.
// 이를 위해 localstorage에서 JWT를 저장하고 읽도록 axios도우미 (axios_helper)를 다음과 같이 작성하였음.
// 이제 로그인 또는 회원가입이 완료되면, JWT를 저장함.


//로컬 스토리지에서 'auth_token'이라는 키로 저장된 JWT 토큰을 가져오는 함수
export const getAuthToken = () => { 
    return window.localStorage.getItem('auth_token');
};

//로컬 스토리지에서 'user_roke'이라는 키로 저장된 사용자 역할을 가져오는 함수
export const getUserRole = () => {
    return window.localStorage.getItem('user_role');
}

//로컬 스토리지에서 'user_portfolio'이라는 키로 저장된 사용자 역할을 가져오는 함수
export const getHasPortfolio = () => {
    return window.localStorage.getItem('user_portfolio');
}

//로컬 스토리지에서 'user_nickname'이라는 키로 저장된 사용자 역할을 가져오는 함수
export const getUserNickName = () => {
    return window.localStorage.getItem('user_nickname');
}

//로컬 스토리지에서 'last_visited_endpoint'이라는 키로 저장된 사용자 역할을 가져오는 함수
export const getLastVisitedEndpoint = () => {
    return window.localStorage.getItem('last_visited_endpoint');
}

//로컬 스토리지에서 'last_last_visited_endpoint'이라는 키로 저장된 사용자 역할을 가져오는 함수
export const getLastLastVisitedEndpoint = () => {
    return window.localStorage.getItem('last_last_visited_endpoint');
}

export const getLastLastLastVisitedEndpoint = () => {
    return window.localStorage.getItem('last_last_last_visited_endpoint');
}


//로컬 스토리지에 JWT 토큰을 'auth_token' 키로 저장하는 함수
export const setAuthHeader = (token) => {
    window.localStorage.setItem('auth_token', token);
};

//로컬 스토리지에 사용자 역할을 'user_role' 키로 저장하는 함수
export const setUserRole = (role) => {
    window.localStorage.setItem('user_role', role);
};

//로컬 스토리지에 포트폴리오 생성여부를 'user_portfolio' 키로 저장하는 함수
export const setHasPortfolio = (isCreated) => {
    window.localStorage.setItem('user_portfolio', isCreated);
};

//로컬 스토리지에 닉네임을 'user_nickname' 키로 저장하는 함수
export const setUserNickName = (nickName) => {
    window.localStorage.setItem('user_nickname', nickName);
};

//로컬스토리지에서 마지막으로 방문한 유효한 엔드포인트를 저장하는 함수
export const setLastVisitedEndpoint = (endPoint) => {
    window.localStorage.setItem('last_visited_endpoint', endPoint);
};

//로컬스토리지에서 마지막 바로 전에 방문한 유효한 엔드포인트를 저장하는 함수
export const setLastLastVisitedEndpoint = (endPoint) => {
    window.localStorage.setItem('last_last_visited_endpoint', endPoint);
};

export const setLastLastLastVisitedEndpoint = (endPoint) => {
    window.localStorage.setItem('last_last_last_visited_endpoint', endPoint);
};

//axios의 기본 설정을 지정. 
//API 요청을 보낼 때 기본 URL을 'http://localhost:9090'(백 서버)으로 설정하고, 
//POST 요청의 Content-Type을 'application/json'으로 지정
axios.defaults.baseURL = 'http://localhost:9090';
axios.defaults.headers.post['Content-Type'] = 'application/json';

// 로그인 성공시, getAuthToken()을 통해 로그인 정보를 가져오고, request틀을 만들어 준다.
export const request = (method, url, data) => {

    // 로그인되지 않은 유저라면, 헤더에 토큰을 달아주지 않는다.
    let headers = {};

    const authToken = getAuthToken();
    console.log('Token:', authToken);

    // 로그인된 유저라면, 빈 헤더에 토큰을 달아준다.
    if (getAuthToken() !== null && getAuthToken() !== "null" && getAuthToken() !== "undefined") {
        // Baerer 백틱 (`) 주의!
        headers = {'Authorization': `Bearer ${getAuthToken()}`};
    }
    //여기서 백에 실제로 요청 보냄
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