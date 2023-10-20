import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { useDispatch } from "react-redux";
import { request } from '../../../../hoc/request';
import { Divider, Row, Col, Button, Modal, message, Input } from 'antd';

import '../ProjectPage.css';
import './DetailProjectPage.css'; // 댓글의 계층에 따른 왼쪽 여백 css

const { TextArea } = Input;

function DetailProjectNotifyPage() {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const { projectId } = useParams(); // URL로부터 projectId 가져오기
    const [data, setData] = useState({}); // 백엔드에서 가져온 데이터를 세팅
    const [isApplicantOpen, setIsApplicantOpen] = useState(true);   // 초기 상태를 "지원자 목록 열기"로 설정
    const [applicantData, setApplicantData] = useState([]);     // 백엔드에서 가져온 지원자 목록 세팅
    const [approveModalVisible, setApproveModalVisible] = useState(false);    // 유저 승인 모달이 보이는지 여부 설정
    const [cancelModalVisible, setCancelModalVisible] = useState(false);    // 유저 승인 취소 모달이 보이는지 여부 설정
    const [applyUserNickName, setApplyUserNickname] = useState('');




    useEffect(() => {
        // ProjectId를 PathVariable로 보내기
        request('GET', `/getProject/${projectId}`, {})
            .then((response) => {
                //console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // 백엔드에서 받아온 데이터 세팅


                // 게시물의 작성자라면, 지원자를 얻어오는 추가적인 쿼리를 날림
                if (response.data.writer) {
                    request('GET', `/getProjectApplicants/${projectId}`, {})
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

 


    const handleNickNameClick = (nickName) => {

        navigate(`/portfolio/notify/${nickName}`);
    };

    const handlePortfolioClick = (nickName) => {

        navigate(`/portfolio/notify/${nickName}`);
    };

    // 승인하려는 유저의 닉네임(nickName)과 게시물 아이디(postsId)를 받아서 승인 허가
    const handleApproveUser = async (applyUserNickName, projectId) => {
        try {
            const queryParams = new URLSearchParams({
                nickName: applyUserNickName, // 닉네임
                postsId: projectId,   // 게시물 ID
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
    const handleCancelApproval = async (applyUserNickName, projectId) => {
        try {
            const queryParams = new URLSearchParams({
                nickName: applyUserNickName, // 닉네임
                postsId: projectId,   // 게시물 ID
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



    // 글 작성자라면, 지원자 목록을 볼 수 있는 토글 버튼 만들기
    const renderApplicantButton = () => {
        return (
            <div>
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginRight: '5%' }}>
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
                    onOk={() => handleApproveUser(applyUserNickName, projectId)}
                    onCancel={() => setApproveModalVisible(false)}
                    okText="예"
                    cancelText="아니오"
                >
                    <p>{applyUserNickName} 님을 승인하시겠습니까?</p>
                </Modal>
                <Modal
                    title="유저 승인 취소"
                    open={cancelModalVisible} // visible로 모달 열림 여부 설정
                    onOk={() => handleCancelApproval(applyUserNickName, projectId)}
                    onCancel={() => setCancelModalVisible(false)}
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



    return (
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            {/** flex : space-between에서, 각 항목끼리 화면에서 차지하는 비중을 결정하는 style */}
            <div style={{ flex: 1 }}>
                {/** 좌측 여백을 위해 만든 더미 div */}
            </div>
            
            <div style={{ flex: 2.5 }}>
                {/** 승인 대기 중인 사람 */}
                {!data.writer && data.applying && !data.applied && (
                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <Button type="text" disabled style={{ fontWeight: 'bold' }}>
                            승인 대기 중..
                        </Button>
                    </div>
                )}
                {/** 승인 완료된 사람 */}
                {!data.writer && !data.applying && data.applied && (
                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <Button type="text" disabled style={{ fontWeight: 'bold' }}>
                            승인 완료
                        </Button>
                    </div>
                )}

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
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <div style={{ marginLeft: '3px' }}>
                                {/** Boolean으로 반환되는 애들은 삼항연산자를 통해 값을 보여줘야 함 */}
                                분류: &nbsp; {data.web ? " Web " : ""}{data.app ? " App " : ""}{data.game ? " Game " : ""}{data.ai ? " AI " : ""}
                            </div>
                            <div style={{ marginRight: '15px' }}>
                                조회 수: {data.viewCount}
                            </div>
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

            </div>

            <div style={{ flex: 1 }}>
                {data.writer && renderApplicantButton()}
            </div>
        </div>
    )
}

export default DetailProjectNotifyPage;