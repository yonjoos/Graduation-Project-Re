import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Divider, Row, Col, Button } from 'antd';
import { request } from '../../../hoc/request';
import Search from '../../utils/Search';
import './ProjectPage.css';

function ProjectPage() {
    const [data, setData] = useState([]); // data is changed by setData.
    const navigate = useNavigate();

    const onClickHandler = () => {
        navigate('/project/upload');
    }
    
    useEffect(() => {
        request('GET', '/getProjectList', {})
            .then((response) => {
                setData(response.data); // Assuming the response.data is an array of objects
            })
            .catch((error) => {
                // Handle error, e.g., redirect to login or display an error message
                console.error("Error fetching data:", error);
            });
    }, [data]);


    // Function to format the date
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // Month is zero-based
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    };

    return (
        <div>
            <Search/>

            {data.map((item, index) => (
                <div key={index} style={{ marginLeft: '10%', marginRight: '10%' }}>
                    {/** 이상하게, antd에서 끌어온 애들은 style = {{}}로 적용이 안되고 css로 적용될 때가 있음 */}
                    <Divider className="bold-divider" />

                    <div>
                        <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                            <Col span={6}>
                                <div style={{ borderRight: '1px' }}>
                                    닉네임: {item.nickName}
                                </div>
                            </Col>
                            {/** 수직선 CSS인 vertical-line을 만들어 주었음 */}
                            <Col span={12} className="vertical-line">
                                <div className="form-outline mb-1" style={{ marginLeft: '3px' }}>
                                    제목: {item.title}
                                </div>
                                <div style={{ marginLeft: '3px' }}>
                                    {/** Boolean으로 반환되는 애들은 삼항연산자를 통해 값을 보여줘야 함 */}
                                    분류: {item.web?"Web ":""}{item.app?"App ":""}{item.game?"Game ":""}{item.ai?"AI ":""}
                                </div>
                            </Col>
                            <Col span={6} className="vertical-line">
                                <div className="form-outline mb-1" style={{ marginLeft: '3px' }}>
                                    모집 인원: {item.recruitmentCount}
                                </div>
                                <div  style={{ marginLeft: '3px' }}>
                                    모집 마감일: {formatDate(item.endDate)}
                                </div>
                            </Col>
                        </Row>
                    </div>

                    <Divider className="bold-divider" />
                </div>
            ))}

            <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                <Button type="primary" onClick={onClickHandler}>
                    Upload Project
                </Button>
            </Row>

        </div>
    );
}

export default ProjectPage;
