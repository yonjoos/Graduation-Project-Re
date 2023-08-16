import React from 'react';
import { Row, Col, } from 'antd';
import WelcomeContent from './Sections/WelcomeContent';
import Auth from '../Auth/Auth';
import RecommendationCard from './Sections/RecommendationCard';
import ProjectCard from './Sections/ProjectCard';
import StudyCard from './Sections/StudyCard';

function LandingPage(props) {

    return (
        <div>
            {/* componentToShow 값에 따른 조건부 렌더링 */}
            {props.isLogin === false && (
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
            {props.isLogin === true && (
                <Row gutter={[16, 16]}>
                    <Col span={24}>
                        {/** 로그인된 회원만 볼 수 있는 페이지 */}
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