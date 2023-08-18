// 여러 reducer 파일들을 하나로 묶어 관리하는 파일인 듯

/**
 * index.js는 리듀서를 가져와 combineReducers를 사용하여 루트 리듀서를 생성합니다.
 *
 * 이 파일은 모든 리듀서 파일들을 Redux의 combineReducers 함수를 사용하여 하나의 rootReducer로 결합합니다.
 * 이로써 애플리케이션의 다른 부분들의 상태를 구조적으로 관리할 수 있습니다.
 */

import { combineReducers } from 'redux';
import user from './user_reducer';

// combineReducers : 여러 reducer 들을 하나의 store 에 저장 할 수 있게 해주는 함수 (여러 reducer를 user에 저장)
const rootReducer = combineReducers({
    user,
});

export default rootReducer;