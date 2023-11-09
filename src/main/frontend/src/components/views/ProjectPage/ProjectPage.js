import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
//import { useDispatch } from 'react-redux';
import { Row, Col, Button, Card, Pagination, Dropdown, Menu, Divider } from 'antd';
import { request } from '../../../hoc/request';
import SearchInProjectPage from './SearchInProjectPage';
//import { lastVisitedEndpoint } from '../../../_actions/actions'
//import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../hoc/request';
import './ProjectPage.css';


function ProjectPage() {
    const navigate = useNavigate();
    //const dispatch = useDispatch();
    const location = useLocation(); //현재 내가 들어와있는 경로를 확인하기 위한 함수

    const [searchTerm, setSearchTerm] = useState(""); // 검색어 값 - 엔터나 클릭 시에만 변경
    const [currentSearchTerm, setCurrentSearchTerm] = useState(""); // 추적하는 검색어 값, 타이핑 시마다 변경
    const [relatedSearchTermEnable, setRelatedSearchTermEnable] = useState(true); // 연관 검색어 렌더링 필드 활성화 여부

    // 실시간 검색어 기반으로 가져온 연관 검색어 목록
    const [searchData, setSearchData] = useState({
        projectSearchDtoList: [], // 프로젝트 제목 관련 최대 5개 가져옴
    });
    const [data, setData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 게시물 목록들 세팅
    const [selectedBanners, setSelectedBanners] = useState(['all']); // 처음 해당 페이지가 setting될 떄는 선택된 배너가 '전체'가 되도록 함
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [sortOption, setSortOption] = useState('latestPosts'); //최신등록순: latestPosts / 모집마감순: nearDeadline


    const pageSize = 5; // 현재 게시물 수가 적으므로 페이징을 5개 단위로 하였음
    const myNickName = window.localStorage.getItem('user_nickname');


    // 키워드를 치는 순간 순간마다 연관 검색어 값을 백엔드에서 받아옴
    useEffect(() => {
        console.log('현재 검색된 키워드: ', currentSearchTerm);
        setRelatedSearchTermEnable(true); // 연관 검색어 렌더링 활성화
        fetchFilteredSearchLists();
    }, [currentSearchTerm]);

    // 페이지가 새로 마운트 될 때마다 실행됨.
    // 현재의 selectedBanners상태(어떤 배너가 선택되어있는지)와 
    // 현재 사용자가 하이라이트한 페이지 번호 상태, 
    // 최신일순/마감일순, 조회순에 대한 정렬 옵션,
    // 검색어 키워드 문자열
    // 를 기반으로 백엔드에 동적쿼리 보냄
    useEffect(() => {
        console.log('현재 선택된 배너 정보', selectedBanners);
        console.log('현재 검색 완료된 키워드: ', searchTerm);
        fetchFilteredPosts();
    }, [selectedBanners, currentPage, sortOption, searchTerm]);

    // 백엔드에 연관 검색어에 기반한 프로젝트 제목 값을 받아오기 위한 요청 보내기
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
                        projectSearchDtoList: response.data.projectSearchDtoList || [],
                    });
                } else {
                    // Handle the case where response.data.content is undefined
                    console.error("Error fetching data: response.data.content is undefined");
                }
            } else {
                // 검색어가 없다면, 빈 배열로 세팅
                setSearchData({
                    projectSearchDtoList: [],
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

    // 백엔드에서 받아온 연관 검색어(프로젝트) 결과를 가지고 실제 렌더링 진행.
    // 프로젝트를 각각 카드로 감싸고, 그 안엔 버튼으로 감쌈
    const renderSection = (title, dataArray) => {

        const handleButtonClick = (title, id, name) => {

            // dispatch(lastVisitedEndpoint('/project', '/project', '/project'));
            // setLastVisitedEndpoint('/project');
            // setLastLastVisitedEndpoint('/project');
            // setLastLastLastVisitedEndpoint('/project');

            // 각각에 대해 올바르게 라우팅 걸어주기
            if (title === 'Project') {
                navigate(`/project/detail/${id}`);
            }
        };

        // 빈 배열이 아니라면, 즉, 렌더링해야하는 값임
        if (dataArray && dataArray.length > 0) {
            return (

                <Card size='small' style={{ padding: 0, margin: 0, width: '100%' }}>
                    <div style={{ width: '100%', textAlign: 'left', padding: 0 }}>
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
        // dispatch(lastVisitedEndpoint('/project', '/project', '/project'));    // 전역에 상태 저장을 위한 애.
        // setLastVisitedEndpoint('/project');   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
        // setLastLastVisitedEndpoint('/project');
        // setLastLastLastVisitedEndpoint('/project');
        navigate(`/project/detail/${projectId}`);
    }

    // 게시물 목록에서 닉네임 필드를 클릭하면, 해당 닉네임을 가진 회원의 포트폴리오 창으로 navigate
    const handleNicknameClick = (nickName) => {

        navigate(`/portfolio/${nickName}`);
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

    // 2023/8/26-11:11분을 2023년 8월 26일 11시 11분 형식으로 변환 
    const formatDateTime = (dateTimeArray) => {

        if (!Array.isArray(dateTimeArray)) {
            // dateTimeArray가 배열이 아닌 경우 오류 처리
            return 'Invalid date and time format';
        }
        const [year, month, day, hours, minutes] = dateTimeArray;
        const date = new Date(year, month - 1, day, hours, minutes);

        // 년, 월, 일, 시간, 분 형식으로 포맷팅
        const formattedYear = date.getFullYear();
        const formattedMonth = (date.getMonth() + 1).toString().padStart(2, '0'); // 월을 2자리로 표현
        const formattedDay = date.getDate().toString().padStart(2, '0'); // 일을 2자리로 표현
        const formattedHours = date.getHours().toString().padStart(2, '0'); // 시를 2자리로 표현
        const formattedMinutes = date.getMinutes().toString().padStart(2, '0'); // 분을 2자리로 표현

        const formattedDateTime = `${formattedYear}.${formattedMonth}.${formattedDay}. ${formattedHours}:${formattedMinutes}`;

        return formattedDateTime;
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
        setRelatedSearchTermEnable(false); // 엔터나 클릭을 눌렀으므로 연관 검색어 렌더링 여부를 false로 설정
        setCurrentPage(0); // 검색어가 변경되면 0페이지로 이동
    };

    // 타이핑 시마다 변경(검색어 관련)
    const handleSearchTerm = (value) => {
        setCurrentSearchTerm(value);

    }


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

    const categoryTagStyle = {
        display: 'flex',
        padding: '0px 5px 0px 5px',
        backgroundColor: '#faf082', /* 원하는 색상으로 변경 */
        borderRadius: '50px', /* 타원형 모양을 만들기 위해 사용 */
        color: '#ff8400', /* 텍스트 색상 설정 */
        marginLeft: '-0.3%',
        marginRight: '5px'
    };

    const linkStyle = {
        textDecoration: 'none',
        transition: 'text-decoration 0.3s',
        color:'black'
      };
    
      const handleMouseEnter = (e) => {
        e.currentTarget.style.textDecoration = 'underline';
      };
    
      const handleMouseLeave = (e) => {
        e.currentTarget.style.textDecoration = 'none';
    };

    // 현재 선택된 selectedBanners에 따라 필터링 된 게시물을 기반으로 실제 렌더링 진행
    const renderPosts = (posts) => {
        return (
            <div style={{ marginTop: '10px', padding: '1px', width: '100%' }} justify="space-between" >
                <Card title={`PROJECTS`} headStyle={{ background: '#fffec1', color: '#ff8400' }}>
                    {posts.map((item, index) => (
                        <div>
                            <div style={{ display: 'flex', marginTop: '0px' }}>
                                <div style={{ width: '80%', display: 'grid', marginLeft: '10px' }}>
                                    <div>
                                        <div style={{display:'flex', marginBottom:'10px', alignItems:'center'}}>
                                            <div >
                                                <Link
                                                    to={myNickName === item.nickName ? (`/portfolio`) :`/portfolio/${item.nickName}`} 
                                                    key={index}
                                                    className="hoverable-item"
                                                    onMouseEnter={handleMouseEnter}
                                                    onMouseLeave={handleMouseLeave}
                                                    style={linkStyle}
                                                >
                                                    <img
                                                        style={{ borderRadius: '50%', width: '40px', height: '40px', border: '3px solid salmon', marginRight:'10px' }}
                                                        src={`https://storage.googleapis.com/hongik-pickme-bucket/${item.imageUrl}`}
                                                    />
                                                </Link>
                                            </div>
                                            <div>
                                            <Link
                                                    to={myNickName === item.nickName ? (`/portfolio`) :`/portfolio/${item.nickName}`} 
                                                    key={index}
                                                    className="hoverable-item"
                                                    onMouseEnter={handleMouseEnter}
                                                    onMouseLeave={handleMouseLeave}
                                                    style={linkStyle}
                                            >
                                                <strong className="nickname">{item.nickName}</strong>
                                            </Link>
                                            </div>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex' }}>
                                        <Link
                                            to={`/project/detail/${item.id}`} 
                                            key={index}
                                            className="hoverable-item"
                                            onMouseEnter={handleMouseEnter}
                                            onMouseLeave={handleMouseLeave}
                                            style={linkStyle}
                                        >
                                            <strong style={{ fontSize: '18px' }}>{truncateString(item.title, 40)}</strong>
                                        </Link>
                                    </div>
                                    <div style={{ marginTop: '10px', marginRight: '20px', textAlign: 'left', cursor: 'pointer' }}
                                        onMouseUp={()=>handleRowClick(item.id)}
                                    >
                                        {truncateString(item.briefContent, 50)}
                                    </div>
                                    <strong style={{ display: 'flex' , fontSize:'12px'}}>
                                        {item.web && <span style={{ ...categoryTagStyle, backgroundColor: '#faf082' }}>#WEB</span>}
                                        {item.app && <span style={{ ...categoryTagStyle, backgroundColor: '#faf082' }}>#APP</span>}
                                        {item.game && <span style={{ ...categoryTagStyle, backgroundColor: '#faf082' }}>#GAME</span>}
                                        {item.ai && <span style={{ ...categoryTagStyle, backgroundColor: '#faf082' }}>#AI</span>}
                                    </strong>
                                </div>
                                <div style={{ display: 'grid', marginLeft: '0px', width: '200px', alignItems: 'center' }}>
                                    <div>
                                        인원: {item.counts} / {item.recruitmentCount} <br></br>마감: {formatDate(item.endDate)} <br></br> 👀 조회 수: {item.viewCount}
                                        <br/><br/><div style={{color: 'gray', fontSize: 'small'}}>{formatDateTime(item.finalUpdatedTime)}</div>
                                    </div>
                                </div>
                            </div>
                            <Divider />
                        </div>
                    ))}
                </Card>
            </div>
        )
    }

    return (
        <div style={{width:'100%'}}>
            {/* 
                검색어 입력 후 엔터/클릭 , 검색어 입력을 할 때마다 바뀌는 이벤트를 별도로 보냄
                handleSearch: 엔터/클릭 관련
                onChange: 동적 타이핑 관련 
            */}
            <br />
            <SearchInProjectPage onSearch={handleSearch} onChange={handleSearchTerm} />

            {/* 연관 검색어 활성화 여부에 따라 렌더링 진행 */}
            <div style={{ display: 'flex', justifyContent: 'center', textAlign: 'center', margin: '20px 0' }}>
                <div style={{ position: 'absolute', zIndex: 2, width: '55%' }}>
                    {(relatedSearchTermEnable ?
                        (renderSection('Project', searchData.projectSearchDtoList)) : null)}
                </div>
            </div>
            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Row>
                    {/** 버튼들을 중앙과 오른쪽 두 경우에만 위치시키기 위해 만든 좌측의 더미 공간 */}
                    <Col span={6}>

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
            <Row>
                <Col span={12} style={{ textAlign: 'left', margin: "0 0" }}>
                    {/** 현재 경로가 localhost:3000/project이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
                    <Button type={location.pathname === '/portfoliocard' ? 'primary' : 'default'} onClick={handlePortfolioCardPage}>
                        Portfolios
                    </Button>
                    <Button type={location.pathname === '/project' ? 'primary' : 'default'} onClick={handleProjectPage}>
                        Project
                    </Button>
                    <Button type={location.pathname === '/study' ? 'primary' : 'default'} onClick={handleStudyPage}>
                        Study
                    </Button>
                </Col>
                <Col span={12} style={{ textAlign: 'right', margin: "0 0" }}>
                    <Dropdown overlay={sortMenu} placement="bottomRight">
                        <Button>
                            정렬
                        </Button>
                    </Dropdown>
                </Col>
            </Row>
            <hr />

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
