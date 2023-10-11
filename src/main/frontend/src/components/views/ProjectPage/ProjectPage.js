import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Divider, Row, Col, Button, Card, Pagination } from 'antd';
import { request } from '../../../hoc/request';
import SearchInProjectPage from './SearchInProjectPage';
import { lastVisitedEndpoint } from '../../../_actions/actions'
import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../hoc/request';
import './ProjectPage.css';


function ProjectPage() {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const location = useLocation(); //현재 내가 들어와있는 경로를 확인하기 위한 함수

    const [data, setData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 게시물 목록들 세팅
    const [selectedBanners, setSelectedBanners] = useState(['all']); // 처음 해당 페이지가 setting될 떄는 선택된 배너가 '전체'가 되도록 함
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [sortOption, setSortOption] = useState('latestPosts'); //최신등록순: latestPosts / 모집마감순: nearDeadline
    const [searchTerm, setSearchTerm] = useState(""); //프로젝트 페이지 내의 검색어 키워드

    const pageSize = 5; // 현재 게시물 수가 적으므로 페이징을 3개 단위로 하였음

    // 페이지가 새로 마운트 될 때마다 실행됨.
    // 현재의 selectedBanners상태(어떤 배너가 선택되어있는지)와 
    // 현재 사용자가 하이라이트한 페이지 번호 상태, 
    // 최신일순/마감일순에 대한 정렬 옵션,
    // 검색어 키워드 문자열
    // 를 기반으로 백엔드에 동적쿼리 보냄
    useEffect(() => {
        console.log('현재 선택된 배너 정보', selectedBanners);
        console.log('현재 검색된 키워드: ', searchTerm);
        fetchFilteredPosts();
    }, [selectedBanners, currentPage, sortOption, searchTerm]);

    // 실제 백엔드에 동적 쿼리 보내는 곳
    const fetchFilteredPosts = async () => {

        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                selectedBanners: selectedBanners.join(','), // selectedBanners 배열을 쉼표로 구분된 문자열로 변환
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                sortOption: sortOption, // 최신 등록순, 모집일자 마감순
                searchTerm: searchTerm // 검색어 키워드 문자열
            });

            //현재 사용자가 선택한 페이지와 배너 정보를 queryParams에 넣어서 백엔드에 요청
            const response = await request('GET', `/getFilteredProjects?${queryParams}`);

            setData(response.data.content); //백엔드에서 받은 게시물 목록을 data에 저장
            setTotalPages(response.data.totalPages); //백엔드에서 받은 전체 페이지 수 정보를 totalPages에 저장
        } catch (error) {
            console.error("Error fetching data:", error);
        }
    };

    // 페이징 된 각 게시물 목록 하나를 클릭하면 그에 해당하는 게시물의 디테일 페이지로 navigate함
    const handleRowClick = (projectId) => {
        // /project/detail/${projectId}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /project로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /project로 설정
        dispatch(lastVisitedEndpoint('/project', '/project', '/project'));    // 전역에 상태 저장을 위한 애.
        setLastVisitedEndpoint('/project');   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
        setLastLastVisitedEndpoint('/project');
        setLastLastLastVisitedEndpoint('/project');
        navigate(`/project/detail/${projectId}`);
    }

    // 현재 페이지에서 게시물 업로드를 할 수 있으므로 project upload 버튼을 클릭 시 업로드할 수 있는 페이지로 navigate함
    const onClickHandler = () => {
        navigate('/project/upload');
    }

    // 2023826 -> 2023년 8월 26일 형식으로 변환
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // Month is zero-based
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    };


    // 배너를 선택할 때마다 selectedBanners가 추가되거나 변경됨
    // 처음엔 all(모든 게시물 상태)
    // all이 아닌 다른 게시물을 선택하는 순간 all은 selectedBanners에서 지워지고, 선택된 배너가 selectedBanners에 추가됨
    // 선택된 배너를 다시 클릭하면 해당 배너를 selectedBanners에서 제외
    // all이 아닌 다른 배너는 중복 선택이되어 selectedBanners에 저장됨
    const toggleBanner = (banner) => {
        if (banner === 'all') { // 만약 선택된 배너가 전체라면 selectedBanners: [all]
            setSelectedBanners(['all']);
        }
        else if (selectedBanners.includes('all')) { // 만약 '전체' 상태에서 '전체'가 아닌 다른 버튼을 눌렀다면, [all] -> [특정 배너]
            setSelectedBanners([banner]);
        }
        else { // 그 외의 경우
            const updatedBanners = selectedBanners.includes(banner) // 만약 활성화된 배너를 다시 클릭했다면 해당 배너를 상태에서 빼줘야함, 만약 비활성화된 배너를 클릭하면 현재 상태에서 지금 클릭한 배너도 현재 상태에 넣어줘야함
                ? selectedBanners.filter((b) => b !== banner)
                : [...selectedBanners, banner];
            // Check if all specific banners are unselected
            const allBannersUnselected = !['web', 'app', 'game', 'ai'].some(b => updatedBanners.includes(b)); // 모든 배너가 제거되어있으면 true , 하나라도 배너가 활성화되어있으면 false

            // If all specific banners are unselected, set selection to "all"
            setSelectedBanners(allBannersUnselected ? ['all'] : updatedBanners); //만약 선택된 배너를 다 비활성화 하면 '전체' 상태로 감
        }

        setCurrentPage(0); // 만약 배너를 다른 걸 고르면 1페이지로 강제 이동시킴
    }

    // 최신등록순, 마감일 순 버튼이 눌러지면 현재 선택된 버튼으로 세팅하고, 페이지는 0번으로 간다
    const handleSortOptionChange = (option) => {
        setSortOption(option);
        setCurrentPage(0);
    };

    // 추천버튼을 누르면 추천 페이지로 이동
    const handlePortfolioCardPage = () => {
        navigate('/portfoliocard'); // Navigate to RecommendationPage
    };

    // 스터디 버튼을 누르면 스터디 페이지로 이동
    const handleStudyPage = () => {
        navigate('/study'); // Navigate to StudyPage
    };

    // 프로젝트 버튼을 누르면 프로젝트 페이지로 이동
    const handleProjectPage = () => {
        navigate('/project'); // Navigate to StudyPage
    };

    // 검색어가 새로이 입력되거나 변경되면 여기서 감지해서 백엔드에 보낼 searchTerm을 세팅함
    const handleSearch = (value) => {
        setSearchTerm(value); // 검색어를 세팅
        setCurrentPage(0); // 검색어가 변경되면 0페이지로 이동
    };

    // 현재 선택된 selectedBanners에 따라 필터링 된 게시물을 기반으로 실제 렌더링 진행
    const renderPosts = (posts) => {
        return (
            <div>
                {posts.map((item, index) => (
                    <Card key={index} style={{ margin: '0 0 0 0'}}> {/*margin bottom속성을 사용 - 각 페이지로 navigate하는 버튼이 card랑 딱 붙여서 보이기 위해 card끼리는 margin bottom으로 간격 띄우고, 첫번째 카드 margin top을 0으로 해서 딱 붙여서 보이게 했음 */}

                        {/**아래의 속성들을 antd Card 컴포넌트로 묶음*/}
                        {/** 이상하게, antd에서 끌어온 애들은 style = {{}}로 적용이 안되고 css로 적용될 때가 있음 */}
                        <div onClick={() => handleRowClick(item.id)} style={{ cursor: 'pointer' }}>
                            <Row gutter={[16, 16]} style={{ marginTop: '10px' }} justify="center" align="middle">
                                {/** 수직선 CSS인 vertical-line을 만들어 주었음 */}
                                <Col span={2} style={{ marginRight: '10px', marginLeft : '5px' , textAlign: 'left' }} align = "left">
                                    <strong style={{ fontSize: '14px' }}> {item.nickName} </strong>
                                </Col>
                                <Col span = {16}>
                                    <Row>
                                        <Col>
                                            <div style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '100%' }}>
                                                <strong style={{ fontSize: '15px' }}>{item.title}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                    <Row>
                                        분류: {item.web ? "Web " : ""}{item.app ? "App " : ""}{item.game ? "Game " : ""}{item.ai ? "AI " : ""}
                                    </Row>
                                    <Divider></Divider>
                                    <Row>
                                        <div style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '100%' }}>
                                        {item.briefContent}
                                        </div>
                                    </Row>

                                </Col>
                                <Col span={4} >
                                    <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
                                        인원: {item.counts} / {item.recruitmentCount}
                                    </div>
                                    <div style={{ marginLeft: '3px', fontSize: '13px' }}>
                                        모집 마감일: {formatDate(item.endDate)}
                                    </div>
                                    <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
                                        조회 수: {item.viewCount}
                                    </div>
                                </Col>
                            </Row>
                        </div>
                    </Card>

                ))}
            </div>
        );
    }

    return (
        <div>
            <SearchInProjectPage setSearchTerm={handleSearch} />
            {/* 프로젝트 페이지에서 전용으로 사용할 하위 컴포넌트인 SearchInProjectPage에서 검색어 입력받고
                검색 완료 후 돋보기 클릭이나 엔터하는 기능을 위임.
                만약 엔터나 돋보기 버튼 클릭하면 하위 컴포넌트의 handleSearch 동작 후에 
                다시 상위 컴포넌트인 ProjectPage의 handleSearch도 동작하면서 백엔드에 보낼 searchTerm을 세팅하고, 
                0페이지로 보내는 것 같음
            */}

            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Row>
                    {/** 버튼들을 중앙과 오른쪽 두 경우에만 위치시키기 위해 만든 좌측의 더미 공간 */}
                    <Col span={6}>

                        {/* Sort buttons - 최신등록순, 마감일자 순 버튼*/}
                        <Button
                            type={sortOption === 'latestPosts' ? 'primary' : 'default'}
                            onClick={() => handleSortOptionChange('latestPosts')}
                            style={{ marginRight: '10px' }}
                        >
                            최신 등록순
                        </Button>
                        <Button
                            type={sortOption === 'nearDeadline' ? 'primary' : 'default'}
                            onClick={() => handleSortOptionChange('nearDeadline')}
                        >
                            가까운 마감일순
                        </Button>

                    </Col>
                    <Col span={12} style={{ textAlign: 'center' }}>
                        <Button
                            type={selectedBanners.includes('all') ? 'primary' : 'default'}
                            onClick={() => toggleBanner('all')}
                            style={{ marginRight: '10px' }}
                        >
                            All
                        </Button>
                        <Button
                            type={selectedBanners.includes('web') ? 'primary' : 'default'}
                            onClick={() => toggleBanner('web')}
                        >
                            Web
                        </Button>
                        <Button
                            type={selectedBanners.includes('app') ? 'primary' : 'default'}
                            onClick={() => toggleBanner('app')}
                        >
                            App
                        </Button>
                        <Button
                            type={selectedBanners.includes('game') ? 'primary' : 'default'}
                            onClick={() => toggleBanner('game')}
                        >
                            Game
                        </Button>
                        <Button
                            type={selectedBanners.includes('ai') ? 'primary' : 'default'}
                            onClick={() => toggleBanner('ai')}
                        >
                            AI
                        </Button>
                    </Col>
                    <Col span={6} style={{ textAlign: 'right' }}>
                        <Button type="primary" onClick={onClickHandler}>
                            Upload Project
                        </Button>
                    </Col>
                </Row>
            </div>
            {/* 각 페이지로 navigate하는 버튼들 추가 완료*/}
            <div style={{ textAlign: 'left', margin: "0 0" }}>
                {/** 현재 경로가 localhost:3000/project이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
                <Button type={location.pathname === '/portfoliocard' ? 'primary' : 'default'} onClick={handlePortfolioCardPage}>
                    Portfolio Card
                </Button>
                <Button type={location.pathname === '/project' ? 'primary' : 'default'} onClick={handleProjectPage}>
                    Project
                </Button>
                <Button type={location.pathname === '/study' ? 'primary' : 'default'} onClick={handleStudyPage}>
                    Study
                </Button>
                <hr></hr>

            </div>

            {renderPosts(data)}

            {/* antd페이지네이션 적용 */}
            {/* 동적으로 쿼리 날렸을 때 페이지 하단에 보이는 페이지 버튼도 동적으로 구성해야 함 -> 백엔드에서 받아온 totalPages를 기반으로 페이지 버튼 수를 만들어 넣어줌 */}
            {/*백엔드에서는 페이징을 0부터 시작하지만, 프론트에서는 페이지 버튼을 1부터 세팅해줘야하므로 이를 위한 코드*/}
            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Pagination
                    current={currentPage + 1} // Ant Design's Pagination starts from 1, while your state starts from 0
                    total={totalPages * pageSize}
                    pageSize={pageSize}
                    onChange={(page) => setCurrentPage(page - 1)} //사용자가 해당 버튼 (예: 2번 버튼)을 누르면 currentPage를 1로 세팅하여 백엔드에 요청 보냄(백엔드는 프런트에서 보는 페이지보다 하나 적은 수부터 페이징을 시작하므로)
                    showSizeChanger={false}
                />
            </div>
        </div>
    );
}

export default ProjectPage;
