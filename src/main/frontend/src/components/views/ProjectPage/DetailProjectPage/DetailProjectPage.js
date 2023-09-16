import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { useSelector } from "react-redux";
import { request, getUserNickName } from '../../../../hoc/request';
import { Divider, Row, Col, Button, Modal, message, Input, Card, Pagination } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import '../ProjectPage.css'; 
import './DetailProjectPage.css'; // 댓글의 계층에 따른 왼쪽 여백 css

const { TextArea } = Input;

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
    const [commentText, setCommentText] = useState(''); // 부모 없는 '댓글' 에 담길 댓글 내용
    const [replyText, setReplyText] = useState('');     // 부모 있는 '답글' 에 담길 답글 내용
    const [replyToCommentId, setReplyToCommentId] = useState(null); // 해당 답글의 부모 댓글 id값
    const [commentData, setCommentData] = useState([]); // 백엔드에서 가져온 해당 게시물의 전체 댓글,답글 내용들
    const [replyVisibility, setReplyVisibility] = useState({}); // 답글 보기 여부. useState 훅의 초기화 값으로 빈 객체 {}를 사용하는 것은 일반적인 패턴 중 하나로, 이렇게 하면 상태 변수 replyVisibility는 객체를 가지며, 해당 객체는 답글의 보이기 여부를 관리하는 데 사용함.
    const [editingCommentId, setEditingCommentId] = useState(null); // 댓글 수정에 해당하는 댓글 id
    const [editedCommentText, setEditedCommentText] = useState(''); // 백엔드에 보낼 댓글 수정 내용
    const [areCommentsVisible, setAreCommentsVisible] = useState(false); // 댓글 컴포넌트 숨기기 여부
    const [currentPage, setCurrentPage] = useState(0); // 부모 댓글 기준 어떤 부모부터 가져올건지(사실상 offset)
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 부모 댓글의 총 개수
    const [pageSize, setPageSize] = useState(3); // offset부터 어디까지 가져올건지(사실상 limit) -> 초기에 3개로 설정
    const [moreCommentsAvailable, setMoreCommentsAvailable] = useState(true); // 더보기 버튼 활성화 여부

    const currentUserNickName = getUserNickName();

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



    // 해당 페이지에 처음 접근했을 때, 또는 댓글 또는 답글 업로드 시, 명시적으로 fetchCommentData() 를 호출하여 다시 최신 댓글정보를 백엔드에서 가져옴
    // pageSize(limit)가 변할때마다 다시 렌더링
    useEffect(() => {
        // Initial data fetch
        fetchCommentData();
    }, [pageSize]);


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

    // 2023/8/26-11:11분을 2023년 8월 26일 11시 11분 형식으로 변환 
    const formatDateTime = (dateTimeArray) => {
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


    // 목록으로 돌아가기 버튼 클릭
    const handleGoBackClick = () => {
        // 가장 마지막에 저장한 엔드포인트에 맞추어 해당 엔드포인트로 이동
        if (lastVisitedEndpoint) {
            navigate(lastVisitedEndpoint);
        }
        // 저장된 엔드포인트가 없다면, 랜딩페이지로 이동
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

    // 어떤 부모에도 속하지 않는 level의 댓글 작성 후 업로드
    const handleCommentSubmit = async () => {

        if (commentText.trim() === '') {
            message.warning("댓글 내용을 입력하세요.");
            return;
        }

        try {
            const response = await request('POST', `/registerCommentsInProject/${projectId}`, {
                content: commentText, // 댓글 내용
                parentId: null, // 부모가 없으므로 parentId는 null
            });


            setCommentText('');
            message.success("댓글이 성공적으로 업로드 되었습니다.");
            setCurrentPage(0); // 댓글 업로드 후, 다시 댓글 렌더링은 0번째부터 가져오게 하기 위함
            fetchCommentData(); // 댓글 업로드가 되었다면, 최근 올린 댓글이 반영된 결과를 다시 조회해옴

        } catch (error) {
            console.error("댓글 업로드에 실패했습니다. 잠시 후 다시 시도하세요.", error);
            message.error("댓글 업로드에 실패했습니다. 잠시 후 다시 시도하세요.");
        }
    };

    // 부모가 있는 답글 작성 후 업로드
    const handleReplySubmit = async () => {
        if (replyText.trim() === '') {
            message.warning("답글 내용을 입력하세요.");
            return;
        }

        try {
            const response = await request('POST', `/registerCommentsInProject/${projectId}`, {
                content: replyText, // 답글 내용
                parentId: replyToCommentId, // 부모 댓글 id
            });


            setReplyText('');
            setReplyToCommentId(null); // 현재 지시 중인 답글의 parentid를 null로 세팅
            setCurrentPage(0); // 답글 업로드 후, 다시 댓글 렌더링은 0번째부터 가져오게 하기 위함
            message.success("답글이 성공적으로 업로드 되었습니다.");

            fetchCommentData(); // 댓글 업로드가 되었다면, 최근 올린 댓글이 반영된 결과를 다시 조회해옴
        } catch (error) {
            console.error("답글 업로드에 실패했습니다. 잠시 후 다시 시도하세요.", error);
            message.error("답글 업로드에 실패했습니다. 잠시 후 다시 시도하세요.");
        }
    };

    // 댓글 또는 답글 업로드 후 가장 최신의 댓글 정보를 백엔드에서 다시 가져오기
    const fetchCommentData = async () => {

        try {
            const queryParams = new URLSearchParams({
                projectId: projectId,
                page: currentPage, // 몇번째 댓글부터
                size: pageSize // 몇번째 댓글까지 가져올건지 설정
            });

            const response = await request('GET', `/getCommentDataInProject?${queryParams}`);


            setCommentData(response.data); // 댓글 가져와서 저장
            setTotalPages(response.data.totalElements) // 현재 백엔드에서 관리되고 있는 부모 댓글들이 몇개인지 확인

            if (pageSize < response.data.totalElements) { // 만약 현재 limit값이 전체 부모 댓글 수보다 적다면
                setMoreCommentsAvailable(true); // 더보기 버튼 활성화
            } else {
                setMoreCommentsAvailable(false);
            }

        }

        catch (error) {
            console.error('Error fetching comments:', error);
        }
    };


    // reply버튼 누르면, replyToCommentId를 해당 댓글(부모) id로 세팅
    const showReplyInput = (commentId) => {
        setReplyToCommentId(commentId);
        setReplyText('');
    };

    // 답글 달기 취소
    const cancelReply = () => {
        setReplyToCommentId(null);
        setReplyText(''); // 답글 작성 취소 시 텍스트 초기화
    };


    // 댓글의 삭제 버튼 누르면, 백엔드에 삭제 요청 보냄
    const handleDeleteComment = async (commentId) => {
        try {
            const response = await request('POST', `/deleteComments/${commentId}`);
            if (response.status === 200) {
                message.success("댓글이 삭제되었습니다.");
                fetchCommentData(); // 삭제 완료 후 다시 최신 댓글 정보 받아옴
            }
        } catch (error) {
            console.error("댓글 삭제에 실패했습니다.", error);
            message.error("댓글 삭제에 실패했습니다.");
        }
    };

    // 답글 숨기기, 답글 보기 관련
    const toggleReplyVisibility = (commentId) => {
        setReplyVisibility((prevState) => ({
            ...prevState,
            [commentId]: !prevState[commentId],
        }));
    };

    // 댓글 수정 버튼을 눌렀을 때
    const handleEditComment = (commentId, commentText) => {

        setEditingCommentId(commentId); // 댓글 수정할 댓글 id를 세팅
        setEditedCommentText(commentText); // 해당 댓글의 내용을 editedCommentText에 설정
    };

    // 수정 완료 버튼을 눌렀을 때
    const handleEditCommentSubmit = async (commentId) => {

        // 수정된 댓글 내용을 백엔드로 전송하는 로직을 추가
        if (editedCommentText.trim() === '') {
            message.warning("댓글 내용을 입력하세요.");
            return;
        }

        try {
            await request('PUT', `/updateComments/${commentId}`, {
                content: editedCommentText, // 답글 내용

            });


            // 수정 상태 초기화
            setEditingCommentId(null);
            setEditedCommentText('');
            message.success("댓글이 성공적으로 수정 되었습니다.");

            fetchCommentData(); // 댓글 수정이 완료되었다면, 최근 수정된 댓글이 반영된 결과를 다시 조회해옴

        } catch (error) {
            console.error("댓글 수정에 실패했습니다. 잠시 후 다시 시도하세요.", error);
            message.error("댓글 수정에 실패했습니다. 잠시 후 다시 시도하세요.");
        }


    };

    // 수정 - 취소 버튼을 눌렀을 때
    const handleCancelEditComment = () => {
        setEditingCommentId(null);
        setEditedCommentText('');
    };

    // 댓글 컴포넌트 숨기기 관련
    const toggleCommentsVisibility = () => {
        setAreCommentsVisible(!areCommentsVisible);
    };

    // 더보기 버튼 관련
    const loadMoreComments = () => {

        // 0번째 댓글부터 가져오기 위함
        setCurrentPage(0);

        const newPageSize = pageSize + 3; // limit값을 3 증가시켜보기
        if (newPageSize >= totalPages) { // 만약 limit+3값이 전체 댓글보다 크거나 같다면 더보기 버튼 더 가져올 수 없고, 마지막 댓글로 limit값 설정

            setPageSize(totalPages);
            setMoreCommentsAvailable(false);
        } else {

            setPageSize(newPageSize); // 만약 limit+3값이 전체 댓글보다 작다면, limit+3값으로 limit를 설정하기
            fetchCommentData(); // 그 후 다시 렌더링
        }

    }





    // 댓글의 렌더링 관련
    // 부모면 상위 level에 세팅,
    // 자식이면 계속 하위 level을 타고 들어가 세팅
    const renderComments = (comments, depth = 0) => {

        console.log('com', comments);

        return comments.map((comment) => (
            <Card key={comment.id} style={{ marginBottom: '16px' }}>
                <div className={`comment-container depth-${depth}`}>
                    <div className="comment-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <div style={{ display: 'flex', alignItems: 'center' }}>
                            <UserOutlined style={{ marginBottom: "12px", marginRight: '5px' }} />
                            <p style={{ marginRight: '10px' }}><strong>{comment.nickName}</strong></p>
                        </div>

                        {comment.commentWriter && (
                            <div>
                                <Button size="small" onClick={() => showReplyInput(comment.id)}>답글 달기</Button>
                                {editingCommentId === comment.id ? (
                                    // 수정 중일 때, Input으로 표시하고 수정 관련 버튼 표시


                                    <Button size="small" onClick={handleCancelEditComment} style={{ marginBottom: '16px' }}>취소</Button>

                                ) : (
                                    // 수정 중이 아닐 때, "수정" 버튼 표시
                                    <Button size="small" onClick={() => handleEditComment(comment.id, comment.content)}>수정</Button>
                                )}
                                <Button size="small" onClick={() => handleDeleteComment(comment.id)}>삭제</Button>
                            </div>
                        )}
                        {!comment.commentWriter && (
                            <Button size="small" onClick={() => showReplyInput(comment.id)}>답글 달기</Button>
                        )}
                    </div>

                    {editingCommentId === comment.id ? (
                        // 수정 중일 때, Input으로 표시
                        <>
                            <TextArea
                                autoSize={{ minRows: 3 }}
                                type="text"
                                value={editedCommentText}
                                onChange={(e) => setEditedCommentText(e.target.value)}
                                placeholder="Edit your comment"

                            />
                            <div style={{ marginBottom: '16px', textAlign: 'right', marginTop: '16px' }}>
                                <Button size="small" onClick={() => handleEditCommentSubmit(comment.id)}>수정 완료</Button>
                            </div>
                        </>
                    ) : (
                        // 수정 중이 아닐 때, <p>로 표시
                        <p style={{ marginTop: '5px', whiteSpace: 'pre-wrap' }}>{insertLineBreaks(comment.content, 45)}</p>
                    )}

                    <div style={{ textAlign: 'right', marginTop: '5px', fontSize: '12px', color: 'gray' }}>
                        {formatDateTime(comment.finalCommentedTime)}
                    </div>
                    {replyToCommentId === comment.id && ( // 답글 달기 버튼 누른 부모 댓글 아래에 답글 작성할 폼 세팅
                        <div className={`reply-container depth-${depth + 1}`} style={{ display: 'flex', alignItems: 'center', marginTop: '5px' }}>
                            <UserOutlined style={{ marginBottom: "12px", marginRight: '5px' }}></UserOutlined>
                            <p style={{ marginRight: '10px' }}><strong>{currentUserNickName}</strong></p>
                            <TextArea
                                autoSize={{ minRows: 3 }}
                                type="text"
                                value={replyText}
                                onChange={(e) => setReplyText(e.target.value)}
                                placeholder="Write a reply"
                                style={{ marginBottom: '16px' }}
                            />
                            <Button size="small" onClick={() => handleReplySubmit(comment.id)} style={{ marginBottom: '16px', marginLeft: '5px' }} >답글 등록</Button>
                            <Button size="small" onClick={cancelReply} style={{ marginBottom: '16px' }}>취소</Button> {/* 취소 버튼 추가 */}
                        </div>
                    )}
                    {/* 1차 level렌더링 후, 각 1차 level에 children이 있다면 하위 level을 재귀적으로 다시 렌더링함 */}
                    {comment.children && comment.children.length > 0 && (
                        <div>
                            <Button size="small" onClick={() => toggleReplyVisibility(comment.id)} style={{ marginBottom: '16px' }}>
                                {replyVisibility[comment.id] ? '답글 숨기기' : '답글 보기'}
                            </Button>
                            {/* Step 2: Conditionally render replies */}
                            {replyVisibility[comment.id] && renderComments(comment.children, depth + 1)}
                        </div>
                    )}
                </div>
            </Card>
        ));
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
                    <div style={{ marginLeft: '3px' }}>
                        {/** Boolean으로 반환되는 애들은 삼항연산자를 통해 값을 보여줘야 함 */}
                        분류: &nbsp; {data.web ? " Web " : ""}{data.app ? " App " : ""}{data.game ? " Game " : ""}{data.ai ? " AI " : ""}
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
                    <div style={{ marginLeft: '3px' }}>
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

            <div style={{ textAlign: 'center', marginTop: '16px' }}>
                <Button size="small" onClick={toggleCommentsVisibility}>
                    {areCommentsVisible ? '댓글 숨기기' : '모든 댓글 보기'}
                </Button>
            </div>



            {/* 프로젝트 내용 하단에 댓글, 답글 렌더링 */}
            {areCommentsVisible && (
                <div>
                    <Divider className="bold-divider" />

                    <h5>댓글</h5>
                    {renderComments(commentData.content)}
                    <div style={{ textAlign: 'center', margin: '20px 0' }}>
                        {moreCommentsAvailable && (
                            <Button size="small" onClick={loadMoreComments}>
                                댓글 더보기
                            </Button>
                        )}
                    </div>

                    <div>
                        <Card>
                            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '12px' }}>
                                <UserOutlined style={{ marginRight: '5px' }} />
                                <p style={{ margin: '0' }}><strong>{currentUserNickName}</strong></p>
                            </div>
                            <TextArea
                                autoSize={{ minRows: 4 }}
                                value={commentText}
                                onChange={(e) => setCommentText(e.target.value)}
                                placeholder="Write a comment"
                            />
                            <div style={{ textAlign: 'right', marginTop: '16px' }}>
                                <Button size="small" onClick={handleCommentSubmit}>댓글 등록</Button>
                            </div>
                        </Card>
                    </div>
                    {/* <div style={{ textAlign: 'center', margin: '20px 0' }}>
                        <Pagination
                            current={currentPage + 1} // Ant Design's Pagination starts from 1, while your state starts from 0
                            total={totalPages * pageSize}
                            pageSize={pageSize}
                            onChange={(page) => setCurrentPage(page - 1)} //사용자가 해당 버튼 (예: 2번 버튼)을 누르면 currentPage를 1로 세팅하여 백엔드에 요청 보냄(백엔드는 프런트에서 보는 페이지보다 하나 적은 수부터 페이징을 시작하므로)
                        />

                    </div> */}
                </div>

            )}

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