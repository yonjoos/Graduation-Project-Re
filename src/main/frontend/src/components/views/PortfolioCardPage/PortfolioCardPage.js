// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { Card, Row, Col, Divider, Button } from 'antd';
import { request } from '../../../hoc/request';
import { useLocation, useNavigate } from 'react-router-dom';
import SearchInPortfolioCardPage from './SearchInPortfolioCardPage';


{/* postController - getFilteredPosts 쿼리 참고하기 */}


function PortfolioCardPage() {
    const [data, setData] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [isClicked, setIsClicked] = useState("unclicked");
    const location = useLocation
    const navigate = useNavigate();
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

