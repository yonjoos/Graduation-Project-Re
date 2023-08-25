import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { Row, Col, Input, Button, Checkbox, InputNumber, /*Upload,*/ DatePicker, message } from 'antd';
//import { UploadOutlined } from '@ant-design/icons';
import { request } from '../../../../hoc/request';
import moment from 'moment';    // 달력 관련 업로드를 위해 필요. moment 라이브러리 설치하기 (npm install moment)

function UploadProjectPage() {
    const navigate = useNavigate();

    const [title, setTitle] = useState('');
    const [postType, setPostType] = useState([]);
    const [recruitmentCount, setRecruitmentCount] = useState(0);
    const [endDate, setEndDate] = useState(null);
    const [content, setContent] = useState('');
    const [promoteImageUrl, setPromoteImageUrl] = useState(null);
    const [fileUrl, setFileUrl] = useState(null);

    const handlePostTypeChange = (selectedOptions) => {
        setPostType(selectedOptions);
    };

    const handleRecruitsChange = (value) => {
        if (!isNaN(value)) {
            setRecruitmentCount(value);
        } else {
            message.warning('Please enter a valid number for recruits.');
        }
    };


    const onSubmitProject = (e) => {
        e.preventDefault();

        if (!title) {
            message.warning('프로젝트 이름을 작성해주세요.');
            return;
        }
        if (postType.length === 0) {
            message.warning('모집 분야 항목을 체크해주세요.');
            return;
        }
        if (recruitmentCount === 0) {
            message.warning('모집 인원을 작성해주세요.');
            return;
        }
        if (!endDate) {
            message.warning('모집 기간을 설정해주세요.');
            return;
        }
        if (!content) {
            message.warning('프로젝트 내용을 적어주세요.');
            return;
        }

        const formattedEndDate = moment(endDate).format('YYYY-MM-DD');
        submitProject(title, postType, recruitmentCount, formattedEndDate, content, promoteImageUrl, fileUrl);
        navigate('/project');
    };
    
    const submitProject = (title, postType, recruitmentCount, endDate, content, promoteImageUrl, fileUrl) => {
        request('POST', '/uploadProjectPost', {
            title: title,
            postType: postType,
            recruitmentCount: recruitmentCount,
            endDate: endDate,
            content: content,
            promoteImageUrl: promoteImageUrl,
            fileUrl: fileUrl
        })
            .then((response) => {
                console.log('Post uploaded successfully:', response.data);
                alert('게시물이 성공적으로 업로드되었습니다.');
            })
            .catch((error) => {
                console.error('Failed to upload post:', error);
                alert('게시물 업로드에 실패하였습니다.');
            });
    };
    

    return (
        <Row justify="center">
            <Col span={12}>
                <form onSubmit={onSubmitProject}>
                    <div className="form-outline mb-1">프로젝트 이름</div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            placeholder="프로젝트 이름"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                        />
                    </div>

                    <div className="form-outline mb-1">모집 분야</div>
                    <div className="form-outline mb-4">
                        <Checkbox.Group options={['Web', 'App', 'Game', 'AI']} value={postType} onChange={handlePostTypeChange} />
                    </div>

                    <div style = {{ display: 'flex', justifyContent: 'space-between' }}>
                        <div>
                            <div className="form-outline mb-1">모집 인원</div>
                            <div className="form-outline mb-4">
                                <InputNumber
                                    placeholder="모집 인원"
                                    value={recruitmentCount}
                                    onChange={handleRecruitsChange}
                                    min={0}  // 이 부분을 추가하여 음수 입력 방지
                                    defaultValue={0}    // 맨 처음에 0 값으로 놓이게 설정
                                />
                            </div>
                        </div>
                        <div style = {{ marginRight : '40%' }}>
                            <div className="form-outline mb-1">모집 마감일</div>
                            <div className="form-outline mb-4">
                            <DatePicker
                                value={endDate ? moment(endDate) : null}
                                onChange={(date, dateString) => setEndDate(dateString)}
                                placeholder="모집 마감일"
                                // disabledDate prop은 현재 날짜(current)가 오늘 날짜 이전인 경우에 true를 반환하도록 설정되어 있습니다.
                                // 따라서 선택할 수 없는 날짜는 비활성화됩니다. moment().endOf('day')는 오늘 날짜의 끝 시간을 나타냅니다.
                                disabledDate={(current) => current && current < moment().endOf('day')}
                            />
                            </div>
                        </div>
                    </div>

                    <div className="form-outline mb-1">프로젝트 내용</div>
                    <div className="form-outline mb-4">
                        <Input.TextArea
                            placeholder="프로젝트 내용"
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                        />
                    </div>

                    <div className="form-outline mb-1">홍보 사진</div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            placeholder="홍보 사진"
                            value={promoteImageUrl}
                            onChange={(e) => setPromoteImageUrl(e.target.value)}
                        />
                    </div>

                    <div className="form-outline mb-1">첨부 파일</div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            placeholder="첨부 파일"
                            value={fileUrl}
                            onChange={(e) => setFileUrl(e.target.value)}
                        />
                    </div>

                    {/* 실제로 사진 및 파일 업로드 시 사용해야할 코드.
                        여러 장의 사진 또는 여러 개의 파일을 업로드하기 위해서는 코드 수정 필요.
                        또한 최대로 올릴 수 있는 사진, 파일의 개수를 조정해야 함. */}
                    {/* <div className="form-outline mb-1">홍보 사진</div>
                    <div className="form-outline mb-4">
                        <Upload
                            accept="image/*"
                            fileList={promoteImageUrl ? [promoteImageUrl] : []}
                            beforeUpload={() => false}
                            onChange={(info) => {
                                if (info.fileList.length > 0) {
                                    setPromoteImageUrl(info.fileList[0]);
                                } else {
                                    setPromoteImageUrl(null);
                                }
                            }}
                        >
                            <Button icon={<UploadOutlined />}>Upload Photos</Button>
                        </Upload>
                    </div>

                    <div className="form-outline mb-1">첨부 파일</div>
                    <div className="form-outline mb-4">
                        <Upload
                            accept=".pdf,.doc,.docx"
                            fileList={fileUrl ? [fileUrl] : []}
                            beforeUpload={() => false}
                            onChange={(info) => {
                                if (info.fileList.length > 0) {
                                    setFileUrl(info.fileList[0]);
                                } else {
                                    setFileUrl(null);
                                }
                            }}
                        >
                            <Button icon={<UploadOutlined />} disabled={!!fileUrl}>Upload Files</Button>
                        </Upload>
                    </div> */}
                    <Button type="primary" block htmlType="submit">Submit</Button>
                </form>
            </Col>
        </Row>
    );
}

export default UploadProjectPage;
