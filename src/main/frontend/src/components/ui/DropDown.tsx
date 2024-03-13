/* eslint-disable react/no-array-index-key */
import { PropsWithChildren, ReactNode, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export type ListType = {
  title: string;
  path: string;
};

export type Action = {
  title: string;
  action: () => void;
};

export type Prop = {
  children: ReactNode;
  list: ListType[];
  action?: Action[];
};

export default function DropDown({
  list: lists,
  action: action,
  children,
}: PropsWithChildren<Prop>) {
  // const [isOpen, toggle] = useToggle(false);
  const [isOpen, toggle] = useState(false);
  const navigation = useNavigate();

  useEffect(() => {
    toggle(false);
  }, [navigation]);

  return (
    <>
      <button onClick={() => toggle((pre) => !pre)}>{children}</button>
      {isOpen && (
        <ul className='grid gap-1 rounded-lg w-32 pt-5 pb-5 bg-white absolute right-0 top-12 shadow-lg text-start text-black font-thin'>
          {lists?.map((item, index) => (
            <li
              key={index}
              className='pr-5 pl-5 hover:underline  hover:text-blue-400 text-base'
            >
              <Link to={item.path}>{item.title}</Link>
            </li>
          ))}
          {action?.map((item, index) => (
            <li
              key={index}
              className='pr-5 pl-5 hover:underline  hover:text-blue-400 text-base'
            >
              <button onClick={item.action}>{item.title}</button>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
