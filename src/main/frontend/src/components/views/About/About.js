import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Image , Card} from 'antd';
import { request, setHasPortfolio } from '../../../hoc/request';



function About() {
    const location = useLocation();
    const navigate = useNavigate();

    const [data, setData] = useState([]); // 백엔드에서 가져온 관련 포트폴리오 자료 값



    const renderMember = () => {
        return(
            <div style={{marginTop:'-100px'}}>
                <div style={{display:'grid'}}>
                    <div style={{display:'flex'}}>
                        <div style={{
                            overflow: 'hidden',
                            width: '170px',
                            height: '200px'
                            }}>
                            <img
                                src={`https://storage.googleapis.com/hongik-pickme-bucket/%E1%84%86%E1%85%A6%E1%84%85%E1%85%A9%E1%86%AB.png`}
                                style={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover'
                                }}
                            />
                        </div>
                        <div style={{display:'flex', justifyContent:'space-between'}}>
                            <div style={{display:'grid', marginLeft:'40px', width:'300px'}}>
                                <div style={{fontSize:'25px'}}>
                                    <strong>박 시 홍</strong>
                                </div>
                                <div>
                                    Backend Developer<br></br>Frontend Developer
                                </div>
                            </div>
                            <div style={{width:'400px', display: 'flex', alignItems:'center'}}>
                                <ul>
                                    <li>고양문화초등학교</li>
                                    <li>고양발산중학교</li>
                                    <li>고양백신고등학교</li>
                                    <li>홍익대학교 서울캠퍼스 자율전공학부 17학번</li>
                                    <li>홍익대학교 서울캠퍼스 컴퓨터공학과 졸업</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
                <hr></hr>
                <div style={{display:'grid'}}>
                    <div style={{display:'flex'}}>
                        <div style={{
                            overflow: 'hidden',
                            width: '170px',
                            height: '200px'
                            }}>
                            <img
                                src={`https://storage.googleapis.com/hongik-pickme-bucket/%E1%84%89%E1%85%B3%E1%84%90%E1%85%B5%E1%84%8E%E1%85%B5.png`}
                                style={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover'
                                }}
                            />
                        </div>
                        <div style={{display:'flex', justifyContent:'space-between'}}>
                            <div style={{display:'grid', marginLeft:'40px', width:'300px'}}>
                                <div style={{fontSize:'25px'}}>
                                    <strong>이 윤 식</strong>
                                </div>
                                <div>
                                    Backend Developer<br></br>Frontend Developer
                                </div>
                            </div>
                            <div style={{width:'400px', display: 'flex', alignItems:'center'}}>
                                <ul>
                                    <li>서울창경초등학교</li>
                                    <li>선덕중학교</li>
                                    <li>청원고등학교</li>
                                    <li>홍익대학교 서울캠퍼스 자율전공학부 17학번</li>
                                    <li>홍익대학교 서울캠퍼스 경영학과 졸업</li>
                                    <li>홍익대학교 서울캠퍼스 컴퓨터공학과 졸업</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
                <hr></hr>
                <div style={{display:'grid'}}>
                    <div style={{display:'flex'}}>
                        <div style={{
                            overflow: 'hidden',
                            width: '170px',
                            height: '200px'
                            }}>
                            <img
                                src={`https://storage.googleapis.com/hongik-pickme-bucket/images.jpeg`}
                                style={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover'
                                }}
                            />
                        </div>
                        <div style={{display:'flex', justifyContent:'space-between'}}>
                            <div style={{display:'grid', marginLeft:'40px', width:'300px'}}>
                                <div style={{fontSize:'25px'}}>
                                    <strong>정 연 주</strong>
                                </div>
                                <div>
                                    Backend Developer<br></br>Frontend Developer<br></br>UX/UI Designer
                                </div>
                            </div>
                            <div style={{width:'400px', display: 'flex', alignItems:'center'}}>
                                <ul>
                                    <li>홍익대학교 서울캠퍼스 자율전공학부 17학번</li>
                                    <li>홍익대학교 서울캠퍼스 시각디자인과 졸업</li>
                                    <li>홍익대학교 서울캠퍼스 컴퓨터공학과 졸업</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }


    // COMPONENTS ###############################################

    

    return (
        <div>
            <Card title="What is P!ck Me?" style={{paddingLeft:'50px', paddingRight:'50px', marginTop: '30px'}}>
                <div style={{display:'grid', padding:'20px', marginBottom:'50px'}}>
                    <div style={{fontSize:'40px', marginBottom:'50px'}}>
                        <strong>P!ck Me</strong>
                    </div>
                    <div style={{ marginRight:'230px'}}>
                        <p>‘픽미’ 웹사이트는, <br/>
                            컴퓨터 공학에 관심있는 홍대생이라면, 누구나 사용할 수 있는 포트폴리오 기반 팀 빌딩 서비스입니다.
                            <br/>
                            저희가 이 서비스를 기획하게 된 목적은 다음과 같습니다. 
                            <br/>
                            보통 저희는 교내 커뮤니티를 통해 프로젝트나 스터디 팀원을 찾습니다.
                            <br/>
                            그러나, 교내 커뮤니티는, 여러 게시물들의 혼재로 인해 모집글을 한 눈에 찾기 어렵습니다.
                            <br/>
                            또한, 학우들의 관심사와 개발 경험을 확인할 수 있는 기능을 제공하지 않습니다.
                            <br/>
                            이로인해, 잠재적 팀원이 될 사람을 찾는 것은 어려운 일이 됩니다.
                            <br/>
                            따라서 저희는, 3가지 솔루션을 제시합니다.
                            <br/>
                            우선, 카테고라이징을 통해 관련 게시물들을 한 눈에 볼 수 있도록 하였습니다.
                            <br/>
                            또한, 서로의 개발 경험이나 관심사를 확인할 수 있는 포트폴리오 폼을 제공함으로써 상대방에 대한 더 많은 정보를 얻을 수 있도록 합니다.
                            <br/>
                            그리고, 포트폴리오 분석을 통해 나와 맞는 사용자를 추천하는 기능을 제공하였습니다.
                            <br/>
                            여러분들의 편의성과 개성, 학습의지를 P!ck me와 함께 붙태워보세요!


                        </p>
                    </div>
                </div>
                <div style={{marginBottom:'50px'}}>
                    <img
                                    src={`https://storage.googleapis.com/hongik-pickme-bucket/Project.png`}
                                    style={{
                                    width: '100%',
                                    height: '100%',
                                    objectFit: 'cover'
                                    }}
                                />
                </div>
                <div style={{display:'grid', padding:'20px', marginBottom:'120px'}}>
                    <div style={{fontSize:'40px', marginTop:'20px'}}>
                        <strong>Member</strong>
                    </div>
                </div>
                {renderMember()}
            </Card>
        </div>

    );
}


export default About;