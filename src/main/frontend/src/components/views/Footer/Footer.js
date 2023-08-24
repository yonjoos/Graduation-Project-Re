import React from "react";
import { Layout, Typography, Row, Col } from "antd";
import { useNavigate } from "react-router-dom"; // Import the useNavigate hook

const { Footer } = Layout;
const { Text } = Typography;

function BottomFooter(props) {
    const navigate = useNavigate(); // Initialize the useNavigate hook

    return (
        <div style={{ width: "100%" }}>
            <Footer style={{ backgroundColor: "white", textAlign: "center", marginTop: "200px"}}>
                <Row type="flex" justify="center">
                    <Col xs={8} style={{ paddingLeft : '20%' }}>
                        {/* * Use the navigate function to navigate
                        <div
                            onClick={() => navigate("/")} // Navigate to the root route
                            style={{ cursor: "pointer", display: "inline-block", fontSize: "20px", textDecoration: "none", marginLeft: "82%" }}
                        >
                            <div style={{ color: "black", fontWeight: "bold" }}>P
                            <span style={{ color: "dodgerblue" }}>!</span>
                            ck Me</div>
                        </div> */}
                        <div onClick={() => navigate("/")}>
                        <img
                            src={props.logoSrc}
                            className="App-logo"
                            alt="logo"
                           
                            style={{ cursor: 'pointer' }}
                        />
                        </div>
                    </Col>
                    <Col xs={16} style={{ paddingRight : '20%', paddingTop : '25px' }}>
                        <Text style={{ fontWeight: "bold" }}>Team Name: 서울고양이</Text>
                        <br />
                        <Text type="secondary">Production team members: 이윤식, 박시홍, 정연주</Text>
                    </Col>
                </Row>
            </Footer>
        </div>
    );
}

export default BottomFooter;