import React from 'react';
import { Card } from 'antd';
import { useNavigate } from 'react-router-dom';

function ProjectCard(props) {
  const navigate = useNavigate();

  const onClickHandler = () => {
    navigate('/project')
  }

  return (
    <Card onClick={onClickHandler} style={{ width: '100%', height: '300px', textAlign: 'center', boxShadow: '0px 2px 8px rgba(0, 0, 0, 0.1)', cursor: 'pointer' }}>
      <h1>This is a Project Card</h1>
      <p>This is a simple landing page using Ant Design Card component.</p>
    </Card>
  );
}

export default ProjectCard;