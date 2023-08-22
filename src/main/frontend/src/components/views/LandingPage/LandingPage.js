import React from 'react';
import { Row, Col } from 'antd';
import { useSelector } from 'react-redux';

import WelcomeContent from './Sections/WelcomeContent';
import Auth from '../Auth/Auth';
import RecommendationCard from './Sections/RecommendationCard';
import ProjectCard from './Sections/ProjectCard';
import StudyCard from './Sections/StudyCard';
import Search from '../../utils/Search';

function LandingPage() {
    // Use useSelector to access isAuthenticated state from Redux store
    //(시홍 뇌피셜: index.js에서 프론트엔드 전역적으로 관리하는 provider태그 안에 store을 넣어줬고,
    //store.js에서 인증과 토큰에 대한 상태 관리를 맡고 있는데,
    //useSelector을 redux로부터 import한 후 갖고 오고 싶은 state를 갖고 올 수 있는듯 하다)
    const isAuthenticated = useSelector(state => state.isAuthenticated);
    const userRole = useSelector(state => state.userRole);

    return (
        <div>
            {/* Conditional rendering based on authentication status */}
            {!isAuthenticated && ( //인증이 안된 아무나 볼 수 있는 컴포넌트
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
            {isAuthenticated && userRole === 'ADMIN' && ( //인증되었고, 관리자만 볼 수 있는 화면
                <Row gutter={[16, 16]}>
                    <Col span={24}>
                        <h2> THIS IS AN ADMIN PAGE </h2>
                        {/* This section is only visible to logged-in members */}
                        <Auth />
                    </Col>
                    <Search/>
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
            {isAuthenticated && userRole === 'USER' && ( //인증되었고 유저만 볼 수 있는 화면
                <Row gutter={[16, 16]}>
                    <Col span={24}>
                        <h2> THIS IS AN USER PAGE </h2>
                        {/* This section is only visible to logged-in members */}
                        <Auth />
                    </Col>
                    <Search/>
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
