import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Row, Col, Input, Button, Checkbox, message } from 'antd';
import { request } from '../../../../hoc/request';
import moment from 'moment';    // 달력 관련 업로드를 위해 필요. moment 라이브러리 설치하기 (npm install moment)

const { TextArea } = Input;

function UpdateProjectPage() {
    const navigate = useNavigate();
    const { projectId } = useParams();

    const [data, setData] = useState({
        title: '',
        postType: [], // Boolean 타입의 배열
        recruitmentCount: 0,
        endDate: '',
        content: '',
        promoteImageUrl: '',
        fileUrl: '',
    });

    useEffect(() => {
        fetchExistingProjectData();
    }, []);

    const fetchExistingProjectData = async () => {
        try {
            const response = await request('GET', `/getProjectForm/${projectId}`);
            const existingData = response.data;

            // Boolean 배열을 Checkbox.Group 값에 대한 문자열 배열로 변환
            const postTypeStrings = options.filter((_, index) => existingData.postType[index]);
            
            setData({
                ...existingData,
                postType: postTypeStrings
            });
        } catch (error) {
            console.error('Error fetching existing project data:', error);
            navigate(`/project/detail/${projectId}`);
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

    const onSubmitProject = async (e) => {
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
            await submitProject(e,
                data.title,
                data.postType,
                data.recruitmentCount,
                formattedEndDate,
                data.content,
                data.promoteImageUrl,
                data.fileUrl
            );
            navigate('/project');
        } catch (error) {
            console.error('Error submitting project:', error);
        }
    };

    const submitProject = async (event, title, postType, recruitmentCount, endDate, content, promoteImageUrl, fileUrl ) => {
        event.preventDefault();
        
        try {
            const response = await request('PUT', `/project/update/${projectId}`, {
                title: title,
                postType: postType,
                recruitmentCount: recruitmentCount,
                endDate: endDate,
                content: content,
                promoteImageUrl: promoteImageUrl,
                fileUrl: fileUrl
            });
            alert('프로젝트 게시물이 성공적으로 업데이트 되었습니다.');
            // navigate(`/project/detail/${projectId}`)
        } catch (error) {
            alert('프로젝트 게시물 업데이트에 실패하였습니다.');
        }
    };
    

    return (
        <Row justify="center">
            <Col span={12}>
                <div className="form-outline mb-4">
                    <label>Post Type &nbsp;&nbsp;&nbsp;</label>
                    {renderCheckboxGroup()}
                </div>
                <form onSubmit={onSubmitProject}>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="title"
                            placeholder="Edit Title"
                            value={data.title}
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div className="form-outline mb-4">
                    <Input
                        type="number"
                        name="recruitmentCount"
                        placeholder="Edit Recruitment Count"
                        value={data.recruitmentCount}
                        onChange={onChangeHandler}
                    />
                    </div>
                    <div className="form-outline mb-4">
                        <Input
                            type="date"
                            name="endDate"
                            placeholder="Edit End Date"
                            value={moment(data.endDate).format('YYYY-MM-DD')}
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div className="form-outline mb-4">
                        <TextArea
                            name="content"
                            placeholder="Edit Content"
                            value={data.content}
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="promoteImageUrl"
                            placeholder="Edit Promote Image URL"
                            value={data.promoteImageUrl}
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            name="fileUrl"
                            placeholder="Edit File URL"
                            value={data.fileUrl}
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'center' }}>
                        <Button type="primary" block htmlType="submit">Update Project</Button>
                    </div>
                </form>
            </Col>
        </Row>
    );
}

export default UpdateProjectPage;
