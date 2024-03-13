import { Route, Routes } from 'react-router';
import { Nav } from './Nav';
import Home from './views/Home';
import My from './views/My';
import Notification from './views/Notification';
function App() {
  return (
    <>
      <Nav />
      <div className='container mx-auto md:max-w-xs lg:max-w-4xl bg-blue-100'>
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/my' element={<My />} />
          <Route path='/notification' element={<Notification />} />
        </Routes>
      </div>
    </>
  );
}

export default App;
