import React from 'react';
import { Card } from 'antd';
import { useNavigate } from 'react-router-dom';

function RecommendationCard(props) {
  const navigate = useNavigate();

  const onClickHandler = () => {
    navigate('/recommendation')
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '30vh' }}>
      <Card onClick={onClickHandler} style={{ width: 400, textAlign: 'center', boxShadow: '0px 2px 8px rgba(0, 0, 0, 0.1)', cursor: 'pointer' }}>
        <h1>This is a Recommendation Card</h1>
        <p>This is a simple landing page using Ant Design Card component.</p>
      </Card>
    </div>
  );
}

export default RecommendationCard;
