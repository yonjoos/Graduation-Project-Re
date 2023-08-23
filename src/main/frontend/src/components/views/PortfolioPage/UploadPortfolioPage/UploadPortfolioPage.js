import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Row, Col, Input, Button, Radio, message } from 'antd';
import { request } from '../../../../hoc/request';

function PortfolioPage() {
    const navigate = useNavigate();
    
    // 포트폴리오 폼의 상태를 관리할 state 변수들
    const [shortIntroduce, setShortIntroduce] = useState('login'); // 한 줄 소개
    const [introduce, setIntoduce] = useState(''); // 소개 및 커리어
    const [fileUrl, setFileUrl] = useState('');   // 파일 경로

    // 입력 필드 변경 시 호출되는 이벤트 핸들러
    const onChangeHandler = (event) => {
        const name = event.target.name;
        const value = event.target.value;
        
        // 입력 필드마다 해당하는 state 변수를 업데이트
        if (name === 'shortIntroduce') setShortIntroduce(value);
        else if (name === 'introduce') setIntoduce(value);
        else if (name === 'fileUrl') setFileUrl(value);
    };
    
    const [preferences, setPreferences] = useState({
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });

    const handlePreferenceChange = (field, value) => {
        // 0 선택은 중복을 허용. 이외의 값들에 대해서는 중복을 허용하지 않음.
        if (value === 0 || !Object.values(preferences).includes(value)) {
            const newPreferences = { ...preferences, [field]: value };
            setPreferences(newPreferences);
        }
        else {
            message.warning('분야 별로 서로 다른 선호도를 체크해주세요.');
        }
    };

    // 작성한 폼 제출
    const submitPortfolio = (event, web, app, game, ai, shortIntroduce, introduce, fileUrl) => {
        event.preventDefault();

        request('POST', '/uploadPortfolio', {
            web: web,
            app: app,
            game: game,
            ai: ai,
            shortIntroduce: shortIntroduce,
            introduce: introduce,
            fileUrl: fileUrl
        })
            .then((response) => {
                alert('포트폴리오가 성공적으로 생성되었습니다.');
            })
            .catch((error) => {
                alert('포트폴리오 생성에 실패하였습니다.');
            });
    };

    const renderRadioGroup = (field) => (
        <Radio.Group
            value={preferences[field]}
            onChange={(e) => handlePreferenceChange(field, e.target.value)}
        >
            <Radio value={0}>0</Radio>
            <Radio value={1}>1</Radio>
            <Radio value={2}>2</Radio>
            <Radio value={3}>3</Radio>
            <Radio value={4}>4</Radio>
        </Radio.Group>
    );

    // 포트폴리오 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitPortfolio = (e) => {
        e.preventDefault();
        // web, app, game, ai는 그냥 접근할 수 없고, preferences를 통해서 접근
        submitPortfolio(e, preferences.web, preferences.app, preferences.game, preferences.ai, shortIntroduce, introduce, fileUrl);
        navigate('/');
    };

    return (
        <Row justify="center">
            <Col span={12}>
                <form onSubmit={onSubmitPortfolio}>
                    {/** mb-4 : "margin Bottom 4"를 의미하며 요소 하단에 여백을 적용하는 데 사용 */}
                    <p>관심 분야와 선호도를 선택해주세요. 정확한 추천을 위해, 각 분야의 선호도에 순서를 정해주세요. 4가 가장 높은 선호도이고, 0은 관심 없는 분야입니다. 관심 없는 분야(0)는 중복해서 선택할 수 있지만, 이외의 선호도는 중복해서 체크할 수 없습니다. </p>
                    <div className="form-outline mb-4">
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
                    </div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="shortIntroduce"
                            placeholder="한 줄 소개를 작성해주세요."
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="introduce"
                            placeholder="소개 및 커리어를 작성해주세요."
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="fileUrl"
                            placeholder="첨부 파일을 넣어주세요."
                            onChange={onChangeHandler}
                        />
                    </div>
                    <Button type="primary" block htmlType="submit">Create Portfolio</Button>
                </form>
            </Col>
        </Row>

        // <Row justify="center">
        //     <Col span={24}>
        //         {/** antd에서 제공하는 form을 작성하고, 제출할 때에는 onFinish 사용 */}
        //         <Form form={form} name="portfolio-form" onFinish={onFinish}>
        //             <p>관심 분야와 선호도를 선택해주세요. 정확한 추천을 위해, 각 분야의 선호도에 순서를 정해주세요. 4가 가장 높은 선호도이고, 0은 관심 없는 분야입니다. 관심 없는 분야(0)는 중복해서 선택할 수 있지만, 이외의 선호도는 중복해서 체크할 수 없습니다. </p>
        //             <Form.Item>
        //                 <table>
        //                     <tbody>
        //                         <tr>
        //                             <td>Web</td>
        //                             <td>{renderRadioGroup('web')}</td>
        //                         </tr>
        //                         <tr>
        //                             <td>App</td>
        //                             <td>{renderRadioGroup('app')}</td>
        //                         </tr>
        //                         <tr>
        //                             <td>Game</td>
        //                             <td>{renderRadioGroup('game')}</td>
        //                         </tr>
        //                         <tr>
        //                             <td>AI</td>
        //                             <td>{renderRadioGroup('ai')}</td>
        //                         </tr>
        //                     </tbody>
        //                 </table>
        //             </Form.Item>
        //             <Form.Item
        //                 label="한 줄 소개"
        //                 rules={[{ required: true, message: 'Please enter a one-line introduction.' }]}
        //             >
        //                 <Input
        //                     type="text"
        //                     name="shortIntroduce"
        //                     placeholder="한 줄 소개를 작성해주세요."
        //                 />
        //             </Form.Item>
        //             <Form.Item
        //                 name="introduce"
        //                 label="소개"
        //                 rules={[{ required: true, message: 'Please enter an introduction.' }]}
        //             >
        //                 <Input
        //                     type="text"
        //                     name="introduce"
        //                     placeholder="소개 및 커리어를 작성해주세요."
        //                 />
        //             </Form.Item>
        //             <Form.Item
        //                 name="fileUrl"
        //                 label="첨부파일"
        //             >
        //                 <Input
        //                     type="text"
        //                     name="introduce"
        //                     placeholder="첨부 파일을 넣어주세요."
        //                 />
        //             </Form.Item>
        //             <Form.Item>
        //                 <Button type="primary" htmlType="submit">
        //                     Create Portfolio
        //                 </Button>
        //             </Form.Item>
        //         </Form>
        //     </Col>
        // </Row>
    );
}

export default PortfolioPage;
