// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Button } from 'antd';
import { request } from '../../../hoc/request';
import Search from '../../utils/Search';

function ProjectPage() {
    const [data, setData] = useState([]);   // data는 setData에 의해 변경된다.
    const navigate = useNavigate();

    const onClickHandler = () => {
      navigate('/project/upload');
    }

    // ProjectPage로 들어오면 /messages로부터 Get방식으로 데이터를 요청하고, 이를 받아서 Data를 세팅한다.
    useEffect(() => {
        request('GET', '/messages', {})
            .then((response) => {
                setData(response.data);
            })
            .catch((error) => {
                // Handle error, e.g., redirect to login or display an error message
                console.error("Error fetching data:", error);
            });
    }, []);
    

    return (
        <div>
            <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                <Col xs={24} sm={16} md={12} lg={8}>
                    <Card title="Backend Response in Project Page" style={{ width: '100%' }}>
                        <p>Content:</p>
                        <ul>
                            {data.map((line, index) => (
                                <li key={index}>{line}</li>
                            ))}
                        </ul>
                    </Card>
                </Col>
                <Search/>
                <Col>
                    <Button type="primary" onClick={onClickHandler}>
                        Upload Project
                    </Button>
                </Col>
            </Row>
        </div>
    );
}

export default ProjectPage;
