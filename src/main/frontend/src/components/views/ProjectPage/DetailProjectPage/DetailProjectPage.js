import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router"; // Import useParams
import { request } from '../../../../hoc/request';
import { Divider, Row, Col, Button, Modal } from 'antd';
import '../ProjectPage.css';

function DetailProjectPage() {
    const navigate = useNavigate();
    const { projectId } = useParams(); // Get the project ID from the URL

    const [data, setData] = useState({}); // State to hold project details
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [modalAction, setModalAction] = useState('');

    useEffect(() => {
        // Make a request to fetch the project details using projectId
        request('GET', `/getProject/${projectId}`, {})
            .then((response) => {
                console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // Update the project state
            })
            .catch((error) => {
                console.error("Error fetching project data:", error);
            });
    }, [projectId]);


    // 2023826 -> 2023년 8월 26일 형식으로 변환
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // Month is zero-based
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    };
    

    const showModal = (action) => {
        setIsModalVisible(true);
        setModalAction(action);
    };
    
    const handleModalConfirm = () => {
        if (modalAction === 'delete') {
            request('POST', `/project/delete/${projectId}`, {})
            .then((response) => {
                console.log("Fetched project data:", response.data); // Log the fetched data
                setData(response.data); // Update the project state
            })
            .catch((error) => {
                console.error("Error fetching project data:", error);
            });
            navigate('/project');
        } else if (modalAction === 'apply') {
            // Add code here to handle applying for the project
            // You can show a success message and keep the user on the same page
        }
        setIsModalVisible(false);
    };
    
    const handleModalCancel = () => {
        setIsModalVisible(false);
    };

    // 글 작성자인지, 아닌지에 따라 다르게 보이도록 설정
    const renderButtons = () => {
        const isWriter = data.writer;

        return (
            <Row>
                <Col span={12}>
                    <Button onClick={() => navigate('/project')}>
                        프로젝트 목록
                    </Button>
                </Col>
                <Col span={12} style={{ textAlign: 'right' }}>
                    {/** 삼항 연산자를 통해, isWriter와 일반 유저가 보이는 버튼이 다르도록 설정 */}
                    {isWriter ? (
                        <div>
                            <Button type="primary" onClick={() => navigate(`/project/update/${projectId}`)}>
                                게시물 수정
                            </Button>
                            <Button onClick={() => showModal('delete')}>
                                게시물 삭제
                            </Button>
                        </div>
                    ) : (
                        <Button type="primary" onClick={() => showModal('apply')}>
                            지원하기
                        </Button>
                    )}
                </Col>
            </Row>
        );
    };

    return (
        <div style={{ marginLeft: '10%', marginRight: '10%' }}>
            {/** 게시물 작성자에게만 보이는 화면. 우측 상단데 게시물 수정, 삭제 버튼이 보임. */}
            {data.writer && renderButtons()}
            {!data.writer && renderButtons()}
            
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
                        모집 인원: {data.recruitmentCount}
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

            <div style={{ marginLeft: '5px' }}>
                내용: {data.content}
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
        </div>
    )
}

export default DetailProjectPage;