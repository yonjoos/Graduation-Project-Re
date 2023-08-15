import * as React from 'react';
import WelcomeContent from './WelcomeContent';
import Auth from '../Auth/Auth';
import Login from '../Login/Login'
import { request, setAuthHeader } from '../../../helpers/axios_helper';
import Buttons from '../Buttons/Buttons';

export default class LandingPage extends React.Component {

    /**
     *  constructor 메서드에서 초기 상태(componentToShow)를 "welcome"로 설정.
     */
    constructor(props) {
        super(props);
        this.state = {
            componentToShow: "welcome"
        }
    };

    /**
     *  login 메서드는 호출되면 componentToShow 상태를 "login"으로 변경.
     */
    login = () => {
        this.setState({componentToShow: "login"})
    };

    /**
     *  logout 메서드는 호출되면 componentToShow 상태를 "welcome"로 변경하고, setAuthHeader 함수를 호출하여 인증 헤더를 제거.
     *  setAuthHeader 함수는 helpers/axios_helper.js에 포함되어있음.
     */
    logout = () => {
        this.setState({componentToShow: "welcome"})
        setAuthHeader(null);
    };


    /**
     *  onLogin 메서드는 로그인을 시도할 때 호출되며, POST 요청을 통해 로그인 정보를 서버로 보냄.
     *  성공 시에는 토큰을 받아 인증 헤더를 설정하고 componentToShow 상태를 "messages"로 변경.
     *  실패 시에는 인증 헤더를 제거하고 componentToShow 상태를 "welcome"로 변경.
     */
    onLogin = (event, username, password) => {
        // 페이지가 자동적으로 refresh되지 않도록 설정
        event.preventDefault();

        request(
            "POST",     // Post Method
            "/login",   // URL이 아닌, 백엔드의 @GetMapping("/login")과 소통하기 위함
            {
                login: username,
                password: password
            })            // Post Method에 채울 body 내용
            .then((response) => {
                setAuthHeader(response.data.token);
                this.setState({componentToShow: "messages"});
            })          // 로그인 성공 시에는 토큰을 받아 인증 헤더를 설정하고 componentToShow 상태를 "messages"로 변경.
            .catch((error) => {
                setAuthHeader(null);
                this.setState({componentToShow: "welcome"})
            }           // 로그인 실패 시에는 인증 헤더를 제거하고 componentToShow 상태를 "welcome"로 변경.
        );
    };

    /**
     *  onRegister 메서드는 회원 가입을 시도할 때 호출되며, POST 요청을 통해 회원 정보를 서버로 보냄.
     *  성공 시에는 토큰을 받아 인증 헤더를 설정하고 componentToShow 상태를 "messages"로 변경.
     *  실패 시에는 인증 헤더를 제거하고 componentToShow 상태를 "welcome"로 변경.
     */
    onRegister = (event, firstName, lastName, username, password) => {
        // 페이지가 자동적으로 refresh되지 않도록 설정
        event.preventDefault();

        request(
            "POST",
            "/register",
            {
                firstName: firstName,
                lastName: lastName,
                login: username,
                password: password
            })
            .then((response) => {
                setAuthHeader(response.data.token);
                this.setState({componentToShow: "messages"});
            })
            .catch((error) => {
                setAuthHeader(null);
                this.setState({componentToShow: "welcome"})
            }
        );
    };

    // HTML 리턴 부분
    /**
     *  render 메서드에서는 랜딩 페이지의 구성요소들을 렌더링함.
     *  Buttons/Buttons.js에서 구현한 Buttons 컴포넌트를 렌더링하며, login과 logout 메서드를 해당 컴포넌트로 전달함.
     *  그리고 componentToShow의 값에 따라 WelcomeContent, Login, 또는 Auth 컴포넌트 중 하나를 조건부로 렌더링함.
     * 
     *  welcome : 로그인하지 않은 유저가 보는 페이지
     *  login : 로그인 화면에서 보는 페이지
     *  messages : 로그인 완료된 상황에서 보는 페이지
     */
    render() {
        return (
            <div>
                <Buttons
                login={this.login}
                logout={this.logout}
                />

                {this.state.componentToShow === "welcome" && <WelcomeContent /> }
                {this.state.componentToShow === "login" && <Login onLogin={this.onLogin} onRegister={this.onRegister} />}
                {this.state.componentToShow === "messages" && <Auth />}
            </div>
        )
    }
}