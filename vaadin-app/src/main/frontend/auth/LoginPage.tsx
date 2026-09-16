import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '@vaadin/react-components/Button.js';
import { PasswordField } from '@vaadin/react-components/PasswordField.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import { login } from './session.js';
import { BrandMark, LoginBackdrop } from './graphics.js';

/**
 * The sign-in page, in React.
 *
 * <p>It lives outside `views/` on purpose. Anything under `views/` is picked up
 * by the file router and wrapped in `views/@layout.tsx` — and a login page
 * inside the application shell would be absurd. Instead `routes.tsx` mounts this
 * component explicitly at `/login`, as a sibling of the layout rather than a
 * child of it.
 *
 * The markup mirrors the Flow view it replaces class for class, so the existing
 * `.login-*` rules in `styles.css` style it unchanged.
 */
export default function LoginPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(false);
  const [busy, setBusy] = useState(false);

  const signIn = async () => {
    setBusy(true);
    try {
      const user = await login(username, password);
      setError(!user);
      if (user) {
        navigate('/dashboard');
      }
    } finally {
      setBusy(false);
    }
  };

  const onEnter = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      signIn();
    }
  };

  return (
    <div className="login-view">
      <LoginBackdrop />

      <div className="login-card">
        <div className="login-header">
          <div className="login-icon-box">
            <BrandMark size={28} />
          </div>
          <div className="login-title-group">
            <h1 className="login-title">WarehousePro</h1>
            <span className="login-version">v3.5 · Enterprise Edition</span>
          </div>
        </div>

        <div className="login-status">
          <div className="status-dot" />
          <span className="status-text">SYSTEM ONLINE</span>
        </div>

        <div className="login-divider" />

        <div className="login-form">
          <TextField
            label="Username"
            value={username}
            onValueChanged={(e) => setUsername(e.detail.value)}
            onKeyDown={onEnter}
            errorMessage="Invalid username or password"
            invalid={error}
          />
          <PasswordField
            label="Password"
            value={password}
            onValueChanged={(e) => setPassword(e.detail.value)}
            onKeyDown={onEnter}
          />
          <Button className="login-btn" theme="primary" disabled={busy} onClick={signIn}>
            Sign In
          </Button>
        </div>

        <p className="login-footer">© 2025 WarehousePro. All rights reserved.</p>
      </div>
    </div>
  );
}
