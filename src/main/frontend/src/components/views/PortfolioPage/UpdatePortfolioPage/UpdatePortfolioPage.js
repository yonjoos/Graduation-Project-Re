import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
//import { useDispatch } from 'react-redux';
import { Row, Col, Input, Button, Radio, message, Upload, Modal, Card } from 'antd';
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
    const [existingPreferences, setExistingPreferences] = useState({    // 디비에서 가져올 애 + 디비에 넣을 애 (UploadPortfolioPage.js의 prefer와 동일한 역할)
        web: 0,
        app: 0,
        game: 0,
        ai: 0
    });
    const [preferences, setPreferences] = useState({    // 순위에 맞는 선호하는 분야
        first: 'nop',
        second: 'nop',
        third: 'nop',
        fourth: 'nop'
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
    const [remove, setRemove] = useState(null);

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

    // preferences를 prefer로 변환
    useEffect(() => {
        updateExistingPreferFromPreferences();
    }, [preferences]);

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
            // 백엔드에서 가져온 데이터인 web: 4, app: 3, game: 2, ai: 0과 같은 형식을 preferences 형식인 first, second, third, fourth에 맞게 변형
            setPreferences({
                first: existingData.web === 4 ? 'web' : (existingData.app === 4 ? 'app' : (existingData.game === 4 ? 'game' : (existingData.ai === 4 ? 'ai' : 'nop'))),
                second: existingData.web === 3 ? 'web' : (existingData.app === 3 ? 'app' : (existingData.game === 3 ? 'game' : (existingData.ai === 3 ? 'ai' : 'nop'))),
                third: existingData.web === 2 ? 'web' : (existingData.app === 2 ? 'app' : (existingData.game === 2 ? 'game' : (existingData.ai === 2 ? 'ai' : 'nop'))),
                fourth: existingData.web === 1 ? 'web' : (existingData.app === 1 ? 'app' : (existingData.game === 1 ? 'game' : (existingData.ai === 1 ? 'ai' : 'nop'))),
            });
            // setExistingPreferences({
            //     web: existingData.web,
            //     app: existingData.app,
            //     game: existingData.game,
            //     ai: existingData.ai
            // });
            setPromoteImageUrl(existingData.promoteImageUrl);
            setFileUrl(existingData.fileUrl);
        } catch (error) {
            console.error('Error fetching existing portfolio data:', error);
        }
    };

    // 순위에 맞게 각 항목을 선택할 수 있도록 하는 함수
    const renderRadioGroup = (field, weight) => {
        if (weight === 4) {
            return (
                <Radio.Group
                    value={preferences[field]}
                    onChange={(e) => handlePreferenceChange(field, e.target.value)}
                >
                    <Radio value={'nop'}>없음</Radio>
                    <Radio value={'web'}>Web</Radio>
                    <Radio value={'app'}>App</Radio>
                    <Radio value={'game'}>Game</Radio>
                    <Radio value={'ai'}>AI</Radio>
                </Radio.Group>
            );
        } else if (weight === 3) {
            return (
                <Radio.Group
                    value={preferences[field]}
                    onChange={(e) => handlePreferenceChange(field, e.target.value)}
                >
                    <Radio value={'nop'}>없음</Radio>
                    <Radio value={'web'}>Web</Radio>
                    <Radio value={'app'}>App</Radio>
                    <Radio value={'game'}>Game</Radio>
                    <Radio value={'ai'}>AI</Radio>
                </Radio.Group>
            );
        } else if (weight === 2) {
            return (
                <Radio.Group
                    value={preferences[field]}
                    onChange={(e) => handlePreferenceChange(field, e.target.value)}
                >
                    <Radio value={'nop'}>없음</Radio>
                    <Radio value={'web'}>Web</Radio>
                    <Radio value={'app'}>App</Radio>
                    <Radio value={'game'}>Game</Radio>
                    <Radio value={'ai'}>AI</Radio>
                </Radio.Group>
            );
        } else if (weight === 1) {
            return (
                <Radio.Group
                    value={preferences[field]}
                    onChange={(e) => handlePreferenceChange(field, e.target.value)}
                >
                    <Radio value={'nop'}>없음</Radio>
                    <Radio value={'web'}>Web</Radio>
                    <Radio value={'app'}>App</Radio>
                    <Radio value={'game'}>Game</Radio>
                    <Radio value={'ai'}>AI</Radio>
                </Radio.Group>
            );
        }
    
        // Default rendering if weight doesn't match any condition
        return null;
    };

    // preferences에 저장된 애를 existingPrefer에 옮겨서 저장
    const updateExistingPreferFromPreferences = () => {
        setExistingPreferences({
            web: preferences.first === 'web' ? 4 : (preferences.second === 'web' ? 3 : (preferences.third === 'web' ? 2 : (preferences.fourth === 'web' ? 1 : 0))),
            app: preferences.first === 'app' ? 4 : (preferences.second === 'app' ? 3 : (preferences.third === 'app' ? 2 : (preferences.fourth === 'app' ? 1 : 0))),
            game: preferences.first === 'game' ? 4 : (preferences.second === 'game' ? 3 : (preferences.third === 'game' ? 2 : (preferences.fourth === 'game' ? 1 : 0))),
            ai: preferences.first === 'ai' ? 4 : (preferences.second === 'ai' ? 3 : (preferences.third === 'ai' ? 2 : (preferences.fourth === 'ai' ? 1 : 0))),
        });
    };

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

        // // body에 내용을 채워서 백에 전송
        // request('POST', '/uploadPortfolio', {
        //     web: web,
        //     app: app,
        //     game: game,
        //     ai: ai,
        //     shortIntroduce: shortIntroduce,
        //     introduce: introduce,
        //     fileUrl: fileUrl
        // })
        //     .then((response) => {
        //         dispatch(uploadPortfolioSuccess(response.data.isCreated)); // uploadPortfolioSuccess을 디스패치
        //         setHasPortfolio(response.data.isCreated);   // 포트폴리오 생성 상태를 로컬 스토리지에 세팅
        //         alert('포트폴리오가 성공적으로 생성되었습니다.');
        //         navigate('/portfolio');
        //     })
        //     .catch((error) => {
        //         alert('포트폴리오 생성에 실패하였습니다.');
        //     });
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
        // 기존 첨부파일 List<파일 url, 파일 원본이름>의 자료형을 백엔드의 FileUrlNameMapperDto가 인식하려면 이러한 방식으로 백엔드에 보내야함!!!
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
                alert('포트폴리오가 성공적으로 업데이트 되었습니다.');

                //포트폴리외 정보 업데이트가 완료되면
                //프사 업데이트 시작
                if(remove){
                    removeProfileImage();
                    

                }else{ handleSubmit(); }
                navigate('/portfolio');
            })
            .catch((error) => {
                alert('포트폴리오 업데이트에 실패하였습니다.');
            });
    };

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
    

    // field 값으로는 nop, web, app, game, ai가 들어옴.
    // value 값으로는 0, 1, 2, 3, 4가 들어옴.
    const handlePreferenceChange = (field, value) => {
        // '없음' 선택은 중복을 허용. 이외의 값들에 대해서는 중복을 허용하지 않음.
        if (value === 'nop' || !Object.values(preferences).includes(value)) {
            const newPreferences = { ...preferences, [field]: value };  // ...을 통해 기존의 preferences 상태를 가져오고, field를 value값으로 세팅. ex) [web] = 1
            setPreferences(newPreferences); // 새롭게 설정된 newPreferences를 Preferences로 세팅
        }
        else {
            // 중복된 값을 선택하면 경고 문구 띄움.
            message.warning('분야 별로 서로 다른 선호도를 체크해주세요.');
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

    const handleRemoveSelectedImage = () => {
        setSelectedImage(null);
        console.log("remove selectedImage" , selectedImage);
        console.log("remove" , remove);
        
    };

    const handleResetProfileImage = () =>{
        setRemove(true);
        console.log("reset to basic, selectedImage : " , selectedImage);
        console.log("removed" , remove);
    };

    const handleRemove = () =>{
        selectedImage ? handleRemoveSelectedImage() : handleResetProfileImage();
    }

    const handleSet = () => {
        const fileInput = document.getElementById("fileInput");
        if (fileInput) {
          fileInput.value = ''; // Reset the file input
        }
        fileInput.click(); // Trigger a click event on the file input
    };

    return (
        <div>
            {hasPortfolio ? (
                <Row justify="center">
                    <Col span={24} >
                        <Card title = {'Write down your information'} style={{marginTop:'20px'}} headStyle={{ background: '#ddeeff' }}>
                            <div style={{paddingLeft:'45px', paddingRight:'45px'}}> 
                                <div style={{display:'flex', alignItems:'center', borderRadius:'10px', border: '1px solid lightgray' }}>
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
                                            border: '5px solid salmon',
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
                                                    <input
                                                    type="file"
                                                    id="fileInput"
                                                    accept="image/*"
                                                    style={{ display: 'none' }}
                                                    onChange={(event) => {
                                                        setSelectedImage(event.target.files[0]);
                                                        console.log("selected! " , selectedImage);
                                                        // Handle the selected image as needed
                                                        setRemove(false);
                                                    }}
                                                />
                                                <span
                                                    onMouseUp={handleSet}
                                                >
                                                    ⚙️ set
                                                </span>
                                                <span 
                                                    style={{marginLeft:'30px', cursor:'pointer'}}
                                                    onMouseUp={()=>handleRemove()}
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

                                    {/* Existing input fields */}
                                    {/** mb-4 : "margin Bottom 4"를 의미하며 요소 하단에 여백을 적용하는 데 사용 */}
                                    <div className="form-outline mb-4" style={{marginTop:'50px'}}>
                                        <strong style={{fontSize:'20px'}}> Fields of Interests</strong>
                                        <hr/>
                                        <p style={{marginLeft:'15px', marginRight:'15px'}}>선호도에 맞는 관심분야를 선택해주세요. 정확한 추천을 위해, 선호도에 순서를 정해주세요.
                                        <br/> 
                                        <b>* 선호도는 중복해서 체크할 수 없습니다. * </b></p>
                                        <p style={{marginLeft:'15px', marginRight:'15px', color:'gray'}}>
                                            * 다양한 선호도 분포는 포트폴리오 추천에 도움이 됩니다
                                        </p>
                                        <table style={{ marginLeft:'15px', marginRight:'15px', marginTop:'40px', display:'flex', justifyContent:'center'}}>
                                            <tbody>
                                                <tr>
                                                    <td>1순위&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                                    <td>{renderRadioGroup('first', 4)}</td>
                                                </tr>
                                                <div style={{ marginBottom: '6px' }}></div>
                                                <tr>
                                                    <td>2순위&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                                    <td>{renderRadioGroup('second', 3)}</td>
                                                </tr>
                                                <div style={{ marginBottom: '6px' }}></div>
                                                <tr>
                                                    <td>3순위&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                                    <td>{renderRadioGroup('third', 2)}</td>
                                                </tr>
                                                <div style={{ marginBottom: '6px' }}></div>
                                                <tr>
                                                    <td>4순위&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                                    <td>{renderRadioGroup('fourth', 1)}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <form onSubmit={onSubmitPortfolio}>
                                        {/* Short Introduce */}
                                        <div className="form-outline mb-4" style={{marginTop:'100px'}}>
                                            <strong style={{fontSize:'20px'}}> Brief Introduction</strong>
                                            <hr></hr>
                                            <p style={{marginLeft:'15px', marginRight:'15px', marginBottom:'40px'}}>프로필과 함께 유저들에게 가장 먼저 보이는 한 줄 소개입니다. 강렬한 문장으로 다른 유저들에게 자신을 소개해 보세요!</p>
                                            <Input
                                                type="text"
                                                name="shortIntroduce"
                                                placeholder="Edit Short Introduce"
                                                value={existingShortIntroduce}
                                                onChange={onChangeHandler}
                                            />
                                        </div>
                                        {/* Introduce */}
                                        <div className="form-outline mb-4" style={{marginTop:'120px'}}>
                                            <strong style={{fontSize:'20px'}}> Experience </strong>
                                            <hr></hr>
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
                                            <strong>Photos</strong>
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
                                            <strong>Attatchment</strong>
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

                            </div>
                        </Card>
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