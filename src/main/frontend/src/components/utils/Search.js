import { useState } from "react";
import { Col, Input } from "antd";

/*
######################################################################################################################################
#############################################  /  Search.js IN Antd  /  ##############################################################
######################################################################################################################################

** 기능 : 검색창 컴포넌트 반환

** 구성요소 
1) SearchComponent 함수 : ANTD lib의 <Input> 의 확장인 <Search> component를 사용해 검색창 모양을 반환함
2) SearchHandler 함수 : SearchComponent안의 'event listener 함수'
    - Event Listener 는 대체로 컴포넌트 안의 onClick, 'on~' 속성에 붙는 것으로 정의된다.
    - 즉, Event Listener 라는 특별한 함수를 따로 정의하는 방법은 없고, onEvent = "" 에 들어가는 함수가 Event Listenr 가 된다

** 동작 원리
1) SearchComponent 가 실행되면 html 형식의 텍스트가 return 됨
2) return 값 안의 text, 즉 컴포넌트 혹은 태그 안의 속성인 onClick 함수가 실행됨


######################################################################################################################################
#############################################  /  useState and React  /  #############################################################
######################################################################################################################################

** useState : React library의 hooker
** hooker : React library 에 접근할 수 있게 해주는 함수

- * - 즉 useState 는 React Library에 접근할 수 있게 해주는 도구


** useState 의 동작원리

1) useState는 크기가 2인 array를 반환한다
2) 첫 번째 원소는, state를 저장할 변수
3) 두 번째 원소는, state를 변경할 수 있는 함수로 주로 'set~' 를 관례로 이름붙여 사용함
4) 따라서 기본적인 틀은 다음과 같다 : [변수, 변수를 업데이트할 함수] = useState 



** useState 의 사용

1) 새로운 함수를 정의하여 그 안에 useState 의 리턴 함수를 실행시키는 것으로 사용, 즉 새로운 함수로 encapsulate 하여 사용
2) [Term, setTerm] = useState(""); 으로 정의를 했다면
3) const newFunc = (event) => { setTerm(event.currentTarget.value) } 의 형태로 사용한다
    3.1) 이렇게 정의된 함수는 "onClick = { newFunc( event ) }" 이런 식으로 Event Listener 로 사용됨
4) 위와 같이 encapsulate 하는 이유는, 소프트공학적인 이유(유지보수, 독립성, etc) 때문이다.
5) 반대로, setTerm 을 바로 Event Listener로 붙여도 상관 없다
6) onClick = { setTerm(event.currentTarget.value) } 이렇게 사용해도 된다는 뜻
7) 6 번과 같이 사용해도 논리적, 기능적으로는 문제가 없으나 효율성이 떨어짐

+) 추가로, const newFunc = (event) => { setTerm(event.currentTarget.value) } 에서
    set 함수의 매개변수로 "event.currentTarget.value" 가 들어가는 이유는 !
    >>> 이 함수가 Input 컴포넌트의 이벤트 리스너이기 때문이다
    >>> Input 컴포넌트의 input값은 대부분 DOM 문서이기 때문에
    >>> newFunc의 input 값인 event는 DOM 문서가 되고, 이를 사용하는 방법은 event.currentTarget 로 고정적이기 때문


######################################################################################################################################
#############################################  /  SUMMARY  /  ########################################################################
######################################################################################################################################

There are 4 Libraries Used

1) React Library : for UI, maintaining "Component's State"
2) Redux Library : for storing, calling "Global State" on Client side memory
    2.1) ACTIONS, STORE and REDUCERS are MANDATORY
    2.2) 글로벌 상태를 관리하기 때문에 그 상태를 관리하는 로직 패턴이 반드시 필요하다. 위의 3 가지 기능 없이는 라이브러리 사용이 불가능함
3) react-redux : an extension of React Library, React 와 Redux의 conjunction을 위한 라이브러리
4) antd : 디자인 라이브러리로 이 문서에서는 <Input> 가 사용되었다


** React lib 와  Redux lib 의 관계
    - React는 개별 기능 컴포넌트를 만들고, 개별기능 상태를 관리한다
    - Redux는 주로 사용자의 정보를 담고있다가 App가 사용자 정보나 상태가 필요할 때마다 제공하고 업데이트함

    - 이 둘을 연결하는 것이 react-redux library다 




** react-redux library 란?
    - React 와 Redux가 데이터를 쉽게 주고받을 수 있도록 지원하는 라이브러리로 React Library의 extension
    - 대표적인 컴포넌트로는 <Provider store = ""> 가 있다
        * <Provider> 의 사용
            - <Provider store = > 에서 store 값으로 Redux 라이브러리로 작성된 store object를 넣어줌으로써 
            - App.js 가 실행되는 동안, store 값에 저장된 redux library의 state에 useSelector로 접근할 수 있다.


** antd 라이브러리
    - 디자인을 위한 라이브러리
    - React 라이브러리가 하나의 페이지에서 어떠한 기능을 가진 컴포넌트를 만든다면
    - antd 라이브러리는 어떤 기능을 가진 컴포넌트를 더 예쁘게 만들어줌....

######################################################################################################################################
*/

// Input을 Search로 받아주어야 검색창 옆에 돋보기 모양이 생성됨
const { Search } = Input;

function SearchComponent() {
    // 검색 창에 글자를 한 개씩 입력할 때마다 SearchTerm이 달라짐
    const [SearchTerm, setSearchTerm] = useState("")
    

    // EVENT LISTENER for <Input>(= <Search> )
    // Input event : DOM file
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