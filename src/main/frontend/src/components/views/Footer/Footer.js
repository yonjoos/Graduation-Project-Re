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
                    <Col xs={8}>
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
                    <Col xs={16}>
                        <Text style={{ fontWeight: "bold" }}>Team Name: Seoul Cat</Text>
                        <br />
                        <Text type="secondary">Production team members: Lee Yoon-sik, Park Si-hong, Jeong Yeon-ju</Text>
                    </Col>
                </Row>
            </Footer>
        </div>
    );
}

export default BottomFooter;