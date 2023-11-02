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
                            <div style={{width:'400px'}}>
                                <ul>
                                    <li>서울@@초등학교 졸업</li>
                                    <li>서울@@중학교 졸업</li>
                                    <li>서울@@고등학교 졸업</li>
                                    <li>홍익대학교 서울캠퍼스 자율전공학부 17학번</li>
                                    <li>홍익대학교 서울캠퍼스 시각디자인과 졸업</li>
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
                            <div style={{width:'400px'}}>
                                <ul>
                                    <li>모름</li>
                                    <li>모름</li>
                                    <li>당연히 모름</li>
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
                            <div style={{width:'400px'}}>
                                <ul>
                                    <li>모름</li>
                                    <li>모름</li>
                                    <li>당연히 모름</li>
                                    <li>홍익대학교 서울캠퍼스 자율전공학부 17학번</li>
                                    <li>홍익대학교 서울캠퍼스 경영학과 졸업</li>
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
            <Card title="What is P!ck Me?" style={{paddingLeft:'50px', paddingRight:'50px'}}>
                <div style={{display:'grid', padding:'20px', marginBottom:'100px'}}>
                    <div style={{fontSize:'40px', marginBottom:'50px'}}>
                        <strong>P!ck Me</strong>
                    </div>
                    <div style={{marginLeft:'30px', marginRight:'350px'}}>
                        <p>픽미는 그 뭐더라 하여튼 사이튼데, 졸업프로젝트고, React랑 Spring으로 만들었어요
                            개 고생하면서 만들었습니다. 보고 감격하고 채용해주세요. 잘 적응하겠습니다. 아 할 말 없다. 진짜 없다. 왜 문단 나누기 안 됨.
                            오른 쪽에 사진 넣을거임 근데 마땅히 넣을거 없다. 목 허리 어깨 켁켁 <br></br>동해물과백두산이마르고 닳도록 하느님이 보우하사 우리나라만세
                            무궁화 삼천리 화려강산 대한사람 대한으로
                            <br></br>
                            <br></br>
                            <i>long live the P!ck Me</i>
                        </p>
                    </div>
                </div>
                <div style={{marginBottom:'150px'}}>
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