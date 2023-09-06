import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';
import { uploadPortfolioSuccess, deletePortfolioSuccess } from '../../../../_actions/actions';
import { useState, useEffect } from 'react';
import { Card, Row, Col, Button, Radio, Progress, Modal, message } from 'antd';
import { request, setHasPortfolio } from '../../../../hoc/request';

function MyPortfolioPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
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

                console.log('hihihi', response.data);
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


    // 포트폴리오 업로드 버튼 클릭 시 해당 엔드포인터로 이동
    const onClickUploadHandler = () => {
        navigate('/portfolio/upload');
    }

    // 포트폴리오 업데이트 버튼 클릭 시 해당 엔드포인터로 이동
    const onClickUpdateHandler = () => {
        navigate('/portfolio/update');
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
                navigate('/'); // Redirect or perform any other action
            })
            .catch((error) => {
                console.error("Error deleting portfolio:", error);
                message.warning('포트폴리오 삭제에 실패했습니다.');
            });

        hideDeleteModal();
    };

    return (
        // 포트폴리오 업로드 후 F5를 누르지 않으면 데이터가 들어오지 않는 문제를 data 안에 들어있는 isCreated사용과 삼항 연산자를 통해 직접적으로 해결.
        <div>
            {/** 아직 포트폴리오를 만들지 않았다면? */}
            {data && !data.isCreated ? (
                <div>
                    <h2>아직 포트폴리오가 작성되지 않았습니다.</h2>
                    <h2>포트폴리오를 만들어주세요!!</h2>
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
            ) : (
                <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px', marginLeft: '20%', marginBottom: '20px' }}>
                        <div>
                            <div style={{ fontSize: '35px' }}>
                                <strong>Welcome To</strong> <i>{data && data.nickName}</i> <strong>'s page ❤️‍🔥</strong>
                                {/* 
                                        == 변경사항 ==
                                        상단 <Divider> 제거, 선이 너무 많음
                                        하단 <hr> 제거, 같은 이유
                                    
                                */}
                            </div>
                            <div style={{ fontSize: '12px' }}><strong>CONTACT :</strong> {data && data.email}</div>
                        </div>
                    </div>

                    {/**  borderBottom: '3px solid black'은 <hr> 요소 하단에 검은색 실선 테두리를 추가하여 더 두껍고 굵게 표시합니다. '3px' 값을 조정하여 원하는 대로 두껍거나 얇게 만들 수 있습니다. */}
                    <hr style={{ marginLeft: '15%', marginRight: '15%', borderBottom: '0.1px solid black' }} />

                    <div style={{ marginLeft: '20%', fontSize: '12px' }}><strong>첨부 파일:</strong> {data && data.fileUrl}</div>

                    <Row justify="center" style={{ marginTop: '20px' }}>
                        <Col span={16}>
                            <Row>
                                <Col span={14}>
                                    <Card title="ABOUT" style={{ height: '100%' }}>
                                    <h6>Nick Name</h6>
                                        {data && data.nickName}
                                        {
                                            /*
                                            === 변경사항 ===
                                            data null 오류 : data && 추가해줌
                                            */
                                        }
                                        <br></br>
                                        <br></br>
                                        <h6>Brief Introduction</h6>
                                        {data && data.shortIntroduce ? (
                                            data.shortIntroduce
                                        ) : (
                                            <p>No introduction available</p>
                                        )}
                                    </Card>


                                </Col>
                                <Col span={10}>
                                    <Card title="관심 분야 선호도" style={{ height: '100%' }}>
                                        {renderPreferenceBar('web')}
                                        {renderPreferenceBar('app')}
                                        {renderPreferenceBar('game')}
                                        {renderPreferenceBar('ai')}
                                    </Card>

                                </Col>
                            </Row>
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
                            <Card title="경력">
                                <div style={{ whiteSpace: 'pre-wrap' }}>
                                    {/** 받아온 데이터에 공백이 없으면, 40번째 글자 이후에 강제로 공백을 넣어주는 함수 */}
                                    {/** Card안에 데이터를 넣는 경우 발생하는 문제인 것 같음. */}
                                    {data && insertLineBreaks(data.introduce, 45)}
                                </div>
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

export default MyPortfolioPage;