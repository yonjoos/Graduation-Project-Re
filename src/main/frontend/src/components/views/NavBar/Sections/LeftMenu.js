// 메인 화면의 좌측 상단 버튼은 관리하는 페이지

import React from 'react';
import { Menu } from 'antd';

// 각 코드는 메인 화면의 좌측 상단 버튼처럼 보이게 하므로, 이렇게 코드를 짜는구나 라고 알면 될 듯
function LeftMenu(props) {
  // RightMenu에서 return되는 형식과 동일하지만
  // favorite 대신에 ...이 뜨는 이유는 아마 좌측 상단의 칸이 부족해서 그런게 아닐까 하는 추측 => width로 임시방편 설정
  
  // 일반 유저가 보는 좌측 NavBar

    return (
      <Menu mode={props.mode}>
        <Menu.Item key="mail">
          {/* Home 버튼 누르면 LandingPage로 갈 수 있도록 href지정 */}
          <a href="/">Home</a>
        </Menu.Item>
      </Menu>
    )
}

export default LeftMenu