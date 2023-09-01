import React from 'react';
import { Card } from 'antd';
import { useNavigate } from 'react-router-dom';

function RecommendationCard(props) {
  const navigate = useNavigate();

  const onClickHandler = () => {
    navigate('/recommendation')
  }

  return (
    
    <Card onClick={onClickHandler} style={{ width: '100%', height: '300px', textAlign: 'center', boxShadow: '0px 2px 8px rgba(0, 0, 0, 0.1)', cursor: 'pointer' }}>
      <h3 style={{
        fontSize: '35px', // Set your desired font size here
        margin: '0',
        wordWrap: 'break-word',
      }}>This is a Recommendation Card</h3>
      <p >This is a simple landing page using Ant Design Card component.
      </p>
    </Card>
   
  );
}

export default RecommendationCard;
