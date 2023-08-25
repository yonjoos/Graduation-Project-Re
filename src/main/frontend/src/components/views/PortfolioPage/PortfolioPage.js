import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';
import { uploadPortfolioSuccess, deletePortfolioSuccess } from '../../../_actions/actions';
import { useState, useEffect } from 'react';
import { Card, Row, Col, Button, Radio } from 'antd';
import { request } from '../../../hoc/request';

function PortfolioPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const userPortfolio = useSelector(state => state.userPortfolio);

    const [data, setData] = useState(null);
    const [existingPreferences, setExistingPreferences] = useState({
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });

    // 포트폴리오 저장 상태를 리덕스로 가져와서
    // 포트폴리오가 비어있으면 업로드 버튼 안보이고, 수정 버튼이 보여야 함

    // PortfolioPage에 들어오면, Get방식으로 백엔드에서 데이터를 가져와서 data에 세팅한다.
    useEffect(() => {
        request('GET', '/getPortfolio', {})
            .then((response) => {
                setData(response.data);
                setExistingPreferences({
                    web: response.data.web,
                    app: response.data.app,
                    game: response.data.game,
                    ai: response.data.ai
                });
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


    const renderRadioGroup = (field) => (
        <Radio.Group
            value={data && existingPreferences[field]} // Assuming the data structure contains the preference values
            style={{ cursor: 'default' }}
        >
            <Radio value={0}>0</Radio>
            <Radio value={1}>1</Radio>
            <Radio value={2}>2</Radio>
            <Radio value={3}>3</Radio>
            <Radio value={4}>4</Radio>
        </Radio.Group>
    );


    // 포트폴리오 업로드 버튼 클릭 시 해당 엔드포인터로 이동
    const onClickUploadHandler = () => {
        navigate('/portfolio/upload');
    }

    // 포트폴리오 업데이트 버튼 클릭 시 해당 엔드포인터로 이동
    const onClickUpdateHandler = () => {
        navigate('/portfolio/update');
    }

    // 포트폴리오 삭제 버튼 클릭 시 해당 엔드포인터로 이동
    const onClickDeleteHandler = () => {
        navigate('/portfolio/delete');
    }

    return (
        <div>
            {!userPortfolio && (
                <div>
                    <h2>This page is for users who don't have a portfolio.</h2>
                    <br />
                    <br />
                    <Row justify="center">
                        <Col>
                            <Button type="primary" onClick={onClickUploadHandler}>
                                포트폴리오 등록
                            </Button>
                        </Col>
                    </Row>
                </div>
            )}

            {userPortfolio && (
                <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px', marginLeft: '20%', marginBottom: '20px' }}>
                        <div>
                            <div style={{ fontSize: '30px' }}><strong>Email:</strong> {data && data.email}</div>
                            <div style={{ fontSize: '30px' }}><strong>Nick Name:</strong> {data && data.nickName}</div>
                        </div>
                    </div>

                    {/**  borderBottom: '3px solid black'은 <hr> 요소 하단에 검은색 실선 테두리를 추가하여 더 두껍고 굵게 표시합니다. '3px' 값을 조정하여 원하는 대로 두껍거나 얇게 만들 수 있습니다. */}
                    <hr style={{ marginLeft: '15%', marginRight: '15%', borderBottom: '2px solid black' }}/>

                    <div style={{ marginLeft: '20%', fontSize: '15px' }}><strong>첨부 파일:</strong> {data && data.fileUrl}</div>

                    <hr style={{ marginLeft: '15%', marginRight: '15%', borderBottom: '2px solid black' }}/>

                    <Row justify="center" style={{ marginTop: '20px' }}>
                        <Col span={16}>
                            <Card title="관심 분야">
                                <table>
                                    <tbody>
                                        <tr>
                                            <td>Web</td>
                                            <td>{renderRadioGroup('web')}</td>
                                        </tr>
                                        <tr>
                                            <td>App</td>
                                            <td>{renderRadioGroup('app')}</td>
                                        </tr>
                                        <tr>
                                            <td>Game</td>
                                            <td>{renderRadioGroup('game')}</td>
                                        </tr>
                                        <tr>
                                            <td>AI</td>
                                            <td>{renderRadioGroup('ai')}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </Card>
                        </Col>
                    </Row>

                    <Row justify="center">
                        <Col span={16}>
                            <Card title="한 줄 소개">
                                <p>{data && data.shortIntroduce}</p>
                            </Card>
                        </Col>
                    </Row>

                    <Row justify="center">
                        <Col span={16}>
                            <Card title="경력">
                                <p>{data && data.introduce}</p>
                            </Card>
                        </Col>
                    </Row>

                    <br />
                    <br />
                    
                    <Row justify="center">
                        <Col>
                            <Button type="primary" style={{ marginRight: '10px' }} onClick={onClickUpdateHandler}>
                                포트폴리오 수정
                            </Button>
                            <Button type="primary" style={{ marginLeft: '10px' }} onClick={onClickDeleteHandler}>
                                포트폴리오 삭제
                            </Button>
                        </Col>
                    </Row>
                </div>
            )}
        </div>
    );
}

export default PortfolioPage;