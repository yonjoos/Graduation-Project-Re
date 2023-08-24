import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Row, Col, Input, Button, Radio, message } from 'antd';
import { request, setHasPortfolio } from '../../../../hoc/request';
import { uploadPortfolioSuccess } from '../../../../_actions/actions';

function DeletePortfolioPage() {
    const navigate = useNavigate();

    return (
        <div>
            <h2>
                This is a delete portfolio page.
            </h2>
        </div>
    )
}

export default DeletePortfolioPage;