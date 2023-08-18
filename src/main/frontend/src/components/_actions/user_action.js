// cf : react + redux 관련 이해 :
// https://medium.com/@ca3rot/%EC%95%84%EB%A7%88-%EC%9D%B4%EA%B2%8C-%EC%A0%9C%EC%9D%BC-%EC%9D%B4%ED%95%B4%ED%95%98%EA%B8%B0-%EC%89%AC%EC%9A%B8%EA%B1%B8%EC%9A%94-react-redux-%ED%94%8C%EB%A1%9C%EC%9A%B0%EC%9D%98-%EC%9D%B4%ED%95%B4-1585e911a0a6


// 사용자의 요청을 받아 넘겨주는 함수가 구현되어있는 파일

/**
 * user_actions.js는 로그인, 등록 등과 같은 사용자 관련 작업을 정의하는 액션 생성자를 정의합니다.
 *
 * 이러한 액션 함수들은 서버 API로의 요청을 생성하고, 적절한 페이로드를 가진 액션을 디스패치하는 역할을 합니다.
 * loginUser, registerUser, auth, logoutUser와 같은 함수들은 API 요청을 캡슐화하고 Redux 액션을 반환합니다.
 */

import axios from 'axios'; // Axios는 브라우저, Node.js를 위한 Promise API를 활용하는 HTTP 비동기 통신 라이브러리
                           // 쉽게 말해서 백엔드랑 프론트엔드랑 통신을 쉽게하기 위해 Ajax와 더불어 사용함

import {
    LOGIN_USER,
    REGISTER_USER,
    //AUTH_USER,
    //LOGOUT_USER
} from './types';

//import { USER_SERVER } from '../components/Config.js';


/*
{
    type: "액션의 종류를 한번에 식별할 수 있는 문자열 혹은 심볼",
    payload: "액션의 실행에 필요한 임의의 데이터",
}
*/


export function loginUser(dataToSubmit) {
    // server 폴더에 있는 index.js에서 app.post가 있는 위치에 설정된 경로인 /api/users/login과 똑같이 맞춰준다.
    // 여기서 request는 백엔드에서 가져온 모든 데이터
    const request = axios.post(`/login`, dataToSubmit)
        .then(response => response.data)

    // request를 넣어주는데 이거는 다시 user_reducer.js에 넣어줘서 확장프로그램의 State가 작동되는 것.
    /**
     *  type: "액션의 종류를 한번에 식별할 수 있는 문자열 혹은 심볼",
     *  payload: "액션의 실행에 필요한 임의의 데이터",
     */
    return {
        type: LOGIN_USER,
        payload: request
    }
}

// RegisterPage의 registerUser와 동일한 이름을 갖도록 한다.
 export function registerUser(dataToSubmit) {
    // server 폴더에 있는 index.js에서 app.post가 있는 위치에 설정된 경로인
    // /api/users/register와 똑같이 맞춰준다.
    const request = axios.post(`/register`, dataToSubmit)
        .then(response => response.data)
    
    // return된 type은 type.js에서 정의
    return {
        type: REGISTER_USER,
        payload: request
    }
}

// get 메소드에서는 parameter를 필요로 하지 않는다.
// export function auth() {
//     // get 메소드로 request를 보낸다.
//     const request = axios.get(`/auth`)
//         .then(response => response.data)

//     // return된 type은 type.js에서 정의
//     return {
//         type: AUTH_USER,
//         payload: request
//     }
// } 


// export function logoutUser() {
//     const request = axios.get(`/logout`)
//         .then(response => response.data);

//     return {
//         type: LOGOUT_USER,
//         payload: request
//     }
// }