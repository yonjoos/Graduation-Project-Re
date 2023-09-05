import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { useSelector } from "react-redux";
import { request } from '../../../../hoc/request';
import { Divider, Row, Col, Button, Modal, message } from 'antd';
import '../ProjectPage.css';

function DetailProjectPage() {
    const navigate = useNavigate();
    const { projectId } = useParams(); // URL로부터 projectId 가져오기
    const lastVisitedEndpoint = useSelector(state => state.endpoint.lastVisitedEndpoint);

    const [data, setData] = useState({}); // 백엔드에서 가져온 데이터를 세팅
    const [isModalVisible, setIsModalVisible] = useState(false);    // 모달이 보이는지 여부 설정
    const [isCancelModalVisible, setIsCancelModalVisible] = useState(false);    // 지원 취소 관련 모달
    const [isScrapModalVisible, setIsScrapModalVisible] = useState(false);      // 스크랩 모달이 보이는지 여부 설정
    const [modalAction, setModalAction] = useState('');     // modalAction은 'delete'와 'apply' 둘 중 하나로 세팅.
    const [cancelAction, setCancelAction] = useState('');   // 승인 허가된 사람과, 승인 허가되지 않은 사람의 지원 취소 request 매커니즘을 다르게 하기 위해 세팅.
    const [scrapAction, setScrapAction] = useState('');     // 스크랩한 사람과, 스크랩하지 않은 사람의 request 매커니즘을 다르게 하기 위해 세팅

    useEffect(() => {
        // ProjectId를 PathVariable로 보내기
        request('GET', `/getProject/${projectId}`, {})
            .then((response) => {
                //console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
                console.log("lastVisitedEndpoint : ", lastVisitedEndpoint);
            })
            .catch((error) => {
                console.error("Error fetching project data:", error);
            });
    }, [projectId]);


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

            // text[i]를 chunks 뒤에 이어 붙이기
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


    // 2023826 -> 2023년 8월 26일 형식으로 변환
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // Month is zero-based
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    };
    

    // 목록으로 돌아가기 버튼 클릭
    const handleGoBackClick = () => {
        if (lastVisitedEndpoint) {
            navigate(lastVisitedEndpoint);
        }
        else {
            navigate('/');
        }
    };

    const showModal = (action) => {
        setIsModalVisible(true);
        setModalAction(action);
    };

    // 지원 취소 모달 띄우기
    const showCancelModal = (action) => {
        setIsCancelModalVisible(true);
        setCancelAction(action);    // 지원 취소 모달 띄울 때, cancelAction이 applying인지 approved인지 세팅함
    };

    // 스크랩 모달 띄우기
    const showScrapModal = (action) => {
        setIsScrapModalVisible(true);
        setScrapAction(action);
    }
    
    const handleModalConfirm = () => {
        // writer가 게시물 삭제 버튼을 누른 경우
        if (modalAction === 'delete') {
            request('POST', `/project/delete/${projectId}`, {})
            .then((response) => {
                //console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
            })
            .catch((error) => {
                // 승인된 인원이 있는 경우, 삭제가 진행이 안됨. 승인된 인원을 모두 승인 해제하더라도, 여전히 삭제는 안됨.
                // 지원한 인원들이 모두 지원을 취소해야 비로소 삭제 가능. 이 부분은 정책 검토 필요.
                message.warning('프로젝트를 삭제하려면 승인했던 인원을 모두 승인 취소해주세요.');
            });

            navigate('/project');
        }
        
        // writer가 아닌 사람이 지원하기 버튼을 누른 경우
        else if (modalAction === 'apply') {
            request('POST', `/project/apply/${projectId}`, {})
            .then((response) => {
                //console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
            })
            .catch((error) => {
                console.error("Error fetching project data:", error);
            });

            navigate(`/project/detail/${projectId}`);
        }

        setIsModalVisible(false);   // 모달 안보이게 숨김
    };

    const handleCancelModalConfirm = async () => {
        try {
            const response = await request('POST', `/project/cancelApply/${projectId}`, {
                // 여기는 requestBody부분. requestParam을 쓰려면 new URLSearchParams을 써야 한다!
                action: cancelAction
            });

            setData(response.data);     // 백엔드에서 받아온 데이터 세팅
            setIsCancelModalVisible(false); // 모달 안보이게 숨김
        } catch (error) {
            console.error("Error approving user:", error);
        }
    };
    
    const handleScrapModalConfirm = async () => {
        // 스크랩 버튼을 누른 경우
        if (scrapAction === 'scrap') {
            request('POST', `/project/scrap/${projectId}`, {})
            .then((response) => {
                //console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
            })
            .catch((error) => {
                // 승인된 인원이 있는 경우, 삭제가 진행이 안됨. 승인된 인원을 모두 승인 해제하더라도, 여전히 삭제는 안됨.
                // 지원한 인원들이 모두 지원을 취소해야 비로소 삭제 가능. 이 부분은 정책 검토 필요.
                message.warning('프로젝트를 삭제하려면 승인했던 인원을 모두 승인 취소해주세요.');
            });

            navigate(`/project/detail/${projectId}`);
        }
        
        // 스크랩 취소 버튼을 누른 경우
        else if (scrapAction === 'cancelScrap') {
            request('POST', `/project/cancelScrap/${projectId}`, {})
            .then((response) => {
                //console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
            })
            .catch((error) => {
                console.error("Error fetching project data:", error);
            });

            navigate(`/project/detail/${projectId}`);
        }

        setIsScrapModalVisible(false);   // 모달 안보이게 숨김
    };
    
    const handleModalCancel = () => {
        setIsModalVisible(false);
    };

    const handleCancelModalCancel = () => {
        setIsCancelModalVisible(false);
    };

    const handleScrapModalCancel = () => {
        setIsScrapModalVisible(false);
    };

    // 글 작성자인지, 아닌지에 따라 다르게 보이도록 설정
    const renderButtons = () => {
        const isWriter = data.writer;       // 게시물 작성자인가?
        const isScrapped = data.scrap;      // 게시물이 스크랩되었나?
        const isApplying = data.applying;   // 승인 대기 중인가?
        const isApplied = data.applied;     // 승인 완료되었나?
    
        return (
            <Row>
                <Col span={12}>
                    {/** navigate(-1)을 통해, 바로 이전에 방문했던 페이지로 돌아갈 수 있음 */}
                    <Button onClick={handleGoBackClick}>
                        목록으로 돌아가기
                    </Button>
                </Col>
                <Col span={12} style={{ textAlign: 'right' }}>
                    {/** isWriter와 일반 유저가 보이는 버튼이 다르도록 설정 */}
                    {isWriter && (
                        <div>
                            <Button type="primary" onClick={() => navigate(`/project/update/${projectId}`)} style={{ marginRight: '5px' }}>
                                게시물 수정
                            </Button>
                            <Button onClick={() => showModal('delete')}>
                                게시물 삭제
                            </Button>
                        </div>
                    )}
                    {/** 게시물에 지원 안한 사람 + 스크랩 안한 사람 */}
                    {!isWriter && !isScrapped && !isApplying && !isApplied && (
                        data.counts === data.recruitmentCount ? (
                            // 근데 만약, 정원이 다 찼다면 모집 마감을 보여줌
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('scrap')} style={{ marginRight: '5px' }}>
                                    게시물 스크랩
                                </Button>
                                <Button type="text" disabled>
                                    모집 마감
                                </Button>
                            </div>
                        ) : (
                            // 근데 만약, 정원이 안찼다면 지원하기 버튼 클릭 가능
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('scrap')} style={{ marginRight: '5px' }}>
                                    게시물 스크랩
                                </Button>
                                <Button type="primary" onClick={() => showModal('apply')}>
                                    지원하기
                                </Button>
                            </div>
                        )
                    )}
                    {/** 게시물에 지원 안한 사람 + 스크랩 한 사람 */}
                    {!isWriter && isScrapped && !isApplying && !isApplied && (
                        data.counts === data.recruitmentCount ? (
                            // 근데 만약, 정원이 다 찼다면 모집 마감을 보여줌
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('cancelScrap')} style={{ marginRight: '5px' }}>
                                    스크랩 취소
                                </Button>
                                <Button type="text" disabled>
                                    모집 마감
                                </Button>
                            </div>
                        ) : (
                            // 근데 만약, 정원이 안찼다면 지원하기 버튼 클릭 가능
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('cancelScrap')} style={{ marginRight: '5px' }}>
                                    스크랩 취소
                                </Button>
                                <Button type="primary" onClick={() => showModal('apply')}>
                                    지원하기
                                </Button>
                            </div>
                        )
                    )}
                    {/** 지원은 했으나, 승인 대기 중인 사람 + 스크랩 안한 사람 */}
                    {!isWriter && !isScrapped && isApplying && (
                        data.counts === data.recruitmentCount ? (
                            // 근데 만약, 정원이 다 찼다면 모집 마감을 보여줌
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('scrap')} style={{ marginRight: '5px' }}>
                                    게시물 스크랩
                                </Button>
                                <Button type="text" disabled>
                                    모집 마감
                                </Button>
                                <Button type="primary" onClick={() => showCancelModal('applying')}>
                                    지원 취소
                                </Button>
                            </div>
                        ) : (
                            // 정원이 아직 다 안찼다면, 승인 대기 중을 보여줌
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('scrap')} style={{ marginRight: '5px' }}>
                                    게시물 스크랩
                                </Button>
                                <Button type="text" disabled>
                                    승인 대기 중..
                                </Button>
                                <Button type="primary" onClick={() => showCancelModal('applying')}>
                                    지원 취소
                                </Button>
                            </div>
                        )
                    )}
                    {/** 지원은 했으나, 승인 대기 중인 사람 + 스크랩 한 사람 */}
                    {!isWriter && isScrapped && isApplying && (
                        data.counts === data.recruitmentCount ? (
                            // 근데 만약, 정원이 다 찼다면 모집 마감을 보여줌
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('cancelScrap')} style={{ marginRight: '5px' }}>
                                    스크랩 취소
                                </Button>
                                <Button type="text" disabled>
                                    모집 마감
                                </Button>
                                <Button type="primary" onClick={() => showCancelModal('applying')}>
                                    지원 취소
                                </Button>
                            </div>
                        ) : (
                            // 정원이 아직 다 안찼다면, 승인 대기 중을 보여줌
                            <div>
                                <Button type="primary" onClick={() => showScrapModal('cancelScrap')} style={{ marginRight: '5px' }}>
                                    스크랩 취소
                                </Button>
                                <Button type="text" disabled>
                                    승인 대기 중..
                                </Button>
                                <Button type="primary" onClick={() => showCancelModal('applying')}>
                                    지원 취소
                                </Button>
                            </div>
                        )
                    )}
                    {/** 승인 허가된 사람 + 스크랩 안한 사람 */}
                    {!isWriter && !isScrapped && isApplied && (
                        <div>
                            <Button type="primary" onClick={() => showScrapModal('scrap')} style={{ marginRight: '5px' }}>
                                게시물 스크랩
                            </Button>
                            <Button type="text" disabled>
                                승인 완료
                            </Button>
                            <Button type="primary" onClick={() => showCancelModal('approved')}>
                                지원 취소
                            </Button>
                        </div>
                    )}
                    {/** 승인 허가된 사람 + 스크랩 한 사람 */}
                    {!isWriter && isScrapped && isApplied && (
                        <div>
                            <Button type="primary" onClick={() => showScrapModal('cancelScrap')} style={{ marginRight: '5px' }}>
                                스크랩 취소
                            </Button>
                            <Button type="text" disabled>
                                승인 완료
                            </Button>
                            <Button type="primary" onClick={() => showCancelModal('approved')}>
                                지원 취소
                            </Button>
                        </div>
                    )}
                </Col>
            </Row>
        );
    };


    return (
        <div style={{ marginLeft: '10%', marginRight: '10%' }}>
            {/** 게시물 작성자에게만 보이는 화면. 우측 상단에 게시물 수정, 삭제 버튼이 보임. */}
            {data.writer && renderButtons()}
                        {/** 게시물을 작성하지 않은 유저에게만 보이는 화면. 우측 상단에 스크랩 버튼과 지원 버튼이 보임. */}
            {!data.writer && !data.scrap && !data.applying && !data.applied && renderButtons()}    {/** 지원 안한 사람 + 스크랩 안한 사람 */}
            {!data.writer && data.scrap && !data.applying && !data.applied && renderButtons()}    {/** 지원 안한 사람 + 스크랩 한 사람 */}
            {!data.writer && !data.scrap && data.applying && !data.applied && renderButtons()}     {/** 지원 O 승인 X인 사람 (승인 대기 중) + 스크랩 안한 사람 */}
            {!data.writer && data.scrap && data.applying && !data.applied && renderButtons()}     {/** 지원 O 승인 X인 사람 (승인 대기 중) + 스크랩 한 사람 */}
            {!data.writer && !data.scrap && !data.applying && data.applied && renderButtons()}     {/** 승인 O인 사람 (승인 완료) + 스크랩 안한 사람 */}
            {!data.writer && data.scrap && !data.applying && data.applied && renderButtons()}     {/** 승인 O인 사람 (승인 완료) + 스크랩 한 사람 */}
            
            {/** 이상하게, antd에서 끌어온 애들은 style = {{}}로 적용이 안되고 css로 적용될 때가 있음 */}
            <Divider className="bold-divider" />

            <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                <Col span={16}>
                    <div style={{ marginLeft: '5%' }}>
                        제목: {data.title}
                    </div>
                </Col>
                {/** 수직선 CSS인 vertical-line을 만들어 주었음 */}
                <Col span={8} className="vertical-line">
                    <div  style={{ marginLeft: '3px' }}>
                        {/** Boolean으로 반환되는 애들은 삼항연산자를 통해 값을 보여줘야 함 */}
                        분류: &nbsp; {data.web?" Web ":""}{data.app?" App ":""}{data.game?" Game ":""}{data.ai?" AI ":""}
                    </div>
                </Col>
            </Row>

            <Divider className="simple-divider" />

            <Row gutter={[16, 16]} justify="center" align="middle">
                <Col span={16}>
                    <div style={{ marginLeft: '5%', borderRight: '1px' }}>
                        닉네임: {data.nickName}
                    </div>
                </Col>
                {/** 수직선 CSS인 vertical-line을 만들어 주었음 */}
                <Col span={8} className="vertical-line">
                    <div className="form-outline mb-1" style={{ marginLeft: '3px' }}>
                        인원: {data.counts} / {data.recruitmentCount}
                    </div>
                    <div  style={{ marginLeft: '3px' }}>
                        모집 마감일: {formatDate(data.endDate)}
                    </div>
                </Col>
            </Row>

            <Divider className="bold-divider" />

            <div style={{ marginLeft: '5px' }}>
                첨부 파일: {data.fileUrl}
            </div>

            <Divider className="bold-divider" />

            <div style={{ marginLeft: '5px' }}>
                홍보 사진: {data.promoteImageUrl}
            </div>

            <Divider className="bold-divider" />

            {/** whiteSpace: 'pre-wrap'을 통해, DB에 저장된 개행을 알아서 <br>로 바꾸고 올바르게 화면에 출력함. */}
            <div style={{ whiteSpace: 'pre-wrap', marginLeft: '5px' }}>
                내용: {insertLineBreaks(data.content, 45)}
            </div>

            {/* Modal */}
            <Modal
                title="Confirm Action"
                open={isModalVisible}
                // 모순적이지만, 익숙한 위치에 두기 위해 함수 이름을 Cross해서 사용
                onOk={handleModalCancel}
                onCancel={handleModalConfirm}
                okText="아니오"
                cancelText="예"
            >
                {modalAction === 'delete' && (
                    <p>게시물을 삭제하시겠습니까?</p>
                )}
                {modalAction === 'apply' && (
                    <p>게시물에 지원하시겠습니까?</p>
                )}
            </Modal>
            <Modal
                title="Confirm Action"
                open={isCancelModalVisible}
                onOk={handleCancelModalCancel}
                onCancel={handleCancelModalConfirm}
                okText="아니오"
                cancelText="예"
                >
                <p>지원을 취소하시겠습니까?</p>
            </Modal>
            <Modal
                title="Confirm Action"
                open={isScrapModalVisible}
                onOk={handleScrapModalCancel}
                onCancel={handleScrapModalConfirm}
                okText="아니오"
                cancelText="예"
            >
                {scrapAction === 'scrap' && (
                    <p>게시물을 스크랩하시겠습니까?</p>
                )}
                {scrapAction === 'cancelScrap' && (
                    <p>스크랩을 취소하시겠습니까?</p>
                )}
            </Modal>
        </div>
    )
}

export default DetailProjectPage;