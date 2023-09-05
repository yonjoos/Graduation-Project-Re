// actions.js

// dispatch(loginSuccess())처럼 useDispatch 사용시, actions.js에 있는 함수를 인자로 넣는다.

// Redux 액션 생성자 함수들을 정의한 것으로 보입니다. Redux는 상태 관리 라이브러리로, 애플리케이션의 상태를 예측 가능하게 관리하는 데 사용됩니다.
// 여기서는 두 개의 액션 생성자 함수를 정의하였습니다.

// 로그인 성공 시 호출되는 액션을 생성합니다.
// type 필드에는 액션의 유형을 나타내는 문자열 'LOGIN_SUCCESS'을 지정합니다.
// payload 필드에는 로그인에 성공한 경우 토큰을 전달합니다.


/*

######################################################################################################################################
=============================================  /  action.js IN Redux  /  ==============================================================
######################################################################################################################################


-------- action.js IN Redux --------

** 기능 : Redux를 사용하기위한 필수항목 중 하나인 3)Action : State 반환 기능을 DEFINE

** 사용 : component 들의 .js 파일에서 state가 변경될 때마다 dispatch함수의 매개변수로 들어가게 됨

ps) dispatch 역할은 state를 store에 전송하는 것



######################################################################################################################################
===========================================  /  REDUX LIBRARY 용도와, 필수코드들  /  =======================================================
######################################################################################################################################

------- Redux VS React -------

Redux : Global State 관리
React : UI 생성, Component State 관리



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




export const loginSuccess = (token, role, isCreated) => {
    return {
        type: 'LOGIN_SUCCESS',
        payload: { token, role, isCreated }
    };
};



// 로그아웃 시 호출되는 액션을 생성합니다.
// type 필드에는 액션의 유형을 나타내는 문자열 'LOGOUT'을 지정합니다.
export const logout = () => {
    //window.localStorage.removeItem('auth_token');
    return {
        type: 'LOGOUT'
    };
};



// 포트폴리오 작성 시 호출되는 액션 생성
export const uploadPortfolioSuccess = (isCreated) => {
    return {
        type: 'UPLOAD_PORTFOLIO_SUCCESS',   // type의 이름
        payload: { isCreated }              // 인자로 받아온 isCreated를 payload로 설정
    };
}



// 포트폴리오 삭제 시 호출되는 액션 생성
export const deletePortfolioSuccess = () => {
    return {
        type: 'DELETE_PORTFOLIO_SUCCESS',   // type의 이름
    };
}



// 가장 최근에 방문한 유효한 엔드포인터를 기억하기 위한 액션 생성
export const setLastVisitedEndpoint = (endpoint) => {
    return {
        type: 'SET_LAST_VISITED_ENDPOINT',
        payload: { endpoint }
    };
};


//이렇게 정의된 액션 생성자 함수들은 리덕스 액션 객체를 반환하며, 이들 액션은 리듀서 함수를 통해 상태의 변화를 관리합니다. 이를 통해 로그인 상태와 로그아웃 상태를 효과적으로 관리할 수 있습니다.
