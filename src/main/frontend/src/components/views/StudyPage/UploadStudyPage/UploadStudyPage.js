import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { Row, Col, Input, Button, Checkbox, InputNumber, DatePicker, message, Upload, Modal } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { getAuthToken } from '../../../../hoc/request';
import axios from 'axios';
import dayjs from 'dayjs';  // moment대신 dayjs를 사용해야 blue background 버그가 발생하지 않음!!

const { TextArea } = Input;
// const { Dragger } = Upload;

function UploadStudyPage() {
    const navigate = useNavigate();

    // 백엔드에 넘겨줄 폼 입력에 사용될 애들
    const [title, setTitle] = useState('');
    const [postType, setPostType] = useState([]);
    const [recruitmentCount, setRecruitmentCount] = useState(2);
    const [endDate, setEndDate] = useState(null);
    const [content, setContent] = useState('');
    const [promoteImageUrl, setPromoteImageUrl] = useState([]);
    const [fileUrl, setFileUrl] = useState([]);
    const [previewImage, setPreviewImage] = useState(null); // To store the image to be previewed
    const [previewVisible, setPreviewVisible] = useState(false); // To control the visibility of the preview modal

    // // 드래그 & 드롭 방식의 파일 업로드 antd 모듈
    // const fileUploadProps = {
    //     name: 'file',
    //     multiple: true,
    //     action: 'https://run.mocky.io/v3/435e224c-44fb-4773-9faf-380c5e6a2188',
    //     onChange(info) {
    //         const { status } = info.file;
    //         if (status !== 'uploading') {
    //             console.log(info.file, info.fileList);
    //         }
    //         if (status === 'done') {
    //             message.success(`${info.file.name} file uploaded successfully.`);
    //         } else if (status === 'error') {
    //             message.error(`${info.file.name} file upload failed.`);
    //         }
    //     },
    //     onDrop(e) {
    //         console.log('Dropped files', e.dataTransfer.files);
    //     },

    // };


    const options = ['Web', 'App', 'Game', 'AI'];   // 체크박스에서 선택 가능한 옵션들
    const MAX_SELECTED_CHECKBOXES = 2;  // 선택 가능한 모집 분야 개수 제한

    const renderCheckboxGroup = () => (
        <Checkbox.Group
            value={postType}
            onChange={(checkedValues) => handleCheckboxChange(checkedValues)}   // 체크박스 선택 시, handleCheckboxChange 실행.
        >
            {options.map((option, index) => (
                <Checkbox
                    key={index}
                    value={option}
                    // 3개 이상 선택하면, 다른 버튼 못누르게 하기
                    disabled={postType.length === MAX_SELECTED_CHECKBOXES && !postType.includes(option)}
                >
                    {option}
                </Checkbox>
            ))}
        </Checkbox.Group>
    )

    const handleCheckboxChange = (checkedValues) => {
        // 2개 이하인 경우 선택 가능
        if (checkedValues.length <= MAX_SELECTED_CHECKBOXES) {
            setPostType(checkedValues); // setPostType 함수를 호출하여 postType 상태 업데이트
        }
    };

    const handleRecruitsChange = (value) => {
        // !isNaN은 주어진 값이 숫자인지 아닌지를 확인하고, 숫자일 경우에만 특정 작업을 수행하려는 경우에 사용
        // 본인 제외, 최소 2명 이상 모집 가능
        if (!isNaN(value) && value >= 2) {
            setRecruitmentCount(value);
        } else {
            message.warning('본인을 포함해서 최소 두 명부터 모집 가능합니다.');
        }
    };


    const onSubmitStudy = (e) => {
        e.preventDefault();

        if (!title) {
            message.warning('스터디 이름을 작성해주세요.');
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
            message.warning('스터디 내용을 적어주세요.');
            return;
        }

        // 백엔드와 싱크를 맞추기 위해, 날짜 형식 변환
        const formattedEndDate = dayjs(endDate).format('YYYY-MM-DD');
        submitStudy(title, postType, recruitmentCount, formattedEndDate, content, promoteImageUrl, fileUrl);

    };

    const submitStudy = (title, postType, recruitmentCount, endDate, content, promoteImageUrl, fileUrl) => {
        const formData = new FormData();
        formData.append('title', title);
        formData.append('postType', postType);
        formData.append('recruitmentCount', recruitmentCount);
        formData.append('endDate', endDate);
        formData.append('content', content);
        promoteImageUrl.forEach((image, index) => {
            formData.append(`promoteImageUrl[${index}]`, image);
        });
        fileUrl.forEach((file, index) => {
            formData.append(`fileUrl[${index}]`, file);
        });
        console.log(formData);

        const config = {
            headers: {
                'Content-Type': 'multipart/form-data', // Set the content type to multipart/form-data
                'Authorization': `Bearer ${getAuthToken()}`, // Include your authorization header if needed
            },
        };
    
        axios
            .post('/uploadStudyPost', formData, config)
            .then((response) => {
                // Handle the response
                alert('게시물이 성공적으로 업로드되었습니다.');
                navigate('/study');
            })
            .catch((error) => {
                // Handle errors
                console.error('Failed to upload post:', error);
                alert('게시물 업로드에 실패하였습니다.');
            });
    };

    const removePromoteImage = (index) => {
        const updatedPromoteImageUrl = [...promoteImageUrl];
        updatedPromoteImageUrl.splice(index, 1);
        setPromoteImageUrl(updatedPromoteImageUrl);
    };

    const removeFile = (index) => {
        const updatedFileList = [...fileUrl];
        updatedFileList.splice(index, 1);
        setFileUrl(updatedFileList);
    };


    // Open the modal to preview the clicked image
    const handlePreview = (image) => {
        setPreviewImage(image);
        setPreviewVisible(true);
    };

    const handleClosePreview = () => {
        setPreviewVisible(false);
    };
    
    // const handleFileClick = (file) => {
    //     // Open the file in a new tab or window
    //     window.open(file.url, '_blank');
    // };

    return (
        <Row justify="center">
            <Col span={12}>
                <form onSubmit={onSubmitStudy} encType="multipart/form-data">
                    <div className="form-outline mb-1">스터디 이름</div>
                    <div className="form-outline mb-4">
                        <Input
                            type="text"
                            placeholder="스터디 이름"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                        />
                    </div>

                    <div className="form-outline mb-1">모집 분야</div>
                    <div className="form-outline mb-4">
                        {renderCheckboxGroup()}
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
                                    onChange={(dateString) => setEndDate(dateString)}
                                    placeholder="모집 마감일"
                                    // disabledDate prop은 현재 날짜(current)가 오늘 날짜 이전인 경우에 true를 반환하도록 설정되어 있습니다.
                                    // 따라서 선택할 수 없는 날짜는 비활성화됩니다. dayjs().endOf('day')는 오늘 날짜의 끝 시간을 나타냅니다.
                                    disabledDate={(current) => current && current < dayjs().endOf('day')}
                                />
                            </div>
                        </div>
                    </div>

                    <div className="form-outline mb-1">스터디 내용</div>
                    <div className="form-outline mb-4">
                        <TextArea
                            placeholder="스터디 내용"
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            autoSize={{ minRows: 20 }}
                        />
                    </div>

                    <div className="form-outline mb-1">홍보 사진</div>
                    <div className="form-outline mb-4">
                        <Upload
                            accept="image/*"
                            showUploadList={false}
                            beforeUpload={(image) => {
                                setPromoteImageUrl([...promoteImageUrl, image]);
                                return false; // Stops the upload action
                            }}
                        >
                            <Button icon={<UploadOutlined />} style={{ marginBottom: '10px' }}>Upload Photo</Button>
                            </Upload>   
                            {promoteImageUrl.map((image, index) => (
                                <div key={index} style={{ display: 'flex', marginBottom: '8px' }}>
                                    <img
                                        src={URL.createObjectURL(image)}
                                        alt="홍보 사진"
                                        style={{ maxWidth: '200px', maxHeight: '200px', marginRight: '16px', cursor: 'pointer' }}
                                        onClick={() => handlePreview(URL.createObjectURL(image))} // Open the modal when clicked
                                    />
                                    <Button onClick={() => removePromoteImage(index)}>Remove</Button>
                                </div>
                            ))}
                    </div>
                    {/* Preview Modal */}
                    <Modal visible={previewVisible} footer={null} onCancel={handleClosePreview}>
                        <img alt="스터디 이미지" style={{ width: '100%' }} src={previewImage} />
                    </Modal>

                    <div className="form-outline mb-1">첨부 파일</div>
                    <div className="form-outline mb-4">
                    <Upload
                            accept=".pdf,.doc,.docx"
                            showUploadList={false}
                            beforeUpload={(file) => {
                                setFileUrl([...fileUrl, file]);
                                return false;
                            }}
                        >
                            <Button icon={<UploadOutlined />} style={{ marginBottom: '10px' }}>Upload Files</Button>
                        </Upload>
                        {fileUrl.map((file, index) => (
                            <div key={index} style={{ display: 'flex', marginBottom: '8px', alignItems: 'center', marginBottom: '8px' }}>

                                <Button onClick={() => window.open(URL.createObjectURL(file), '_blank')}>
                                    {file.name}
                                </Button>

                                <Button onClick={() => removeFile(index)}>Remove</Button>
                            </div>
                        ))}

                        {/*  드래그앤 드롭 
                        <Dragger style={{ cursor: 'pointer' }}
                            {...fileUploadProps}
                            multiple
                            accept=".pdf,.doc,.docx"
                            showUploadList={true}
                            className="custom-upload-dragger"
                            beforeUpload={(file) => {
                                setFileUrl([...fileUrl, file]);
                                return false;
                            }}
                            onRemove={(file) => {
                                const index = fileUrl.indexOf(file);
                                if (index !== -1) {
                                    removeFile(index);
                                }
                            }}
                            onPreview={(file) => {
                                const url = URL.createObjectURL(file.originFileObj);
                                window.open(url, '_blank');
                            }}


                        >
                            <p className="ant-upload-drag-icon">
                                <UploadOutlined />
                            </p>
                            <p className="ant-upload-text">Click or drag file to this area to upload</p>
                            <p className="ant-upload-hint">
                                Support for a single or bulk upload. Strictly prohibited from uploading company data or other banned files.
                            </p>
                           
                            <style>{`
                                    .custom-upload-dragger .ant-upload-list-item-info {
                                        cursor: pointer; // 파일 이름 부분에 포인터 커서 스타일을 설정
                                    }
                                    .custom-upload-dragger .ant-upload-list-item-name {
                                        cursor: pointer; // "@" 부분에 포인터 커서 스타일을 설정
                                    }
                                `}</style>

                        </Dragger>
                         드래그앤 드롭  */}
                         

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

export default UploadStudyPage;
