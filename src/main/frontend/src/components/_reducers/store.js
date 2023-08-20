// store.js
// Redux 스토어를 생성하고, 초기 상태 및 리듀서를 설정하는 코드. 
// Redux는 상태 관리를 위한 핵심 요소 중 하나인 스토어를 통해 상태를 저장하고 관리
// 즉 프런트 단위에서 전역적으로 지켜볼 수 잇는 상태? 를 지켜보는 듯 하다
// action.js와 밀접한 관련이 있음 -> action.js에서 생성자 함수가 호출될 때마다 이 파일의 switch 문이 실행되는듯

import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';

// initialState: 앱의 초기 상태를 정의하는 객체임. 여기서는 isAuthenticated와 authToken을 초기화 수행
const initialState = {
    isAuthenticated: false,
    authToken: null
};

/**
 * Redux에서는 액션 타입이 문자열이므로, 리듀서 함수 내에서 해당 타입 문자열을 사용하여 액션을 처리하고 상태를 업데이트합니다.
 *  store.js 파일에서는 액션 타입을 문자열로 사용하므로, 해당 파일에서 actions.js를 import하지 않아도 액션을 처리할 수 있습니다.
 */

//authReducer: 액션에 따라 상태를 업데이트하는 리듀서 함수
    // switch 문을 사용하여 각 액션 타입에 따라 상태를 조작합니다.
    // LOGIN_SUCCESS 액션은 isAuthenticated를 true로, authToken을 액션의 페이로드로 업데이트합니다.
    // LOGOUT 액션은 isAuthenticated를 false로, authToken을 null로 업데이트합니다.
    // 기본적으로는 이전 상태를 그대로 반환합니다.
    
const authReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'LOGIN_SUCCESS':
            return {
                ...state,
                isAuthenticated: true,
                authToken: action.payload.token,
                userRole: action.payload.role
            };
        case 'LOGOUT':
            return {
                ...state,
                isAuthenticated: false,
                authToken: null,
                userRole: null
            };
        default:
            return state;
    }
};
    


// 3. 스토어 (store):

    // createStore 함수를 사용하여 Redux 스토어를 생성합니다.
    // authReducer를 리듀서로 사용하고, applyMiddleware 함수를 사용하여 미들웨어 thunk를 적용합니다.
    // 스토어 객체는 상태를 저장하고 액션에 따라 상태를 업데이트합니다.
    // 이렇게 정의된 스토어는 '앱 전체'에서 상태 관리를 담당하며, 리덕스 액션 및 리듀서를 사용하여 상태의 변화를 예측 가능한 방식으로 처리합니다.
const store = createStore(authReducer, applyMiddleware(thunk));

export default store;
