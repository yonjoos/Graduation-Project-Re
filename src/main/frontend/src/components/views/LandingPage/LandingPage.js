import React from 'react';
import { Row, Col } from 'antd';
import { useSelector } from 'react-redux'; // Import useSelector

import WelcomeContent from './Sections/WelcomeContent';
import Auth from '../Auth/Auth';
import RecommendationCard from './Sections/RecommendationCard';
import ProjectCard from './Sections/ProjectCard';
import StudyCard from './Sections/StudyCard';

function LandingPage() {
    // Use useSelector to access isAuthenticated state from Redux store
    const isAuthenticated = useSelector(state => state.isAuthenticated);

    return (
        <div>
            {/* Conditional rendering based on authentication status */}
            {!isAuthenticated && (
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
            {isAuthenticated && (
                <Row gutter={[16, 16]}>
                    <Col span={24}>
                        {/* This section is only visible to logged-in members */}
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
