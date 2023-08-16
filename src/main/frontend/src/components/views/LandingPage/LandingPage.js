import { useNavigate } from 'react-router-dom';
import React, { useState } from 'react';
import { Row, Col } from 'antd';
import WelcomeContent from './Sections/WelcomeContent';
import Auth from '../Auth/Auth';
import Login from '../Login/Login';
import { request, setAuthHeader } from '../../../hoc/auth';
import Buttons from '../Buttons/Buttons';
import RecommendationCard from './Sections/RecommendationCard';
import ProjectCard from './Sections/ProjectCard';
import StudyCard from './Sections/StudyCard';

function LandingPage() {
    const navigate = useNavigate();
    const [componentToShow, setComponentToShow] = useState('welcome');

    const login = () => {
        navigate('/login');
        setComponentToShow('login');
    };

    const logout = () => {
        setComponentToShow('welcome');
        setAuthHeader(null);
    };

    const onLogin = (event, username, password) => {
        event.preventDefault();

        request('POST', '/login', {
            login: username,
            password: password
        })
            .then((response) => {
                setAuthHeader(response.data.token);
                setComponentToShow('messages');
            })
            .catch((error) => {
                setAuthHeader(null);
                setComponentToShow('welcome');
            });
    };

    const onRegister = (event, firstName, lastName, username, password) => {
        event.preventDefault();

        request('POST', '/register', {
            firstName: firstName,
            lastName: lastName,
            login: username,
            password: password
        })
            .then((response) => {
                setAuthHeader(response.data.token);
                setComponentToShow('messages');
            })
            .catch((error) => {
                setAuthHeader(null);
                setComponentToShow('welcome');
            });
    };

    return (
        <div>
            <Row gutter={[16, 16]}>
                <Col span={24}>
                    <Buttons login={login} logout={logout} />
                </Col>
            </Row>

            {componentToShow === 'welcome' && (
                <Row gutter={[16, 16]}>
                    <Col span={24}>
                        <WelcomeContent />
                    </Col>
                    <Col span={8}>
                        <RecommendationCard />
                    </Col>
                    <Col span={8}>
                        <ProjectCard />
                    </Col>
                    <Col span={8}>
                        <StudyCard />
                    </Col>
                </Row>
            )}
            {componentToShow === 'login' && (
                <Login onLogin={onLogin} onRegister={onRegister} />
            )}
            {componentToShow === 'messages' && (
                <Row gutter={[16, 16]}>
                    <Col span={24}>
                        <Auth />
                    </Col>
                    <Col span={8}>
                        <RecommendationCard />
                    </Col>
                    <Col span={8}>
                        <ProjectCard />
                    </Col>
                    <Col span={8}>
                        <StudyCard />
                    </Col>
                </Row>
            )}
        </div>
    );
}

export default LandingPage;

