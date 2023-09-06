import { useState } from "react";
import { Col, Input } from "antd";

const { Search } = Input;

function SearchInPortfolioCardPage({ setSearchTerm }) {

    const [localSearchTerm, setLocalSearchTerm] = useState(""); // 독자적인 자기 자신의 검색어 입력 필드 값 가지고 세팅. 추후에 handleSearch에 의해 이 값이 상위로 넘어갈 것임

    const handleSearch = (value) => {
        setSearchTerm(value);
    };

    // 엔터키 눌렀을 때도 되게 하는 함수용, 왜냐하면 handleSearch 만으로는 돋보기 클릭만 됨
    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            handleSearch(localSearchTerm);
        }
    };

    return (
        <Col span={24} style={{ textAlign: 'center' }}>
            <Search
                placeholder="유저를 검색해보세요!"
                value={localSearchTerm}
                onChange={(event) => setLocalSearchTerm(event.target.value)}
                onSearch={handleSearch} // 돋보기 버튼 클릭하면 호출
                onPressEnter={handleKeyPress} // 엔터 버튼 눌르면 호출
                style={{ width: 800 }}
            />
        </Col>
    );
}



export default SearchInPortfolioCardPage;

