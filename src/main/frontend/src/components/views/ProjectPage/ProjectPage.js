// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Button } from 'antd';
import { request } from '../../../hoc/request';

function ProjectPage() {
    const [data, setData] = useState([]);
    const navigate = useNavigate();

    const onClickHandler = () => {
      navigate('/uploadPost');
    }

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
            <div>
                <Row justify="center" style={{ marginTop: '20px' }}>
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
                </Row>
            </div>

            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>

            {/** justifyContent: 'center'를 적용시키려면 display: 'flex'가 함께 있어야 한다. */}
            <div style = {{ display: 'flex', justifyContent: 'center', }}> 
                <Button type="primary" onClick={onClickHandler}>
                    Upload Post
                </Button>
            </div>
        </div>
    );
}

export default ProjectPage;
