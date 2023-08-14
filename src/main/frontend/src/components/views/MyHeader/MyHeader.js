import * as React from 'react';

// NavBar.js 역할을 대체하는 애
export default function MyHeader(props) {
    return (
        <header className="App-header">
            <img src={props.logoSrc} className="App-logo" alt="logo"/>
            <h1 className="App-title">{props.pageTitle}</h1>
        </header>
    )
}