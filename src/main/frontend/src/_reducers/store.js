// store.js
// Redux 스토어를 생성하고, 초기 상태 및 리듀서를 설정하는 코드. 
// Redux는 상태 관리를 위한 핵심 요소 중 하나인 스토어를 통해 상태를 저장하고 관리
// 즉 프런트 단위에서 전역적으로 지켜볼 수 잇는 상태? 를 지켜보는 듯 하다
// action.js와 밀접한 관련이 있음 -> action.js에서 생성자 함수가 호출될 때마다 이 파일의 switch 문이 실행되는듯


/*

######################################################################################################################################
=============================================  /  store.js IN Redux  /  ==============================================================
######################################################################################################################################

                                                 
   ** 기능 : Redux를 사용하기위한 필수항목들을 DEFINE    
    1)Reducer : State 업데이트                     
    2)Store : State 저장   
    
    
    ** 사용 : 
    1) store 생성 : index.js 에서 <Provider> 의 store 컴포넌트 값에 store을 넣어줌
    2) store 사용 : component.js 에서 상태를 useState 혹은 setState로 initialize 할 때 Global State인 store을 불러와서 사용함
    3) reducer 함수 생성
    4) reducer 함수 사용 : store 객체 생성 시에 매개변수로 사용.       
        ex >> store = createStore( reducer 함수 , 미들웨어 );
                                                

######################################################################################################################################
=============================================  / 참고 : Redux  /  =====================================================================
######################################################################################################################################


------- About Redux ------- 

** 목적 : Global State 관리

** 필수적으로 필요한 코드 
    1) Reducer : State 업데이트
    2) Store : Reducer로 변경된 State 저장
    3) Actions : State 반환

** 각 필수코드들 사이의 관계
    1) Store: store 객체를 생성, client에 보관.        ex>> store = createState( 'reducer 함수 ', '미들웨어함수'); 
    2) Reducer : reducer함수는 redux에서 createState의 매개변수로 들어감
    3) Actions : state 들을 반환하는 함수, 반환된 값은 useDispatch() 함수를 통해 client의 store 에 전달됨.
        -이 프로젝트에서는 각 component의 .js 파일에서 state가 바뀔 때 dispatch(action함수); 형태로 실행됨.
    
    ps) 이 프로젝트에서는 store와 reducer 를 store.js 파일에 몰아 작성함


######################################################################################################################################

*/

import { createStore, applyMiddleware, combineReducers } from 'redux';
import thunk from 'redux-thunk';

// initialState: 앱의 초기 상태를 정의하는 객체임. 여기서는 isAuthenticated와 authToken을 초기화 수행
const initialState = {
    isAuthenticated: false,
    authToken: null,
    userRole: null,
    userPortfolio: null,
    userNickName: null,
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
                isAuthenticated: true, //로그인 상태로 가는 경우, 인증된 상태임
                authToken: action.payload.token, //로그인 상태로 가는 경우, 토큰이 있는 상태임
                userRole: action.payload.role, //로그인 상태로 가는 경우,  role도 저장된 상태임
                userPortfolio: action.payload.isCreated, // 포트폴리오 유무 여부
                userNickName: action.payload.nickName       // 유저 닉네임
            };
        case 'LOGOUT':
            return {
                ...state,
                isAuthenticated: false, //로그아웃 상태로 가는 경우, 인증허가가 끝난 상태임
                authToken: null, //로그아웃 상태로 가는 경우,토큰 반환
                userRole: null, //로그아웃 상태로 가는경우, role도 없음
                userPortfolio: null, //로그아웃 상태로 가는경우, portfolio도 없음
                userNickName: null
            };
        // 포트폴리오 생성 케이스 만들기.
        // 포트폴리오 생성되면 userPortfolio: true로 바꾸기
        case 'UPLOAD_PORTFOLIO_SUCCESS':
            return {
                ...state,               // 기존 상태는 그대로 가져오되,
                userPortfolio: true     // userPortfolio의 상태를 true로 변경한다.
            };
        // 포트폴리오 삭제 케이스 만들기.
        // 포트폴리오 삭제되면 userPortfolio: false로 바꾸기
        case 'DELETE_PORTFOLIO_SUCCESS':
            return {
                ...state,
                userPortfolio: false
            }    
        default:
            return state;
    }
};
    



const initialRecommendState = {
    recommendedList: [],
    isRecommededPortfolioView: false
};
    
const recommendReducer = (state = initialRecommendState, action) => {
    switch (action.type) {
        case 'SAVE_RECOMMENDED_LIST':
            return {
                ...state,
                recommendedList: action.recommendedList
            }
        case 'SET_RECOMMENDED_PORTFOLIO_VIEW':
            return {
                ...state,
                isRecommededPortfolioView: action.isRecommededPortfolioView
            }
        default:
            return state;
    }
};


// // 엔드포인트는 맨 처음 null로 세팅됨.
// const initialEndpointState = {
//     lastVisitedEndpoint: null,
//     lastLastVisitedEndpoint: null,
//     lastLastLastVisitedEndpoint: null
// };

// const endpointReducer = (state = initialEndpointState, action) => {
//     switch (action.type) {
//         // 엔드포인트 세팅하는 케이스 만들기
//         // 입력되는 엔드포인트를 저장하기
//         case 'SET_LAST_VISITED_ENDPOINT':
//             return {
//                 lastVisitedEndpoint: action.payload.endpoint,
//                 lastLastVisitedEndpoint: action.payload.endEndPoint,
//                 lastLastLastVisitedEndpoint: action.payload.endEndEndPoint
//             };
//         default:
//             return state;
//     }
// };

// 3. 스토어 (store):

    // createStore 함수를 사용하여 Redux 스토어를 생성합니다.
    // authReducer를 리듀서로 사용하고, applyMiddleware 함수를 사용하여 미들웨어 thunk를 적용합니다.
    // 스토어 객체는 상태를 저장하고 액션에 따라 상태를 업데이트합니다.
    // 이렇게 정의된 스토어는 '앱 전체'에서 상태 관리를 담당하며, 리덕스 액션 및 리듀서를 사용하여 상태의 변화를 예측 가능한 방식으로 처리합니다.

// Combine both reducers into a single rootReducer
const rootReducer = combineReducers({
    auth: authReducer, // authReducer manages authentication-related state
    //endpoint: endpointReducer // endpointReducer manages endpoint-related state
    recommend: recommendReducer
});

const store = createStore(rootReducer, applyMiddleware(thunk));

export default store;
