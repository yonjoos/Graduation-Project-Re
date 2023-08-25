import React from 'react';
import { Card } from 'antd';
import { useNavigate } from 'react-router-dom';

function ProjectCard(props) {
  const navigate = useNavigate();

  const onClickHandler = () => {
    navigate('/project')
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '30vh' }}>
      {/** cursor: 'pointer' : 카드 위에 마우스 갖다대었을 때 손 모양이 뜨게 함 */}
      <Card onClick={onClickHandler} style={{ width: 400, textAlign: 'center', boxShadow: '0px 2px 8px rgba(0, 0, 0, 0.1)', cursor: 'pointer' }}>
        <h1>This is a Project Card</h1>
        <p>This is a simple landing page using Ant Design Card component.</p>
      </Card>
    </div>
  );
}

export default ProjectCard;