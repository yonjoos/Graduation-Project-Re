import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { Card, Row, Col, Button } from 'antd';
import { request } from '../../../hoc/request';

function PortfolioPage() {
    const navigate = useNavigate();
    const [data, setData] = useState(null);

    // 포트폴리오 저장 상태를 리덕스로 가져와서
    // 포트폴리오가 비어있으면 업로드 버튼 안보이고, 수정 버튼이 보여야 함

    // PortfolioPage에 들어오면, Get방식으로 백엔드에서 데이터를 가져와서 data에 세팅한다.
    useEffect(() => {
        request('GET', '/getPortfolio', {})
            .then((response) => {
                setData(response.data);
            })
            .catch((error) => {
                // Handle error, e.g., redirect to login or display an error message
                console.error("Error fetching data:", error);
            });
    }, []);

    const onClickHandler = () => {
        navigate('/portfolio/upload');
    }

    return (
        <div>
            <h2>
                This is a PortfolioPage
            </h2>
            <div>
                <Row justify="center" style={{ marginTop: '20px' }}>
                    <Col xs={24} sm={16} md={12} lg={8}>
                        <Card title="저장된 포트폴리오 정보 백엔드에서 가져오기" style={{ width: '100%' }}>
                            {data && (
                                <ul>
                                    <li><strong>Nick Name:</strong> {data.nickName}</li>
                                    <li><strong>Email:</strong> {data.email}</li>
                                    <li><strong>Web:</strong> {data.web}</li>
                                    <li><strong>App:</strong> {data.app}</li>
                                    <li><strong>Game:</strong> {data.game}</li>
                                    <li><strong>AI:</strong> {data.ai}</li>
                                    <li><strong>ShortIntroduce:</strong> {data.shortIntroduce}</li>
                                    <li><strong>Introduce:</strong> {data.introduce}</li>
                                    <li><strong>FileUrl:</strong> {data.fileUrl}</li>
                                    {/* Add other properties as needed */}
                                </ul>
                            )}
                        </Card>
                    </Col>
                </Row>
            </div>
            <br/>
            <br/>
            <div style={{ display : 'flex', justifyContent : 'center'}}>
                <Button type="primary" onClick={onClickHandler}> 포트폴리오 업로드 </Button>
            </div>
        </div>
    )
}

export default PortfolioPage;