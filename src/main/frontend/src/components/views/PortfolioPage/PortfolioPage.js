import { useNavigate, useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
//import { useSelector } from "react-redux";
import { Button, Card, Row, Col, Radio, Progress, Divider, Modal, Image } from 'antd';

//import { lastVisitedEndpoint } from '../../../_actions/actions';
import { request } from '../../../hoc/request';

function PortfolioPage() {
    const navigate = useNavigate();
    const { nickName } = useParams();
    //const visitedEndpoint = useSelector(state => state.endpoint.lastVisitedEndpoint);
    //const visitedEndEndEndpoint = useSelector(state => state.endpoint.lastLastLastVisitedEndpoint);

    const [postData, setPostData] = useState([]);
    const [loadPosts, setloadPosts] = useState("more");
    const [profileImage, setProfileImage] = useState(null); //프사 띄우는 용도


    const [data, setData] = useState({});
    const [hasPortfolio, setHasPortfolio] = useState('');
    const [existingPreferences, setExistingPreferences] = useState({
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });


    /*
    UseEffect #############################################################################################################
    UseEffect #############################################################################################################
    */

    // PortfolioPage에 들어오면, Get방식으로 백엔드에서 데이터를 가져와서 data에 세팅한다.
    useEffect(() => {
        request('GET', `/getUserPortfolio?nickName=${nickName}`, {})
            .then((response) => {
                setData(response.data);
                setHasPortfolio(response.data.isCreated);
                setExistingPreferences({
                    web: response.data.web,
                    app: response.data.app,
                    game: response.data.game,
                    ai: response.data.ai
                });
            })
            .catch((error) => {
                console.error("Error fetching data:", error);
            });

        
    }, [nickName]);

    useEffect(()=>{


        request('GET', `/getOtherUsersProfileImage?nickName=${nickName}`)
            .then((response) => {
                console.log(response.data.imageUrl);
                setProfileImage(response.data.imageUrl);
            })
            .catch((error) => {
                console.error("Error fetching profile image:", error);
            });

    }, [profileImage])

    // 아무런 정보도 없는 유저의 포트폴리오에 접근 시 (존재하지 않는 유저의 포트폴리오에 접근 시) visitedEndpoint로 강제로 이동
    useEffect(() => {
        if (hasPortfolio === null) {

            navigate(-1);
        }
    }, [hasPortfolio]);


    /*
    COMPONENTS #############################################################################################################
    COMPONENTS #############################################################################################################
    */


    // Component
    // INPUT : PostsListsDTO
    // RETURN : Posts Lists <Card> components
    const renderPosts = (posts) => {

        if (loadPosts === "fold") {
            return (

                posts.map((post) => (
                    <Row justify="center" key={post.id}>
                        <Col span={16}>
                            <Card headStyle={{ background: '#f0fff0' }}
                                onClick={() => onClickPosts(post)}
                                style={{ height: '150px', cursor: 'pointer' }}
                                title={
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <div style={{ fontWeight: 'bold' }}>{post.title}</div>
                                        <div style={{ fontSize: '12px', color: 'gray' }}>{post.postType}</div>
                                    </div>
                                }>
                                <div>
                                    <strong style={{ display: 'flex' }}>

                                        {post.web ? <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#WEB</span> : ""}
                                        {post.app ? <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#APP</span> : ""}
                                        {post.game ? <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#GAME</span> : ""}
                                        {post.ai ? <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#AI</span> : ""}
                                    </strong>
                                </div>
                                <div style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '100%' }}>
                                    {post.briefContent}
                                </div>
                            </Card>
                        </Col>
                    </Row>
                )))
        }
        else {
            return (
                <div />
            )
        }


    };


    // Component
    // INPUT : fields of interests
    // RETURN : bar-graph to preferencies
    const renderPreferenceBar = (field) => {
        const preferenceValue = data && existingPreferences[field];
        return (
            <div style={{ marginBottom: '10px' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '5px' }}>
                    <div style={{ width: '100px', textAlign: 'left', marginRight: '10px' }}>{field}:</div>
                    <Progress percent={preferenceValue * 25} showInfo={false} strokeColor={getBarColor(field)} />
                </div>
            </div>
        );
    };

    // Component (for > Component-renderPreferenceBar)
    // INPUT : fields of interests
    // OUTPUT : 필드에 따른 색상코드
    const getBarColor = (field) => {
        if (field === "web") {
            return '#FE708F';
        } else if (field === "app") {
            return '#f9f56e';
        } else if (field === "game") {
            return '#83edff';
        } else {
            return '#91e2c3';
        }
    };


    // 백엔드에서 받아온 데이터에 공백이 없으면, maxCharacters번째 글자 이후에 공백을 넣어주는 함수
    // text: 덩어리로 나누어 줄 바꿈을 삽입하려는 입력 텍스트.
    // maxCharacters: 줄 바꿈을 삽입하기 전의 최대 문자 수.
    function insertLineBreaks(text, maxCharacters) {
        // 함수는 먼저 text 매개변수가 거짓인지(비어 있거나 정의되지 않음) 확인. text가 비어 있거나 정의되지 않은 경우 함수는 동일한 입력 텍스트를 반환함.
        if (!text) return text;

        // text가 비어 있지 않으면 함수는 chunks라는 빈 배열을 초기화함. 이 배열은 줄 바꿈을 사용하여 텍스트 덩어리를 저장하는 역할을 함.
        const chunks = [];
        // 띄어쓰기가 없는 한 개의 문자열의 인덱스
        let j = 0;

        for (let i = 0; i < text.length; i++) {
            // 공백을 만나면, 문자열의 길이를 세는 j를 0으로 초기화.
            if (text[i] === ' ') {
                j = 0;
            }

            chunks.push(text[i]);
            j++;

            // 띄어쓰기 없이 maxCharacters까지 왔다면, 강제로 띄어쓰기 삽입 후, j = 0으로 초기화.
            if (j === maxCharacters) {
                chunks.push(' ')
                j = 0;
            }
        }

        return chunks;
    }


    /*
    HANDLER #############################################################################################################
    HANDLER #############################################################################################################
    */


    // Handler
    // 목록으로 돌아가기 버튼 클릭
    // const handleGoBackClick = () => {
    //     if(visitedEndEndEndpoint === "/portfoliocard") {
    //         navigate(visitedEndEndEndpoint);
    //     }
    //     else if(visitedEndEndEndpoint === '/group') {
    //         navigate(visitedEndEndEndpoint);
    //     }
    //     else if(visitedEndEndEndpoint === '/scrap') {
    //         navigate(visitedEndEndEndpoint);
    //     }
    //     else if(visitedEndEndEndpoint === '/') {
    //         navigate(visitedEndEndEndpoint);
    //     }
    //     else if(visitedEndEndEndpoint.includes('search/portfoliocard/query')) {
    //         navigate(visitedEndEndEndpoint);
    //     }
    //     else {
    //         navigate(visitedEndpoint);
    //     }
    // };

    // Handler
    // OnClick : FETCH PostsListsDTO, switch 'loadPosts' status
    const onLoadPosts = () => {

        if (loadPosts === "more") {

            request('GET', `/getOtherUsersPosts?nickName=${nickName}`)
                .then((response) => {

                    setPostData(response.data);
                    setloadPosts("fold");

                })
                .catch((error) => {

                    console.error("Error fetching posts:", error);

                });
        }
        else if (loadPosts === "fold") {
            setloadPosts("more");
        }

    };

    // Handler
    // onClick : move to post's detail page
    const onClickPosts = (post) => {

        if (post.postType === "PROJECT") {
            navigate(`/project/detail/${post.id}`);
        }
        else {
            navigate(`/study/detail/${post.id}`);
        }

    }


    const categoryTagStyle = {
        display: 'flex',
        padding: '0px 5px 0px 5px',
        backgroundColor: '#ff9900', /* 원하는 색상으로 변경 */
        borderRadius: '50px', /* 타원형 모양을 만들기 위해 사용 */
        color: '#677779', /* 텍스트 색상 설정 */
        marginLeft: '-0.3%',
        marginRight: '5px'
    };


    /*
    RETURN #####################################################################################################################
    RETURN #####################################################################################################################
    */

    return (
        // 포트폴리오 업로드 후 F5를 누르지 않으면 데이터가 들어오지 않는 문제를 data 안에 들어있는 isCreated사용과 삼항 연산자를 통해 직접적으로 해결.
        <div style={{width:'100%'}}>
            <div style={{ marginLeft: '15%', marginRight: '15%' }}>
                {/** navigate(-1)을 통해, 바로 이전에 방문했던 페이지로 돌아갈 수 있음 */}
                {/* <Button type="primary" onClick={handleGoBackClick}>
                    목록으로 돌아가기
                </Button> */}

            </div>

            {/** 아직 포트폴리오를 만들지 않았다면? */}
            {data && !data.isCreated ? (
                <div style={{ marginLeft: '15%' }}>
                    <br />
                    <h2> {data.nickName} 님의 포트폴리오가 아직 작성되지 않았습니다.</h2>
                    <br />
                    <br />
                </div>
            ) : (
                <div>
                    <div style={{ display:'grid', marginLeft: '20%', marginRight: '20%', marginTop: '20px', marginBottom: '20px' }}>
                        <div style={{ display: 'flex' }}>
                            <div style={{ marginRight: '20px', borderRadius: '50%', overflow: 'hidden', width: '100px', height: '100px' }}>
                                <img
                                    style={{ borderRadius: '50%', width: '100%', height: '100%', marginBottom: '15px', border: '5px solid lightblue' }}
                                    src={`https://storage.googleapis.com/hongik-pickme-bucket/${profileImage}`}
                                />
                            </div>
                            <div style={{ width: '90%', marginTop: '30px', fontSize: '30px', display:'grid' }}>
                                <div>
                                    <i>{data && data.nickName}</i><b>'s portfolio page</b>
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <div style={{ fontSize: '12px', marginTop:'10px' }}>
                                            <strong>CONTACT : </strong>
                                            {data && data.email}
                                        </div>
                                    <div style={{ fontSize: '12px', marginTop:'10px' }}>
                                            <strong> 조회수 : </strong>
                                            {data && data.viewCount}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/**  borderBottom: '3px solid black'은 <hr> 요소 하단에 검은색 실선 테두리를 추가하여 더 두껍고 굵게 표시합니다. '3px' 값을 조정하여 원하는 대로 두껍거나 얇게 만들 수 있습니다. */}
                    <hr style={{ marginLeft: '15%', marginRight: '15%', borderBottom: '0.1px solid black' }} />


                    <Row justify="center">
                        <Col span={16}>
                            <div>

                                {data.fileUrl && data.fileUrl.length >= 1 ? (
                                    <Card size='small' title={`첨부파일`} bodyStyle={{ paddingTop: '0px', paddingBottom: '0px', paddingRight: '0px', paddingLeft: '0px' }} headStyle={{ background: '#ddeeff' }}>
                                        {
                                            data.fileUrl ? (
                                                data.fileUrl.map((file, index) => (
                                                    <div style={{ display: 'flex', justifyContent: 'left', width: '100%' }} key={index}>
                                                        <Button type='text' style={{ width: '100%', textAlign: 'left' }}
                                                            onClick={() => window.open(`https://storage.googleapis.com/hongik-pickme-bucket/${file.fileUrl}`, '_blank')} // 파일 열기 함수 호출
                                                        >
                                                            {file.fileName} {/* 파일 이름 표시 */}
                                                        </Button>
                                                    </div>
                                                ))
                                            ) : (
                                                null
                                            )}
                                    </Card>
                                ) : null}


                            </div>
                        </Col>
                    </Row>


                    <Row justify="center" style={{ marginTop: '20px' }}>
                        <Col span={16}>
                            <Row>
                                <Col span={14}>
                                    <Card title="ABOUT" style={{ height: '100%' }} headStyle={{ background: '#ddeeff' }}>
                                        {/* 
                                            == 변경사항 ==
                                            1) 라디오 카드, 한 줄 소개 카드 없애고
                                            2) 그 두 개를 하나의 카드 안에 넣음
                                        */}

                                        <h6>Nick Name</h6>
                                        {nickName}
                                        <br />
                                        <br />
                                        <h6>Brief Introduction</h6>
                                        {data && data.shortIntroduce ? (
                                            data.shortIntroduce
                                        ) : (
                                            <p>No introduction available</p>
                                        )}
                                    </Card>
                                </Col>
                                <Col span={10}>
                                    <Card title="관심 분야 선호도" style={{ height: '100%' }} headStyle={{ background: '#ddeeff' }}>
                                        {/* 
                                        == 변경사항 ==
                                        관심 분야 선호도 "그래프" -> 관심분야 선호도 그래프 
                                    */}
                                        {renderPreferenceBar('web')}
                                        {renderPreferenceBar('app')}
                                        {renderPreferenceBar('game')}
                                        {renderPreferenceBar('ai')}
                                    </Card>
                                </Col>
                            </Row>
                        </Col>
                    </Row>



                    {/**멀티라인 콘텐츠를 데이터베이스에 저장된 대로 프론트엔드에서 줄바꿈(새 줄 문자)을 포함하여 표시하려면
                 *  <pre> HTML 태그나 CSS 스타일을 사용하여 공백 및 줄바꿈 형식을 보존할 수 있다.
                 * 
                 * <Row justify="center">
                 *     <Col span={16}>
                 *         <Card title="한 줄 소개">
                 *             //<pre> 태그를 사용하여 형식과 줄바꿈을 보존합니다
                 *             <pre>{data && data.introduce}</pre>
                 *         </Card>
                 *     </Col>
                 * </Row>
                 *
                 * 
                 * 스타일링에 대한 더 많은 제어를 원하는 경우 CSS를 사용하여 동일한 효과를 얻을 수 있다.
                 * 즉, style={{ whiteSpace: 'pre-wrap' }} 을 사용한다.
                 *  */}
                    <Row justify="center">
                        <Col span={16}>
                            <Card title="경력" headStyle={{ background: '#ddeeff' }}>
                                <div style={{ whiteSpace: 'pre-wrap' }}>
                                    {/** 받아온 데이터에 공백이 없으면, 40번째 글자 이후에 강제로 공백을 넣어주는 함수 */}
                                    {/** Card안에 데이터를 넣는 경우 발생하는 문제인 것 같음. */}
                                    {data && insertLineBreaks(data.introduce, 45)}
                                </div>
                            </Card>
                        </Col>
                    </Row>

                    <Row justify="center">
                        <Col span={16}>
                            <div style={{ marginTop: '20px' }} >
                                {data.promoteImageUrl && data.promoteImageUrl.length >= 1 ? (
                                    <Card size='small' title={`홍보 사진`} bodyStyle={{ paddingTop: '0px', paddingBottom: '0px', paddingRight: '0px', paddingLeft: '0px' }} headStyle={{ background: '#ddeeff' }}>

                                        {data.promoteImageUrl ?
                                            (
                                                data.promoteImageUrl.map((imageUrl, index) => (
                                                    <div style={{ display: 'flex', justifyContent: 'center' }} key={index}>
                                                        <Image
                                                            key={index}
                                                            src={`https://storage.googleapis.com/hongik-pickme-bucket/${imageUrl}`}
                                                            alt={`홍보 사진 ${index + 1}`}
                                                            style={{ margin: '10px', width: 300 }}
                                                        />
                                                    </div>
                                                ))
                                            ) : (
                                                <p>이미지가 없습니다</p>
                                            )}
                                    </Card>) : null}

                            </div>
                        </Col>
                    </Row>
                    <br></br>

                    {/* >> Posts Lists << */}
                    <Row justify="center">
                        <Col span={16}>
                            <Card >
                                <Row justify="space-between">
                                    <Col span={8}>
                                        <div style={{ fontWeight: 'bold' }}>{data && data.nickName}님이 작성한 게시물</div>
                                    </Col>
                                    <Col span={8} style={{ textAlign: 'right' }}>
                                        <div onClick={onLoadPosts}>
                                            <strong style={{ cursor: 'pointer' }}>{loadPosts}</strong>
                                        </div>
                                    </Col>
                                </Row>
                            </Card>
                        </Col>
                    </Row>

                    {/* >> Posts << */}
                    {postData && postData.length > 0 ? (
                        renderPosts(postData)
                    ) : (
                        <div></div>

                    )}
                </div>
            )}
        </div>
    );
}

export default PortfolioPage;