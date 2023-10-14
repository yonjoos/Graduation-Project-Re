import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { Row, Col, Button, Card, Pagination, Divider, Menu, Dropdown } from 'antd';
import { request } from '../../../hoc/request';
import SearchInLandingPage from '../LandingPage/SearchInLandingPage';
import { lastVisitedEndpoint } from '../../../_actions/actions';
import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../hoc/request';


function SearchProjectListPage(onSearch) {

    const navigate = useNavigate();
    const dispatch = useDispatch();
    const location = useLocation(); //현재 내가 들어와있는 경로를 확인하기 위한 함수

    const [searchTerm, setSearchTerm] = useState(useParams());
    const currentSearchTerm = useParams(); // 이건 한번 유효한 검색이 완료된 후에는 일시적으로 고정된 값 

    // 백엔드에서 받은 검색어 기반 결과 리스트(3개)를 정의. 처음에 이 페이지에 들어오면 빈 배열
    const [data, setData] = useState({
        projectSearchDtoList: [], // 프로젝트 제목 관련 최대 5개 가져옴
        studySearchDtoList: [], // 스터디 제목 관련 최대 5개 가져옴
        userSearchDtoList: [], // 유저 이름 관련 최대 5개 가져옴
    });
    const [selectedBanners, setSelectedBanners] = useState(['all']); // 처음 해당 페이지가 setting될 떄는 선택된 배너가 '전체'가 되도록 함
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [sortOption, setSortOption] = useState('latestPosts'); //최신등록순: latestPosts / 모집마감순: nearDeadline / 조회수순: byViewCount
    const [projectData, setProjectData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 프로젝트 목록들 세팅

    const pageSize = 5; // 현재 게시물 수가 적으므로 페이징을 5개 단위로 하였음

    // 키워드를 치는 순간 순간마다 백엔드에서 데이터 받아옴
    useEffect(() => {
        console.log('현재 검색된 키워드: ', searchTerm);
        fetchFilteredSearchLists();
    }, [searchTerm]);

    // 백엔드에서 검색 결과로 나온 프로젝트 게시물 렌더링을 위함
    useEffect(() => {
        console.log('현재 쿼리 스트링 키워드: ', currentSearchTerm.searchTerm);
        console.log('현재 선택된 배너 정보', selectedBanners);
        fetchSearchResultLists();
    }, [currentSearchTerm.searchTerm, currentPage, selectedBanners, sortOption]);


    // 백엔드에 입력된 검색어 기반으로, match되는 검색 결과물 가져오기
    const fetchSearchResultLists = async () => {
        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                selectedBanners: selectedBanners.join(','),
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 9개씩만 나오도록 구성했음)
                sortOption: sortOption,
                searchTerm: currentSearchTerm.searchTerm // 검색어 키워드 문자열
            });

            const response = await request('GET', `/getProjectSearchResult?${queryParams}`);

            setProjectData(response.data.content);
            setTotalPages(response.data.totalPages);

        } catch (error) {
            console.error("Error fetching data:", error);
        }

    }

    // 페이징 된 각 게시물 목록 하나를 클릭하면 그에 해당하는 게시물의 디테일 페이지로 navigate함
    const handleRowClick = (projectId) => {

        // /project/detail/${projectId}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /search/project/query/${currentSearchTerm.searchTerm}로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /search/project/query/${currentSearchTerm.searchTerm}로 설정
        dispatch(lastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`, `/search/project/query/${currentSearchTerm.searchTerm}`, `/search/project/query/${currentSearchTerm.searchTerm}`));    // 전역에 상태 저장을 위한 애.
        setLastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`);   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
        setLastLastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`);
        setLastLastLastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`);
        navigate(`/project/detail/${projectId}`);
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

    // 최신등록순, 마감일 순, 조회수 순 버튼이 눌러지면 현재 선택된 버튼으로 세팅하고, 페이지는 0번으로 간다
    const handleSortOptionChange = (option) => {
        setSortOption(option);
        setCurrentPage(0);
    };

    // 드롭다운 박스에서 정렬 옵션
    const sortMenu = (
        <Menu>
            <Menu.Item key="latestPosts" onClick={() => handleSortOptionChange('latestPosts')}>
                최신 등록순
            </Menu.Item>
            <Menu.Item key="nearDeadline" onClick={() => handleSortOptionChange('nearDeadline')}>
                가까운 마감일 순
            </Menu.Item>
            <Menu.Item key="byViewCount" onClick={() => handleSortOptionChange('byViewCount')}>
                조회수 순
            </Menu.Item>
            {/* <Menu.Item key="viewExpired" onClick={() => handleSortOptionChange('viewExpired')}>
                마감 지난 게시물 보기
            </Menu.Item> */}
        </Menu>
    );


    // 현재 선택된 selectedBanners에 따라 필터링 된 게시물을 기반으로 실제 렌더링 진행
    const renderPosts = (posts) => {
        return (
            <div>
                {posts.map((item, index) => (
                    <Card key={index} style={{ margin: '0 0 0 0', padding: '1px', textAlign: 'left' }}> {/*margin bottom속성을 사용 - 각 페이지로 navigate하는 버튼이 card랑 딱 붙여서 보이기 위해 card끼리는 margin bottom으로 간격 띄우고, 첫번째 카드 margin top을 0으로 해서 딱 붙여서 보이게 했음 */}

                        {/**아래의 속성들을 antd Card 컴포넌트로 묶음*/}
                        {/** 이상하게, antd에서 끌어온 애들은 style = {{}}로 적용이 안되고 css로 적용될 때가 있음 */}
                        <div onClick={() => handleRowClick(item.id)} style={{ cursor: 'pointer' }}>
                            <Row gutter={[16, 16]} style={{ marginTop: '10px', padding: '1px' }} justify="space-between" align="middle">
                                {/** 수직선 CSS인 vertical-line을 만들어 주었음 */}
                                <Col span={2} style={{ marginRight: '10px', marginLeft: '5px', textAlign: 'left' }} align="left">
                                    <strong style={{ fontSize: '14px' }}> {item.nickName} </strong>
                                </Col>
                                <Col span={16}>
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

    // 배너 버튼 클릭 시 네비게이트 관련
    const handleSearchPortfolioCard = () => {
        navigate(`/search/portfoliocard/query/${currentSearchTerm.searchTerm}`);
    };

    // 배너 버튼 클릭 시 네비게이트 관련
    const handleSearchProject = () => {
        navigate(`/search/project/query/${currentSearchTerm.searchTerm}`);
    };

    // 배너 버튼 클릭 시 네비게이트 관련
    const handleSearchStudy = () => {
        navigate(`/search/study/query/${currentSearchTerm.searchTerm}`);
    };

    // 백엔드에서 받아온 검색 결과를 가지고 실제 렌더링 진행.
    // 프로젝트, 스터디, 유저를 각각 카드로 감싸고, 그 안엔 버튼으로 감쌈
    const renderSection = (title, dataArray) => {

        const handleButtonClick = (title, id, name) => {


            dispatch(lastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`, `/search/project/query/${currentSearchTerm.searchTerm}`, `/search/project/query/${currentSearchTerm.searchTerm}`));    // 전역에 상태 저장을 위한 애.
            setLastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`);   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
            setLastLastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`);
            setLastLastLastVisitedEndpoint(`/search/project/query/${currentSearchTerm.searchTerm}`);

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
                <Col span={24} style={{ display: 'flex', justifyContent: 'center', textAlign: 'center' }}>
                    <Card size='small' style={{ padding: 0, margin: 0, width: 800 }}>
                        <div style={{ width: 800, textAlign: 'left', padding: 0 }}>
                            <strong># {title}</strong>
                        </div>
                        <div style={{ margin: 0 }}>
                            {dataArray.map(item => (
                                <Button
                                    key={item.id}
                                    type="text"
                                    style={{ width: '100%', textAlign: 'left', padding: 0, margin: 0 }}
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
            <SearchInLandingPage onSearch={handleSearch} initialSearchTerm={searchTerm.searchTerm} />

            <div style={{ margin: '20px 0' }}>
                {renderSection('User', data.userSearchDtoList)}
                {renderSection('Project', data.projectSearchDtoList)}
                {renderSection('Study', data.studySearchDtoList)}
            </div>

            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Row style={{ display: 'flex', justifyContent: 'center' }}>
                    <Button type={selectedBanners.includes('all') ? 'primary' : 'default'}
                        onClick={() => toggleBanner('all')}
                        style={{ marginRight: '10px' }}>
                        All
                    </Button>
                    <Button
                        type={selectedBanners.includes('web') ? 'primary' : 'default'}
                        onClick={() => toggleBanner('web')}>
                        Web
                    </Button>
                    <Button
                        type={selectedBanners.includes('app') ? 'primary' : 'default'}
                        onClick={() => toggleBanner('app')}>
                        App
                    </Button>
                    <Button
                        type={selectedBanners.includes('game') ? 'primary' : 'default'}
                        onClick={() => toggleBanner('game')}>
                        Game
                    </Button>
                    <Button
                        type={selectedBanners.includes('ai') ? 'primary' : 'default'}
                        onClick={() => toggleBanner('ai')}>
                        AI
                    </Button>
                </Row>
            </div>

            <div style={{ textAlign: 'left', margin: "0 0" }}>
                <Row>
                    <Col span={18} style={{ textAlign: 'left' }}>
                        {/** 현재 경로가 http://localhost:3000/search/project이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
                        <Button type={location.pathname.includes('/search/portfoliocard') ? 'primary' : 'default'} onClick={handleSearchPortfolioCard}>
                            Portfolio Card
                        </Button>
                        <Button type={location.pathname.includes('/search/project') ? 'primary' : 'default'} onClick={handleSearchProject}>
                            Project
                        </Button>
                        <Button type={location.pathname.includes('/search/study') ? 'primary' : 'default'} onClick={handleSearchStudy}>
                            Study
                        </Button>
                    </Col>
                    <Col span={6} style={{ textAlign: 'right' }}>
                        <Dropdown overlay={sortMenu} placement="bottomRight">
                            <Button>
                                정렬
                            </Button>
                        </Dropdown>
                    </Col>
                </Row>
                <hr />


            </div>


            {renderPosts(projectData)}

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

export default SearchProjectListPage;