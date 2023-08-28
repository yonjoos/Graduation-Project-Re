import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';
import { uploadPortfolioSuccess, deletePortfolioSuccess } from '../../../_actions/actions';
import { useState, useEffect } from 'react';
import { Card, Row, Col, Button, Radio, Progress, Modal, message } from 'antd';
import { request, setHasPortfolio } from '../../../hoc/request';

function PortfolioPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const userPortfolio = useSelector(state => state.userPortfolio);
    const [isDeleteModalVisible, setIsDeleteModalVisible] = useState(false);    // 모달이 보이는지 안보이는지 설정하기 위한 애

    const [data, setData] = useState(null);
    const [existingPreferences, setExistingPreferences] = useState({
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });


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

    // 선호도 그래프 관련
    const renderPreferenceBar = (field) => {
        const preferenceValue = data && existingPreferences[field];
        return (
            <div style={{ marginBottom: '10px' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '5px' }}>
                    <div style={{ width: '100px', textAlign: 'left', marginRight: '10px' }}>{field}:</div>
                    <Progress percent={preferenceValue * 25} showInfo={false} strokeColor={getBarColor(field)} />
                </div>
            </div>
        );
    };

    // 선호도 그래프 관련
    const getBarColor = (field) => {
        if (field === "web") {
            return '#FE708F';
        } else if (field === "app") {
            return '#f9f56e';
        } else if (field === "game") {
            return '#83edff';
        } else {
            return '#91e2c3';
        }
    };

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


    const showDeleteModal = () => {
        setIsDeleteModalVisible(true);
    };

    const hideDeleteModal = () => {
        setIsDeleteModalVisible(false);
    };

    const handleDelete = () => {
        request('POST', '/deletePortfolio', {}) // Adjust the endpoint accordingly
            .then((response) => {
                alert('포트폴리오 삭제가 완료되었습니다.'); // 삭제 성공 메시지 띄우기
                setHasPortfolio(false);                     // 포트폴리오를 삭제했으므로, 포트폴리오 상태를 false로 변경
                dispatch(deletePortfolioSuccess()); // Dispatch를 통해 deletePortfolioSuccess()를 실행하고, 상태를 변경
                navigate('/portfolio'); // Redirect or perform any other action
            })
            .catch((error) => {
                console.error("Error deleting portfolio:", error);
                message.warning('포트폴리오 삭제에 실패했습니다.');
            });

        hideDeleteModal();
    };

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
                    <hr style={{ marginLeft: '15%', marginRight: '15%', borderBottom: '2px solid black' }} />

                    <div style={{ marginLeft: '20%', fontSize: '15px' }}><strong>첨부 파일:</strong> {data && data.fileUrl}</div>

                    <hr style={{ marginLeft: '15%', marginRight: '15%', borderBottom: '2px solid black' }} />

                    <Row justify="center" style={{ marginTop: '20px' }}>
                        <Col span={16}>
                            <Row>
                                <Col span={12}>
                                    <Card title="관심 분야" style={{ height: '100%' }}>
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
                                <Col span={12}>
                                    <Card title="관심 분야 선호도 그래프" style={{ height: '100%' }}>
                                        {renderPreferenceBar('web')}
                                        {renderPreferenceBar('app')}
                                        {renderPreferenceBar('game')}
                                        {renderPreferenceBar('ai')}
                                    </Card>

                                </Col>
                            </Row>
                        </Col>
                    </Row>

                    <Row justify="center">
                        <Col span={16}>
                            <Card title="한 줄 소개">
                                <p>{data && data.shortIntroduce}</p>
                            </Card>
                        </Col>
                    </Row>

                {/**멀티라인 콘텐츠를 데이터베이스에 저장된 대로 프론트엔드에서 줄바꿈(새 줄 문자)을 포함하여 표시하려면
                 *  <pre> HTML 태그나 CSS 스타일을 사용하여 공백 및 줄바꿈 형식을 보존할 수 있다.
                 * 
                 * <Row justify="center">
                 *     <Col span={16}>
                 *         <Card title="한 줄 소개">
                 *             //<pre> 태그를 사용하여 형식과 줄바꿈을 보존합니다
                 *             <pre>{data && data.introduce}</pre>
                 *         </Card>
                 *     </Col>
                 * </Row>
                 *
                 * 
                 * 스타일링에 대한 더 많은 제어를 원하는 경우 CSS를 사용하여 동일한 효과를 얻을 수 있다.
                 * 즉, style={{ whiteSpace: 'pre-wrap' }} 을 사용한다.
                 *  */}
                    <Row justify="center">
                        <Col span={16}>
                            <Card title="한 줄 소개">
                                <div style={{ whiteSpace: 'pre-wrap' }}>{data && data.introduce}</div>
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
                            <Button type="primary" style={{ marginLeft: '10px' }} onClick={showDeleteModal}>
                                포트폴리오 삭제
                            </Button>
                        </Col>
                    </Row>

                    {/* 삭제 모달 */}
                    <Modal
                        title="포트폴리오 삭제"
                        open={isDeleteModalVisible}
                        onCancel={handleDelete}
                        onOk={hideDeleteModal}
                        okText="아니오"
                        cancelText="예"
                    >
                        <p>정말로 포트폴리오를 삭제하시겠습니까?</p>
                    </Modal>
                </div>
            )}
        </div>
    );
}

export default PortfolioPage;