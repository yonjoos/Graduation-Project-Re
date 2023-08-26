import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Divider, Row, Col, Button, Card } from 'antd';
import { request } from '../../../hoc/request';
import Search from '../../utils/Search';
import './ProjectPage.css';


function ProjectPage() {
    const [data, setData] = useState([]); // data is changed by setData.
    const [selectedBanners, setSelectedBanners] = useState(['all']);
    const navigate = useNavigate();

    // useEffect의 마지막 []에 [data]를 넣어주어야, 업로드 완료 후에도 방금 업로드한 게시물이 바로 업데이트 되어 올라옴.
    // 하지만, http://localhost:3000/project에 계속 머물러 있으면, 계속해서 백엔드에서 쿼리를 날린다는 문제가 발생함.

    useEffect(() => {
        request('GET', '/getProjectList', {})
            .then((response) => {
                setData(response.data); // Assuming the response.data is an array of objects
            })
            .catch((error) => {
                // Handle error, e.g., redirect to login or display an error message
                console.error("Error fetching data:", error);
            });
    }, []);

    
    const handleRowClick = (projectId) => {
        navigate(`/project/detail/${projectId}`);
    }

    const onClickHandler = () => {
        navigate('/project/upload');
    }

    // 2023826 -> 2023년 8월 26일 형식으로 변환
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1; // Month is zero-based
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    };

    // 선택된 배너에 따라 게시물을 필터링하는 작업 수행
    const filterPostsBySelectedBanners = () => {
        let filteredPosts = data;
        console.log(filteredPosts);
        console.log(selectedBanners);

        if (!selectedBanners.includes('all')) {
            filteredPosts = data.filter((item) =>
            ((selectedBanners.includes('web') && item.web) ||
                (selectedBanners.includes('app') && item.app) ||
                (selectedBanners.includes('game') && item.game) ||
                (selectedBanners.includes('ai') && item.ai)
            ));
        }
        console.log(filteredPosts);
        console.log(selectedBanners);
        return filteredPosts;
    }

    // 배너를 선택할 떄마다 selectedBanners가 추가되거나 변경됨
    // 처음엔 all(모든 게시물 상태)
    // all이 아닌 다른 게시물을 선택하는 순간 all은 selectedBanners에서 지워지고, 선택된 배너가 selectedBanners에 추가됨
    // 선택된 배너를 다시 클릭하면 해당 배너를 selectedBanners에서 제외
    // all이 아닌 다른 배너는 중복 선택이되어 selectedBanners에 저장됨
    const toggleBanner = (banner) => {
        if (banner === 'all') {
            setSelectedBanners(['all']);
        } else if (selectedBanners.includes('all')) {
            setSelectedBanners([banner]);
        }
        else {

            const updatedBanners = selectedBanners.includes(banner)
                ? selectedBanners.filter((b) => b !== banner)
                : [...selectedBanners, banner];
            // Check if all specific banners are unselected
            const allBannersUnselected = !['web', 'app', 'game', 'ai'].some(b => updatedBanners.includes(b));

            // If all specific banners are unselected, set selection to "all"
            setSelectedBanners(allBannersUnselected ? ['all'] : updatedBanners);
        }
    }

    // 현재 선택된 selectedBanners에 따라 필터링 된 게시물을 기반으로 실제 렌더링 진행
    const renderPosts = (posts) => {
        return (
            <div>
                {posts.map((item, index) => (
                    <Card key={index} style={{ margin: '10px 0' }}> {/**아래의 속성들을 antd Card 컴포넌트로 묶음*/}
                        {/** 이상하게, antd에서 끌어온 애들은 style = {{}}로 적용이 안되고 css로 적용될 때가 있음 */}
                        <Divider className="ball-divider" />
                        <div onClick={() => handleRowClick(item.id)} style={{ cursor: 'pointer' }}>
                            <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                                <Col span={6}>
                                    <div style={{ borderRight: '1px' }}>
                                        닉네임: {item.nickName}
                                    </div>
                                </Col>
                                {/** 수직선 CSS인 vertical-line을 만들어 주었음 */}
                                <Col span={12} className="vertical-line">
                                    <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
                                        제목: {item.title}
                                    </div>
                                    {/** Boolean으로 반환되는 애들은 삼항연산자를 통해 값을 보여줘야 함 */}
                                    <div style={{ marginLeft: '3px' }}>
                                        분류: {item.web ? "Web " : ""}{item.app ? "App " : ""}{item.game ? "Game " : ""}{item.ai ? "AI " : ""}
                                    </div>
                                </Col>
                                <Col span={6} className="vertical-line">
                                    <div className="shape-outline mb-1" style={{ marginLeft: '3px' }}>
                                        모집 인원: {item.recruitmentCount}
                                    </div>
                                    <div style={{ marginLeft: '3px' }}>
                                        모집 마감일: {formatDate(item.endDate)}
                                    </div>
                                </Col>
                            </Row>
                        </div>
                        <Divider className="ball-divider" />
                    </Card>

                ))}
            </div>
        );
    }

    return (
        <div>
            <Search />

            <div style={{ textAlign: 'center', margin: '20px 0' }}>
                <Button
                    type={selectedBanners.includes('all') ? 'primary' : 'default'}
                    onClick={() => toggleBanner('all')}
                    style={{ marginRight: '10px' }}
                >
                    전체
                </Button>
                <Button
                    type={selectedBanners.includes('web') ? 'primary' : 'default'}
                    onClick={() => toggleBanner('web')}
                >
                    웹
                </Button>
                <Button
                    type={selectedBanners.includes('app') ? 'primary' : 'default'}
                    onClick={() => toggleBanner('app')}
                >
                    앱
                </Button>
                <Button
                    type={selectedBanners.includes('game') ? 'primary' : 'default'}

                    onClick={() => toggleBanner('game')}
                >
                    게임
                </Button>
                <Button
                    type={selectedBanners.includes('ai') ? 'primary' : 'default'}

                    onClick={() => toggleBanner('ai')}
                >
                    AI
                </Button>
            </div>

            {renderPosts(filterPostsBySelectedBanners())}
            <Row gutter={[16, 16]} style={{ marginTop: '20px' }} justify="center" align="middle">
                <Button type="primary" onClick={onClickHandler}>
                    Upload Project
                </Button>
            </Row>
        </div>
    );
}

export default ProjectPage;
