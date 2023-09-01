import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Divider, Row, Col, Button, Card, Pagination } from 'antd';
import { request } from '../../../hoc/request';
import './GroupPage.css';

function GroupPage() {
    const [data, setData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 게시물 목록들 세팅
    const [postsOption, setPostsOption] = useState("writer"); // 내가 쓴 글이면 postsOption === writer / 내가 지원한 글이면 postsOption === applicant
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [sortOption, setSortOption] = useState('latestPosts'); //최신등록순: latestPosts / 모집마감순: nearDeadline
    const pageSize = 3; // 현재 게시물 수가 적으므로 페이징을 3개 단위로 하였음
    const navigate = useNavigate();

    // 페이지가 새로 마운트 될 때마다 실행됨. 
    // 내가 보고있는 게시물이 내가 쓴 글인지(postsOption === writer) 또는 내가 지원한 글인지(postsOption === applicant)
    // 현재 사용자가 하이라이트한 페이지 번호 상태, 
    // 최신일순/마감일순에 대한 정렬 옵션,
    // 검색어 키워드 문자열
    // 를 기반으로 백엔드에 동적쿼리 보냄
    useEffect(() => {
        fetchFilteredPosts();
    }, [postsOption, currentPage, sortOption]);

    // 실제 백엔드에 동적 쿼리 보내는 곳
    const fetchFilteredPosts = async () => {

        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                postsOption: postsOption,
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                sortOption: sortOption, // 최신 등록순, 모집일자 마감순
            });

            //현재 사용자가 선택한 페이지와 배너 정보를 queryParams에 넣어서 백엔드에 요청
            const response = await request('GET', `/getGroupPosts?${queryParams}`);

            console.log("response.data : " + response.data);
            console.log("postsOption : ", postsOption);
            setData(response.data.content); //백엔드에서 받은 게시물 목록을 data에 저장
            setTotalPages(response.data.totalPages); //백엔드에서 받은 전체 페이지 수 정보를 totalPages에 저장
        } catch (error) {
            console.error("Error fetching data:", error);
        }
    };

    // 페이징 된 각 게시물 목록 하나를 클릭하면 그에 해당하는 게시물의 디테일 페이지로 navigate함
    // 스터디 네비게이트할 때에는 다르게 설정!
    const handleRowClick = (projectId) => {
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

    
    const handleNickNameClick = (nickName) => {
        // 해당 사용자 포트폴리오 페이지로 이동 (PortfolioPage.js와 연관)
        navigate(`/portfolio/${nickName}`);
    }


    const renderPosts = (posts) => {
        return (
            <div>
                {posts.map((item, index) => (
                    <Card key={index} style={{ margin: '0 0 10px 0' }}> {/*margin bottom속성을 사용 - 각 페이지로 navigate하는 버튼이 card랑 딱 붙여서 보이기 위해 card끼리는 margin bottom으로 간격 띄우고, 첫번째 카드 margin top을 0으로 해서 딱 붙여서 보이게 했음 */}

                        {/**아래의 속성들을 antd Card 컴포넌트로 묶음*/}
                        {/** 이상하게, antd에서 끌어온 애들은 style = {{}}로 적용이 안되고 css로 적용될 때가 있음 */}
                        <Divider className="bold-divider" />
                            <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                                {/** 수직선 CSS인 vertical-line을 만들어 주었음 */}
                                {/** JS 최신 버전에서 css는 import 안해도 전역에 적용되는듯..?? vertical-line의 이름은 똑같고, 내용을 바꿨더니, 둘 다 적용되는 버그가 발생해서 이런 의심이 생겼음. */}
                                <Col span={12} className="vertical-line2" onClick={() => handleRowClick(item.id)} style={{ cursor: 'pointer' }}>
                                    <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
                                        <strong style={{ fontSize: '18px' }}>{item.title}</strong>
                                    </div>
                                    {/** Boolean으로 반환되는 애들은 삼항연산자를 통해 값을 보여줘야 함 */}
                                    <div style={{ marginLeft: '3px' }}>
                                        게시판 이름: {item.postType} &nbsp;/&nbsp; 분류: {item.web ? "Web " : ""}{item.app ? "App " : ""}{item.game ? "Game " : ""}{item.ai ? "AI " : ""}
                                    </div>
                                </Col>
                                <Col span={6} className="vertical-line2"  onClick={() => handleRowClick(item.id)} style={{ cursor: 'pointer' }}>
                                    <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
                                        인원: {item.counts} / {item.recruitmentCount}
                                    </div>
                                    <div style={{ marginLeft: '3px' }}>
                                        모집 마감일: {formatDate(item.endDate)}
                                    </div>
                                </Col>
                                <Col span={6}>
                                    <div style={{ borderRight: '1px' }}>
                                        {postsOption === 'writer' ? (
                                            // writer에게는 지원자 목록을 보여주어야 한다.
                                            <div>
                                                <div>
                                                    지원자
                                                </div>
                                                {/** item.applyNickNames가 비어있는 경우 (null인 경우)를 반드시 처리해주기!! */}
                                                {item.applyNickNames ? (
                                                    <div>
                                                    {item.applyNickNames.map((nickName, index) => (
                                                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                                            <div key={index} onClick={() => handleNickNameClick(nickName)} style={{ cursor: 'pointer' }}>
                                                                {nickName}
                                                            </div>
                                                            <div>
                                                                <Button size="small" onClick={() => handleNickNameClick(nickName)} style={{ marginRight: '5px' }}>
                                                                    포트폴리오
                                                                </Button>
                                                                <Button size="small">
                                                                    승인
                                                                </Button>
                                                            </div>
                                                        </div>
                                                    ))}
                                                    </div>
                                                ) : (
                                                    <div>

                                                    </div>
                                                )}
                                            </div>
                                        ) : (
                                            // applicant에게는 게시물 작성자를 보여주어야 한다.
                                            <div>
                                                <div>
                                                    작성자
                                                </div>
                                                <div onClick={() => handleNickNameClick(item.writerNickName)} style={{ cursor: 'pointer' }}>
                                                    {item.writerNickName}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                </Col>
                            </Row>
                        <Divider className="bold-divider" />
                    </Card>

                ))}
            </div>
        );
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
                            type={postsOption === "writer" ? 'primary' : 'default'}
                            onClick={() => handlePostsOptionChange("writer")}
                            style={{ marginRight: '10px' }}
                        >
                            내가 작성한 게시물
                        </Button>
                        <Button
                            type={postsOption === 'applicant' ? 'primary' : 'default'}
                            onClick={() => handlePostsOptionChange("applicant")}
                        >
                            내가 지원한 게시물
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
                    total={totalPages * pageSize}
                    pageSize={pageSize}
                    onChange={(page) => setCurrentPage(page - 1)} //사용자가 해당 버튼 (예: 2번 버튼)을 누르면 currentPage를 1로 세팅하여 백엔드에 요청 보냄(백엔드는 프런트에서 보는 페이지보다 하나 적은 수부터 페이징을 시작하므로)
                />
            </div>
        </div>
    );
}

export default GroupPage;