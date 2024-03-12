import { useEffect, useState } from "react";
import { useSelector/*, useDispatch*/ } from "react-redux";
import { EventSourcePolyfill } from "event-source-polyfill";
import { getAuthToken } from "../../hoc/request";
import { notification } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import { message } from "antd";
import { request/*, setLastVisitedEndpoint, setLastLastVisitedEndpoint, setLastLastLastVisitedEndpoint*/ } from "../../hoc/request";
//import { lastVisitedEndpoint } from "../../_actions/actions";

function Notifications() {
    const navigate = useNavigate();
    //const dispatch = useDispatch();
    const location = useLocation();
    const currentEndpoint = location.pathname;
    const nickName = useSelector(state => state.auth.userNickName);
    const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
    //const visitedEndEndEndpoint = useSelector(state => state.endpoint.lastVisitedEndEndEndpoint);
    const [messages, setMessages] = useState([]);


    useEffect(() => {

        if (!isAuthenticated) {
            return; // 인증되지 않은 경우 SSE 연결하지 않음
        }

        console.log("Attempting to connect to SSE...");
        console.log("nickName : ", nickName);
        const authToken = getAuthToken(); // Retrieve JWT token
        const eventSource = new EventSourcePolyfill(`http://localhost:9090/sse/subscribe/${nickName}`, {
            headers: {
                Authorization: `Bearer ${authToken}`, // Include JWT token in headers
            },
            withCredentials: true,
            heartbeatTimeout: 600000,    // 600000ms동안 한 번의 SSE가 열려있음. 즉, 600초(10분)간 연결 유지 후 연결 종료 후 새로운 다음 SSE 연결 시도
        });

        // 백엔드의 sendToClient함수의 send의 인자인 .name("sse")와 연관됨.
        eventSource.addEventListener('sse', event => {
            console.log("event", event);
            // 서버에서 온 알림을 표시합니다.
            const eventData = JSON.parse(event.data); // event.data를 JSON 객체로 파싱
            const newMessage = eventData.message; // "message" 속성의 값 추출
            let editedMessage = eventData.message; // 실제로 알림 메세지에 들어갈 메세지
            let extractedContent = ""; // 첫번째 : 를 기반으로 문자열 추출하는 데 사용됨
            let extractedNotificationId = null; // notificationId를 추출할 변수

            // "EventStream Created" 메시지 필터링 -> 사용자가 직접 작성한 게시물 제목, 또는 닉네임과 상관 없이 백엔드에서 EventStream Created란 문자열로 시작하는 게 있으면 거르므로 문제 없음
            if (newMessage.startsWith("EventStream Created")) {
                return;
            }

            // 만약 editedMessage 즉 newMessage가 study 또는 project로 시작한다면 
            if (editedMessage.startsWith("study") || editedMessage.startsWith("project")) {
                const firstColonIndex = editedMessage.indexOf(":"); // 첫 번째 ":"의 위치를 찾는다

                if (firstColonIndex !== -1) { // 만약 : 가 있다면
                    // 첫 번째 ":" 다음의 내용을 추출
                    extractedContent = editedMessage.slice(firstColonIndex + 1).trim();

                    // 숫자를 추출하기 위한 정규 표현식
                    const regex = /(\d+)$/; // 문자열 끝에 있는 숫자를 찾음
                    const match = extractedContent.match(regex);

                    if (match) {
                        extractedNotificationId = parseInt(match[1], 10); // 추출된 숫자를 정수로 변환
                        // 숫자를 제외한 내용을 description에 넣기 위해 extractedContent를 업데이트
                        extractedContent = extractedContent.replace(match[0], "").trim();
                    }
                }
            }

            const notificationKey = new Date().getTime().toString(); // 고유한 키 생성

            console.log('newMessage : ', event.data);
            setMessages(prevMessages => [...prevMessages, newMessage]);



            // antd의 notification을 사용하여 알림을 표시
            notification.info({
                message: "New Notification",
                description: extractedContent, // 가공된 메세지를 렌더링 (실제 실시간 알림을 알려주는 카드 내용에만 extractedContent를 넣고, 실제 프론트 상에서 사용하는 건 newMessage임)
                duration: 30,   // 알림이 떠있는 시간 30초로 설정
                key: notificationKey, // 위에서 생성된 고유한 키 설정
                style: {
                    backgroundImage: "linear-gradient(-20deg, #e9defa 0%, #fbfcdb 100%)",
                    cursor: 'pointer',
                },
                // notifications를 볼 수 있도록, 또는 해당 디테일 페이지로 이동할 수 있도록 하기
                onClick: () => {

                    // 알림을 읽으면, Notifications table의 checked를 true로 바꾸기 위해 put request 전송
                    request('PUT', `sse/checkNotification/${extractedNotificationId}`, {})
                        .then((response) => {
                            console.log("알림을 읽었습니다.");
                        })
                        .catch((error) => {
                            console.log("Error fetching data:", error);
                            message.error('데이터베이스에서 checked를 true로 바꾸는데 실패했습니다.');
                        });

                    //const currentEndpoint = location.pathname;
                    console.log('현재 path',currentEndpoint);

                    if (newMessage.startsWith("project")) { // 만약 newMessage가 project로 시작하면, project와 연관된 알림임
                        const regex = /project\/detail\/(\d+)/;
                        const match = newMessage.match(regex);
                        if (match) {
                            const postId = match[1]; // 백엔드에서 넘어온 게시물 id를 추출

                            if(!currentEndpoint.startsWith("/project/detail/") && !currentEndpoint.startsWith("/study/detail/"))
                            {
                                //dispatch(lastVisitedEndpoint(currentEndpoint, currentEndpoint, '/portfoliocard'));    // 전역에 상태 저장을 위한 
                                //setLastVisitedEndpoint(currentEndpoint);   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
                                //setLastLastVisitedEndpoint(currentEndpoint);
                                //setLastLastLastVisitedEndpoint('/portfoliocard');
                            }
                            
                            console.log('last2', currentEndpoint);
                            console.log('last-last2',currentEndpoint);
                            // // 새 창을 열어서 페이지를 띄우기
                            // const newWindow = window.open(`/project/detail/notify/${postId}`, '_blank');
                            // if (newWindow) {
                            //     newWindow.opener = null; // 새 창에서 브라우저 열기
                            // } else {
                            //     message.error('팝업 창을 열 수 없습니다. 팝업 차단 설정을 확인하세요.');
                            // }
                            navigate(`/project/detail/${postId}`); // 해당 게시물로 올바르게 navigate
                        }
                    } else if (newMessage.startsWith("study")) { // 만약 newMessage가 study로 시작하면, study와 연관된 알림임
                        const regex = /study\/detail\/(\d+)/;
                        const match = newMessage.match(regex);
                        if (match) {
                            const postId = match[1]; // 백엔드에서 넘어온 게시물 id를 추출

                            if(!currentEndpoint.startsWith("/project/detail/") && !currentEndpoint.startsWith("/study/detail/"))
                            {
                                //dispatch(lastVisitedEndpoint(currentEndpoint, currentEndpoint, '/portfoliocard'));    // 전역에 상태 저장을 위한 
                                //setLastVisitedEndpoint(currentEndpoint);   // 새로고침 문제를 해결하기 위한 애. 로컬스토리지에 저장.
                                //setLastLastVisitedEndpoint(currentEndpoint);
                                //setLastLastLastVisitedEndpoint('/portfoliocard');
                            }
                            console.log('last2', currentEndpoint);
                            console.log('last-last2',currentEndpoint);
                            
                            // // 새 창을 열어서 페이지를 띄우기
                            // const newWindow = window.open(`/study/detail/notify/${postId}`, '_blank');
                            // if (newWindow) {
                            //     newWindow.opener = null; // 새 창에서 브라우저 열기
                            // } else {
                            //     message.error('팝업 창을 열 수 없습니다. 팝업 차단 설정을 확인하세요.');
                            // }
                            navigate(`/study/detail/${postId}`); // 해당 게시물로 올바르게 navigate
                        }
                    }

                    // 고유한 키를 통해, 클릭 시 해당 알림만 닫기
                    notification.destroy(notificationKey);
                }
            });
        });

        eventSource.onopen = () => {
            console.log("SSE connection opened.");
            console.log('eventSource', eventSource);
        };

        eventSource.onmessage = (event) => {
            try {
                console.log("SSE message received:", event.data);
                const eventData = JSON.parse(event.data); // event.data를 JSON 객체로 파싱
                const newMessage = eventData.message; // "message" 속성의 값 추출
                setMessages(prevMessages => [...prevMessages, newMessage]);
            } catch (error) {
                console.error("Error in onmessage:", error);
            }
        };

        eventSource.onerror = (error) => {
            console.error("SSE error:", error);
        };

        return () => {
            eventSource.close();
            console.log("SSE connection closed.");
        };
    }, [isAuthenticated, nickName, currentEndpoint]);


}

export default Notifications;

// import { useEffect, useState } from "react";
// import { useSelector, useDispatch } from "react-redux";
// import { EventSourcePolyfill } from "event-source-polyfill";
// import { getAuthToken } from "../../hoc/request";
// import { notification } from "antd";
// import { useNavigate, useLocation } from "react-router-dom";
// import { message } from "antd";
// import { request } from "../../hoc/request";

// function Notifications() {
//     const navigate = useNavigate();
//     const dispatch = useDispatch();
//     const location = useLocation();
//     const currentEndpoint = location.pathname;
//     const nickName = useSelector(state => state.auth.userNickName);
//     const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
//     const [messages, setMessages] = useState([]);

//     useEffect(() => {
//         if (!isAuthenticated) {
//             return; // 인증되지 않은 경우 SSE 연결하지 않음
//         }

//         console.log("Attempting to connect to SSE...");
//         console.log("nickName : ", nickName);
//         const authToken = getAuthToken(); // JWT 토큰 가져오기
//         const eventSource = new EventSourcePolyfill(`http://localhost:9090/sse/subscribe/${nickName}`, {
//             headers: {
//                 Authorization: `Bearer ${authToken}`, // 헤더에 JWT 토큰 포함
//             },
//             withCredentials: true,
//             heartbeatTimeout: 600000,
//         });

//         // Broadcast 채널 생성
//         const notificationChannel = new BroadcastChannel("BroadcastNotification");
//         console.log('notificationChannel', notificationChannel)

//         eventSource.addEventListener('sse', event => {
//             console.log("event", event);
//             const eventData = JSON.parse(event.data);
//             const newMessage = eventData.message;
//             let editedMessage = eventData.message;
//             let extractedContent = "";
//             let extractedNotificationId = null;

//             if (newMessage.startsWith("EventStream Created")) {
//                 return;
//             }

//             if (editedMessage.startsWith("study") || editedMessage.startsWith("project")) {
//                 const firstColonIndex = editedMessage.indexOf(":");

//                 if (firstColonIndex !== -1) {
//                     extractedContent = editedMessage.slice(firstColonIndex + 1).trim();
//                     const regex = /(\d+)$/;
//                     const match = extractedContent.match(regex);

//                     if (match) {
//                         extractedNotificationId = parseInt(match[1], 10);
//                         extractedContent = extractedContent.replace(match[0], "").trim();
//                     }
//                 }
//             }

//             const notificationKey = new Date().getTime().toString();

//             console.log('newMessage : ', event.data);
//             setMessages(prevMessages => [...prevMessages, newMessage]);

//             // Broadcast 채널을 통해 모든 열린 창/탭에 알림 브로드캐스트
//             notificationChannel.postMessage({
                
//                 message: {
//                     notificationKey,
//                     extractedContent,
//                     extractedNotificationId,
//                     newMessage,
//                 },
//             });
//         });

//         eventSource.onopen = () => {
//             console.log("SSE connection opened.");
//             console.log('eventSource', eventSource);
//         };

//         eventSource.onmessage = (event) => {
//             try {
//                 console.log("SSE message received:", event.data);
//                 const eventData = JSON.parse(event.data);
//                 const newMessage = eventData.message;
//                 setMessages(prevMessages => [...prevMessages, newMessage]);
//             } catch (error) {
//                 console.error("Error in onmessage:", error);
//             }
//         };

//         eventSource.onerror = (error) => {
//             console.error("SSE error:", error);
//         };

//         // 알림 채널에서 메시지 수신 대기
//         notificationChannel.addEventListener("message", (event) => {

//             console.log('broadcast', event.data.message);
//             console.log('broad', event.data);

            
//                 const {
//                     notificationKey,
//                     extractedContent,
//                     extractedNotificationId,
//                     newMessage,
//                 } = event.data.message;

//                 // antd의 notification을 사용하여 알림을 표시
//                 notification.info({
//                     message: "New Notification",
//                     description: extractedContent,
//                     duration: 30,
//                     key: notificationKey,
//                     style: {
//                         backgroundImage: "linear-gradient(-20deg, #e9defa 0%, #fbfcdb 100%)",
//                         cursor: 'pointer',
//                     },
//                     onClick: () => {
//                         request('PUT', `sse/checkNotification/${extractedNotificationId}`, {})
//                             .then((response) => {
//                                 console.log("알림을 읽었습니다.");
//                             })
//                             .catch((error) => {
//                                 console.log("Error fetching data:", error);
//                                 message.error('데이터베이스에서 checked를 true로 바꾸는데 실패했습니다.');
//                             });

//                         console.log('현재 path', currentEndpoint);

//                         if (newMessage.startsWith("project")) {
//                             const regex = /project\/detail\/(\d+)/;
//                             const match = newMessage.match(regex);
//                             if (match) {
//                                 const postId = match[1];
//                                 navigate(`/project/detail/notify/${postId}`);
//                             }
//                         } else if (newMessage.startsWith("study")) {
//                             const regex = /study\/detail\/(\d+)/;
//                             const match = newMessage.match(regex);
//                             if (match) {
//                                 const postId = match[1];
//                                 navigate(`/study/detail/notify/${postId}`);
//                             }
//                         }

//                         // 고유한 키를 통해, 클릭 시 해당 알림만 닫기
//                         notification.destroy(notificationKey);
//                     }
//                 });
            
//         });

//         return () => {
//             eventSource.close();
//             notificationChannel.close();
//             console.log("SSE connection closed.");
//         };
//     }, [isAuthenticated, nickName, currentEndpoint]);

//     // 알림 UI를 렌더링
// }

// export default Notifications;
