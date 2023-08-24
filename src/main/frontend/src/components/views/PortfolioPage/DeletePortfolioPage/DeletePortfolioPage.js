import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Button, message } from 'antd';
import { request, setHasPortfolio } from '../../../../hoc/request';
import { deletePortfolioSuccess } from '../../../../_actions/actions';

function DeletePortfolioPage() {
    const navigate = useNavigate();
    const dispatch = useDispatch();

    const onClickHandler = () => {
        request('POST', '/deletePortfolio', {}) // Adjust the endpoint accordingly
            .then((response) => {
                // Show success message
                alert('포트폴리오 삭제가 완료되었습니다.');
                setHasPortfolio(false);
                dispatch(deletePortfolioSuccess()); // Dispatch login success action with role
                navigate('/portfolio'); // Redirect or perform any other action
            })
            .catch((error) => {
                // Handle error, e.g., display an error message
                console.error("Error signing out:", error);
                message.warning('포트폴리오 삭제에 실패했습니다.');
            });
    }

    return (
        <div>
            <h2>
                This is a delete portfolio page.
            </h2>
            <Button type="primary" onClick={onClickHandler}>
                포트폴리오 삭제
            </Button>
        </div>
    )
}

export default DeletePortfolioPage;