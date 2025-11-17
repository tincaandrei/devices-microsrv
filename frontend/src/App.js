import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./login/MainLogin.js";
import LandingPage from "./login/components/LandingPage.js";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/landing" element={<LandingPage />} />
      </Routes>
    </Router>
  );
}

export default App;
