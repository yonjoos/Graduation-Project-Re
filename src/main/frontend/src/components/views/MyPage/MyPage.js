// 로그인된 회원만 볼 수 있는 페이지
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Button } from 'antd';

function MyPage() {

    const navigate = useNavigate();

    const onClickHandler = () => {
      navigate('/signOut');
    }
    return (
        <div>   
            <div>
                <h2>
                    This is a My Page
                </h2>
            </div>

            {/** justifyContent: 'center'를 적용시키려면 display: 'flex'가 함께 있어야 한다. */}
            <div style = {{ display: 'flex', justifyContent: 'center', }}> 
            <Button type="primary" onClick={onClickHandler}>
                Sign Out
            </Button>
            </div>
        </div>
    );
    
}

export default MyPage;