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
    const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
    const userRole = useSelector(state => state.auth.userRole);

    return (
        <div>
            {/* Conditional rendering based on authentication status */}
            {!isAuthenticated && ( //인증이 안된 아무나 볼 수 있는 컴포넌트
                // Row, Col : 그리드(창의 크기에 맞춘 반응형)를 위해 사용되는 애.
                
                //  gutter : Row의 열 사이의 간격을 지정함.
                // [가로, 세로]라는 두 개의 값을 갖는 배열임.
                // gutter={[16, 16]}는 열 사이의 가로 및 세로 간격을 각각 16픽셀로 설정
                // 즉, 세로로 따지면 <br/>을 사용하지 않고도, Col 간의 간격이 알아서 16px로 설정됨.

                // span : Col 구성 요소가 확장되어야 하는 열 수를 지정함.
                // 그리드 레이아웃의 총 열 수는 일반적으로 24개.
                // 따라서 span={8}을 설정하면 열이 사용 가능한 너비의 1/3 (8/24)을 차지한다는 의미
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
                    <Col xs={24} sm={8}>
                        <RecommendationCard />
                    </Col>
                    <Col xs={24} sm={8}>
                        <ProjectCard />
                    </Col>
                    <Col xs={24} sm={8}>
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
