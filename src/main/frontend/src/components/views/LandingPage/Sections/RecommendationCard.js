import React from 'react';
import ReactDOM from 'react-dom';
import { Card } from 'antd';

function RecommendationCard(props) {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '30vh' }}>
      <Card style={{ width: 400, textAlign: 'center', boxShadow: '0px 2px 8px rgba(0, 0, 0, 0.1)' }}>
        <h1>This is a Recommendation Card</h1>
        <p>This is a simple landing page using Ant Design Card component.</p>
      </Card>
    </div>
  );
}

export default RecommendationCard;
