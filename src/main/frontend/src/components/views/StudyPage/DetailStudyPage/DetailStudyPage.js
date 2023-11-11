import { useEffect, useState } from "react";
import React from "react";
import { useNavigate, useParams } from "react-router";
import { Link } from "react-router-dom";
//import { useSelector, useDispatch } from "react-redux";
import { request, getUserNickName } from '../../../../hoc/request';
import { Divider, Row, Col, Button, Modal, message, Input, Card, Image } from 'antd';
import { UserOutlined } from '@ant-design/icons';
//import { lastVisitedEndpoint } from "../../../../_actions/actions";
//import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from "../../../../hoc/request";
import '../StudyPage.css';
import './DetailStudyPage.css'; // 댓글의 계층에 따른 왼쪽 여백 css

const { TextArea } = Input;

function DetailStudyPage() {
    const navigate = useNavigate();
    //const dispatch = useDispatch();
    const { studyId } = useParams(); // URL로부터 studyId 가져오기
    // const visitedEndpoint = useSelector(state => state.endpoint.lastVisitedEndpoint);
    // const visitedEndEndpoint = useSelector(state => state.endpoint.lastLastVisitedEndpoint);
    // const visitedEndEndEndpoint = useSelector(state => state.endpoint.lastLastLastVisitedEndpoint);

    const [data, setData] = useState({}); // 백엔드에서 가져온 데이터를 세팅
    const [isModalVisible, setIsModalVisible] = useState(false);    // 모달이 보이는지 여부 설정
    const [isCancelModalVisible, setIsCancelModalVisible] = useState(false);    // 지원 취소 관련 모달
    const [isScrapModalVisible, setIsScrapModalVisible] = useState(false);      // 스크랩 모달이 보이는지 여부 설정
    const [modalAction, setModalAction] = useState('');     // modalAction은 'delete'와 'apply' 둘 중 하나로 세팅.
    const [cancelAction, setCancelAction] = useState('');   // 승인 허가된 사람과, 승인 허가되지 않은 사람의 지원 취소 request 매커니즘을 다르게 하기 위해 세팅.
    const [scrapAction, setScrapAction] = useState('');     // 스크랩한 사람과, 스크랩하지 않은 사람의 request 매커니즘을 다르게 하기 위해 세팅
    const [isApplicantOpen, setIsApplicantOpen] = useState(true);   // 초기 상태를 "지원자 목록 열기"로 설정
    const [applicantData, setApplicantData] = useState([]);     // 백엔드에서 가져온 지원자 목록 세팅
    const [approveModalVisible, setApproveModalVisible] = useState(false);    // 유저 승인 모달이 보이는지 여부 설정
    const [cancelModalVisible, setCancelModalVisible] = useState(false);    // 유저 승인 취소 모달이 보이는지 여부 설정
    const [applyUserNickName, setApplyUserNickname] = useState('');

    // 아래는 댓글 기능 관련 상태변수
    const [commentText, setCommentText] = useState(''); // 부모 없는 '댓글' 에 담길 댓글 내용
    const [replyText, setReplyText] = useState('');     // 부모 있는 '답글' 에 담길 답글 내용
    const [replyToCommentId, setReplyToCommentId] = useState(null); // 해당 답글의 부모 댓글 id값
    const [commentData, setCommentData] = useState([]); // 백엔드에서 가져온 해당 게시물의 전체 댓글,답글 내용들
    const [replyVisibility, setReplyVisibility] = useState({}); // 답글 보기 여부. useState 훅의 초기화 값으로 빈 객체 {}를 사용하는 것은 일반적인 패턴 중 하나로, 이렇게 하면 상태 변수 replyVisibility는 객체를 가지며, 해당 객체는 답글의 보이기 여부를 관리하는 데 사용함.
    const [editingCommentId, setEditingCommentId] = useState(null); // 댓글, 답글 수정에 해당하는 댓글, 답글 id
    const [editedCommentText, setEditedCommentText] = useState(''); // 백엔드에 보낼 댓글, 답글 수정 내용
    const [deleteCommentId, setDeleteCommentId] = useState(null); // 댓글, 답글 삭제에 해당하는 댓글,답글 id
    const [areCommentsVisible, setAreCommentsVisible] = useState(false); // 댓글 컴포넌트 숨기기 여부
    const [currentPage, setCurrentPage] = useState(0); // 부모 댓글 기준 어떤 부모부터 가져올건지(사실상 offset)
    const [totalPages, setTotalPages] = useState(0); // 동적 쿼리를 날렸을 때 백엔드에서 주는 현재 상태에서의 부모 댓글의 총 개수
    const [pageSize, setPageSize] = useState(3); // offset부터 어디까지 가져올건지(사실상 limit) -> 초기에 3개로 설정
    const [moreCommentsAvailable, setMoreCommentsAvailable] = useState(true); // 더보기 버튼 활성화 여부
    const [commentEditConfirmModalVisible, setCommentEditConfirmModalVisible] = useState(false); // 댓글, 답글 수정 관련 모달 활성화 여부
    const [isTopLevelUsedByEditing, setIsTopLevelUsedByEditing] = useState(null); // 댓글, 답글 수정 시 해당 댓글 또는 답글이 최상위 부모인지 아닌지 판단하는 기준값
    const [commentDeleteConfirmModalVisible, setCommentDeleteConfirmModalVisible] = useState(false); // 댓글, 답글 삭제 관련 모달 활성화 여부
    const [isTopLevelUsedByDelete, setIsTopLevelUsedByDelete] = useState(null); // 댓글, 답글 삭제 시 해당 댓글 또는 답글이 최상위 부모인지 아닌지 판단하는 기준값


    const currentUserNickName = getUserNickName();
    const [profileImage, setProfileImage] = useState(null);

    useEffect(() => {
        // StudyId를 PathVariable로 보내기
        request('GET', `/getStudy/${studyId}`, {})
            .then((response) => {
                //console.log("Fetched study data:", response.data); // Log the fetched data
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
                // console.log("visitedEndpoint : ", visitedEndpoint);
                // console.log("visitedEndEndpoint : ", visitedEndEndpoint);

                // 게시물의 작성자라면, 지원자를 얻어오는 추가적인 쿼리를 날림
                if (response.data.writer) {
                    request('GET', `/getStudyApplicants/${studyId}`, {})
                        .then((additionalResponse) => {
                            setApplicantData(additionalResponse.data);
                            console.log("additionalResponse.data : ", additionalResponse.data);
                        })
                        .catch((additionalError) => {
                            console.error("Error fetching additional data:", additionalError);
                        });
                }
            })
            .catch((error) => {
                console.error("Error fetching study data:", error);
            });
    }, [studyId]);

    // 해당 페이지에 처음 접근했을 때, 또는 댓글 또는 답글 업로드 시, 명시적으로 fetchCommentData() 를 호출하여 다시 최신 댓글정보를 백엔드에서 가져옴
    // pageSize(limit)가 변할때마다 다시 렌더링
    useEffect(() => {
        // Initial data fetch
        fetchCommentData();
    }, [pageSize]);

    //프로필 사진 백에서 가져오기
    useEffect(() => {

        request('GET', '/userProfileImage')
            .then((response) => {
                console.log(response.data.imageUrl);
                setProfileImage(response.data.imageUrl);
            })
            .catch((error) => {
                console.error("Error fetching profile image:", error);
            });

    }, [profileImage])


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

            if (text[i] === '\n' || (text[i] === '\r' && text[i + 1] === '\n')) {
                chunks.push('\n');
                j = 0;

                if (text[i] === '\r') {
                    i++; // Skip the next character ('\n')
                }
            }
        }

        return chunks.join('');
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

    const categoryTagStyle = {
        display: 'flex',
        padding: '0px 5px 0px 5px',
        backgroundColor: '#ff9900', /* 원하는 색상으로 변경 */
        borderRadius: '50px', /* 타원형 모양을 만들기 위해 사용 */
        color: '#ff4646', /* 텍스트 색상 설정 */
        marginLeft: '-0.3%',
        marginRight: '5px'
    };


    // 목록으로 돌아가기 버튼 클릭
    // const handleGoBackClick = () => {
    //     // 가장 마지막에 저장한 엔드포인트에 맞추어 해당 엔드포인트로 이동
    //     // 포트폴리오를 누르지 않아, 유효한 전 페이지와 유효한 전 전 페이지가 동일한 상황 -> 전 페이지로 이동하여 목록으로 돌아가기 버튼 정상 작동
    //     if (visitedEndpoint === visitedEndEndpoint) {
    //         navigate(visitedEndpoint);
    //     }
    //     // 포트폴리오를 눌러서, 유효한 전 페이지와 유효한 전 전 페이지가 동일하지 않은 상황 -> 전 전 페이지로 이동하여 목록으로 돌아가기 버튼 정상 작동
    //     else {
    //         navigate(visitedEndEndpoint);
    //     }
    // };

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

    // const handleModalConfirm = () => {
    //     // writer가 게시물 삭제 버튼을 누른 경우
    //     if (modalAction === 'delete') {
    //         request('POST', `/study/delete/${studyId}`, {})
    //             .then((response) => {
    //                 //console.log("Fetched study data:", response.data); // Log the fetched data
    //                 setData(response.data); // 백엔드에서 받아온 데이터 세팅
    //             })
    //             .catch((error) => {
    //                 // 승인된 인원이 있는 경우, 삭제가 진행이 안됨. 승인된 인원을 모두 승인 해제하더라도, 여전히 삭제는 안됨.
    //                 // 지원한 인원들이 모두 지원을 취소해야 비로소 삭제 가능. 이 부분은 정책 검토 필요.
    //                 message.warning('스터디를 삭제하려면 승인했던 인원을 모두 승인 취소해주세요.');
    //             });

    //         navigate('/study');
    //     }

    //     // writer가 아닌 사람이 지원하기 버튼을 누른 경우
    //     else if (modalAction === 'apply') {
    //         request('POST', `/study/apply/${studyId}`, {})
    //             .then((response) => {
    //                 //console.log("Fetched study data:", response.data); // Log the fetched data
    //                 setData(response.data); // 백엔드에서 받아온 데이터 세팅
    //             })
    //             .catch((error) => {
    //                 console.error("Error fetching study data:", error);
    //             });

    //         navigate(`/study/detail/${studyId}`);
    //     }

    //     setIsModalVisible(false);   // 모달 안보이게 숨김
    // };

    const handleModalConfirm = async () => {
        // writer가 게시물 삭제 버튼을 누른 경우
        if (modalAction === 'delete') {
            try {
                const response = await request('POST', `/study/delete/${studyId}`, {});
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
                message.success('스터디가 성공적으로 삭제되었습니다');
                navigate('/study');
            } catch (error) {
                // 승인된 인원이 있는 경우, 삭제가 진행이 안됨. 승인된 인원을 모두 승인 해제하더라도, 여전히 삭제는 안됨.
                // 지원한 인원들이 모두 지원을 취소해야 비로소 삭제 가능. 이 부분은 정책 검토 필요.
                message.warning('스터디를 삭제하려면 승인했던 인원을 모두 승인 취소해주세요.');
            }
        }

        // writer가 아닌 사람이 지원하기 버튼을 누른 경우
        else if (modalAction === 'apply') {
            try {
                const response = await request('POST', `/study/apply/${studyId}`, {});
                setData(response.data); // 백엔드에서 받아온 데이터 세팅
                window.location.reload(); // 페이지 새로고침
            } catch (error) {
                console.error("Error fetching study data:", error);
            }
        }

        setIsModalVisible(false);   // 모달 안보이게 숨김
    };


    const handleCancelModalConfirm = async () => {
        try {
            const response = await request('POST', `/study/cancelApply/${studyId}`, {
                // 여기는 requestBody부분. requestParam을 쓰려면 new URLSearchParams을 써야 한다!
                action: cancelAction
            });

            setData(response.data);     // 백엔드에서 받아온 데이터 세팅
            setIsCancelModalVisible(false); // 모달 안보이게 숨김
            window.location.reload(); // 페이지 새로고침
        } catch (error) {
            console.error("Error approving user:", error);
        }
    };

    const handleScrapModalConfirm = async () => {
        // 스크랩 버튼을 누른 경우
        if (scrapAction === 'scrap') {
            request('POST', `/study/scrap/${studyId}`, {})
                .then((response) => {
                    //console.log("Fetched study data:", response.data); // Log the fetched data
                    setData(response.data); // 백엔드에서 받아온 데이터 세팅
                    window.location.reload(); // 페이지 새로고침
                })
                .catch((error) => {
                    // 승인된 인원이 있는 경우, 삭제가 진행이 안됨. 승인된 인원을 모두 승인 해제하더라도, 여전히 삭제는 안됨.
                    // 지원한 인원들이 모두 지원을 취소해야 비로소 삭제 가능. 이 부분은 정책 검토 필요.
                    message.warning('스터디를 삭제하려면 승인했던 인원을 모두 승인 취소해주세요.');
                });

            navigate(`/study/detail/${studyId}`);
        }

        // 스크랩 취소 버튼을 누른 경우
        else if (scrapAction === 'cancelScrap') {
            request('POST', `/study/cancelScrap/${studyId}`, {})
                .then((response) => {
                    //console.log("Fetched study data:", response.data); // Log the fetched data
                    setData(response.data); // 백엔드에서 받아온 데이터 세팅
                    window.location.reload(); // 페이지 새로고침
                })
                .catch((error) => {
                    console.error("Error fetching study data:", error);
                });

            navigate(`/study/detail/${studyId}`);
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

    const linkStyle = {
        textDecoration: 'none',
        transition: 'text-decoration 0.3s',
        color: 'black'
    };

    const handleMouseEnter = (e) => {
        e.currentTarget.style.textDecoration = 'underline';
    };

    const handleMouseLeave = (e) => {
        e.currentTarget.style.textDecoration = 'none';
    };


    // 지원한 사람의 포폴 확인 또는 작성자의 포폴 확인
    const handleNickNameClick = (nickName) => {
        // /portfolio/${nickName}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /study/detail/${studyId}로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /study/detail/${studyId}로 설정
        // 전에 방문했던 페이지는 현재 페이지로, 전 전에 방문했던 페이지는 현재 페이지 이전에 방문했던 페이지로 설정
        // dispatch(lastVisitedEndpoint(`/study/detail/${studyId}`, visitedEndEndpoint, visitedEndEndEndpoint));    // 전역에 상태 저장을 위한 애.
        // setLastVisitedEndpoint(`/study/detail/${studyId}`);   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
        // setLastLastVisitedEndpoint(visitedEndEndpoint);
        // setLastLastLastVisitedEndpoint(visitedEndEndEndpoint);
        navigate(`/portfolio/${nickName}`);
    };

    const handlePortfolioClick = (nickName) => {
        // /portfolio/${nickName}로 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
        // 가장 마지막에 저장한 엔드포인트인 /study/detail/${studyId}로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /study/detail/${studyId}로 설정
        // 전에 방문했던 페이지는 현재 페이지로, 전 전에 방문했던 페이지는 현재 페이지 이전에 방문했던 페이지로 설정
        // dispatch(lastVisitedEndpoint(`/study/detail/${studyId}`, visitedEndEndpoint, visitedEndEndEndpoint));    // 전역에 상태 저장을 위한 애.
        // setLastVisitedEndpoint(`/study/detail/${studyId}`);   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
        // setLastLastVisitedEndpoint(visitedEndEndpoint);
        // setLastLastLastVisitedEndpoint(visitedEndEndEndpoint);
        navigate(`/portfolio/${nickName}`);
    };

    // 승인하려는 유저의 닉네임(nickName)과 게시물 아이디(postsId)를 받아서 승인 허가
    const handleApproveUser = async (applyUserNickName, studyId) => {
        try {
            const queryParams = new URLSearchParams({
                nickName: applyUserNickName, // 닉네임
                postsId: studyId,   // 게시물 ID
            });

            // 승인 상태를 '수정'하는 것이므로, put request 
            const response = await request('PUT', `/posts/detail/approve?${queryParams}`);

            // 기존 data 객체의 내용을 복사하여 새로운 객체를 생성
            const newData = { ...data };

            // count 값을 response.data[0].count로 업데이트
            newData.counts = response.data[0].count;

            // 변경된 데이터를 세팅
            setData(newData);

            setApplicantData(response.data);    // 변경된 데이터를 갖고 새롭게 data를 세팅함
            setApproveModalVisible(false);       // 모달은 안보이게 설정
            setCancelModalVisible(false);
        } catch (error) {
            console.error("Error approving user:", error);
        }
    };

    // 승인 취소하려는 유저의 닉네임(nickName)과 게시물 아이디(postsId)를 받아서 승인 허가 취소
    const handleCancelApproval = async (applyUserNickName, studyId) => {
        try {
            const queryParams = new URLSearchParams({
                nickName: applyUserNickName, // 닉네임
                postsId: studyId,   // 게시물 ID
            });

            // 승인 상태를 '수정'하는 것이므로, put request 
            const response = await request('PUT', `/posts/detail/cancelApprove?${queryParams}`);

            // 기존 data 객체의 내용을 복사하여 새로운 객체를 생성
            const newData = { ...data };

            // count 값을 response.data[0].count로 업데이트
            newData.counts = response.data[0].count;
            console.log("response.data[0].count : ", response.data[0].count);

            // 변경된 데이터를 세팅
            setData(newData);

            setApplicantData(response.data);     // 변경된 데이터를 갖고 새롭게 data를 세팅함
            setApproveModalVisible(false);       // 모달은 안보이게 설정
            setCancelModalVisible(false);
        } catch (error) {
            console.error("Error approving user:", error);
        }
    };


    // 어떤 부모에도 속하지 않는 level의 댓글 작성 후 업로드
    const handleCommentSubmit = async () => {

        if (commentText.trim() === '') {
            message.warning("댓글 내용을 입력하세요.");
            return;
        }

        try {
            const response = await request('POST', `/registerCommentsInStudy/${studyId}`, {
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
            const response = await request('POST', `/registerCommentsInStudy/${studyId}`, {
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
                studyId: studyId,
                page: currentPage, // 몇번째 댓글부터
                size: pageSize // 몇번째 댓글까지 가져올건지 설정
            });

            const response = await request('GET', `/getCommentDataInStudy?${queryParams}`);


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


    // 답글 숨기기, 답글 보기 관련
    const toggleReplyVisibility = (commentId) => {
        setReplyVisibility((prevState) => ({
            ...prevState,
            [commentId]: !prevState[commentId],
        }));
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

    ////////////////// 댓글 , 답글 수정 관련 ////////////////

    // 댓글 수정 버튼을 눌렀을 때
    const handleEditComment = (commentId, commentText) => {

        setEditingCommentId(commentId); // 댓글 수정할 댓글 id를 세팅
        setEditedCommentText(commentText); // 해당 댓글의 내용을 editedCommentText에 설정
    };

    // 수정 - 취소 버튼을 눌렀을 때
    const handleCancelEditComment = () => {
        setEditingCommentId(null);
        setEditedCommentText('');
    };

    // 수정 완료 버튼을 누르면, 해당 댓글 또는 답글의 id와 최상위 여부 값을 전달받음
    const showCommentEditConfirmModal = (commentId, isTopLevel) => {

        setIsTopLevelUsedByEditing(isTopLevel) // 댓글, 답글 수정 기능에서 사용되는 최상위 부모 여부 값을 상태에 저장
        setEditingCommentId(commentId); // 현재 수정하고자 하는 댓글, 답글의 id값을 상태에 저장
        setCommentEditConfirmModalVisible(true); // 댓글, 답글 수정 관련 모달을 활성화

    }

    // 수정 완료 모달에서 ok 버튼을 눌렀을 때
    const handleCommentEditModalOk = () => {
        setCommentEditConfirmModalVisible(false); // 모달 렌더링을 비활성화
        handleEditCommentSubmit(editingCommentId, isTopLevelUsedByEditing); // 백엔드에 해당 댓글 또는 답글의 수정 요청을 보냄
    }

    // 수정 완료 모달에서 cancel 버튼을 눌렀을 때
    const handleCommentEditModalCancel = () => {
        setCommentEditConfirmModalVisible(false); // 모달 렌더링을 비활성화
    }

    // 수정 완료 버튼을 눌렀을 때- 백엔드에 요청 보내기
    const handleEditCommentSubmit = async (commentId, isTopLevel) => {

        // 수정된 댓글 내용을 백엔드로 전송하는 로직을 추가
        if (editedCommentText.trim() === '') {
            if (isTopLevel === true) {
                message.warning("댓글 내용을 입력하세요.");
            }

            else {
                message.warning("답글 내용을 입력하세요.");
            }

            return;
        }

        try {
            await request('PUT', `/updateComments/${commentId}`, {
                content: editedCommentText, // 답글 내용
            });


            // 수정 상태 초기화
            setEditingCommentId(null);
            setEditedCommentText('');
            setIsTopLevelUsedByEditing(null);

            if (isTopLevel === true) {
                message.success("댓글이 성공적으로 수정 되었습니다.");
            }
            else {
                message.success("답글이 성공적으로 수정 되었습니다.");
            }


            fetchCommentData(); // 댓글 수정이 완료되었다면, 최근 수정된 댓글이 반영된 결과를 다시 조회해옴

        } catch (error) {
            console.error("수정에 실패했습니다. 잠시 후 다시 시도하세요.", error);
            if (isTopLevel === true) {
                message.error("댓글 수정에 실패했습니다. 잠시 후 다시 시도하세요.");
            }
            else {
                message.error("답글 수정에 실패했습니다. 잠시 후 다시 시도하세요.");
            }


        }
    };

    ////////////////////////////////////////////////////////

    ////////////////// 댓글 , 답글 삭제 관련 ////////////////

    // 댓글의 삭제 버튼 누르면, 삭제 관련 모달을 띄우기
    const showCommentDeleteConfirmModal = (commentId, isTopLevel) => {

        setIsTopLevelUsedByDelete(isTopLevel); // 댓글, 답글 삭제 기능에서 사용되는 최상위 부모 여부 값을 상태에 저장
        setDeleteCommentId(commentId); // 현재 삭제하고자 하는 댓글, 답글의 id값을 상태에 저장
        setCommentDeleteConfirmModalVisible(true); // 댓글, 답글 삭제 관련 모달을 활성화

    };

    // 삭제 모달에서 ok 버튼을 눌렀을 때
    const handleCommentDeleteModalOk = () => {
        setCommentDeleteConfirmModalVisible(false); // 모달 렌더링을 비활성화
        handleDeleteComment(deleteCommentId, isTopLevelUsedByDelete); // 백엔드에 해당 댓글 또는 답글의 삭제 요청을 보냄
    }

    // 삭제 모달에서 cancel 버튼을 눌렀을 때
    const handleCommentDeleteModalCancel = () => {
        setCommentDeleteConfirmModalVisible(false); // 모달 렌더링을 비활성화
    }

    // 댓글 또는 답글의 삭제 요청을 백엔드에 보내기
    const handleDeleteComment = async (commentId, isTopLevel) => {
        try {
            const response = await request('POST', `/deleteComments/${commentId}`);
            if (response.status === 200) {

                if (isTopLevel === true) {
                    message.success("댓글이 삭제되었습니다.");
                }
                else {
                    message.success("답글이 삭제되었습니다.");
                }

                setIsTopLevelUsedByDelete(null);
                fetchCommentData(); // 삭제 완료 후 다시 최신 댓글 정보 받아옴
            }
        } catch (error) {
            console.error("삭제에 실패했습니다.", error);
            if (isTopLevel === true) {
                message.error("댓글 삭제에 실패했습니다.");
            }
            else {
                message.error("답글 삭제에 실패했습니다.");
            }


        }
    };
    ////////////////////////////////////////////////////////


    // 댓글의 렌더링 관련
    // 부모면 상위 level에 세팅,
    // 자식이면 계속 하위 level을 타고 들어가 세팅
    const renderComments = (comments, depth = 0) => {

        console.log('com', comments);

        return comments.map((comment) => (

            <div className={`comment-container depth-${depth}`}>
                <div className="comment-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                        <Link
                            to={`/portfolio/${data.nickName}`}

                            className="hoverable-item"
                            onMouseEnter={handleMouseEnter}
                            onMouseLeave={handleMouseLeave}
                            style={linkStyle}
                        >
                            <img
                                style={{ borderRadius: '50%', width: '40px', height: '40px', border: '3px solid salmon', marginRight: '10px' }}
                                src={`https://storage.googleapis.com/hongik-pickme-bucket/${comment.imageUrl}`}
                            />
                            <strong>{comment.nickName}</strong>
                        </Link>
                    </div>

                    {comment.commentWriter && (
                        <div style={{ display: 'flex', textAlign: 'right' }}>
                            <Button size="small" type="text" style={{ color: 'black' }} onClick={() => showReplyInput(comment.id)}>답글 달기</Button>
                            {editingCommentId === comment.id ? (
                                // 수정 중일 때, Input으로 표시하고 수정 관련 버튼 표시


                                <Button size="small" type="text" style={{ color: 'black' }} onClick={handleCancelEditComment}>취소</Button>

                            ) : (
                                // 수정 중이 아닐 때, "수정" 버튼 표시
                                <Button size="small" type="text" style={{ color: 'black' }} onClick={() => handleEditComment(comment.id, comment.content)}>수정</Button>
                            )}
                            {/* <Button size="small" onClick={() => handleDeleteComment(comment.id)}>삭제</Button> */}
                            <Button size="small" type="text" style={{ color: 'black' }} onClick={() => showCommentDeleteConfirmModal(comment.id, comment.isTopLevel)}>삭제</Button>
                        </div>
                    )}
                    {!comment.commentWriter && (
                        <div style={{ display: 'flex' }}>
                            <Button size="small" type="text" style={{ color: 'black', marginLeft: '2px' }} onClick={() => showReplyInput(comment.id)}>답글 달기</Button></div>
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
                            style={{ marginTop: '10px' }}
                            placeholder="Edit your comment"

                        />
                        <div style={{ marginBottom: '16px', textAlign: 'right', marginTop: '16px' }}>
                            {/* <Button size="small" onClick={() => handleEditCommentSubmit(comment.id)}>수정 완료</Button> */}
                            <Button size="small" onClick={() => showCommentEditConfirmModal(comment.id, comment.isTopLevel)}>
                                수정 완료
                            </Button>
                        </div>
                    </>
                ) : (
                    // 수정 중이 아닐 때, <p>로 표시
                    <p style={{ marginTop: '5px', whiteSpace: 'pre-wrap' }}>
                        {comment.content && comment.content.split('\n').map((line, index) => (
                            <React.Fragment key={index}>
                                {insertLineBreaks(line, 45)}
                                {index < comment.content.split('\n').length - 1 && <br />}
                            </React.Fragment>
                        ))}
                    </p>
                )}

                <div style={{ textAlign: 'right', marginTop: '5px', fontSize: '12px', color: 'gray', marginRight: '10px' }}>
                    {formatDateTime(comment.finalCommentedTime)}
                    <hr />
                </div>
                {replyToCommentId === comment.id && ( // 답글 달기 버튼 누른 부모 댓글 아래에 답글 작성할 폼 세팅
                    <div className={`reply-container depth-${depth + 1}`} style={{ display: 'flex', alignItems: 'center', marginTop: '5px', marginBottom: '20px' }}>
                        <img
                            style={{ borderRadius: '50%', width: '40px', height: '40px', border: '3px solid salmon', marginRight: '10px' }}
                            src={`https://storage.googleapis.com/hongik-pickme-bucket/${profileImage}`}
                        />
                        <p style={{ marginRight: '10px' }}><strong>{currentUserNickName}</strong></p>
                        <TextArea
                            autoSize={{ minRows: 3 }}
                            type="text"
                            value={replyText}
                            onChange={(e) => setReplyText(e.target.value)}
                            placeholder="Write a reply"
                            style={{ marginBottom: '16px' }}
                        />
                        <Button size="small" onClick={() => handleReplySubmit(comment.id)} style={{ marginLeft: '5px', marginRight: '5px', marginTop: '33px' }} >답글 등록</Button>
                        <Button size="small" style={{ marginTop: '33px' }} onClick={cancelReply} >취소</Button> {/* 취소 버튼 추가 */}
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

        ));
    };

    // 글 작성자라면, 지원자 목록을 볼 수 있는 토글 버튼 만들기
    const renderApplicantButton = () => {
        return (
            <div>
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginRight: '5%', marginTop: '10px' }}>
                    <Button onClick={toggleContent}>
                        {isApplicantOpen ? '지원자 목록 닫기' : '지원자 목록 열기'}
                    </Button>
                </div>
                <div style={{ marginLeft: '15%', marginRight: '5%' }}>
                    {isApplicantOpen && applicantData.length > 0 && // applicantData가 비어있지 않을 때 렌더링
                        <div>
                            {applicantData.map((applicant, index) => (
                                <div key={index}>
                                    <Divider />
                                    <Row>
                                        <Col span={8} onClick={() => handleNickNameClick(applicant.nickName)} style={{ cursor: 'pointer' }}>
                                            {applicant.nickName}
                                        </Col>
                                        <Col span={8} style={{ display: 'flex', justifyContent: 'center' }}>
                                            <Button size="small" onClick={() => handlePortfolioClick(applicant.nickName)}>
                                                포트폴리오
                                            </Button>
                                        </Col>
                                        <Col span={8} style={{ display: 'flex', justifyContent: 'center' }}>
                                            {applicant.confirm ? (
                                                <Button
                                                    size="small"
                                                    onClick={() => {
                                                        setApplyUserNickname(applicant.nickName); // 승인 취소 대상 유저의 닉네임 저장
                                                        setCancelModalVisible(true);
                                                    }}
                                                >
                                                    승인 취소
                                                </Button>
                                            ) : (
                                                <Button
                                                    size="small"
                                                    onClick={() => {
                                                        setApplyUserNickname(applicant.nickName);
                                                        setApproveModalVisible(true);
                                                        if (data.counts === data.recruitmentCount) {
                                                            message.warning('정원이 모두 찼습니다!');
                                                        }
                                                    }}
                                                >
                                                    승인
                                                </Button>
                                            )}
                                        </Col>
                                    </Row>
                                    <Divider />
                                </div>
                            ))}
                        </div>
                    }
                </div>
                <Modal
                    title="유저 승인"
                    open={approveModalVisible}
                    onOk={() => handleApproveUser(applyUserNickName, studyId)}
                    onCancel={() => setApproveModalVisible(false)}
                    okText="예"
                    cancelText="아니오"
                >
                    <p>{applyUserNickName} 님을 승인하시겠습니까?</p>
                </Modal>
                <Modal
                    title="유저 승인 취소"
                    open={cancelModalVisible} // visible로 모달 열림 여부 설정
                    onOk={() => handleCancelApproval(applyUserNickName, studyId)} // 취소 버튼을 누르면 모달 닫기
                    onCancel={() => setCancelModalVisible(false)} // "예" 버튼을 누르면 승인 취소 동작 처리 함수 호출
                    okText="예"
                    cancelText="아니오"
                >
                    <p>{applyUserNickName} 님을 승인 취소하시겠습니까?</p>
                </Modal>
            </div>
        )
    }

    // 지원자 목록 열기 || 지원자 목록 닫기 버튼을 클릭했을 때 텍스트와 내용을 토글
    const toggleContent = () => {
        setIsApplicantOpen(!isApplicantOpen);
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
                    {/* <Button onClick={handleGoBackClick}>
                        목록으로 돌아가기
                    </Button> */}
                </Col>
                <Col span={12} style={{ textAlign: 'right' }}>
                    {/** isWriter와 일반 유저가 보이는 버튼이 다르도록 설정 */}
                    {isWriter && (
                        <div style={{ marginTop: '10px', marginBottom: '10px' }}>
                            <Button type="primary" onClick={() => navigate(`/study/update/${studyId}`)} style={{ marginRight: '5px' }}>
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
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
                                <Button type="primary" onClick={() => showScrapModal('scrap')} style={{ marginRight: '5px' }}>
                                    게시물 스크랩
                                </Button>
                                <Button type="text" disabled>
                                    모집 마감
                                </Button>
                            </div>
                        ) : (
                            // 근데 만약, 정원이 안찼다면 지원하기 버튼 클릭 가능
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
                                <Button type="primary" onClick={() => showScrapModal('cancelScrap')} style={{ marginRight: '5px' }}>
                                    스크랩 취소
                                </Button>
                                <Button type="text" disabled>
                                    모집 마감
                                </Button>
                            </div>
                        ) : (
                            // 근데 만약, 정원이 안찼다면 지원하기 버튼 클릭 가능
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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
                            <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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
                        <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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
                        <div style={{ marginTop: '10px', marginBottom: '10px' }}>
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

    const renderPost = (data) => {
        return (
            <div>
                <Card>
                    <div style={{ display: 'grid', marginLeft: '10px', marginRight: '10px' }}>
                        <div >
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <div style={{ display: 'grid' }}>
                                    <div style={{ fontSize: '25px', fontWeight: 'bold' }}>
                                        {data.title}
                                    </div>
                                    <strong style={{ display: 'flex', marginTop: '10px', fontSize: '12px' }}>

                                        {data.web && <span style={{ ...categoryTagStyle, backgroundColor: '#fee5eb' }}>#WEB</span>}
                                        {data.app && <span style={{ ...categoryTagStyle, backgroundColor: '#fee5eb' }}>#APP</span>}
                                        {data.game && <span style={{ ...categoryTagStyle, backgroundColor: '#fee5eb' }}>#GAME</span>}
                                        {data.ai && <span style={{ ...categoryTagStyle, backgroundColor: '#fee5eb' }}>#AI</span>}
                                    </strong>

                                    <div style={{ marginTop: '25px', display: 'flex', alignItems: 'center' }}>
                                        <Link
                                            to={`/portfolio/${data.nickName}`}

                                            className="hoverable-item"
                                            onMouseEnter={handleMouseEnter}
                                            onMouseLeave={handleMouseLeave}
                                            style={linkStyle}
                                        >
                                            <img
                                                style={{ borderRadius: '50%', width: '40px', height: '40px', border: '3px solid salmon', marginRight: '10px' }}
                                                src={`https://storage.googleapis.com/hongik-pickme-bucket/${data.imageUrl}`}
                                            />

                                            <strong>{data.nickName}</strong>
                                        </Link>
                                    </div>

                                </div>
                                <div style={{ alignItems: 'center' }}>
                                    조회 수 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{data.viewCount}
                                    <br />
                                    모집 인원 &nbsp;{data.counts} / {data.recruitmentCount}
                                    <br />
                                    모집 기한 &nbsp;{formatDate(data.endDate)}
                                    <br />
                                    <br />
                                    <br />
                                    <div style={{ color: 'gray' }}>{formatDateTime(data.finalUpdatedTime)}</div>
                                </div>
                            </div>
                            <hr></hr>
                            <div>

                                {data.fileUrl && data.fileUrl.length >= 1 ? (
                                    <Card style={{ borderborderRadius: '0px', border: 'none' }} size='small' title={`첨부파일`} bodyStyle={{ paddingTop: '0px', paddingBottom: '0px', paddingRight: '0px', paddingLeft: '0px' }} headStyle={{ borderRadius: '0px', background: '#fee5eb' }}>
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

                            <div style={{ display: 'grid', marginTop: '20px' }}>
                                <Card style={{ borderborderRadius: '0px', border: 'none' }} size='small' title={`스터디 소개`} headStyle={{ borderRadius: '0px', background: '#fee5eb' }} bodyStyle={{ minHeight: '250px', paddingTop: '0px', paddingBottom: '10px' }} >
                                    <div style={{ marginTop: '20px' }}>
                                        {data.content && data.content.split('\n').map((line, index) => (
                                            <React.Fragment key={index}>
                                                {insertLineBreaks(line, 45)}
                                                {index < data.content.split('\n').length - 1 && <br />}
                                            </React.Fragment>
                                        ))}
                                    </div>
                                </Card>

                                <div style={{ marginTop: '20px' }} >
                                    {data.promoteImageUrl && data.promoteImageUrl.length >= 1 ? (
                                        <Card style={{ borderborderRadius: '0px', border: 'none' }} size='small' title={`홍보 사진`} bodyStyle={{ borderRadius: '0px', paddingTop: '0px', paddingBottom: '0px', paddingRight: '0px', paddingLeft: '0px' }} headStyle={{ borderRadius: '0px', background: '#fee5eb' }}>

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
                                <hr></hr>
                            </div>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <div style={{ marginBottom: '30px' }}>
                                <strong>댓글</strong>
                            </div>
                            <div>
                                <Button size="small" onClick={toggleCommentsVisibility}>
                                    {areCommentsVisible ? '댓글 숨기기' : '모든 댓글 보기'}
                                </Button>
                            </div>
                        </div>
                        <div style={{ display: 'grid', marginLeft: '10px', marginRight: '10px' }}>
                            {areCommentsVisible && (
                                <div>
                                    {renderComments(commentData.content)}
                                    <div style={{ textAlign: 'center', margin: '20px 0' }}>
                                        {moreCommentsAvailable && (
                                            <Button size="small" onClick={loadMoreComments}>
                                                댓글 더보기
                                            </Button>
                                        )}
                                    </div>

                                    <div style={{ display: 'flex', alignItems: 'center', marginBottom: '12px' }}>
                                        <img
                                            style={{ borderRadius: '50%', width: '40px', height: '40px', border: '3px solid salmon', marginRight: '10px' }}
                                            src={`https://storage.googleapis.com/hongik-pickme-bucket/${profileImage}`}
                                        />
                                        <p style={{ margin: '0' }}><strong>{currentUserNickName}</strong></p>
                                    </div>
                                    <TextArea
                                        autoSize={{ minRows: 4 }}
                                        value={commentText}
                                        onChange={(e) => setCommentText(e.target.value)}
                                        placeholder="Write a comment"
                                    />
                                    <div style={{ textAlign: 'right', marginTop: '16px' }}>
                                        <Button size="small" onClick={handleCommentSubmit}>등록</Button>
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
                        </div>
                    </div>
                </Card>
            </div>

        )
    };

    return (
        <div>
            {data.writer !== null ? (
                // data.writer가 null이 아닌 경우 (게시물이 존재하는 경우)
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    {/** flex : space-between에서, 각 항목끼리 화면에서 차지하는 비중을 결정하는 style */}
                    <div style={{ flex: 1 }}>
                        {/** 좌측 여백을 위해 만든 더미 div */}
                    </div>

                    <div style={{ flex: 2.5 }}>
                        {/** 게시물 작성자에게만 보이는 화면. 우측 상단에 게시물 수정, 삭제 버튼이 보임. */}
                        {/* data.writer && renderButtons() */}
                        {/* {renderButtons()} */}
                        {/** 게시물을 작성하지 않은 유저에게만 보이는 화면. 우측 상단에 스크랩 버튼과 지원 버튼이 보임. */}
                        {!data.writer && !data.scrap && !data.applying && !data.applied && renderButtons()}    {/** 지원 안한 사람 + 스크랩 안한 사람 */}
                        {!data.writer && data.scrap && !data.applying && !data.applied && renderButtons()}    {/** 지원 안한 사람 + 스크랩 한 사람 */}
                        {!data.writer && !data.scrap && data.applying && !data.applied && renderButtons()}     {/** 지원 O 승인 X인 사람 (승인 대기 중) + 스크랩 안한 사람 */}
                        {!data.writer && data.scrap && data.applying && !data.applied && renderButtons()}     {/** 지원 O 승인 X인 사람 (승인 대기 중) + 스크랩 한 사람 */}
                        {!data.writer && !data.scrap && !data.applying && data.applied && renderButtons()}     {/** 승인 O인 사람 (승인 완료) + 스크랩 안한 사람 */}
                        {!data.writer && data.scrap && !data.applying && data.applied && renderButtons()}     {/** 승인 O인 사람 (승인 완료) + 스크랩 한 사람 */}

                        {renderPost(data)}

                        {/* Modal */}
                        <Modal
                            title="Confirm Action"
                            open={isModalVisible}
                            onOk={handleModalConfirm}
                            onCancel={handleModalCancel}
                            okText="예"
                            cancelText="아니오"
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
                            onOk={handleCancelModalConfirm}
                            onCancel={handleCancelModalCancel}
                            okText="예"
                            cancelText="아니오"
                        >
                            <p>지원을 취소하시겠습니까?</p>
                        </Modal>
                        <Modal
                            title="Confirm Action"
                            open={isScrapModalVisible}
                            onOk={handleScrapModalConfirm}
                            onCancel={handleScrapModalCancel}
                            okText="예"
                            cancelText="아니오"
                        >
                            {scrapAction === 'scrap' && (
                                <p>게시물을 스크랩하시겠습니까?</p>
                            )}
                            {scrapAction === 'cancelScrap' && (
                                <p>스크랩을 취소하시겠습니까?</p>
                            )}
                        </Modal>
                        <Modal // 댓글 또는 답글의 수정 완료 버튼 클릭 시 보여지는 모달
                            title={isTopLevelUsedByEditing ? '댓글 수정' : '답글 수정'}
                            open={commentEditConfirmModalVisible}
                            onOk={handleCommentEditModalOk}
                            onCancel={handleCommentEditModalCancel}
                            okText="예"
                            cancelText="아니오"
                        >
                            {isTopLevelUsedByEditing ? '댓글을 수정하시겠습니까?' : '답글을 수정하시겠습니까?'}
                        </Modal>
                        <Modal // 댓글 또는 답글의 삭제 버튼 클릭 시 보여지는 모달
                            title={isTopLevelUsedByDelete ? '댓글 삭제' : '답글 삭제'}
                            open={commentDeleteConfirmModalVisible}
                            onOk={handleCommentDeleteModalOk}
                            onCancel={handleCommentDeleteModalCancel}
                            okText="예"
                            cancelText="아니오"
                        >
                            {isTopLevelUsedByDelete ? '댓글을 삭제하시겠습니까?' : '답글을 삭제하시겠습니까?'}
                        </Modal>
                    </div>

                    <div style={{ flex: 1 }}>
                        {data.writer && renderApplicantButton()}
                    </div>
                </div>
            ) : (
                // data.writer가 null인 경우 (게시물이 없는 경우)
                <div style={{ marginLeft: '15%', marginRight: '15%' }}>
                    <h2>해당하는 스터디 게시물이 없습니다!</h2>
                </div>
            )}
        </div>
    )
}

export default DetailStudyPage;