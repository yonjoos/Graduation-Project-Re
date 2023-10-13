import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { Row, Col, Button, Card, Pagination, Divider, Menu, Dropdown } from 'antd';
import { request } from '../../../hoc/request';
import { lastVisitedEndpoint } from '../../../_actions/actions';
import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../hoc/request';
import SearchInLandingPage from '../LandingPage/SearchInLandingPage';

function SearchPortfolioListPage(onSearch) {

    const navigate = useNavigate();
    const dispatch = useDispatch();
    const location = useLocation(); //현재 내가 들어와있는 경로를 확인하기 위한 함수

    const [searchTerm, setSearchTerm] = useState(useParams()); // 현재 추적중인 검색어 값
    const currentSearchTerm = useParams(); // 이건 한번 유효한 검색이 완료된 후에는 일시적으로 고정된 값

    // 백엔드에서 받은 검색어 기반 결과 리스트(3개)를 정의. 처음에 이 페이지에 들어오면 빈 배열
    const [data, setData] = useState({
        projectSearchDtoList: [], // 프로젝트 제목 관련 최대 5개 가져옴
        studySearchDtoList: [], // 스터디 제목 관련 최대 5개 가져옴
        userSearchDtoList: [], // 유저 이름 관련 최대 5개 가져옴
    });
    const [currentPage, setCurrentPage] = useState(0); //  페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [portfolioData, setPortfolioData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 포트폴리오 카드 목록들 세팅
    const [selectedBanners, setSelectedBanners] = useState(['all']); // 처음 해당 페이지가 setting될 떄는 선택된 배너가 '전체'가 되도록 함
    const [sortOption, setSortOption] = useState('latestPortfolio'); //최신등록순: latestPosts / 조회수순: byViewCount
    const pageSize = 9;


    // 키워드를 치는 순간 순간마다 백엔드에서 데이터 받아옴
    useEffect(() => {
        console.log('현재 검색된 키워드: ', searchTerm);
        fetchFilteredSearchLists();
    }, [searchTerm]);

    // http://localhost:3000/search/portfoliocard/query/검색어값 인 경우
    // 검색어를 추출해서, 그 값을 기반으로 백엔드에 관련 포폴 카드를 가져오기
    useEffect(() => {
        console.log('현재 쿼리 스트링 키워드: ', currentSearchTerm.searchTerm);
        console.log('현재 선택된 배너 정보', selectedBanners);
        fetchSearchResultLists();
    }, [currentSearchTerm.searchTerm, currentPage, selectedBanners, sortOption]);


    // 백엔드에 입력 완료된 검색어 기반으로, match되는 검색 결과물 가져오기
    const fetchSearchResultLists = async () => {
        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                selectedBanners: selectedBanners.join(','),
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 9개씩만 나오도록 구성했음)
                sortOption: sortOption,
                searchTerm: currentSearchTerm.searchTerm // 검색어 키워드 문자열
            });

            const response = await request('GET', `/getPortfolioSearchResult?${queryParams}`);

            setPortfolioData(response.data.content);
            setTotalPages(response.data.totalPages);

        } catch (error) {
            console.error("Error fetching data:", error);
        }

    }

    const onClickHandler = (nickName) => {

        // 변경해야함
        // /portfolio/${nickName}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /search/portfoliocard/query/${currentSearchTerm.searchTerm}로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /search/portfoliocard/query/${currentSearchTerm.searchTerm}로 설정
        dispatch(lastVisitedEndpoint(`/search/portfoliocard/query/${currentSearchTerm.searchTerm}`, `/search/portfoliocard/query/${currentSearchTerm.searchTerm}`, `/search/portfoliocard/query/${currentSearchTerm.searchTerm}`));
        setLastVisitedEndpoint(`/search/portfoliocard/query/${currentSearchTerm.searchTerm}`);
        setLastLastVisitedEndpoint(`/search/portfoliocard/query/${currentSearchTerm.searchTerm}`);
        setLastLastLastVisitedEndpoint(`/search/portfoliocard/query/${currentSearchTerm.searchTerm}`);

        // Error name : Actions must be plain objects. Instead, the actual type was: 'undefined'.
        // Solution : SetLastVisitedEndpoint is not a typical Redux action creator, cannot be stated in dispatch().
        navigate(`/portfolio/${nickName}`);
    }

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

    // 드롭다운을 위한 코드
    const menu = (
        <Menu selectedKeys={[sortOption]}>
            <Menu.Item key="latestPortfolio" onClick={() => handleSortOptionChange('latestPortfolio')}>
                최신 등록 순
            </Menu.Item>
            <Menu.Item key="byViewCount" onClick={() => handleSortOptionChange('byViewCount')}>
                조회수 순
            </Menu.Item>
        </Menu>
    );

    // 최신등록순, 조회수 순 버튼이 눌러지면 현재 선택된 버튼으로 세팅하고, 페이지는 0번으로 간다
    const handleSortOptionChange = (option) => {
        setSortOption(option);
        setCurrentPage(0);
    };

    // 포폴 카드 렌더링 관련
    const renderCards = (cards) => {
        if (!cards || cards.length === 0) {
            return <div>No data available</div>; // or any other appropriate message
        }

        return (
            <div>
                <Row gutter={16}>
                    {cards.map((item, index) => (

                        <Col xs={24} sm={8} key={index}>
                            <Card onClick={() => onClickHandler(item.nickName)} title={`👩🏻‍💻 ${item.nickName}`} style={{ height: '270px', marginBottom: '10px', cursor: 'pointer' }}>
                                {/* style = {{cursor: 'pointer'}} */}
                                <b>Field Of Interests</b>
                                <br />
                                {item.web ? "Web " : ""}{item.app ? "App " : ""}{item.game ? "Game " : ""}{item.ai ? "AI " : ""}
                                <Divider style={{ marginTop: '10px', marginBottom: '10px' }}></Divider>
                                <b>Brief Introduction</b>
                                <br />
                                {item.shortIntroduce}
                                <Divider style={{ marginTop: '10px', marginBottom: '10px' }}></Divider>
                                <b>조회 수 : </b>
                                {item.viewCount}
                            </Card>
                        </Col>
                    ))}
                </Row>
            </div>
        )
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

    const handleSearchPortfolioCard = () => {
        navigate(`/search/portfoliocard/query/${currentSearchTerm.searchTerm}`);
    };

    const handleSearchProject = () => {
        navigate(`/search/project/query/${currentSearchTerm.searchTerm}`);
    };

    const handleSearchStudy = () => {
        navigate(`/search/study/query/${currentSearchTerm.searchTerm}`);
    };

    // 백엔드에서 받아온 검색 결과를 가지고 실제 렌더링 진행.
    // 프로젝트, 스터디, 유저를 각각 카드로 감싸고, 그 안엔 버튼으로 감쌈
    const renderSection = (title, dataArray) => {

        const handleButtonClick = (title, id, name) => {

            // // 버튼을 클릭하면, 현재 위치를 다 '/'로 세팅해서 디스패치
            // dispatch(lastVisitedEndpoint('/', '/', '/'));
            // setLastVisitedEndpoint('/');
            // setLastLastVisitedEndpoint('/');
            // setLastLastLastVisitedEndpoint('/');

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
                        {/** 현재 경로가 localhost:3000/search/portfoliocard이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
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
                        <Dropdown overlay={menu} placement="bottomRight">
                            <Button>
                                정렬
                            </Button>
                        </Dropdown>
                    </Col>
                </Row>

                <hr />


            </div>


            {renderCards(portfolioData)}

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

export default SearchPortfolioListPage;