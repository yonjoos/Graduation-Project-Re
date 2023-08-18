// store.js
import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';

const initialState = {
    isAuthenticated: false,
    authToken: null
};

/**
 * Redux에서는 액션 타입이 문자열이므로, 리듀서 함수 내에서 해당 타입 문자열을 사용하여 액션을 처리하고 상태를 업데이트합니다.
 *  store.js 파일에서는 액션 타입을 문자열로 사용하므로, 해당 파일에서 actions.js를 import하지 않아도 액션을 처리할 수 있습니다.
 */
const authReducer = (state = initialState, action) => {
    switch (action.type) {
        case 'LOGIN_SUCCESS':
            return {
                ...state,
                isAuthenticated: true,
                authToken: action.payload.token
            };
        case 'LOGOUT':
            return {
                ...state,
                isAuthenticated: false,
                authToken: null
            };
        default:
            return state;
    }
};

const store = createStore(authReducer, applyMiddleware(thunk));

export default store;
