import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Dropdown, Menu } from 'antd';

function CustomDropdown(props) {
    const navigate = useNavigate();

    const userItems = [
        {
            key: '1',
            label: 'My Page',
            link: '/myPage',
        },
        {
            key: '2',
            label: 'My Portfolio',
            link: '/portfolio',
        },
        {
            key: '3',
            label: 'My Group',
            link: '/group',
        },
        {
            key: '4',
            label: 'My Scrap',
            link: '/scrap',
        },
        {
            key: '5',
            label: 'Logout',
            onClick: props.handleLogout,
        },
    ];

    const adminItems = [
        {
            key: '1',
            label: 'Admin Page',
            link: '/adminPage',
        },
        {
            key: '2',
            label: 'My Page',
            link: '/myPage',
        },
        {
            key: '3',
            label: 'My Portfolio',
            link: '/portfolio',
        },
        {
            key: '4',
            label: 'My Group',
            link: '/group',
        },
        {
            key: '5',
            label: 'My Scrap',
            link: '/scrap',
        },
        {
            key: '6',
            label: 'Logout',
            onClick: props.handleLogout,
        },
    ];

    const menu = (
        <Menu>
            {props.userRole === 'USER' && userItems.map(item => (
                <div>
                    <Menu.Item key={item.key} onClick={item.onClick || (() => navigate(item.link))}>
                        {item.label}
                    </Menu.Item>
                </div>
            ))}
            {props.userRole === 'ADMIN' && adminItems.map(item => (
                <Menu.Item key={item.key} onClick={item.onClick || (() => navigate(item.link))}>
                    {item.label}
                </Menu.Item>
            ))}
        </Menu>
    );

    return (
        <Dropdown overlay={menu} placement="bottomRight" arrow>
            <Button type="text" value="large" style={{ color: 'black', background: 'transparent', fontSize: '18px', height: '10vh', }}>Me</Button>
        </Dropdown>
    );
}

export default CustomDropdown;