import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
//import { useDispatch } from 'react-redux';
import { Divider, Row, Col, Button, Card, Pagination, message } from 'antd';
import { request } from '../../../hoc/request';
//import { lastVisitedEndpoint } from '../../../_actions/actions';
//import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../hoc/request';
import './ScrapPage.css';

function ScrapPage() {
    const navigate = useNavigate();
    //const dispatch = useDispatch();

    const [data, setData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 게시물 목록들 세팅
    const [postsOption, setPostsOption] = useState("project"); // 프로젝트 게시물이면 postsOption === project / 스터디 게시물이면 postsOption === study
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [sortOption, setSortOption] = useState('latestPosts'); //최신등록순: latestPosts / 모집마감순: nearDeadline
    const pageSize = 3; // 현재 게시물 수가 적으므로 페이징을 3개 단위로 하였음


    // 페이지가 새로 마운트 될 때마다 실행됨. 
    // 프로젝트 게시물이면 postsOption === project / 스터디 게시물이면 postsOption === study
    // 현재 사용자가 하이라이트한 페이지 번호 상태, 
    // 최신일순/마감일순에 대한 정렬 옵션,
    // 를 기반으로 백엔드에 동적쿼리 보냄
    useEffect(() => {
        fetchFilteredPosts();
    }, [postsOption, currentPage, sortOption]);

    // 실제 백엔드에 동적 쿼리 보내는 곳
    const fetchFilteredPosts = async () => {

        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                postsOption: postsOption,   // 프로젝트 게시물이면 postsOption === project / 스터디 게시물이면 postsOption === study
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                sortOption: sortOption, // 최신 등록순, 모집일자 마감순
            });

            //현재 사용자가 선택한 페이지와 배너 정보를 queryParams에 넣어서 백엔드에 요청
            const response = await request('GET', `/getScrapPosts?${queryParams}`);

            setData(response.data.content); //백엔드에서 받은 게시물 목록을 data에 저장
            setTotalPages(response.data.totalPages); //백엔드에서 받은 전체 페이지 수 정보를 totalPages에 저장
        } catch (error) {
            console.error("Error fetching data:", error);
        }
    };

    // 페이징 된 각 게시물 목록 하나를 클릭하면 그에 해당하는 게시물의 디테일 페이지로 navigate함
    const handleRowClick = (postsId, postType) => {
        // /project/detail/${postsId} 또는 /study/detail/${postsId}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /scrap으로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /scrap으로 설정
        // dispatch(lastVisitedEndpoint('/scrap', '/scrap', '/scrap'));
        // setLastVisitedEndpoint('/scrap');
        // setLastLastVisitedEndpoint('/scrap');
        // setLastLastLastVisitedEndpoint('/scrap');

        if (postType === "PROJECT") {
            navigate(`/project/detail/${postsId}`);
        }
        else {
            navigate(`/study/detail/${postsId}`);
        }
    }

    // 게시물 목록에서 닉네임 필드를 클릭하면, 해당 닉네임을 가진 회원의 포트폴리오 창으로 navigate
    const handleNicknameClick = (nickName) => {
        navigate(`/portfolio/${nickName}`);
    }

    // 2023826 -> 2023년 8월 26일 형식으로 변환
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // Month is zero-based
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    };

    // 최신등록순, 마감일 순 버튼이 눌러지면 현재 선택된 버튼으로 세팅하고, 페이지는 0번으로 간다
    const handleSortOptionChange = (option) => {
        setSortOption(option);
        setCurrentPage(0);
    };

    // 작성한 글 또는 지원한 글 버튼이 눌러지면 현재 선택된 버튼으로 세팅하고, 페이지는 0번으로 간다
    const handlePostsOptionChange = (option) => {
        setPostsOption(option);
        setCurrentPage(0);
    };

    // 지원자 또는 글쓴이 닉네임 클릭 핸들러
    const handleNickNameClick = (nickName) => {
        // dispatch(lastVisitedEndpoint('/scrap', '/scrap', '/scrap'));
        // setLastVisitedEndpoint('/scrap');
        // setLastLastVisitedEndpoint('/scrap');
        // setLastLastLastVisitedEndpoint('/scrap');
        // 해당 사용자 포트폴리오 페이지로 이동 (PortfolioPage.js와 연관)
        navigate(`/portfolio/${nickName}`);
    }

    // const renderPosts = (posts) => {
    //     return (
    //        <div>
    //             {posts.map((item, index) => (
    //                 <Card key={index} style={{ margin: '0 0 10px 0' }}>
    //                     <Divider className="bold-divider" />
    //                     <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
    //                         <Col span={12} className="vertical-line2" onClick={() => handleRowClick(item.id, item.postType)} style={{ cursor: 'pointer' }}>
    //                             <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
    //                                 <strong style={{ fontSize: '18px' }}>{item.title}</strong>
    //                             </div>
    //                             <div style={{ marginLeft: '3px' }}>
    //                                 분류: {item.web ? "Web " : ""}{item.app ? "App " : ""}{item.game ? "Game " : ""}{item.ai ? "AI " : ""}
    //                             </div>
    //                         </Col>
    //                         <Col span={6} className="vertical-line2" onClick={() => handleRowClick(item.id, item.postType)} style={{ cursor: 'pointer' }}>
    //                             <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
    //                                 인원: {item.counts} / {item.recruitmentCount}
    //                             </div>
    //                             <div style={{ marginLeft: '3px' }}>
    //                                 모집 마감일: {formatDate(item.endDate)}
    //                             </div>
    //                         </Col>
    //                         <Col span={6}>
    //                             <div style={{ display: 'flex', justifyContent: 'space-between', borderRight: '1px' }}>
    //                                 <div>
    //                                     <div>
    //                                         작성자
    //                                     </div>
    //                                     <div onClick={() => handleNickNameClick(item.nickName)} style={{ cursor: 'pointer' }}>
    //                                         {item.nickName}
    //                                         <Button size="small" onClick={() => handleNickNameClick(item.nickName)} style={{ marginLeft: '5px' }}>
    //                                             포트폴리오
    //                                         </Button>
    //                                     </div>
    //                                 </div>
    //                                 {/** alignItems로 상하의 가운데에 놓기 */}
    //                                 <div style={{ display: 'flex', alignItems: 'center' }}>
    //                                     <div>
    //                                         {item.isApplied ? (
    //                                             // 게시물에 지원했을 때 보여줄 내용
    //                                             item.isApproved ? (
    //                                                 // 승인이 완료되었다면
    //                                                 "승인 완료"
    //                                             ) : (
    //                                                 // 승인이 완료되지 않았다면
    //                                                 item.counts === item.recruitmentCount ? (
    //                                                     // 정원이 다 찼다면, 모집 마감을 보여줌
    //                                                     "모집 마감"
    //                                                 ) : (
    //                                                     // 정원이 다 안찼다면, 승인 대기 중을 보여줌
    //                                                     "승인 대기 중"
    //                                                 )
    //                                             )
    //                                         ) : (
    //                                             // 게시물에 지원하지 않았을 때 보여줄 내용
    //                                             item.counts === item.recruitmentCount ? (
    //                                                 // 정원이 다 찼다면, 모집 마감을 보여줌
    //                                                 "모집 마감"
    //                                             ) : (
    //                                                 // 정원이 다 안찼다면, 승인 대기 중을 보여줌
    //                                                 "미지원"
    //                                             )
    //                                         )}
    //                                     </div>
    //                                 </div>
    //                             </div>
    //                         </Col>
    //                     </Row>
    //                     <Divider className="bold-divider" />
    //                 </Card>
    //             ))}
    //         </div>
    //     );
    // }

    // 너무 긴 제목이나 닉네임이면 적당한 길이로 자르고, ... 붙이기
    const truncateString = (str, maxLength) => {
        if (str.length > maxLength) {
            return str.slice(0, maxLength) + '...';
        }
        return str;
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

    // const categoryTagStyle = {
    //     display: 'inline-block',
    //     padding: '0px 5px 0px 5px',
    //     backgroundColor: '#ff9900', /* 원하는 색상으로 변경 */
    //     borderRadius: '50px', /* 타원형 모양을 만들기 위해 사용 */
    //     marginLeft: '5px', /* 태그 사이 간격 조절을 위해 사용 */
    //     color: '#677779', /* 텍스트 색상 설정 */
    //     marginLeft: '-0.3%',
    //     marginRight: '0.6%'
    // };

    // const renderPosts = (posts) => {
    //     return (
    //        <div gutter={[16, 16]} style={{ marginTop: '0px', padding: '0px', width:'100%' }} justify="space-between">
    //             <Card title={`SCRAPS`}>
    //             {posts.map((item, index) => (
    //                 <div key={index} style={{ display: 'grid', cursor: 'pointer' }} onClick={() => handleRowClick(item.id, item.postType)}>
    //                     <div style={{marginLeft:'10px' }}>
    //                         {item.postType}
    //                     </div>
    //                     <div style={{ display: 'flex', marginTop: '0px' }}>
    //                         <div style={{ width: '80%', display: 'grid', marginLeft:'10px' }}>  
    //                             <div>
    //                                 <strong style={{ fontSize: '18px' }}>{truncateString(item.title, 40)}</strong>
    //                             </div>
    //                             <strong style={{ display: 'inline-block' }}>
    //                                 {item.web && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#WEB</span>}
    //                                 {item.app && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#APP</span>}
    //                                 {item.game && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#GAME</span>}
    //                                 {item.ai && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#AI</span>}
    //                             </strong>
    //                         </div>
    //                         <div style={{ display: 'grid', marginLeft: '0px', width: '200px', alignItems:'center' }}>
    //                             <div>
    //                             인원: {item.counts} / {item.recruitmentCount} <br></br>마감: {formatDate(item.endDate)}
    //                             <br/><div style={{color: 'gray', fontSize: 'small'}}>{formatDateTime(item.finalUpdatedTime)}</div>
    //                             </div>
    //                         </div>
    //                     </div>
    //                     <Divider />
    //                 </div>
    //             ))}  
    //             </Card>
    //         </div>
    //     );
    // }

    const categoryTagStyle = {
        display: 'flex',
        padding: '0px 5px 0px 5px',
        borderRadius: '50px', /* 타원형 모양을 만들기 위해 사용 */
        marginLeft: '-0.3%',
        marginRight: '5px',
        color: '#677779',
    };

    const renderPosts = (posts) => {
        return (
            <div gutter={[16, 16]} style={{ marginTop: '10px', padding: '1px', width: '100%', cursor: 'pointer' }} justify="space-between" >
                <Card title={`SCRAPS`} headStyle={{ background: '#DDEEFF' }}>
                    {posts.map((item, index) => (
                        <div key={index} style={{ display: 'grid' }} onClick={(e) => {
                            if (e.target.tagName.toLowerCase() === 'strong' && e.target.classList.contains('nickname')) { //strong 태그 중 nickname이라는 클래스네임이 달려 있는 태그의 경우에만 포트폴리오로 navigate
                                handleNicknameClick(item.nickName);
                            } else {
                                handleRowClick(item.id, item.postType);
                            }
                        }}>
                            <div style={{ display: 'flex', marginTop: '0px' }}>
                                <div style={{ width: '80%', display: 'grid', marginLeft: '10px' }}>
                                    <strong style={{ display: 'flex' }}>
                                        {item.web && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#WEB</span>}
                                        {item.app && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#APP</span>}
                                        {item.game && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#GAME</span>}
                                        {item.ai && <span style={{ ...categoryTagStyle, backgroundColor: '#91e2c3' }}>#AI</span>}
                                    </strong>
                                    <div style={{ display: 'flex' }}>
                                        <strong style={{ fontSize: '18px' }}>{truncateString(item.title, 40)}</strong>
                                    </div>
                                    <div style={{display:'flex'}}>
                                        <div>
                                            <img
                                                    style={{ borderRadius: '50%', width: '40px', height: '40px', border: '3px solid lightblue', marginRight:'10px' }}
                                                    src={`https://storage.googleapis.com/hongik-pickme-bucket/${item.imageUrl}`}
                                                    />
                                        </div>
                                        <div>
                                            <strong className="nickname">{item.nickName}</strong>
                                        </div>
                                    </div>
                                    <div style={{ marginTop: '10px', marginRight: '20px', textAlign: 'left' }}>
                                        {truncateString(item.briefContent, 50)}
                                    </div>
                                </div>
                                <div style={{ display: 'grid', marginLeft: '0px', width: '200px', alignItems: 'center' }}>
                                    <div>
                                        인원: {item.counts} / {item.recruitmentCount} <br></br>마감: {formatDate(item.endDate)} <br></br> 👀 조회 수: {item.viewCount}
                                        <br /><br /><div style={{ color: 'gray', fontSize: 'small' }}>{formatDateTime(item.finalUpdatedTime)}</div>
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
        <div>
            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Row>
                    {/** 버튼들을 중앙과 오른쪽 두 경우에만 위치시키기 위해 만든 좌측의 더미 공간 */}
                    <Col span={12} style={{ textAlign: 'left' }}>

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
                    <Col span={12} style={{ textAlign: 'right' }}>

                        {/** Sort buttons - 최신등록순, 마감일자 순 버튼 */}
                        <Button
                            type={postsOption === "project" ? 'primary' : 'default'}
                            onClick={() => handlePostsOptionChange("project")}
                            style={{ marginRight: '10px' }}
                        >
                            프로젝트
                        </Button>
                        <Button
                            type={postsOption === "study" ? 'primary' : 'default'}
                            onClick={() => handlePostsOptionChange("study")}
                        >
                            스터디
                        </Button>

                    </Col>
                </Row>
            </div>

            {renderPosts(data)}

            {/* antd페이지네이션 적용 */}
            {/* 동적으로 쿼리 날렸을 때 페이지 하단에 보이는 페이지 버튼도 동적으로 구성해야 함 -> 백엔드에서 받아온 totalPages를 기반으로 페이지 버튼 수를 만들어 넣어줌 */}
            {/*백엔드에서는 페이징을 0부터 시작하지만, 프론트에서는 페이지 버튼을 1부터 세팅해줘야하므로 이를 위한 코드*/}
            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Pagination
                    current={currentPage + 1} // Ant Design's Pagination starts from 1, while your state starts from 0
                    total={totalPages * pageSize}   // 내용물의 총 개수 = 페이지 수 * 페이지 당 몇 개씩
                    pageSize={pageSize}             // 한 페이지에 몇 개씩 보여줄 것인가?
                    onChange={(page) => setCurrentPage(page - 1)} //사용자가 해당 버튼 (예: 2번 버튼)을 누르면 currentPage를 1로 세팅하여 백엔드에 요청 보냄(백엔드는 프런트에서 보는 페이지보다 하나 적은 수부터 페이징을 시작하므로)
                    showSizeChanger={false}
                />
            </div>
        </div>
    );
}

export default ScrapPage;