import * as React from 'react';
import { useNavigate } from 'react-router-dom';

function MyHeader(props) {
    const navigate = useNavigate();

    const handleLogoClick = () => {
        // Navigate to the landing page route
        navigate('/');
    };

    return (
        <header className="App-header">
            {/* Use onClick to call handleLogoClick when the logo is clicked */}
            <img
                src={props.logoSrc}
                className="App-logo"
                alt="logo"
                onClick={handleLogoClick}
                style={{ cursor: 'pointer' }} // Make the logo look clickable
            />
            <h1 className="App-title">{props.pageTitle}</h1>
        </header>
    );
}

export default MyHeader;
