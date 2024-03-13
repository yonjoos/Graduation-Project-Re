import { NavLink } from 'react-router-dom';
import DropDown from './components/ui/DropDown';

import { ListType } from './components/ui/DropDown';
import { Action } from './components/ui/DropDown';
const list: ListType[] = [
  { title: 'Settings', path: '/my' },
  { title: 'My Portfolio', path: '/myportfolio' },
];
const actions: Action[] = [
  {
    title: 'Sign-out',
    action: () => {
      console.log('log out');
    },
  },
];

export const Nav = () => {
  return (
    <>
      <nav className='flex items-center justify-center h-16 bg-main-color text-zinc-50 font-semibold text-lg'>
        <div className='flex justify-between w-outer-layer'>
          <NavLink to='/'>Logo</NavLink>

          <span className='flex gap-20 relative'>
            <NavLink to='/notification'>Notification</NavLink>
            <DropDown list={list} action={actions}>
              myInfo
            </DropDown>
          </span>
        </div>
      </nav>
    </>
  );
};
