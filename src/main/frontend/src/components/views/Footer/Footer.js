import React from "react";
import { Layout, Typography, Row, Col } from "antd";
import { useNavigate, useLocation, Link } from "react-router-dom"; // Import the useNavigate hook

const { Footer } = Layout;
const { Text } = Typography;

function BottomFooter(props) {
    const navigate = useNavigate(); // Initialize the useNavigate hook
    const location = useLocation();

    const renderFooter = () => {
        return (
            !location.pathname.includes('/project/detail/notify/') &&
            !location.pathname.includes('/study/detail/notify/')
        );
    };

    return (
        <div style={{ width: "100%" }}>
            <Footer style={{ backgroundColor: "white", textAlign: "center", marginTop: "200px"}}>
                <Row type="flex" justify="center">
                    <Col xs={12} style={{ paddingTop: '5px' }}>
                        {renderFooter() ? (
                            <div onClick={() => navigate("/")}>
                            <img
                                src={props.logoSrc}
                                className="App-logo"
                                alt="logo"
                            
                                style={{ cursor: 'pointer', maxWidth: '200px', maxHeight: '40px' }}
                            />
                            </div>
                        ) : (
                            <img
                                src={props.logoSrc}
                                className="App-logo"
                                alt="logo"
                            
                                style={{ maxWidth: '200px', maxHeight: '40px' }}
                            />
                        )}

                    </Col>
                    <Col xs={12} style={{ paddingTop: '5px' }}>
                        <Link
                            to={`/about`}
                        >
                            <Text style={{ fontWeight: "bold" }}>About Us</Text>
                        </Link>
                        <br />
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