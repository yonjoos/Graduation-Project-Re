import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Row, Col, Input, Button, Radio, message } from 'antd';
import { request, setHasPortfolio } from '../../../../hoc/request';
import { uploadPortfolioSuccess } from '../../../../_actions/actions';

function UpdatePortfolioPage() {
    const navigate = useNavigate();
    const dispatch = useDispatch();

    // 기존의 포트폴리오 데이터를 가져오고, 새로운 데이터를 입력할 수 있도록 하기 위한 useState
    const [existingShortIntroduce, setExistingShortIntroduce] = useState('');
    const [existingIntroduce, setExistingIntroduce] = useState('');
    const [existingFileUrl, setExistingFileUrl] = useState('');
    const [existingPreferences, setExistingPreferences] = useState({
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });

    // Fetch existing portfolio data on component mount
    useEffect(() => {
        // Make an API request to fetch existing portfolio data
        // Update state variables with fetched data
        // For example:
        fetchExistingPortfolioData();
    }, []);

    // Function to fetch existing portfolio data
    const fetchExistingPortfolioData = async () => {
        try {
            const response = await request('GET', '/getPortfolioForm');
            const existingData = response.data;
            setExistingShortIntroduce(existingData.shortIntroduce);
            setExistingIntroduce(existingData.introduce);
            setExistingFileUrl(existingData.fileUrl);
            setExistingPreferences({
                web: existingData.web,
                app: existingData.app,
                game: existingData.game,
                ai: existingData.ai
            });
        } catch (error) {
            console.error('Error fetching existing portfolio data:', error);
        }
    };

    // Web, App, Game, Ai 필드가 0, 1, 2, 3, 4를 선택할 수 있게 하기 위한 함수.
    const renderRadioGroup = (field) => (
        <Radio.Group
            value={existingPreferences[field]}
            onChange={(e) => handlePreferenceChange(field, e.target.value)}
        >
            <Radio value={0}>0</Radio>
            <Radio value={1}>1</Radio>
            <Radio value={2}>2</Radio>
            <Radio value={3}>3</Radio>
            <Radio value={4}>4</Radio>
        </Radio.Group>
    );

    // 입력 필드 변경 시 호출되는 이벤트 핸들러
    const onChangeHandler = (event) => {
        const name = event.target.name;
        const value = event.target.value;

        // 입력 필드(Input)마다 name에 해당하는 이름을 찾고, 해당하는 state 변수를 업데이트
        if (name === 'shortIntroduce') setExistingShortIntroduce(value);
        else if (name === 'introduce') setExistingIntroduce(value);
        else if (name === 'fileUrl') setExistingFileUrl(value);
    };

    // 포트폴리오 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitPortfolio = async (e) => {
        e.preventDefault();
        // web, app, game, ai는 한 번에 바로 접근할 수 없고, preferences를 통해서 접근한다.
        try {
            await submitPortfolio(
                e,
                existingPreferences.web,
                existingPreferences.app,
                existingPreferences.game,
                existingPreferences.ai,
                existingShortIntroduce,
                existingIntroduce,
                existingFileUrl
            );

            navigate('/portfolio');
        } catch (error) {
            console.error('Error submitting portfolio:', error);
        }
    };

    // 작성한 폼 제출
    const submitPortfolio = async (event, web, app, game, ai, shortIntroduce, introduce, fileUrl) => {
        event.preventDefault();

        try {
            const response = await request('PUT', '/updatePortfolio', {
                web: web,
                app: app,
                game: game,
                ai: ai,
                shortIntroduce: shortIntroduce,
                introduce: introduce,
                fileUrl: fileUrl
            });

            dispatch(uploadPortfolioSuccess(response.data.isCreated)); // uploadPortfolioSuccess 를 통해 디스패치
            setHasPortfolio(response.data.isCreated);       // 로컬 스토리지에 isCreated 세팅
            alert('포트폴리오가 성공적으로 업데이트되었습니다.');
        } catch (error) {
            alert('포트폴리오 업데이트에 실패하였습니다.');
        }
    };

    // 선호도 체크
    const handlePreferenceChange = (field, value) => {
        // 0은 중복해서 선택할 수 있지만, 다른 값들은 중복해서 선택할 수 없도록 함
        if (value === 0 || !Object.values(existingPreferences).includes(value)) {
            const newPreferences = { ...existingPreferences, [field]: value };      // 기존의 상태를 가져온 후, 필드에 값 세팅. ex) [Web] : 1
            setExistingPreferences(newPreferences);     // 새롭게 변경된 상태를 로컬스토리지에 저장
        } else {
            // 0 이외의 값을 중복 체크하면 warning 띄우기
            message.warning('Please select unique preferences for each field.');
        }
    };

    return (
        <Row justify="center">
            <Col span={12}>
            {/* Existing input fields */}
                {/* ... */}
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
                <form onSubmit={onSubmitPortfolio}>
                    {/* Short Introduce */}
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="shortIntroduce"
                            placeholder="Edit Short Introduce"
                            value={existingShortIntroduce}
                            onChange={onChangeHandler}
                        />
                    </div>
                    {/* Introduce */}
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="introduce"
                            placeholder="Edit Introduce"
                            value={existingIntroduce}
                            onChange={onChangeHandler}
                        />
                    </div>
                    {/* File URL */}
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="fileUrl"
                            placeholder="Edit File URL"
                            value={existingFileUrl}
                            onChange={onChangeHandler}
                        />
                    </div>
                    {/* Submit button */}
                    <Button type="primary" block htmlType="submit">Update Portfolio</Button>
                </form>
            </Col>
        </Row>
    );
}

export default UpdatePortfolioPage;