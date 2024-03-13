import { useReducer } from 'react';

// eslint-disable-next-line @typescript-eslint/no-inferrable-types
export const useToggle = (defaultVal: boolean = false) => {
  const [flag, toggle] = useReducer((pre) => !pre, defaultVal);

  return [flag, toggle] as const;
};
