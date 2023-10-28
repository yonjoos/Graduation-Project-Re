// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
//import { useDispatch } from 'react-redux';
import { Card, Row, Col, Divider, Button, Pagination, Menu, Dropdown } from 'antd';
import { request, setHasPortfolio } from '../../../hoc/request';
//import { lastVisitedEndpoint } from '../../../_actions/actions';
//import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../hoc/request';
import SearchInPortfolioCardPage from './SearchInPortfolioCardPage';


function PortfolioCardPage() {
    const location = useLocation();
    const navigate = useNavigate();
    //const dispatch = useDispatch();

    const [searchTerm, setSearchTerm] = useState(""); // 검색어 값 -  엔터나 클릭 시에만 변경
    const [currentSearchTerm, setCurrentSearchTerm] = useState(""); // 추적하는 검색어 값, 타이핑 시마다 변경
    const [relatedSearchTermEnable, setRelatedSearchTermEnable] = useState(true); // 연관 검색어 렌더링 필드 활성화 여부

    // 실시간 검색어 기반으로 가져온 연관 검색어 목록
    const [searchData, setSearchData] = useState({
        userSearchDtoList: [], //유저 이름 관련 최대 5개 가져옴
    });
    const [data, setData] = useState([]); // 백엔드에서 가져온 관련 포트폴리오 자료 값
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [selectedBanners, setSelectedBanners] = useState(['all']); // 처음 해당 페이지가 setting될 떄는 선택된 배너가 '전체'가 되도록 함
    const [sortOption, setSortOption] = useState('latestPortfolio'); // 최신 등록 순 기본으로 선택
    const [reload, setReload] = useState(0);
    const [recommend, setRecommend] = useState(0);
    const [sustain, setSustain] = useState(0);

    const pageSize = 9;

    // 키워드를 치는 순간 순간마다 연관 검색어 값을 백엔드에서 받아옴
    useEffect(() => {
        console.log('현재 검색된 키워드: ', currentSearchTerm);
        setRelatedSearchTermEnable(true); // 연관 검색어 렌더링 활성화
        fetchFilteredSearchLists();
    }, [currentSearchTerm]);

    // <Button> PortfolioCard 다시 눌렀을 때 실행
    // Handler : handleReload() 에 의해 호출됨

    useEffect(() => {
        setCurrentPage(0);
        setTotalPages(0);
        setSearchTerm("");
        setSelectedBanners(['all']);

        // REQUEST FUNCTION 
        fetchUsers();

        setReload(0);
    }, [reload]);

    useEffect(()=>{
        if(recommend == 1) {
            Recommend();
            setRecommend(0);
            setSustain(1);
        }
        else{
        }
    }, [recommend])


    // 검색 조건이 바뀔 때 실행
    // Handler : toggleBanner / handleSearch, toggleBanner, Pagination / handleSearch
    useEffect(() => {

        console.log('현재 선택된 배너 정보', selectedBanners);
        console.log('현재 검색 완료된 키워드: ', searchTerm);
        fetchUsers();
    }, [selectedBanners, currentPage, sortOption, searchTerm]);


    // 백엔드에 연관 검색어에 기반한 닉네임 값을 받아오기 위한 요청 보내기
    const fetchFilteredSearchLists = async () => {
        try {
            // 만약 검색어가 있다면,
            if (currentSearchTerm !== "") {
                const queryParams = new URLSearchParams({
                    searchTerm: currentSearchTerm, // 검색어 세팅
                });

                // 백엔드에서 데이터 받아오기
                const response = await request('GET', `/getFilteredSearchLists?${queryParams}`);

                // 데이터가 있다면 세팅, 없으면 각각 빈 배열로 세팅
                if (response.data) {
                    setSearchData({
                        userSearchDtoList: response.data.userSearchDtoList || [],
                    });
                } else {
                    // Handle the case where response.data.content is undefined
                    console.error("Error fetching data: response.data.content is undefined");
                }
            } else {
                // 검색어가 없다면, 빈 배열로 세팅
                setSearchData({
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

    // 백엔드에서 받아온 연관 검색어(닉네임) 결과를 가지고 실제 렌더링 진행.
    // 유저를 각각 카드로 감싸고, 그 안엔 버튼으로 감쌈
    const renderSection = (title, dataArray) => {

        const handleButtonClick = (title, id, name) => {

            // dispatch(lastVisitedEndpoint('/portfoliocard', '/portfoliocard', '/portfoliocard'));
            // setLastVisitedEndpoint('/portfoliocard');
            // setLastLastVisitedEndpoint('/portfoliocard');
            // setLastLastLastVisitedEndpoint('/portfoliocard');

            // 각각에 대해 올바르게 라우팅 걸어주기
            if (title === 'User') {
                navigate(`/portfolio/${name}`);
            }
        };

        // 빈 배열이 아니라면, 즉, 렌더링해야하는 값임
        if (dataArray && dataArray.length > 0) {
            return (

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

            );
        }
        return null;
    };


    // 백엔드에서 받아온 포트폴리오 정보를 카드로 만들어서 뿌려줌
    const fetchUsers = async () => {

        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                selectedBanners: selectedBanners.join(','), // selectedBanners 배열을 쉼표로 구분된 문자열로 변환
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 9개씩만 나오도록 구성했음)
                sortOption: sortOption, // 최신 등록순, 조회수 순
                searchTerm: searchTerm // 검색어 키워드 문자열
            });

            const response = await request('GET', `/getCards?${queryParams}`);
            setData(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Error fetching data:", error);
        }
    };



    // 포트폴리오 카드 클릭 핸들러, 해당 유저의 포트폴리오로 이동
    const onClickHandler = (nickName) => {
        // /portfolio/${nickName}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /portfoliocard로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /portfoliocard로 설정
        // dispatch(lastVisitedEndpoint('/portfoliocard', '/portfoliocard', '/portfoliocard'));
        // setLastVisitedEndpoint('/portfoliocard');
        // setLastLastVisitedEndpoint('/portfoliocard');
        // setLastLastLastVisitedEndpoint('/portfoliocard');

        // Error name : Actions must be plain objects. Instead, the actual type was: 'undefined'.
        // Solution : SetLastVisitedEndpoint is not a typical Redux action creator, cannot be stated in dispatch().
        navigate(`/portfolio/${nickName}`);
    }


    // 엔터나 클릭 시에만 변경됨(검색어 관련)
    const handleSearch = (value) => {
        setSustain(0);
        setSearchTerm(value); // 검색어를 세팅
        setRelatedSearchTermEnable(false); // 엔터나 클릭을 눌렀으므로 연관 검색어 렌더링 여부를 false로 설정
        setCurrentPage(0); // 검색어가 바뀌면, 강제로 1페이지로 이동시킴
    };

    // 타이핑 시마다 변경(검색어 관련)
    const handleSearchTerm = (value) => {
        setCurrentSearchTerm(value);

    }


    // <Button> Project의 핸들러, ProjectPage로 이동
    const handleProjectPage = () => {
        setSustain(0);
        navigate('/project');
    };


    // <Button> PortfolioCard 의 핸들러, 페이지 리로딩
    const handleReload = () => {
        setSustain(0);
        setReload(1);
    };


    // <Button> Study의 핸들러, StudyPage로 이동
    const handleStudyPage = () => {
        setSustain(0);
        navigate('/study');
    };


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


    const Recommend = async() => {

        try{

            const response = await request('GET', `/getRecommendation`);
            setData(response.data); 
            setTotalPages(response.data.totalPages);
            console.log(data);
        } catch (error) {
            console.error("레코멘드 노노", error);
        }



    }

    const handleRecommend = () => {
        setRecommend(1);
    }


    // 드롭다운을 위한 코드
    const menu = (
        <Menu selectedKeys={[sortOption]}>
            <Menu.Item key="latestPortfolio" onClick={() => setSortOption('latestPortfolio')}>
                최신 등록 순
            </Menu.Item>
            <Menu.Item key="byViewCount" onClick={() => setSortOption('byViewCount')}>
                조회수 순
            </Menu.Item>
        </Menu>
    );


    // COMPONENTS ###############################################

    // renderCards
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


    return (
        <div>
            {/* 
                검색어 입력 후 엔터/클릭 , 검색어 입력을 할 때마다 바뀌는 이벤트를 별도로 보냄
                handleSearch: 엔터/클릭 관련
                onChange: 동적 타이핑 관련 
            */}
            <SearchInPortfolioCardPage onSearch={handleSearch} onChange={handleSearchTerm} />

            {/* 연관 검색어 활성화 여부에 따라 렌더링 진행 */}
            <div style={{ display: 'flex', justifyContent: 'center', textAlign: 'center', margin: '20px 0' }}>
                <div style={{ position: 'absolute', zIndex: 2 }}>
                    {(relatedSearchTermEnable ?
                        (renderSection('User', searchData.userSearchDtoList)) : null)}
                </div>

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
            <div style={{ textAlign: 'center', marginTop: '15px', marginBottom: '15px' }}>
                <Row>
                    <Col span={18} style={{ textAlign: 'left' }}>
                        {/** 현재 경로가 localhost:3000/project이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
                        <Button type={location.pathname === '/portfoliocard' ? 'primary' : 'default'} onClick={handleReload}>
                            Portfolio Card
                        </Button>
                        <Button type={location.pathname === '/project' ? 'primary' : 'default'} onClick={handleProjectPage}>
                            Project
                        </Button>
                        <Button type={location.pathname === '/study' ? 'primary' : 'default'} onClick={handleStudyPage}>
                            Study
                        </Button>
                        <Button onClick={handleRecommend}>
                            RECOMMEND
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
            <div style={{display:'grid'}}> 
                {sustain === 1 ? (
                    <div style={{ textAlign: 'center', marginBottom:'20px', backgroundColor: 'skyblue'  }} >
                        <strong>이런 사람은 어떠세요?</strong>
                    </div>
                ) : null}
                {renderCards(data)}
            </div>
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


export default PortfolioCardPage;