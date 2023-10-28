import { useState } from "react";
import { Col, Input } from "antd";
import { useNavigate } from 'react-router-dom';

const { Search } = Input;

// 랜딩페이지에서 검색어 입력과 엔터 및 클릭 버튼을 위임받은 하위 검색 컴포넌트
function SearchInLandingPage({ onSearch, initialSearchTerm }) {

    const navigate = useNavigate();

    //const [localSearchTerm, setLocalSearchTerm] = useState(""); // 독자적인 자기 자신의 검색어 입력 필드 값 가지고 세팅.
    const [localSearchTerm, setLocalSearchTerm] = useState(initialSearchTerm || '');
    // 검색 창에서 돋보기 버튼 클릭할 떄 동작 -> 구현해야함
    const handleSearch = () => {
        //setSearchTerm(value);
        onSearch(localSearchTerm);
        navigate(`/search/portfoliocard/query/${localSearchTerm}`); // 검색어 입력 후 클릭 누르면, search/portfoliocard로 default 이동
        console.log('클릭', localSearchTerm);

        setLocalSearchTerm(''); // 클릭하면 랜딩페이지에서 검색어 비워주기
    };

    // 검색 창에서 엔터 누를 떄 동작 -> 구현해야함
    const handleKeyPress = (event) => {

        if (event.key === 'Enter') {
            //handleSearch(localSearchTerm);
            event.preventDefault();

            // Send a request to the backend with the current searchTerm
            onSearch(localSearchTerm);
            navigate(`/search/portfoliocard/query/${localSearchTerm}`); // 검색어 입력 후 클릭 누르면, search/portfoliocard로 default 이동
            console.log('엔터', localSearchTerm);


            // Optionally, clear the search input after Enter is pressed
            setLocalSearchTerm('');
        }
    };

    return (
        <Col span={24} style={{ textAlign: 'center' }}>
            <Search
                placeholder="키워드를 검색해보세요!"
                value={localSearchTerm}
                onChange={(event) => {
                    setLocalSearchTerm(event.target.value); // 현재 검색어 변경 값을 추적해서
                    onSearch(event.target.value); // 상위 컴포넌트 (랜딩페이지로)
                }}
                onSearch={handleSearch} // 돋보기 버튼 클릭하면 호출
                onPressEnter={handleKeyPress} // 엔터 버튼 눌르면 호출
                style={{ width: '80%' }}
            />
        </Col>
    );
}


export default SearchInLandingPage;
