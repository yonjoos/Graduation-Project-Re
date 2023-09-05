import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Row, Col, Input, Button, Checkbox, DatePicker, message, InputNumber } from 'antd';
import { request } from '../../../../hoc/request';
import dayjs from 'dayjs';  // moment대신 dayjs를 사용해야 blue background 버그가 발생하지 않음!!

const { TextArea } = Input;

function UpdateStudyPage() {
    const navigate = useNavigate();
    const { studyId } = useParams();    // URL에 있는 parameter 추출

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
        fetchExistingStudyData();
    }, []);

    const fetchExistingStudyData = async () => {
        try {
            const response = await request('GET', `/getStudyForm/${studyId}`);
            const existingData = response.data;
    
            // Boolean 배열을 Checkbox.Group 값에 대한 문자열 배열로 변환
            const postTypeStrings = options.filter((_, index) => existingData.postType[index]);
            
            setData({
                ...existingData,    // 기존 데이터 가져오기
                postType: postTypeStrings,      // 문자열 배열로 변환된 애들로 다시 postType 세팅
                endDate: dayjs(existingData.endDate) // dayjs를 통해 endDate를 포매팅한 후 다시 세팅
            });
        } catch (error) {
            console.error('Error fetching existing study data:', error);
            navigate(`/study/detail/${studyId}`);   // 수정할 데이터 폼을 가져오는 것이므로, navigate를 try catch문 아래에 적어주면 안된다.
        }
    };
    

    const options = ['Web', 'App', 'Game', 'AI'];
    const MAX_SELECTED_CHECKBOXES = 2;

    const renderCheckboxGroup = () => (
        <Checkbox.Group
            value={data.postType}   // data.postType 배열에 포함된 모집 분야가 선택되어 체크박스에 표시됨
            onChange={(checkedValues) => handleCheckboxChange(checkedValues)}   // 사용자가 체크박스를 선택하거나 해제할 때 handleCheckboxChange 호출
        >
            {/** options 배열은 다양한 모집 분야(예: Web, App, Game, AI)를 나타내는 문자열을 포함 */}
            {options.map((option, index) => (
                <Checkbox
                    key={index}
                    value={option}  // option 값은 web, app, game, ai 중 하나
                    disabled={data.postType.length === MAX_SELECTED_CHECKBOXES && !data.postType.includes(option)}  // 이미 MAX_SELECTED_CHECKBOXES (2)개의 체크박스가 선택되었고, 사용자가 추가로 체크박스를 선택하려고 할 때, 선택이 비활성화되도록 함
                >
                    {option}
                </Checkbox>
            ))}
        </Checkbox.Group>
    );

    const handleCheckboxChange = (checkedValues) => {
        // 체크박스에서 선택된 개수가 2개 이하라면
        if (checkedValues.length <= MAX_SELECTED_CHECKBOXES) {
            // 이전 데이터를 그대로 가져오되, postType에 선택된 값만 바꾸어서 저장
            setData(prevData => ({
                ...prevData,
                postType: checkedValues
            }));
        }
    };
    
    // data의 형식에 맞게 변환하여 값을 저장
    const onChangeHandler = (event) => {
        const { name, value } = event.target;

        // 모집 인원을 백엔드에서 가져온 데이터인 data.counts보다 작게 설정하지 못하도록 확인
        if (name === 'recruitmentCount' && value < data.counts) {
            message.warning('모집 인원은 현재 모집된 인원보다 작게 설정할 수 없습니다.');
            return;
        }

        // 모집 인원을 백엔드에서 가져온 데이터인 data.counts보다 작게 설정하지 못하도록 확인
        if (name === 'recruitmentCount' && value < 2) {
            message.warning('모집 인원은 자신을 포함한, 최소 2명부터 모집 가능합니다.');
            return;
        }
        
        setData(prevData => ({ ...prevData, [name]: value }));
    };

    const onSubmitStudy = async (e) => {
        e.preventDefault();

        if (!data.title) {
            message.warning('스터디 이름을 작성해주세요.');
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
            message.warning('스터디 내용을 적어주세요.');
            return;
        }

        // 백엔드와 싱크를 맞추기 위해, 백엔드에서 요구하는 형식으로 날짜 변환
        const formattedEndDate = dayjs(data.endDate).format('YYYY-MM-DD');

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
            navigate(`/study/detail/${studyId}`);
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
            alert('스터디 게시물이 성공적으로 업데이트 되었습니다.');
            // navigate(`/study/detail/${studyId}`)
        } catch (error) {
            alert('스터디 게시물 업데이트에 실패하였습니다.');
        }
    };
    

    return (
        <Row justify="center">
            <Col span={12}>
                <form onSubmit={onSubmitStudy}>
                    <div className="form-outline mb-1">스터디 이름</div>
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
                        <label>Post Type &nbsp;&nbsp;&nbsp;</label>
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
                                    // InputNumber 컴포넌트에서는 onChange 이벤트가 일반적인 event 객체를 전달하지 않고 숫자 값만 전달하므로, name을 직접 만들어 준다.
                                    onChange={(value) => onChangeHandler({ target: { name: 'recruitmentCount', value } })}
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
                                    disabledDate={(current) => current && current < dayjs().endOf('day')}
                                />
                            </div>
                        </div>
                    </div>

                    <div className="form-outline mb-1">스터디 내용</div>
                    <div className="form-outline mb-4">
                        <TextArea
                            name="content"
                            placeholder="스터디 내용 변경"
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
