import { useState } from "react";
import { Col, Input } from "antd";

const { Search } = Input;

// 스터디 페이지에서 검색어 입력과 엔터 및 클릭 버튼을 위임받은 하위 검색 컴포넌트
function SearchInStudyPage({ setSearchTerm }) {
    const [localSearchTerm, setLocalSearchTerm] = useState(""); // 독자적인 자기 자신의 검색어 입력 필드 값 가지고 세팅. 추후에 handleSearch에 의해 이 값이 상위로 넘어갈 것임

    const handleSearch = (value) => {
        setSearchTerm(value);
    };

    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            handleSearch(localSearchTerm);
        }
    };

    return (
        <Col span={24} style={{ textAlign: 'center' }}>
            <Search
                placeholder="키워드를 검색해보세요!"
                value={localSearchTerm}
                onChange={(event) => setLocalSearchTerm(event.target.value)}
                onSearch={handleSearch} // 돋보기 버튼 클릭하면 호출
                onPressEnter={handleKeyPress} // 엔터 버튼 눌르면 호출
                style={{ width: 800 }}
            />
        </Col>
    );
}


export default SearchInStudyPage;
