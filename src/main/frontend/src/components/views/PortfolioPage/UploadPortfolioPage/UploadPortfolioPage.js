import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Row, Col, Input, Button, Radio, message, Upload, Modal, Card } from 'antd';
import { request, setHasPortfolio } from '../../../../hoc/request';
import { UploadOutlined } from '@ant-design/icons';
import { getAuthToken } from '../../../../hoc/request';
import axios from 'axios';
import { uploadPortfolioSuccess } from '../../../../_actions/actions';

const { TextArea } = Input;

function UploadPortfolioPage() {
    const navigate = useNavigate();
    const dispatch = useDispatch();

    // 포트폴리오 폼의 상태를 관리할 state 변수들
    const [shortIntroduce, setShortIntroduce] = useState(''); // 한 줄 소개
    const [introduce, setIntoduce] = useState(''); // 소개 및 커리어
    const [preferences, setPreferences] = useState({    // 각 분야의 선호도
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });
    const [promoteImageUrl, setPromoteImageUrl] = useState([]); // 올릴 이미지 파일 리스트
    const [fileUrl, setFileUrl] = useState([]); // 올림 첨부 파일 리스트
    const [previewImage, setPreviewImage] = useState(null); // To store the image to be previewed
    const [previewVisible, setPreviewVisible] = useState(false); // To control the visibility of the preview modal

    //프사 관련
    const [selectedImage, setSelectedImage] = useState(null); //업로드할 이미지, 내 도큐먼트에서 선택한거
    const [profileImage, setProfileImage] = useState(null); //이미 등록되어있는 프사 띄우는 용도
    const [profileUploaded, setProfileUploaded] = useState(false);
    const [remove, setRemove] = useState(false);

    const greetingMessage = (
        <div>
         <strong>포트폴리오를 작성하여 자신의 관심사와 경력을 자유롭게 표현할 수 있습니다.</strong>
         <br></br>
          <br />개성있는 포트폴리오를 작성하여 다른 유저들에게 능력을 어필해 보세요! 뭐라고 더 적고싶은데 쓸 말이 없다. 허전하네
          <br />얼어붙은 달그림자 물결위에 차고 한겨울에 거센 파도 어쩌고 저쩌고 동해물과 백두산이 마르고 닳도록 하느님이 보우하사 어떻게든 되겠지...
          <br />그럼 건투를 빕니당 🍭🍬
        </div>
    );

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


    // 입력 필드 변경 시 호출되는 이벤트 핸들러
    const onChangeHandler = (event) => {
        const name = event.target.name;
        const value = event.target.value;

        // 입력 필드(Input)마다 name에 해당하는 이름을 찾고, 해당하는 state 변수를 업데이트
        if (name === 'shortIntroduce') setShortIntroduce(value);
        else if (name === 'introduce') setIntoduce(value);

    };

    // field 값으로는 web, app, game, ai가 들어옴.
    // value 값으로는 0, 1, 2, 3, 4가 들어옴.
    const handlePreferenceChange = (field, value) => {
        // 0 선택은 중복을 허용. 이외의 값들에 대해서는 중복을 허용하지 않음.
        if (value === 0 || !Object.values(preferences).includes(value)) {
            const newPreferences = { ...preferences, [field]: value };  // ...을 통해 기존의 preferences 상태를 가져오고, field를 value값으로 세팅. ex) [web] = 1
            setPreferences(newPreferences); // 새롭게 설정된 newPreferences를 Preferences로 세팅
        }
        else {
            // 중복된 값을 선택하면 경고 문구 띄움.
            message.warning('분야 별로 서로 다른 선호도를 체크해주세요.');
        }
    };


    // Web, App, Game, Ai 필드가 0, 1, 2, 3, 4를 선택할 수 있게 하기 위한 함수.
    const renderRadioGroup = (field) => (
        <Radio.Group
            value={preferences[field]}
            onChange={(e) => handlePreferenceChange(field, e.target.value)}
        >
            <Radio value={0}>매우 싫음</Radio>
            <Radio value={1}>싫음</Radio>
            <Radio value={2}>보통</Radio>
            <Radio value={3}>좋음</Radio>
            <Radio value={4}>매우 좋음</Radio>
        </Radio.Group>
    );

    // 포트폴리오 폼 제출 시 호출되는 이벤트 핸들러
    const onSubmitPortfolio = (e) => {
        e.preventDefault();

        if (!shortIntroduce) {
            message.warning('한 줄 소개를 입력해주세요!');
            return;
        }

        // web, app, game, ai는 한 번에 바로 접근할 수 없고, preferences를 통해서 접근한다.
        submitPortfolio(e, preferences.web, preferences.app, preferences.game, preferences.ai, shortIntroduce, introduce, promoteImageUrl, fileUrl);
    };


    // 작성한 폼 제출
    const submitPortfolio = (event, web, app, game, ai, shortIntroduce, introduce, promoteImageUrl, fileUrl) => {
        event.preventDefault();
    
        const formData = new FormData();
        formData.append('web', web);
        formData.append('app', app);
        formData.append('game', game);
        formData.append('ai', ai);
        formData.append('shortIntroduce', shortIntroduce);
        formData.append('introduce', introduce);
        promoteImageUrl.forEach((image, index) => {
            formData.append(`promoteImageUrl[${index}]`, image);
        });
        fileUrl.forEach((file, index) => {
            formData.append(`fileUrl[${index}]`, file);
        });
    
        const config = {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${getAuthToken()}`,
            },
        };
    
        axios
            .post('/uploadPortfolio', formData, config)
            .then((response) => {
                // Handle the response
                dispatch(uploadPortfolioSuccess(response.data.isCreated));
                setHasPortfolio(response.data.isCreated);
                alert('포트폴리오가 성공적으로 생성되었습니다.');
    
                //프사 업로드
                if(remove){
                    return removeProfileImage();
                    

                }else{return handleSubmit(); }
            })
            .catch((error) => {
                console.error('Failed to upload post:', error);
                alert('포트폴리오 생성에 실패하였습니다.');
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

    const handleRemoveProfileImage = () => {
        setSelectedImage(null);
        setRemove(true);
        
    }

    const removeProfileImage = () =>{
        
        return new Promise((resolve, reject) => {
                
                const formData = new FormData();
                formData.append('imageUrl', selectedImage);
                const config = {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'Authorization': `Bearer ${getAuthToken()}`,
                    },
                };
                
                axios
                    .put(`/removeProfileImage`, formData, config)
                    .then((response) => {
                        if (response.data === 'success') {
                            setProfileUploaded(true);
                            setProfileImage(null);
                            setRemove(false);
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
       
        });

    }


    return (
        <Row justify="center">
            <Col span={24}>
                <Card title = {'Write down your information'} style={{marginTop:'20px'}} headStyle={{ background: '#ddeeff' }}>
                <form onSubmit={onSubmitPortfolio} style={{paddingLeft:'45px', paddingRight:'45px'}}>
                    <div style={{display:'flex', alignItems:'center', borderRadius:'10px', border: '1px solid lightgray'}}> 
                    <div 
                                        style={{display:'grid',
                                        justifyContent:'center', 
                                        marginLeft:'30px',
                                        marginBottom:'20px',
                                    }}>
                                        <div 
                                            style={{
                                            width: '140px',  
                                            height: '140px',  
                                            borderRadius: '50%',
                                            border: '5px solid lightblue',
                                            overflow: 'hidden',
                                            marginTop:'20px',
                                            marginBottom:'10px'
                                        }}>
                                            {(remove) ? (
                                                    <img
                                                    style={{ borderRadius: '50%', width: '100%', height: '100%', marginBottom: '15px',  }}
                                                    src={`https://storage.googleapis.com/hongik-pickme-bucket/comgongWow.png`}
                                                />

                                                ) : (null)}

                                                {!remove && selectedImage ? (
                                                    //새로 바꿀 이미지
                                                    <img
                                                    src={URL.createObjectURL(selectedImage)}
                                                    style={{ borderRadius: '50%',  width: '100%', height: '100%', marginBottom: '15px',  }}
                                                    onClick={() => handlePreview(URL.createObjectURL(selectedImage))} // Open the modal when clicked
                                                    />
                                                ):(
                                                    //기존 프사
                                                    null

                                                )}
                                                {!remove && !selectedImage ? (
                                                    <img
                                                    style={{ borderRadius: '50%',  width: '100%', height: '100%', marginBottom: '15px', }}
                                                    src={`https://storage.googleapis.com/hongik-pickme-bucket/${profileImage}`}
                                                />
                                                ):(null)}
                                        </div>
                                        <div style={{ display: 'flex', justifyContent: 'center' }}>
                                                {/* 업로드할 사진 */}
                                                <label htmlFor="fileInput" className="custom-upload" style={{cursor:'pointer',}}>
                                                    ⚙️ set
                                                    </label>
                                                    <input
                                                    type="file"
                                                    id="fileInput"
                                                    accept="image/*"
                                                    style={{ display: 'none' }}
                                                    onChange={(event) => {
                                                        setSelectedImage(event.target.files[0]);
                                                        // Handle the selected image as needed
                                                    }}
                                                />
                                                <span 
                                                    style={{marginLeft:'30px', cursor:'pointer'}}
                                                    onMouseUp={handleRemoveProfileImage}
                                                >
                                                    remove
                                                </span>
                                                
                                            </div>
                                    </div>
                        <div style={{marginLeft:'40px', marginRight:'40px', display:'flex', alignItems:'center'}}>
                            <p>
                                {greetingMessage}
                            </p>
                        </div>
                    </div>

                                    
                    {/** mb-4 : "margin Bottom 4"를 의미하며 요소 하단에 여백을 적용하는 데 사용 */}
                    <div className="form-outline mb-4" style={{marginTop:'50px'}}>
                        <strong style={{fontSize:'20px'}}> Fields of Interests</strong>
                        <hr></hr>
                        <p style={{marginLeft:'15px', marginRight:'15px'}}>관심 분야와 선호도를 선택해주세요. 정확한 추천을 위해, 각 분야의 선호도에 순서를 정해주세요. 4가 가장 높은 선호도이고, 0은 관심 없는 분야입니다. 관심 없는 분야(0)는 중복해서 선택할 수 있지만, 이외의 
                        <b>* 선호도는 중복해서 체크할 수 없습니다. * </b></p>
                        <p style={{marginLeft:'15px', marginRight:'15px', color:'gray'}}>
                            * 다양한 선호도 분포는 포트폴리오 추천에 도움이 됩니다
                        </p>
                        <table style={{ marginLeft:'15px', marginRight:'15px', marginTop:'40px', display:'flex', justifyContent:'center'}}>
                            <tbody > 
                                <tr >
                                    <td width='70px' >Web</td>
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
                    
                    <div className="form-outline mb-4" style={{marginTop:'120px'}}>
                        <strong style={{fontSize:'20px'}}> Brief Introduction</strong>
                        <hr></hr>
                        <p style={{marginLeft:'15px', marginRight:'15px', marginBottom:'40px'}}>프로필과 함께 유저들에게 가장 먼저 보이는 한 줄 소개입니다. 강렬한 문장으로 다른 유저들에게 자신을 소개해 보세요!</p>

                        <Input
                            type="text"
                            name="shortIntroduce"
                            placeholder="한 줄 소개를 작성해주세요."
                            onChange={onChangeHandler}
                        />
                    </div>
                    <div className="form-outline mb-4" style={{marginTop:'120px'}}>
                        <strong style={{fontSize:'20px'}}> Experience </strong>
                        <hr></hr>
                            <TextArea
                                type="text"
                                name="introduce"
                                placeholder="소개 및 커리어를 작성해주세요."
                                onChange={onChangeHandler}
                                autoSize={{ minRows: 20 }}
                            />
                        </div>
                    <div style={{ marginTop: '5px', marginBottom: '5px' }}>
                        <strong>Photos</strong>
                    </div>
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
                        <img alt="프로젝트 이미지" style={{ width: '100%' }} src={previewImage} />
                    </Modal>
                    <div style={{ marginTop: '5px', marginBottom: '5px' }}>
                        <strong>Attatchment</strong>
                    </div>
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
                    </div>
                    <Button type="primary" block htmlType="submit">
                        Create Portfolio
                    </Button>
                </form>

                </Card>
                
            </Col>
        </Row>
    );
}

export default UploadPortfolioPage;
