import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { Row, Col, Button, Card, Pagination } from 'antd';
import { request } from '../../../hoc/request';
import SearchInLandingPage from '../LandingPage/SearchInLandingPage';
function SearchPortfolioListPage(onSearch) {

    const navigate = useNavigate();
    const dispatch = useDispatch();
    const location = useLocation(); //현재 내가 들어와있는 경로를 확인하기 위한 함수
    const [currentPage, setCurrentPage] = useState(0); //  페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [portfolioData, setPortfolioData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 포트폴리오 카드 목록들 세팅
    const pageSize = 9;

    // 백엔드에서 받은 검색어 기반 결과 리스트(3개)를 정의. 처음에 이 페이지에 들어오면 빈 배열
    const [data, setData] = useState({
        projectSearchDtoList: [], // 프로젝트 제목 관련 최대 5개 가져옴
        studySearchDtoList: [], // 스터디 제목 관련 최대 5개 가져옴
        userSearchDtoList: [], // 유저 이름 관련 최대 5개 가져옴
    });

    // const { searchTerm } = useParams();    // URL에 있는 parameter 추출
    const [searchTerm, setSearchTerm] = useState(useParams());
    const currentSearchTerm = useParams(); // 이건 한번 유효한 검색이 완료된 후에는 일시적으로 고정된 값 

    // 키워드를 치는 순간 순간마다 백엔드에서 데이터 받아옴
    useEffect(() => {
        console.log('현재 검색된 키워드: ', searchTerm);
        fetchFilteredSearchLists();
    }, [searchTerm]);

    useEffect(() => {
        console.log('현재 쿼리 스트링 키워드: ', currentSearchTerm);
        fetchSearchResultLists();
    },[currentSearchTerm, currentPage]);


    // 백엔드에 입력된 검색어 기반으로, match되는 검색 결과물 가져오기
    const fetchSearchResultLists = async () => {
        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 9개씩만 나오도록 구성했음)
                searchTerm: currentSearchTerm // 검색어 키워드 문자열
            });

            const response = await request('GET', `/getPortfolioSearchResult?${queryParams}`);
            setPortfolioData(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Error fetching data:", error);
        }

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
        <Row gutter={[16, 16]}>
            <Col span={24}>
                <SearchInLandingPage onSearch={handleSearch} initialSearchTerm={searchTerm.searchTerm} />

            </Col>
            <Col span={24}>
                {renderSection('User', data.userSearchDtoList)}
                {renderSection('Project', data.projectSearchDtoList)}
                {renderSection('Study', data.studySearchDtoList)}
            </Col>

            <div style={{ textAlign: 'left', margin: "0 0", width: '100%' }}>
                {/** 현재 경로가 localhost:3000/project이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
                <Button type={location.pathname.includes('/search/portfoliocard') ? 'primary' : 'default'} onClick={handleSearchPortfolioCard}>
                    Portfolio Card
                </Button>
                <Button type={location.pathname.includes('/search/project')  ? 'primary' : 'default'} onClick={handleSearchProject}>
                    Project
                </Button>
                <Button type={location.pathname.includes('/search/study')  ? 'primary' : 'default'} onClick={handleSearchStudy}>
                    Study
                </Button>
                <hr style={{ width: '100%' }} />
                

            </div>

            

            {/* <Col span={24} style={{ textAlign: 'center' }}>

                {searchTerm}
            </Col> */}
        </Row>



    );

}

export default SearchPortfolioListPage;