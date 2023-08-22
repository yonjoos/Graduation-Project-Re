// 메인 화면의 우측 상단 버튼은 관리하는 페이지

/* eslint-disable jsx-a11y/anchor-is-valid */
import React from "react";
import { Menu } from "antd";
import { useNavigate } from "react-router-dom";

// 각 코드는 메인 화면의 우측 상단 버튼처럼 보이게 하므로, 이렇게 코드를 짜는구나 라고 알면 될 듯
function RightMenu(props) {
    const navigate = useNavigate();
    
    return (
        <Menu mode={props.mode}>
            <Menu.Item key="mail">
                <a href="/login">로그인</a>
            </Menu.Item>
            <Menu.Item key="app">
                <a href="/register">회원가입</a>
            </Menu.Item>
        </Menu>
    );
}

export default RightMenu;