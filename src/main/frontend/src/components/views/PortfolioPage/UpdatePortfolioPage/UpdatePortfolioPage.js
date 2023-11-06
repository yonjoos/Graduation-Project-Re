import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
//import { useDispatch } from 'react-redux';
import { Row, Col, Input, Button, Radio, message, Upload, Modal } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { getAuthToken } from '../../../../hoc/request';
import axios from 'axios';
// import { request, setHasPortfolio } from '../../../../hoc/request';
// import { uploadPortfolioSuccess } from '../../../../_actions/actions';
import { request } from '../../../../hoc/request';

const { TextArea } = Input;

function UpdatePortfolioPage() {
    const navigate = useNavigate();
    //const dispatch = useDispatch();

    // 기존의 포트폴리오 데이터를 가져오고, 새로운 데이터를 입력할 수 있도록 하기 위한 useState
    const [hasPortfolio, setHasPortfolio] = useState('');
    const [existingShortIntroduce, setExistingShortIntroduce] = useState('');
    const [existingIntroduce, setExistingIntroduce] = useState('');
    const [existingPreferences, setExistingPreferences] = useState({
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });
    const [promoteImageUrl, setPromoteImageUrl] = useState([]); // 기존에 있던 이미지 상태변수
    const [fileUrl, setFileUrl] = useState([]); // 기존에 있던 첨부파일 상태변수
    const [newPromoteImageUrl, setNewPromoteImageUrl] = useState([]); // 새롭게 추가된 이미지 관리하는 상태변수
    const [newFileUrl, setNewFileUrl] = useState([]); // 새롭게 추가된 첨부파일 관리하는 상태변수
    const [previewImage, setPreviewImage] = useState(null); // 이미지 확대 관련
    const [previewVisible, setPreviewVisible] = useState(false); //이미지 확대 모달 관련

    //프사 관련
    const [selectedImage, setSelectedImage] = useState(null); //업로드할 이미지, 내 도큐먼트에서 선택한거
    const [profileImage, setProfileImage] = useState(null); //이미 등록되어있는 프사 띄우는 용도
    const [profileUploaded, setProfileUploaded] = useState(false);


    //프사 띄우기
    useEffect(()=>{

        request('GET', '/userProfileImage')
            .then((response) => {
                console.log(response.data.imageUrl);
                setProfileImage(response.data.imageUrl);
                setProfileUploaded(false);
            })
            .catch((error) => {
                console.error("Error fetching profile image:", error);
            });

    }, [profileImage])

    // Fetch existing portfolio data on component mount
    useEffect(() => {
        // Make an API request to fetch existing portfolio data
        // Update state variables with fetched data
        // For example:
        fetchExistingPortfolioData();
    }, []);

    useEffect(() => {
        if (hasPortfolio === null) {
            navigate('/portfolio');
        }
    }, [hasPortfolio]);

    //프사 업로드
    const handleSubmit = () => {
        return new Promise((resolve, reject) => {
            if (selectedImage) {
                const formData = new FormData();
                formData.append('imageUrl', selectedImage);
                const config = {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'Authorization': `Bearer ${getAuthToken()}`,
                    },
                };
                axios
                    .post('/updateProfileImage', formData, config)
                    .then((response) => {
                        if (response.data === 'success') {
                            setSelectedImage(null);
                            window.location.reload();
                            resolve('success'); 
                        } else {
                            console.error('Unknown response:', response.data);
                            message.error('Failed to update information.');
                            reject('failure'); 
                        }
                    })
                    .catch((error) => {
                        reject('failure'); 
                    });
            } else {
                resolve('noImage'); 
            }
        });
    };

    // Function to fetch existing portfolio data - db에서 기존의 포트폴리오를 가져오기
    const fetchExistingPortfolioData = async () => {
        try {
            const response = await request('GET', '/getPortfolioForm');
            const existingData = response.data;
            setHasPortfolio(existingData.hasPortfolio);
            setExistingShortIntroduce(existingData.shortIntroduce);
            setExistingIntroduce(existingData.introduce);
            setExistingPreferences({
                web: existingData.web,
                app: existingData.app,
                game: existingData.game,
                ai: existingData.ai
            });
            setPromoteImageUrl(existingData.promoteImageUrl);
            setFileUrl(existingData.fileUrl);
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
    };

    // 포트폴리오 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitPortfolio = async (e) => {
        e.preventDefault();

        if (!existingShortIntroduce) {
            message.warning('한 줄 소개를 입력해주세요!');
            return;
        }

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
                promoteImageUrl, // 기존의 이미지 변경사항
                fileUrl, // 기존의 첨부파일 목록 변경사항
                newPromoteImageUrl, // 새로 업로드할 이미지 
                newFileUrl // 새로 업로드할 파일 목록
            );


        } catch (error) {
            console.error('Error submitting portfolio:', error);
        }
    };

    // 작성한 폼 제출
    const submitPortfolio = async (event, web, app, game, ai, shortIntroduce, introduce, promoteImageUrl, fileUrl, newPromoteImageUrl, newFileUrl) => {
        event.preventDefault();
    
        const formData = new FormData();
        formData.append('web', web);
        formData.append('app', app);
        formData.append('game', game);
        formData.append('ai', ai);
        formData.append('shortIntroduce', shortIntroduce);
        formData.append('introduce', introduce);
        formData.append('promoteImageUrl', promoteImageUrl);
        newPromoteImageUrl.forEach((image, index) => {
            formData.append(`newPromoteImageUrl[${index}]`, image);
        });
        fileUrl.forEach((file, index) => {
            formData.append(`fileUrl[${index}].fileUrl`, file.fileUrl);
            formData.append(`fileUrl[${index}].fileName`, file.fileName);
        });
        newFileUrl.forEach((file, index) => {
            formData.append(`newFileUrl[${index}]`, file);
        });
    
        const config = {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${getAuthToken()}`,
            },
        };
    
        axios
            .put(`/updatePortfolio`, formData, config)
            .then(async (response) => {
                // Handle the response
                alert('포트폴리오가 성공적으로 업데이트 되었습니다.');
                await handleSubmit(); // Call the handleSubmit function here
                navigate('/portfolio');
            })
            .catch((error) => {
                // Handle errors
                alert('포트폴리오 업데이트에 실패하였습니다.');
            });
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

    // 새로 올려진 이미지 remove 클릭 시 목록에서 제거
    const removeNewPromoteImage = (index) => {
        const updatedNewPromoteImageUrl = [...newPromoteImageUrl];
        updatedNewPromoteImageUrl.splice(index, 1);


        setNewPromoteImageUrl(updatedNewPromoteImageUrl);
        console.log(newPromoteImageUrl);
    };

    // 기존에 있던 이미지 remove 클릭 시 목록에서 제거
    const removePromoteImage = (index) => {
        const updatedPromoteImageUrl = [...promoteImageUrl];
        updatedPromoteImageUrl.splice(index, 1);

        setPromoteImageUrl(updatedPromoteImageUrl);
        console.log(updatedPromoteImageUrl);
    };

    // 새로 올려진 첨부파일 remove 클릭 시 목록에서 제거
    const removeNewFile = (index) => {
        const updatedNewFileUrl = [...newFileUrl];
        updatedNewFileUrl.splice(index, 1);

        setNewFileUrl(updatedNewFileUrl);
        console.log(newFileUrl);
    };

    // 기존에 있던 첨부파일 remove 클릭 시 목록에서 제거
    const removeFile = (index) => {
        const updatedNewFileUrl = [...fileUrl];
        updatedNewFileUrl.splice(index, 1);

        setFileUrl(updatedNewFileUrl);

    };


    // Open the modal to preview the clicked image
    const handlePreview = (image) => {
        setPreviewImage(image);
        setPreviewVisible(true);
    };

    const handleClosePreview = () => {
        setPreviewVisible(false);
    };

    return (
        <div>
            {hasPortfolio ? (
                <Row justify="center">
                    <Col span={12}>
                    <div style={{ display: 'flex', marginBottom: '8px' }}>
                        {selectedImage ? (
                            <img
                            src={URL.createObjectURL(selectedImage)}
                            style={{ borderRadius: '50%', width: '200px', height: '200px', marginBottom: '15px', border: '5px solid lightblue' }}
                            onClick={() => handlePreview(URL.createObjectURL(selectedImage))} // Open the modal when clicked
                            />
                        ):(
                            <img
                                style={{ borderRadius: '50%', width: '190px', height: '190px', marginBottom: '15px', border: '5px solid lightblue' }}
                                src={`https://storage.googleapis.com/hongik-pickme-bucket/${profileImage}`}
                            />

                        )}
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'center' }}>
                        {/* 업로드할 사진 */}
                        <Upload
                            accept="image/*"
                            showUploadList={false}
                            beforeUpload={(image) => {
                                setSelectedImage(image);
                                return false; // Stops the upload action
                            }}
                        >
                            <Button icon={<UploadOutlined />} style={{ marginBottom: '10px' }}>Upload Image</Button>
                        </Upload>
                    </div>
                        {/* Existing input fields */}
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
                        <form onSubmit={onSubmitPortfolio}>
                            {/* Short Introduce */}
                            <div style={{ marginTop: '5px', marginBottom: '5px' }}>
                                한 줄 소개
                            </div>
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
                            <div style={{ marginTop: '5px', marginBottom: '5px' }}>
                                경력
                            </div>
                            <div className="form-outline mb-4">
                                <TextArea
                                    type="text"
                                    name="introduce"
                                    placeholder="Edit Introduce"
                                    value={existingIntroduce}
                                    onChange={onChangeHandler}
                                    autoSize={{ minRows: 20 }}
                                />
                            </div>
                            {/* File URL */}
                            <div style={{ marginTop: '5px', marginBottom: '5px' }}>
                                Photos
                            </div>
                            <div className="form-outline mb-4">
                                <div>
                                    <Upload
                                        accept="image/*"
                                        showUploadList={false}
                                        beforeUpload={(image) => {
                                            setNewPromoteImageUrl([...newPromoteImageUrl, image]);
                                            return false; // Stops the upload action
                                        }}
                                    >
                                        <Button icon={<UploadOutlined />}>Upload Photo</Button>
                                    </Upload>

                                    {/* 기존에 올려놨던 이미지 세팅 */}
                                    {promoteImageUrl ? (
                                        promoteImageUrl.map((imageUrl, index) => (
                                            <div key={index} >
                                                <img
                                                    key={index}
                                                    src={`https://storage.googleapis.com/hongik-pickme-bucket/${imageUrl}`}
                                                    alt={`홍보 사진 ${index + 1}`}
                                                    style={{ width: 300, marginRight: '16px', cursor: 'pointer' }}
                                                    onClick={() => handlePreview(`https://storage.googleapis.com/hongik-pickme-bucket/${imageUrl}`)
                                                    }
                                                />
                                                <Button onClick={() => removePromoteImage(index)}>Remove</Button>
                                            </div>

                                        ))
                                    ) : (
                                        <p>이미지가 없습니다</p>
                                    )}

                                    {/* 새로 올릴 이미지 세팅 */}
                                    {newPromoteImageUrl ?
                                        (newPromoteImageUrl.map((image, index) => (
                                            <div key={index} >
                                                <img
                                                    src={URL.createObjectURL(image)}
                                                    alt="홍보 사진"
                                                    style={{ width: 300, marginRight: '16px', cursor: 'pointer' }}
                                                    onClick={() => handlePreview(URL.createObjectURL(image))} // Open the modal when clicked
                                                />
                                                <Button onClick={() => removeNewPromoteImage(index)}>Remove</Button>
                                            </div>
                                        )))
                                        : (
                                            null
                                        )}
                                </div>
                            </div>
                            <Modal visible={previewVisible} footer={null} onCancel={handleClosePreview}>
                                <img alt="포트폴리오 이미지" style={{ width: '100%' }} src={previewImage} />
                            </Modal>
                            <div style={{ marginTop: '5px', marginBottom: '5px' }}>
                                첨부파일
                            </div>
                            <div className="form-outline mb-4">
                                <Upload
                                    accept=".pdf,.doc,.docx"
                                    showUploadList={false}
                                    beforeUpload={(file) => {
                                        setNewFileUrl([...newFileUrl, file]);
                                        return false;
                                    }}
                                >
                                    <Button icon={<UploadOutlined />} style={{ marginBottom: '10px' }}>Upload Files</Button>
                                </Upload>

                                {/* 기존에 올려놨던 첨부파일 세팅 */}
                                {fileUrl ? (
                                    fileUrl.map((file, index) => (

                                        <div style={{ display: 'flex', justifyContent: 'left' }} key={index}>
                                            <Button
                                                onClick={() => window.open(`https://storage.googleapis.com/hongik-pickme-bucket/${file.fileUrl}`, '_blank')} // 파일 열기 함수 호출
                                            >
                                                {file.fileName} {/* 파일 이름 표시 */}
                                            </Button>
                                            <Button onClick={() => removeFile(index)}>Remove</Button>
                                        </div>



                                    ))
                                ) : (
                                    <p>첨부파일이 없습니다</p>
                                )}

                                {/* 새로 올릴 첨부파일 세팅 */}
                                {newFileUrl ?
                                    (newFileUrl.map((file, index) => (
                                        <div key={index} >

                                            <Button onClick={() => window.open(URL.createObjectURL(file), '_blank')}>
                                                {file.name}
                                            </Button>
                                            <Button onClick={() => removeNewFile(index)}>Remove</Button>
                                        </div>
                                    )))
                                    : (
                                        null
                                    )}
                            </div>
                            {/* Submit button */}
                            <Button type="primary" block htmlType="submit">Update Portfolio</Button>
                        </form>
                    </Col>
                </Row>
            ) : (
                <div>

                </div>
            )}
        </div>
    );
}

export default UpdatePortfolioPage;