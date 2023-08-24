import { useState } from "react";
import { Col, Input } from "antd";

// Input을 Search로 받아주어야 검색창 옆에 돋보기 모양이 생성됨
const { Search } = Input;

function SearchComponent() {
    // 검색 창에 글자를 한 개씩 입력할 때마다 SearchTerm이 달라짐
    const [SearchTerm, setSearchTerm] = useState("")
    
    const searchHandler = (event) => {
        setSearchTerm(event.currentTarget.value)
    }

    return (
        <Col span={24} style={{ textAlign: 'center' }}>
            <Search
                placeholder="키워드를 검색해보세요!"
                onChange={searchHandler} // 바로 위에서 함수로 구현
                style={{ width: 800 }}
            />
        </Col>
    )
}

export default SearchComponent;