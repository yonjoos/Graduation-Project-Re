import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Dropdown, Menu } from 'antd';
import { useState, useEffect } from 'react';
import { request } from '../../../../hoc/request';






function CustomDropdown(props) {

    const [profileImage, setProfileImage] = useState(null); //프사 띄우는 용도


useEffect(()=>{

        request('GET', '/userProfileImage')
            .then((response) => {
                console.log(response.data.imageUrl);
                setProfileImage(response.data.imageUrl);
            })
            .catch((error) => {
                console.error("Error fetching profile image:", error);
            });

    }, [profileImage])
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
                <Menu.Item key={item.key} onClick={item.onClick || (() => navigate(item.link))}>
                    {item.label}
                </Menu.Item>
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
            <Button type="text" value="large" style={{ color: 'black', background: 'transparent', fontSize: '18px', height: '10vh', }}>
                <img
                    style={{ borderRadius: '50%', width: '40px', height: '40px', border: '3px solid salmon'}}
                    src={`https://storage.googleapis.com/hongik-pickme-bucket/${profileImage}`}
                />
            </Button>
        </Dropdown>
    );
}

export default CustomDropdown;