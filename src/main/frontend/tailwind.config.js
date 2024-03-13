/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{html,js,tsx,jsx,ts}'],
  theme: {
    extend: {
      colors: {
        'main-color': '#4287f5',
      },
      width: {
        'outer-layer': '900px',
      },
    },
  },
  plugins: [],
};
