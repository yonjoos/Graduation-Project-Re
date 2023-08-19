// actions.js
//Redux 액션 생성자 함수들을 정의한 것으로 보입니다. Redux는 상태 관리 라이브러리로, 애플리케이션의 상태를 예측 가능하게 관리하는 데 사용됩니다. 여기서는 두 개의 액션 생성자 함수를 정의하였습니다.

//로그인 성공 시 호출되는 액션을 생성합니다.
// type 필드에는 액션의 유형을 나타내는 문자열 'LOGIN_SUCCESS'을 지정합니다.
// payload 필드에는 로그인에 성공한 경우 토큰을 전달합니다.
export const loginSuccess = (token) => {
    return {
        type: 'LOGIN_SUCCESS',
        payload: { token }
    };
};


// 로그아웃 시 호출되는 액션을 생성합니다.
// type 필드에는 액션의 유형을 나타내는 문자열 'LOGOUT'을 지정합니다.
export const logout = () => {
    return {
        type: 'LOGOUT'
    };
};

//이렇게 정의된 액션 생성자 함수들은 리덕스 액션 객체를 반환하며, 이들 액션은 리듀서 함수를 통해 상태의 변화를 관리합니다. 이를 통해 로그인 상태와 로그아웃 상태를 효과적으로 관리할 수 있습니다.
