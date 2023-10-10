import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Row, Col, Button, Card} from 'antd';
import { useSelector, useDispatch } from 'react-redux';
import { useState, useEffect } from "react";
import WelcomeContent from './Sections/WelcomeContent';
import PortfolioCard from './Sections/PortfolioCard';
import ProjectCard from './Sections/ProjectCard';
import StudyCard from './Sections/StudyCard';
import SearchInLandingPage from './SearchInLandingPage';
import { request } from '../../../hoc/request';
import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../hoc/request';
import { lastVisitedEndpoint } from '../../../_actions/actions';

function LandingPage() {
    // Use useSelector to access isAuthenticated state from Redux store
    //(시홍 뇌피셜: index.js에서 프론트엔드 전역적으로 관리하는 provider태그 안에 store을 넣어줬고,
    //store.js에서 인증과 토큰에 대한 상태 관리를 맡고 있는데,
    //useSelector을 redux로부터 import한 후 갖고 오고 싶은 state를 갖고 올 수 있는듯 하다)

    const navigate = useNavigate();
    const dispatch = useDispatch();

    const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
    const userRole = useSelector(state => state.auth.userRole);

    // 백엔드에서 받은 검색어 기반 결과 리스트(3개)를 정의. 처음에 이 페이지에 들어오면 빈 배열
    const [data, setData] = useState({
        projectSearchDtoList: [], // 프로젝트 제목 관련 최대 5개 가져옴
        studySearchDtoList: [], // 스터디 제목 관련 최대 5개 가져옴
        userSearchDtoList: [], // 유저 이름 관련 최대 5개 가져옴
    });
    const [searchTerm, setSearchTerm] = useState(""); //랜딩페이지 내의 검색어 키워드 입력값

    // 키워드를 치는 순간 순간마다 백엔드에서 데이터 받아옴
    useEffect(() => {
        console.log('현재 검색된 키워드: ', searchTerm);
        fetchFilteredSearchLists();
    }, [searchTerm]);

    // 백엔드에 검색어 요청 보내기
    const fetchFilteredSearchLists = async () => {
        try {
            // 만약 검색어가 있다면,
            if (searchTerm !== "") {
                const queryParams = new URLSearchParams({
                    searchTerm: searchTerm, // 검색어 세팅
                });

                // 백엔드에서 데이터 받아오기
                const response = await request('GET', `/getFilteredSearchLists?${queryParams}`);

                // 데이터가 있다면 세팅, 없으면 각각 빈 배열로 세팅
                if (response.data) {
                    setData({
                        projectSearchDtoList: response.data.projectSearchDtoList || [],
                        studySearchDtoList: response.data.studySearchDtoList || [],
                        userSearchDtoList: response.data.userSearchDtoList || [],
                    });
                } else {
                    // Handle the case where response.data.content is undefined
                    console.error("Error fetching data: response.data.content is undefined");
                }
            } else {
                // 검색어가 없다면, 빈 배열로 세팅
                setData({
                    projectSearchDtoList: [],
                    studySearchDtoList: [],
                    userSearchDtoList: [],
                });
            }
        } catch (error) {
            console.error("Error fetching data:", error);
        }
    };

    // 너무 긴 제목이나 닉네임이면 적당한 길이로 자르고, ... 붙이기
    const truncateString = (str, maxLength) => {
        if (str.length > maxLength) {
            return str.slice(0, maxLength) + '...'; 
        }
        return str;
    };


    // 검색어가 새로이 입력되거나 변경될때마다 여기서 감지해서 백엔드에 보낼 searchTerm을 세팅함
    const handleSearch = (value) => {
        setSearchTerm(value); // 검색어를 세팅
        // console.log('검색어', searchTerm);
    };

    // 백엔드에서 받아온 검색 결과를 가지고 실제 렌더링 진행.
    // 프로젝트, 스터디, 유저를 각각 카드로 감싸고, 그 안엔 버튼으로 감쌈
    const renderSection = (title, dataArray) => {

        const handleButtonClick = (title, id, name) => {

            // 버튼을 클릭하면, 현재 위치를 다 '/'로 세팅해서 디스패치
            dispatch(lastVisitedEndpoint('/', '/', '/'));
            setLastVisitedEndpoint('/');
            setLastLastVisitedEndpoint('/');
            setLastLastLastVisitedEndpoint('/');

            // 각각에 대해 올바르게 라우팅 걸어주기
            if (title === 'Project') {
                navigate(`/project/detail/${id}`);
            } else if (title === 'Study') {
                navigate(`/study/detail/${id}`);
            } else if (title === 'User') {
                navigate(`/portfolio/${name}`);
            }
        };

        // 빈 배열이 아니라면, 즉, 렌더링해야하는 값임
        if (dataArray && dataArray.length > 0) {
            return (
                <Col span={24} style={{ textAlign: 'center' }}>
                    <Card size='small' style={{padding: 0, margin: 0 }}>
                        <div style={{ width: 800, textAlign: 'left', padding: 0}}>
                            <strong># {title}</strong>
                        </div>
                        <div style={{ margin: 0 }}>
                            {dataArray.map(item => (
                                <Button
                                    key={item.id}
                                    type="text"
                                    style={{ width: '100%', textAlign: 'left', padding: 0, margin: 0}}
                                    onClick={() => handleButtonClick(title, item.id, item.name)}
                                >
                                    {truncateString(item.name, 55)}
                                </Button>
                            ))}
                        </div>
                    </Card>
                </Col>
            );
        }
        return null;
    };
    
    
    

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
                        <PortfolioCard />
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
                        <SearchInLandingPage onSearch={handleSearch} />
                    </Col>
                    <Col span={24}>
                        {renderSection('Project', data.projectSearchDtoList)}
                        {renderSection('Study', data.studySearchDtoList)}
                        {renderSection('User', data.userSearchDtoList)}
                    </Col>                   
                    <Col xs={24} sm={8}>
                        <PortfolioCard />
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
                        <SearchInLandingPage onSearch={handleSearch} />
                    </Col>
                    <Col span={24}>
                        {renderSection('Project', data.projectSearchDtoList)}
                        {renderSection('Study', data.studySearchDtoList)}
                        {renderSection('User', data.userSearchDtoList)}
                    </Col>
                    <Col xs={24} sm={8}>
                        <PortfolioCard />
                    </Col>
                    <Col xs={24} sm={8}>
                        <ProjectCard />
                    </Col>
                    <Col xs={24} sm={8}>
                        <StudyCard />
                    </Col>
                </Row>
            )}
        </div>
    );
}

export default LandingPage;
