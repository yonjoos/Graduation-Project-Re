import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';
import { uploadPortfolioSuccess, deletePortfolioSuccess } from '../../../_actions/actions';
import { useState, useEffect } from 'react';
import { Card, Row, Col, Button } from 'antd';
import { request } from '../../../hoc/request';

function PortfolioPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const [data, setData] = useState(null);
    const userPortfolio = useSelector(state => state.userPortfolio);

    // 포트폴리오 저장 상태를 리덕스로 가져와서
    // 포트폴리오가 비어있으면 업로드 버튼 안보이고, 수정 버튼이 보여야 함

    // PortfolioPage에 들어오면, Get방식으로 백엔드에서 데이터를 가져와서 data에 세팅한다.
    useEffect(() => {
        request('GET', '/getPortfolio', {})
            .then((response) => {
                setData(response.data);
                // dispatch를 통해 현재 상태를 세팅해줘야 F5 눌렀을 때 에러가 안남!!
                if (response.data.isCreated) {
                    dispatch(uploadPortfolioSuccess(true));
                } else {
                    dispatch(deletePortfolioSuccess());
                }
            })
            .catch((error) => {
                console.error("Error fetching data:", error);
            });
    }, [dispatch]);

    const onClickUploadHandler = () => {
        navigate('/portfolio/upload');
    }

    const onClickUpdateHandler = () => {
        navigate('/portfolio/update');
    }

    const onClickDeleteHandler = () => {
        navigate('/portfolio/delete');
    }

    return (
        <div>
            {/** 포트폴리오가 없는 사람에게는 업로드 버튼만 보이게 */}
            {!userPortfolio && (
            <div>
                <h2>
                    This page is for users who don't have a portfolio.
                </h2>
                <br/>
                <br/>
                <div style={{ display : 'flex', justifyContent : 'center'}}>
                    <Button type="primary" onClick={onClickUploadHandler}>
                        포트폴리오 업로드
                    </Button>
                </div>
            </div>)}



            {/** 포트폴리오가 있는 사람에게는 업데이트, 삭제 버튼이 보이게 */}
            {userPortfolio && (
                <div>
                    <h2>
                        This page is for users with a portfolio.
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
                    <div style={{ display: 'flex', justifyContent: 'center'}}>
                        <Button type="primary" style={{ marginRight: '10px' }} onClick={onClickUpdateHandler}>
                            포트폴리오 수정
                        </Button>
                        <Button type="primary" style={{ marginLeft: '10px' }} onClick={onClickDeleteHandler}>
                            포트폴리오 삭제
                        </Button>
                    </div>
                </div>
            )}
    </div>
    )
}

export default PortfolioPage;