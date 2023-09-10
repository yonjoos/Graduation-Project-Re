// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Card, Row, Col, Divider, Button } from 'antd';
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
    const [isClicked, setIsClicked] = useState("unclicked");

    const page = 0;
    const size = 3;

    // USE EFFECT ###############################################

    

    useEffect(() => {

        if(isClicked == "clicked"){
            console.log('현재 검색된 키워드: ', searchTerm);
            fetchUsers();
        }
    }, [searchTerm]);

    useEffect(() => {
        fetchCards();
    }, []); 


    // REQUEST ###############################################

    const fetchCards = async() => {

        try{

            const response = await request('GET', `/getPortfolioCards` );
            setData(response.data);

        }catch(error){

        }
    }


    const fetchUsers = async () => {

        try {
            const queryParams = new URLSearchParams({ 
                searchTerm: searchTerm ,
                size : size ,
                page : page

            });

            const response = await request('GET', `/getCards?${queryParams}`);
            setData(response.data.content); 

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
        navigate(`/portfolio/${nickName}`);
    }


    // function name : handleSearch
    // for Searching component
    const handleSearch = (value) => {

        setIsClicked("clicked");
        setSearchTerm(value); // 검색어를 세팅
        
    };
    


    // COMPONENTS ###############################################

    // renderCards
    const renderCards = (cards) => {
        
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
            <div style={{ textAlign: 'left', margin: "0 0", marginTop:'15px'}}>
                {/** 현재 경로가 localhost:3000/project이면 primary형식으로 버튼 표시, 다른 경로라면 default로 표시 */}
                <Button type={location.pathname === '/project' ? 'primary' : 'default'} >
                    TBD 
                </Button>
                <Button type={location.pathname === '/study' ? 'primary' : 'default'} >
                    To be
                </Button>
                <Button type={location.pathname === '/portfolioCard' ? 'primary' : 'default'} >
                    Decided
                </Button>
                <Divider></Divider>
            </div>
            <div>
            {renderCards(data)}
            </div>
        </div>
    );
}


export default PortfolioCardPage;

