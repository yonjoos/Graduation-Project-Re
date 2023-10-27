import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'antd';
import '../LandingPage.css';
//import { useDispatch } from 'react-redux';
//import { lastVisitedEndpoint } from '../../../../_actions/actions';
//import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../../hoc/request';

function PortfolioCard(props) {
  const navigate = useNavigate();
  //const dispatch = useDispatch();

  const onClickHandler = () => {
    // 알림을 클릭하여 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
    // 가장 마지막에 저장한 엔드포인트인 /으로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /로 설정
    // dispatch(lastVisitedEndpoint('/', '/', '/'));
    // setLastVisitedEndpoint('/');
    // setLastLastVisitedEndpoint('/');
    // setLastLastLastVisitedEndpoint('/');

    navigate('/portfoliocard')
  }
  
  // return (
  //   <div onClick={onClickHandler} style={{ position: 'relative', marginTop: '30px', cursor: 'pointer' }}>
  //     <div className="white-rectangle" style={{ position: 'absolute', marginTop: '-3.0%', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', zIndex: 1 }}>
  //       {/** 얘는 뒤에 카드 모양 배경 */}
  //     </div>
  //     <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column', position: 'relative', zIndex: 2 }}>
  //       <img
  //         src={'https://storage.googleapis.com/hongik-pickme-bucket/3%EC%9E%90%EC%82%B0%204.png'}
  //         style={{ width: '65%', height: '65%' }}
  //       />
  //       <p style={{ fontSize: '30px' }}> PORTFOLIO </p>
  //     </div>
  //   </div>
  // );  

  return (
    <div onClick={onClickHandler} style={{ position: 'relative', marginTop: '30px', cursor: 'pointer' }}>
      
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column', position: 'relative', zIndex: 2 }}>
        <img
          src={'https://storage.googleapis.com/hongik-pickme-bucket/PortfolioCard.png'}
          style={{ width: '100%', height: '100%', borderRadius: '15px' }}
        />
      </div>
    </div>
  ); 
}

export default PortfolioCard;


{/**
동그라미 제거 버전
    <div>
      <div onClick={onClickHandler} style={{ display: 'flex', justifyContent: 'center', cursor: 'pointer' }}>
        <img
            src={'https://storage.googleapis.com/hongik-pickme-bucket/PortfolioIcon.png'}
            style={{ width: '50%', height: '50%' }}
        />
      </div>
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        <p style={{ fontSize: '30px' }}> PORTFOLIO </p>
      </div>
    </div>


동그라미 버전
    <div>
      <div style={{ position: 'relative', marginTop: '50px', marginBottom: '50px' }}>
        <div className="white-circle" style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', zIndex: 1 }}>
        </div>
        <div onClick={onClickHandler} style={{ display: 'flex', justifyContent: 'center', cursor: 'pointer', position: 'relative', zIndex: 2 }}>
          <img
              src={'https://storage.googleapis.com/hongik-pickme-bucket/PortfolioIcon.png'}
              style={{ width: '50%', height: '50%' }}
          />
        </div>
      </div>
      <div style={{ display: 'flex', justifyContent: 'center' }}>
        <p style={{ fontSize: '30px' }}> PORTFOLIO </p>
      </div>
    </div>
*/}