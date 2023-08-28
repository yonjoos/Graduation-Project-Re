import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Row, Col, Input, Button, Checkbox, message, DatePicker, InputNumber } from 'antd';
import { request } from '../../../../hoc/request';
import moment from 'moment';    // 달력 관련 업로드를 위해 필요. moment 라이브러리 설치하기 (npm install moment)

const { TextArea } = Input;

function UpdateStudyPage() {
    const navigate = useNavigate();
    const { studyId } = useParams();

    const [data, setData] = useState({
        title: '',
        postType: [],
        recruitmentCount: 0,
        endDate: null, // Change to null
        content: '',
        promoteImageUrl: '',
        fileUrl: '',
    });

    useEffect(() => {
        fetchExistingStudyData();
    }, []);

    const fetchExistingStudyData = async () => {
        try {
            const response = await request('GET', `/getStudyForm/${studyId}`);
            const existingData = response.data;

            // Boolean 배열을 Checkbox.Group 값에 대한 문자열 배열로 변환
            const postTypeStrings = options.filter((_, index) => existingData.postType[index]);

            setData({
                ...existingData,
                postType: postTypeStrings,
                endDate: moment(existingData.endDate) // Initialize with moment object
            });
        } catch (error) {
            console.error('Error fetching existing study data:', error);
            navigate(`/study/detail/${studyId}`);
        }
    };

    const options = ['Web', 'App', 'Game', 'AI'];
    const MAX_SELECTED_CHECKBOXES = 2;

    const renderCheckboxGroup = () => (
        <Checkbox.Group
            value={data.postType}
            onChange={(checkedValues) => handleCheckboxChange(checkedValues)}
        >
            {options.map((option, index) => (
                <Checkbox
                    key={index}
                    value={option}
                    disabled={data.postType.length === MAX_SELECTED_CHECKBOXES && !data.postType.includes(option)}
                >
                    {option}
                </Checkbox>
            ))}
        </Checkbox.Group>
    );

    const handleCheckboxChange = (checkedValues) => {
        if (checkedValues.length <= MAX_SELECTED_CHECKBOXES) {
            setData(prevData => ({
                ...prevData,
                postType: checkedValues
            }));
        }
    };
    
    const onChangeHandler = (event) => {
        const { name, value } = event.target;
        setData(prevData => ({ ...prevData, [name]: value }));
    };

    const onSubmitStudy = async (e) => {
        e.preventDefault();

        if (!data.title) {
            message.warning('프로젝트 이름을 작성해주세요.');
            return;
        }
        if (data.postType.length === 0) {
            message.warning('모집 분야 항목을 체크해주세요.');
            return;
        }
        if (data.recruitmentCount === 0) {
            message.warning('모집 인원을 작성해주세요.');
            return;
        }
        if (!data.endDate) {
            message.warning('모집 기간을 설정해주세요.');
            return;
        }
        if (!data.content) {
            message.warning('프로젝트 내용을 적어주세요.');
            return;
        }

        const formattedEndDate = moment(data.endDate).format('YYYY-MM-DD');

        try {
            await submitStudy(e,
                data.title,
                data.postType,
                data.recruitmentCount,
                formattedEndDate,
                data.content,
                data.promoteImageUrl,
                data.fileUrl
            );
            navigate('/study');
        } catch (error) {
            console.error('Error submitting study:', error);
        }
    };

    const submitStudy = async (event, title, postType, recruitmentCount, endDate, content, promoteImageUrl, fileUrl ) => {
        event.preventDefault();
        
        try {
            await request('PUT', `/study/update/${studyId}`, {
                title: title,
                postType: postType,
                recruitmentCount: recruitmentCount,
                endDate: endDate,
                content: content,
                promoteImageUrl: promoteImageUrl,
                fileUrl: fileUrl
            });
            alert('프로젝트 게시물이 성공적으로 업데이트 되었습니다.');
        } catch (error) {
            alert('프로젝트 게시물 업데이트에 실패하였습니다.');
        }
    };

    return (
        <Row justify="center">
            <Col span={12}>
                <form onSubmit={onSubmitStudy}>
                    <div className="form-outline mb-1">프로젝트 이름</div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="title"
                            placeholder="제목 변경"
                            value={data.title}
                            onChange={onChangeHandler}
                        />
                    </div>

                    <div className="form-outline mb-1">모집 분야</div>
                    <div className="form-outline mb-4">
                        <label>Post Type    </label>
                        {renderCheckboxGroup()}
                    </div>

                    <div style = {{ display: 'flex', justifyContent: 'space-between' }}>
                        <div>
                        <div className="form-outline mb-1">모집 인원</div>
                            <div className="form-outline mb-4">
                                <InputNumber
                                    type="number"
                                    name="recruitmentCount"
                                    placeholder="모집 인원 변경"
                                    value={data.recruitmentCount}
                                    onChange={onChangeHandler}
                                />
                            </div>
                        </div>
                        <div style = {{ marginRight : '40%' }}>
                            <div className="form-outline mb-1">모집 마감일</div>
                            <div className="form-outline mb-4">
                                <DatePicker
                                    name="endDate"
                                    value={data.endDate}
                                    onChange={(date) => onChangeHandler({ target: { name: 'endDate', value: date } })}
                                    placeholder="모집 마감일 변경"
                                    format="YYYY-MM-DD"
                                    disabledDate={(current) => current && current < moment().endOf('day')}
                                />
                            </div>
                        </div>
                    </div>

                    <div className="form-outline mb-1">프로젝트 내용</div>
                    <div className="form-outline mb-4">
                        <TextArea
                            name="content"
                            placeholder="내용 변경"
                            value={data.content}
                            onChange={onChangeHandler}
                            autoSize={{ minRows: 20 }}
                        />
                    </div>

                    <div className="form-outline mb-1">홍보 사진</div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="promoteImageUrl"
                            placeholder="홍보 사진 변경"
                            value={data.promoteImageUrl}
                            onChange={onChangeHandler}
                        />
                    </div>

                    <div className="form-outline mb-1">첨부 파일</div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="fileUrl"
                            placeholder="파일 변경"
                            value={data.fileUrl}
                            onChange={onChangeHandler}
                        />
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'center' }}>
                        <Button type="primary" block htmlType="submit">Update Study</Button>
                    </div>
                </form>
            </Col>
        </Row>
    );
}

export default UpdateStudyPage;
