import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Divider, Row, Col, Button, Card, Pagination, Modal, message } from 'antd';
import { request } from '../../../hoc/request';
import './GroupPage.css';

function GroupPage() {
    const navigate = useNavigate();

    const [data, setData] = useState([]); // 백엔드에서 동적 쿼리를 바탕으로 현재 페이지에서 보여질 게시물 목록들 세팅
    const [postsOption, setPostsOption] = useState("writer"); // 내가 쓴 글이면 postsOption === writer / 내가 지원한 글이면 postsOption === applicant
    const [currentPage, setCurrentPage] = useState(0); // Java 및 Spring Boot를 포함한 페이징은 일반적으로 0부터 시작하므로 처음 이 페이지가 세팅될 떄는 0페이지(사실상 1페이지)로 삼음
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 total 페이지 수 세팅을 위함
    const [sortOption, setSortOption] = useState('latestPosts'); //등록순: latestPosts / 모집마감순: nearDeadline
    const [isModalVisible, setIsModalVisible] = useState(false);    // 유저 승인 모달이 보이는지 여부 설정
    const [cancelModalVisible, setCancelModalVisible] = useState(false);    // 유저 승인 취소 모달이 보이는지 여부 설정
    const [nickName, setNickName] = useState(null);       // 승인할 때 필요한 유저 nickName을 저장
    const [postsId, setPostsId] = useState();               // 승인할 때 필요한 게시물 ID를 저장
    const pageSize = 3; // 현재 게시물 수가 적으므로 페이징을 3개 단위로 하였음


    // 페이지가 새로 마운트 될 때마다 실행됨. 
    // 내가 보고있는 게시물이 내가 쓴 글인지(postsOption === writer) 또는 내가 지원한 글인지(postsOption === applicant)
    // 현재 사용자가 하이라이트한 페이지 번호 상태, 
    // 등록순/마감일순에 대한 정렬 옵션,
    // 를 기반으로 백엔드에 동적쿼리 보냄
    useEffect(() => {
        fetchFilteredPosts();
    }, [postsOption, currentPage, sortOption]);

    // 실제 백엔드에 동적 쿼리 보내는 곳
    const fetchFilteredPosts = async () => {

        try {
            const queryParams = new URLSearchParams({ //URLSearchParams 이 클래스는 URL에 대한 쿼리 매개변수를 작성하고 관리하는 데 도움. 'GET' 요청의 URL에 추가될 쿼리 문자열을 만드는 데 사용됨.
                postsOption: postsOption,   // 내가 쓴 글인가? 내가 지원한 글인가?
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                sortOption: sortOption, // 등록순, 모집일자 마감순
            });

            //현재 사용자가 선택한 페이지와 배너 정보를 queryParams에 넣어서 백엔드에 요청
            const response = await request('GET', `/getGroupPosts?${queryParams}`);

            setData(response.data.content); //백엔드에서 받은 게시물 목록을 data에 저장
            setTotalPages(response.data.totalPages); //백엔드에서 받은 전체 페이지 수 정보를 totalPages에 저장
        } catch (error) {
            console.error("Error fetching data:", error);
        }
    };

    // 페이징 된 각 게시물 목록 하나를 클릭하면 그에 해당하는 게시물의 디테일 페이지로 navigate함
    const handleRowClick = (postsId, postType) => {
        if (postType === "PROJECT") {
            navigate(`/project/detail/${postsId}`);
        }
        else {
            navigate(`/study/detail/${postsId}`);
        }
    }

    // 2023826 -> 2023년 8월 26일 형식으로 변환
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // Month is zero-based
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    };

    // 등록순, 마감일 순 버튼이 눌러지면 현재 선택된 버튼으로 세팅하고, 페이지는 0번으로 간다
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
        // 해당 사용자 포트폴리오 페이지로 이동 (PortfolioPage.js와 연관)
        navigate(`/portfolio/${nickName}`);
    }

    // 승인하려는 유저의 닉네임(nickName)과 게시물 아이디(postsId)를 받아서 승인 허가
    const handleApproveUser = async (nickName, postsId) => {
        try {
            const queryParams = new URLSearchParams({
                nickName: nickName, // 닉네임
                postsId: postsId,   // 게시물 ID
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                sortOption: sortOption, // 등록순, 모집일자 마감순
            });

            // 승인 상태를 '수정'하는 것이므로, put request 
            const response = await request('PUT', `/posts/approve?${queryParams}`);

            setData(response.data.content);    // 변경된 데이터를 갖고 새롭게 data를 세팅함
            setIsModalVisible(false);       // 모달은 안보이게 설정
            setCancelModalVisible(false);
        } catch (error) {
            console.error("Error approving user:", error);
        }
    };

    // 승인 취소하려는 유저의 닉네임(nickName)과 게시물 아이디(postsId)를 받아서 승인 허가 취소
    const handleCancelApproval = async (nickName, postsId) => {
        try {
            const queryParams = new URLSearchParams({
                nickName: nickName, // 닉네임
                postsId: postsId,   // 게시물 ID
                page: currentPage, //현재 페이지 정보
                size: pageSize, //페이징을 할 크기(현재는 한페이지에 3개씩만 나오도록 구성했음)
                sortOption: sortOption, // 등록순, 모집일자 마감순
            });

            // 승인 상태를 '수정'하는 것이므로, put request 
            const response = await request('PUT', `/posts/cancelApprove?${queryParams}`);

            setData(response.data.content);     // 변경된 데이터를 갖고 새롭게 data를 세팅함
            setIsModalVisible(false);       // 모달은 안보이게 설정
            setCancelModalVisible(false);
        } catch (error) {
            console.error("Error approving user:", error);
        }
    };

    const renderPosts = (posts) => {
        return (
           <div>
                {posts.map((item, index) => (
                    <Card key={index} style={{ margin: '0 0 10px 0' }}>
                        <Divider className="bold-divider" />
                        <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                            <Col span={12} className="vertical-line2" onClick={() => handleRowClick(item.id, item.postType)} style={{ cursor: 'pointer' }}>
                                <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
                                    <strong style={{ fontSize: '18px' }}>{item.title}</strong>
                                </div>
                                <div style={{ marginLeft: '3px' }}>
                                    게시판 이름: {item.postType} &nbsp;/&nbsp; 분류: {item.web ? "Web " : ""}{item.app ? "App " : ""}{item.game ? "Game " : ""}{item.ai ? "AI " : ""}
                                </div>
                            </Col>
                            <Col span={6} className="vertical-line2" onClick={() => handleRowClick(item.id, item.postType)} style={{ cursor: 'pointer' }}>
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
                                        // 내가 쓴 게시물을 눌렀을 때 보이는 화면
                                        <div>
                                            <div>
                                                지원자
                                            </div>
                                            {item.applyNickNames ? (
                                                <div>
                                                    {item.applyNickNames.length > 0 && item.applyNickNames.map((nickName, index) => (
                                                        <div key={index} style={{ display: 'flex', justifyContent: 'space-between' }}>
                                                            <div onClick={() => handleNickNameClick(nickName)} style={{ cursor: 'pointer' }}>
                                                                {nickName}
                                                            </div>
                                                            <div>
                                                                <Button size="small" onClick={() => handleNickNameClick(nickName)} style={{ marginRight: '5px' }}>
                                                                    포트폴리오
                                                                </Button>
                                                                {item.approved[index] ? (
                                                                    // 승인 취소 버튼 클릭 시 모달 열기
                                                                    <Button
                                                                        size="small"
                                                                        onClick={() => {
                                                                            setNickName(nickName); // 승인 취소 대상 유저의 닉네임 저장
                                                                            setPostsId(item.id); // 게시물 ID 저장
                                                                            setCancelModalVisible(true); // 모달 열기
                                                                        }}
                                                                        style={{ marginRight: '5px' }}
                                                                    >
                                                                        승인 취소
                                                                    </Button>
                                                                ) : (
                                                                    <Button
                                                                        size="small"
                                                                        onClick={() => {
                                                                            setNickName(nickName);
                                                                            setPostsId(item.id);
                                                                            setIsModalVisible(true);
                                                                            if (item.isFull) {
                                                                                message.warning('정원이 모두 찼습니다!');
                                                                            }
                                                                        }}
                                                                        style={{ marginRight: '5px' }}
                                                                    >
                                                                        승인
                                                                    </Button>
                                                                )}
                                                            </div>
                                                        </div>
                                                    ))}
                                                    </div>
                                            ) : (
                                                <div>
                                                    {/** item.applyNickNames가 null인 경우 처리. 이 부분 처리 안하면 에러 발생함!! */}
                                                </div>
                                            )}
                                        </div>
                                    ) : (
                                        // 내가 지원한 게시물을 클릭했을 때 보이는 화면
                                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                            <div>
                                                <div>
                                                    작성자
                                                </div>
                                                <div onClick={() => handleNickNameClick(item.writerNickName)} style={{ cursor: 'pointer' }}>
                                                    {item.writerNickName}
                                                </div>
                                            </div>
                                            {/** alignItems로 상하의 가운데에 놓기 */}
                                            <div style={{ display: 'flex', alignItems: 'center' }}>
                                                <div>
                                                    {item.isApproved ? (
                                                        // 승인 완료시 보여줄 내용
                                                        "승인 완료"
                                                    ) : (
                                                        // 승인이 완료되지 않았고
                                                        item.counts === item.recruitmentCount ? (
                                                            // 정원이 다 찼다면, 모집 마감을 보여줌
                                                            "모집 마감"
                                                        ) : (
                                                            // 정원이 다 안찼다면, 승인 대기 중을 보여줌
                                                            "승인 대기 중"
                                                        )
                                                    )}
                                                </div>
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

                        {/* Sort buttons - 등록순, 마감일자 순 버튼*/}
                        <Button
                            type={sortOption === 'latestPosts' ? 'primary' : 'default'}
                            onClick={() => handleSortOptionChange('latestPosts')}
                            style={{ marginRight: '10px' }}
                        >
                            등록순
                        </Button>
                        <Button
                            type={sortOption === 'nearDeadline' ? 'primary' : 'default'}
                            onClick={() => handleSortOptionChange('nearDeadline')}
                        >
                            가까운 마감일순
                        </Button>

                    </Col>
                    <Col span={12} style={{ textAlign: 'right' }}>

                        {/** Sort buttons - 등록순, 마감일자 순 버튼 */}
                        <Button
                            type={postsOption === "writer" ? 'primary' : 'default'}
                            onClick={() => handlePostsOptionChange("writer")}
                            style={{ marginRight: '10px' }}
                        >
                            내가 작성한 게시물
                        </Button>
                        <Button
                            type={postsOption === "applicant" ? 'primary' : 'default'}
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
                    total={totalPages * pageSize}   // 내용물의 총 개수 = 페이지 수 * 페이지 당 몇 개씩
                    pageSize={pageSize}             // 한 페이지에 몇 개씩 보여줄 것인가?
                    onChange={(page) => setCurrentPage(page - 1)} //사용자가 해당 버튼 (예: 2번 버튼)을 누르면 currentPage를 1로 세팅하여 백엔드에 요청 보냄(백엔드는 프런트에서 보는 페이지보다 하나 적은 수부터 페이징을 시작하므로)
                />
            </div>
            <Modal
                title="유저 승인"
                open={isModalVisible}
                onOk={() => setIsModalVisible(false)}
                onCancel={() => handleApproveUser(nickName, postsId)}
                okText="아니오"
                cancelText="예"
            >
                <p>{nickName} 님을 승인하시겠습니까?</p>
            </Modal>
            <Modal
                title="유저 승인 취소"
                open={cancelModalVisible} // visible로 모달 열림 여부 설정
                onOk={() => setCancelModalVisible(false)} // 취소 버튼을 누르면 모달 닫기
                onCancel={() => handleCancelApproval(nickName, postsId)} // "예" 버튼을 누르면 승인 취소 동작 처리 함수 호출
                okText="아니오"
                cancelText="예"
            >
                <p>{nickName} 님을 승인 취소하시겠습니까?</p>
            </Modal>
        </div>
    );
}

export default GroupPage;