// store.js
import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';

const initialState = {
    isAuthenticated: false,
    authToken: null
};

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
