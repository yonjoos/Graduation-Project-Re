import React from 'react';
import { Card } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { lastVisitedEndpoint } from '../../../../_actions/actions';
import { setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint } from '../../../../hoc/request';

function ProjectCard(props) {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const onClickHandler = () => {
    // 알림을 클릭하여 이동했을 때, 해당 페이지에서 "목록으로 돌아가기" 버튼을 클릭하면,
    // 가장 마지막에 저장한 엔드포인트인 /으로 오게끔 dispatch를 통해 lastVisitedEndpoint를 /로 설정
    dispatch(lastVisitedEndpoint('/', '/', '/'));
    setLastVisitedEndpoint('/');
    setLastLastVisitedEndpoint('/');
    setLastLastLastVisitedEndpoint('/');

    navigate('/project')
  }

  return (
    <Card onClick={onClickHandler} style={{ width: '100%', height: '200px', textAlign: 'center', boxShadow: '0px 2px 8px rgba(0, 0, 0, 0.1)', cursor: 'pointer' }}>
      <h1>This is a Project Card</h1>
      <p>This is a simple landing page using Ant Design Card component.</p>
    </Card>
  );
}

export default ProjectCard;