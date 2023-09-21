// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Card, Row, Col, Divider, Button, Pagination } from 'antd';
import { request } from '../../../hoc/request';
import { lastVisitedEndpoint } from '../../../_actions/actions';
import { setLastVisitedEndpoint } from '../../../hoc/request';
import SearchInPortfolioCardPage from './SearchInPortfolioCardPage';


{/* postController - getFilteredPosts 쿼리 참고하기 */}


function PortfolioCardPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const dispatch = useDispatch();

    
    const [data, setData] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [recommend, setRecommend] = useState(0);
    const [selectedBanners, setSelectedBanners] = useState(['all']); // 처음 해당 페이지가 setting될 떄는 선택된 배너가 '전체'가 되도록 함
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [reload, setReload] = useState(0);



    const page = 0;
    const pageSize = 9;

    // USE EFFECT ###############################################


    useEffect(() => {
        setCurrentPage(0);
        setTotalPages(0);
        setSearchTerm("");
        setSelectedBanners(['all']);
        
        fetchUsers();
        setReload(0);
    }, [reload]);

    useEffect(()=>{
        Recommend();
    }, [recommend])

    useEffect(() => {
        console.log('현재 선택된 배너 정보', selectedBanners);
        console.log('현재 검색된 키워드: ', searchTerm);
        fetchUsers();
    }, [selectedBanners, currentPage, searchTerm]);

    

    // REQUEST ###############################################


    const Recommend = async() =>{
        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                selectedBanners: selectedBanners.join(','), // selectedBanners 배열을 쉼표로 구분된 문자열로 변환
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                searchTerm: searchTerm // 검색어 키워드 문자열
            });

            const response = await request('GET', `/recommend?${queryParams}`);
            setData(response.data); 
            setTotalPages(response.data.totalPages);
            console.log(data);
        } catch (error) {
            console.error("레코멘드 노노", error);
        }

    }
    const fetchUsers = async () => {

        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                selectedBanners: selectedBanners.join(','), // selectedBanners 배열을 쉼표로 구분된 문자열로 변환
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                searchTerm: searchTerm // 검색어 키워드 문자열
            });

            const response = await request('GET', `/getCards?${queryParams}`);
            setData(response.data.content); 
            setTotalPages(response.data.totalPages);
            setRecommend(0);
        } catch (error) {
            console.error("Error fetching data:", error);
        }
    };

    


    // HANDLER FUNCTIONS ###############################################

    // function name : onClickHandler
    const onClickHandler = (nickName) => {
        // /portfolio/${nickName}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /portfoliocard로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /portfoliocard로 설정
        dispatch(lastVisitedEndpoint('/portfoliocard'));  
        setLastVisitedEndpoint('/portfoliocard');
        // Error name : Actions must be plain objects. Instead, the actual type was: 'undefined'.
        // Solution : SetLastVisitedEndpoint is not a typical Redux action creator, cannot be stated in dispatch().
        navigate(`/portfolio/${nickName}`);
    }


    // function name : handleSearch
    // for Searching component
    const handleSearch = (value) => {
        setSearchTerm(value); // 검색어를 세팅
    };

    const onGetRecommend = async() => {
        
        setRecommend("please");

    };


    // function name ; handleProjectPage
    // <Button> Project의 핸들러, ProjectPage로 이동
    const handleProjectPage = () => {
        navigate('/project'); 
    };

    const handleReload = () => {
        setReload(1);
    };


    // function name ; handleStudyPage
    // <Button> Study의 핸들러, StudyPage로 이동
    const handleStudyPage = () => {
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


    const onRecommend = () =>{
        setRecommend(1);
    };


    // COMPONENTS ###############################################

    // renderCards
    const renderCards = (cards) => {
        if (!cards || cards.length === 0) {
            return <div>No data available</div>; // or any other appropriate message
        }
        
        return (
            <div>
                <Row gutter={16}>
                    {cards.map((item,index) => (

                        <Col xs={24} sm={8} key={index}>
                            <Card onClick={()=> onClickHandler(item.nickName)} title={`👩🏻‍💻 ${item.nickName}`} style={{ height:'270px', marginTop: '20px', cursor: 'pointer' }}>
                                {/* style = {{cursor: 'pointer'}} */ }
                                <b>Field Of Interests</b>
                                <br></br>
                                {item.web ? "Web " : ""}{item.app ? "App " : ""}{item.game ? "Game " : ""}{item.ai ? "AI " : ""}
                                <Divider></Divider>
                                <b>Brief Introduction</b>
                                <br></br>
                                {item.shortIntroduce}
                            </Card>
                        </Col>
                    ))}
                </Row>
            </div>
        )
    }



    // RETURN ####################################################################################
    // RETURN ####################################################################################
    return (
        <div>
            <div>
                <SearchInPortfolioCardPage setSearchTerm={handleSearch} /> 
            </div>
            <div>
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
            </div>
            <div style={{ textAlign: 'left', margin: "0 0", marginTop:'15px'}}>
                {/** 현재 경로가 localhost:3000/project이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
                <Button type={location.pathname === '/portfoliocard' ? 'primary' : 'default'} onClick={handleReload} >
                    Protfolio Card
                </Button>
                <Button type={location.pathname === '/project' ? 'primary' : 'default'} onClick={handleProjectPage} >
                    Project
                </Button>
                <Button type={location.pathname === '/study' ? 'primary' : 'default'} onClick={handleStudyPage}>
                    Study
                </Button>
                <Button onClick={onRecommend} >
                    RECOMMEND
                </Button>
                <Divider></Divider>
            </div>
            <div>
            {renderCards(data)}
            </div>
            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Pagination
                    current={currentPage + 1} // Ant Design's Pagination starts from 1, while your state starts from 0
                    total={totalPages * pageSize}
                    pageSize={pageSize}
                    onChange={(page) => setCurrentPage(page - 1)} //사용자가 해당 버튼 (예: 2번 버튼)을 누르면 currentPage를 1로 세팅하여 백엔드에 요청 보냄(백엔드는 프런트에서 보는 페이지보다 하나 적은 수부터 페이징을 시작하므로)
                />
            </div>
        </div>
    );
}


export default PortfolioCardPage;


