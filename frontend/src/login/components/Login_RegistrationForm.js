import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login_RegistrationForm.css";

export default function LoginForm() {
  const [isRightPanelActive, setIsRightPanelActive] = useState(false);
  const [loginUsername, setLoginUsername] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [registerUsername, setRegisterUsername] = useState("");
  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const navigate = useNavigate();

  const handleSignUp = () => {
    setErrorMessage("");
    setIsRightPanelActive(true);
  };

  const handleSignIn = () => {
    setErrorMessage("");
    setIsRightPanelActive(false);
  };

  const handleLoginSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response = await fetch("http://localhost/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username: loginUsername,
          password: loginPassword,
        }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Login failed");
      }

      const data = await response.json();
      if (data.token) {
        localStorage.setItem("authToken", data.token);
        localStorage.setItem("authUsername", data.username);
        localStorage.setItem("authRole", data.role);
      }

      navigate("/landing");
    } catch (error) {
      setErrorMessage("Login failed. Please check your credentials.");
      console.error("Login error:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRegisterSubmit = async (event) => {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response = await fetch("http://localhost/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username: registerUsername,
          password: registerPassword,
          email: registerEmail,
        }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Registration failed");
      }

      const data = await response.json();
      if (data.token) {
        localStorage.setItem("authToken", data.token);
        localStorage.setItem("authUsername", data.username);
        localStorage.setItem("authRole", data.role);
      }

      // After successful registration, show login panel
      setIsRightPanelActive(false);
    } catch (error) {
      setErrorMessage(
        "Registration failed. Please check your data and try again."
      );
      console.error("Registration error:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-page-wrapper">
      <div
        className={`container ${
          isRightPanelActive ? "right-panel-active" : ""
        }`}
      >
        {/* Sign In Form */}
        <div className="form-container sign-in-container">
          <form onSubmit={handleLoginSubmit}>
            <div className="form-header">
              <h1>Sign in</h1>
              <span>Use your account</span>
            </div>
            <div className="form-body">
              <input
                type="text"
                placeholder="Username"
                value={loginUsername}
                onChange={(event) => setLoginUsername(event.target.value)}
                required
              />
              <input
                type="password"
                placeholder="Password"
                value={loginPassword}
                onChange={(event) => setLoginPassword(event.target.value)}
                required
              />

              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Signing in..." : "Sign In"}
              </button>
            </div>
          </form>
        </div>

        {/* Sign Up Form */}
        <div className="form-container sign-up-container">
          <form onSubmit={handleRegisterSubmit}>
            <div className="form-header">
              <h1>Create Account</h1>
              <span>Use your email for registration</span>
            </div>
            <div className="form-body">
              <input
                type="text"
                placeholder="Username"
                value={registerUsername}
                onChange={(event) => setRegisterUsername(event.target.value)}
                required
              />
              <input
                type="email"
                placeholder="Email"
                value={registerEmail}
                onChange={(event) => setRegisterEmail(event.target.value)}
                required
              />
              <input
                type="password"
                placeholder="Password"
                value={registerPassword}
                onChange={(event) => setRegisterPassword(event.target.value)}
                required
              />
              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Signing up..." : "Sign Up"}
              </button>
            </div>
          </form>
        </div>

        {/* Overlay Container */}
        <div className="overlay-container">
          <div className="overlay">
            {/* Overlay Left - Welcome Back */}
            <div className="overlay-panel overlay-left">
              <h1>Welcome Back!</h1>
              <p>
                To keep connected with us please login with your personal info
              </p>
              <button className="ghost" onClick={handleSignIn}>
                Sign In
              </button>
            </div>

            {/* Overlay Right - Hello Friend */}
            <div className="overlay-panel overlay-right">
              <h1>Hello, Friend!</h1>
              <p>Enter your personal details and start your journey with us</p>
              <button className="ghost" onClick={handleSignUp}>
                Sign Up
              </button>
            </div>
          </div>
        </div>

        {errorMessage && (
          <div className="auth-error-message">{errorMessage}</div>
        )}
      </div>
    </div>
  );
}
