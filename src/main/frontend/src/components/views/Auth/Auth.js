// 로그인된 회원만 볼 수 있는 페이지
import React, { useState, useEffect } from 'react';
import { Card, Row, Col } from 'antd';
import { request } from '../../../hoc/request';

/*
######################################################################################################################################
#############################################  /  Callback VS Arrow VS Anonymous  FUNCTION/  #########################################
######################################################################################################################################

    -------------- There are 3 types of functions commonly used in javaScript ---------------------

    1) Callback function : 다른 함수의 arg 로 들어가는 함수
    2) Anonymous function  : function(arg) {..}
    3) Arrow function : () => {..}
   
    +) 람다함수, not used in javaScript but typically used in Python. 람다함수보다는 arrow function 을 사용함


    -------------- 각 함수 비교하기 ---------------------

    1) Callback function : 다른 함수의 argument 로 들어가는 함수

        - callback 함수는 여러 방법으로 정의될 수 있다

            1_ Arrow function 으로 정의
                    ex > function( Arrow = () => {...} );

                    * 여기에서 Arrow 라는 arrow 함수가 콜백함수, function의 매개변수이기 때문

            2_ 일반 함수로 정의
                    ex >   
                        func_1() {...}
                        func_2(func_1) {...}

                        * 여기에서 func_1 가 바로 func_2의 콜백함수, func_2의 매개변수이기 때문






    2) Anonymous function : 이름이 없는 함수로 아래와 같이 사용될 수 있다
        1_ Callback functions
                ex > const squaredNumbers = numbers.map(function(number) {
                        return number * number;
                    });

                    * 여기에서, .map 안에 있는 function(number) 가 callback 함수

        2_ IIFE (Immediately Invoked Function Expression)
        3_ Event Handlers in React
        4_ Anonymous functions can be used in higher-order functions like filter, reduce, and forEach.
        5_ functional component definitions in React





                    

    3) Arrow function : Anonymous function 의 한 종류, a subtype of Anonymousu function
        - it can have a name or not
        - when used as a callback, 이름 ㄴㄴ: () => {...}
        - when has a name, 이름 ㅇㅇ : const name = () => {...}








    -------------------- / Summary / -------------------------------
    -------------------- / Summary / -------------------------------
    -------------------- / Summary / -------------------------------

     - 콜백함수는 다른 함수의 매개변수로 들어가는 함수를 뜻함
     - 콜백 함수는 arrow function, 일반 함수, anonymous 함수로 작성될 수 있음
     - arrow function은 anonymous 함수의 한 종류

*/

/*

######################################################################################################################################
#############################################  /  useEffect /  #######################################################################
######################################################################################################################################

    ** useEffect : side effects 를 관리하는 hook
                    주로 언제 사용되냐면 :
                    1_ interacting(fetching) with external resources(APIs, DOM)
                    2_ (un)subscribing data streams (변경감지라 생각하면 될 듯), somehow similar to event handler
                    3_ Updating component state based on props changes

    ** [main component] VS [side effects]
        - main components : React 로 작성된 함수나 컴포넌트의 기능
        - side effects : React로 작성된 컴포넌트가 작동되기 위한 자잘한 준비들
                    ex > 어떤 컴포넌트의 메인 기능이 회원 이름 띄우는 것이라면, side effects 는 서버나 클라이언트로부터 회원 이름을 가져오는 것.
                        이름을 표시하는 것이 컴포넌트의 기능이자 목적이기 때문에 이름을 띄우기 위해 가져오는 것은 side effect
    
        - side effects INVOLVE : 
                    1_ interacting(fetching) with external resources(APIs, DOM)
                    2_ (un)subscribing data streams (변경감지라 생각하면 될 듯), somehow similar to event handler
                    3_ Updating component state based on props changes
    
    
    
------------- useEffect 의 구조 ------------------

 ** useEffect 는 2 개의 arguments 로 이루어져있다
    첫 번째는, side effect를 정의한 콜백함수
    두 번째는, dependencies array로, 이 array 안에 있는 값들이 변하면 useEffect의 코드가 실행됨

    구조 : useEffect( () => {...} , [ arg1, arg2 ] )





------------- useEffect 의 작동 방식 ------------------


 ** useEffect의 2 번째 arguments 유무에 따른 작동조건

    - dependencies array 가 비어있다면, 컴포넌트가 처음 실행될 때만 작동
    - dependency 가 있다면, 컴포넌트가 처음 실행될 때에 한번 실행된 후에 dependency가 바뀔 때마다 계속 실행됨

    * 즉, array가 비어있든 아니든 어쨌든 첫 한번은 무조건 실행됨


 ** useEffect 가 한 컴포넌트 내에 여러 개 있으면, initially rendering 할 때 뭐가 먼저 실행?
    - 선언된 순서대로 실행된다
    - 맨 위에 적혀있는 것부터 순서대로



######################################################################################################################################
#############################################  /  useEffect VS event handler /  ######################################################
######################################################################################################################################

    useEffects also detect changes of dependencies, then what makes it so different from event handler?
    
    - useEffects usually interacts with data sources, actions
    - event handler interacts with User



######################################################################################################################################
#############################################  /  .then .catch /  ######################################################
######################################################################################################################################

    ** When and How do we use ".then .catch" ??
        - Promise 객체에 .then과 .catch를 사용할 수 있음
        - Promise 객체의 생성자는 Promise( (resolve, reject)  => { } ) 이런 식으로 생겼는데
        - Promise 객체는 어떤 조건에 따라 성공하면 reslove 함수를 실행하고, 실패하면 reject를 실행한다
        - 참고로 Promise 의 콜백함수 안에 들어간 resolve 와 reject 는 함수다. 성공하면 실행할 함수와 실패하면 실행할 함수

    ** request 함수는 자세히는 모르지만, Promise객체를 반환하는 함수일 것
        - request 함수는 resolve와 reject 함수를 선언한 뒤에 Promise의 파라미터로 pass 함
        - 그렇게 만들어진 Promise 는 request 함수의 리턴값이 되고
        - 따라서 request(..).then().catch() 가 사용이 가능한 것
        - 또한 then의 파라미터로 들어가는 콜백함수의 response는 request 내에 선언된 변수로부터 (어떻게든) 만들어져 반환된 데이터값
        - 따라서 .then(res) 에서 res 가 뭐일지는 request함수를 알거나 request 내의 Promise 콜백함수를 살펴봐야함


######################################################################################################################################
*/

function Auth() {
    const [data, setData] = useState([]);

    useEffect(() => {
        request('GET', '/messages', {})
            .then((response) => {
                setData(response.data);
            })
            .catch((error) => {
                // Handle error, e.g., redirect to login or display an error message
                console.error("Error fetching data:", error);
            });
    }, []);
    

    return (
        <Row justify="center" style={{ marginTop: '20px' }}>
            <Col xs={24} sm={16} md={12} lg={8}>
                <Card title="Backend Response" style={{ width: '100%' }}>
                    <p>Content:</p>
                    <ul>
                        {data.map((line, index) => (
                            <li key={index}>{line}</li>
                        ))}
                    </ul>
                </Card>
            </Col>
        </Row>
    );
}

export default Auth;
