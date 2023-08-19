// 사이트 최하단에 있는 영역으로 보통 저작권 정보나 개인정보처리 방침, 이용약관처럼 정책 페이지 링크를 두기 위해 존재

import React from "react";
import { Layout, Typography, Row, Col } from "antd";

const { Footer } = Layout;
const { Text } = Typography;

function BottomFooter() {
    return (
        <div style={{ width: "100%" }}>
            <Footer style={{ backgroundColor: "white", textAlign: "center", marginTop: "200px"}}>
                <Row type="flex" justify="center">
                    <Col xs={8}>
                        {/** textDecoration: "none" -> 하이퍼링크로 인해 생기는 언더라인을 없애줌 */}
                        <a href="/" style={{ display: "inline-block", fontSize: "20px", textDecoration: "none", marginLeft: "82%" }}>
                            <div style={{color : 'black', fontWeight: 'bold'}}>P
                            <span style={{color : 'dodgerblue'}}>!</span>
                            ck Me</div>
                        </a>
                    </Col>
                    <Col xs={16}>
                        <Text style={{ fontWeight: "bold" }}>팀 이름 : &nbsp;&nbsp; 서울고양이</Text>
                        <br />
                        <Text type="secondary">제작 팀원 : &nbsp;&nbsp; 이윤식, 박시홍, 정연주</Text>
                    </Col>
                </Row>
            </Footer>
        </div>
    )
}

export default BottomFooter