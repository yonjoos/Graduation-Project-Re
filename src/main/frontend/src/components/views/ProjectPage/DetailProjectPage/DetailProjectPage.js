import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router"; // Import useParams
import { request } from '../../../../hoc/request';
import { Divider, Row, Col, Button } from 'antd';
import '../ProjectPage.css';

function DetailProjectPage() {
    const navigate = useNavigate();
    const { projectId } = useParams(); // Get the project ID from the URL

    const [data, setData] = useState({}); // State to hold project details

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
    

    return (
        <div>
            <div style={{ marginLeft: '10%', marginRight: '10%' }}>
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

                <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
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

            </div>
        </div>
    )
}

export default DetailProjectPage;