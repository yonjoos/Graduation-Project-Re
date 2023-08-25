import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Button, Modal, message } from 'antd';
import { request, setHasPortfolio } from '../../../../hoc/request';
import { deletePortfolioSuccess } from '../../../../_actions/actions';

function DeletePortfolioPage() {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [isModalOpen, setIsModalOpen] = useState(false); // 모달이 보이는지 여부 설정

    const showModal = () => {
        setIsModalOpen(true);
    };

    // Modal (정말로 삭제하시겠습니까?)에서 '네'를 누른 경우
    const handleOk = () => {
        // Perform the deletion and other actions
        request('POST', '/deletePortfolio', {}) // Adjust the endpoint accordingly
        .then((response) => {
            alert('포트폴리오 삭제가 완료되었습니다.'); // 삭제 성공 메시지 띄우기
            setHasPortfolio(false);                     // 포트폴리오를 삭제했으므로, 포트폴리오 상태를 false로 변경
            dispatch(deletePortfolioSuccess()); // Dispatch를 통해 deletePortfolioSuccess()를 실행하고, 상태를 변경
            navigate('/portfolio'); // Redirect or perform any other action
        })
        .catch((error) => {
            console.error("Error deleting portfolio:", error);
            message.warning('포트폴리오 삭제에 실패했습니다.');
        });

        setIsModalOpen(false); // 모달 닫기
    };

    // Modal (정말로 삭제하시겠습니까?)에서 '아니오'를 누른 경우
    const handleCancel = () => {
        setIsModalOpen(false); // 모달 닫기
    };

    return (
        <div>
            <h2>This is a delete portfolio page.</h2>
            <Button type="primary" onClick={showModal}>
                포트폴리오 삭제
            </Button>
            {/** 삭제 버튼을 누르면 Modal을 띄우면서, 정말로 삭제하겠냐는 문구로 한 번 더 물어봄 */}
            <Modal
                title="포트폴리오 삭제"
                /** antd가 만들어 놓은 Modal의 디폴트가 왼쪽이 아니오, 오른쪽이 예 임. 즉 [아니오, 예] 순서.
                하지만 우리나라 웹사이트는 보통 [예, 아니오] 순서를 가짐.
                이를 맞추기 위해 onOk가 오른쪽 버튼이지만 '아니오'의 역할을 하도록 했음. */
                open={isModalOpen}
                onOk={handleCancel}
                onCancel={handleOk}
                okText="아니오"       // OK 버튼의 텍스트를 '아니오'로 변경. 왜냐하면 [네, 아니오] 순서를 맞추기 위함.
                cancelText="네" // CANCEL 버튼의 텍스트를 '네' 변경. 왜냐하면 [네, 아니오] 순서를 맞추기 위함.
            >
                <p>정말로 삭제하시겠습니까?</p>
            </Modal>
        </div>
    );
}

export default DeletePortfolioPage;
