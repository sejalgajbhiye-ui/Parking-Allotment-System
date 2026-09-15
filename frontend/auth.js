const authScreen = document.querySelector('#auth-screen');
const appShell = document.querySelector('#app-shell');
const authTitle = document.querySelector('#auth-title');
const authLead = document.querySelector('#auth-lead');
const authForm = document.querySelector('#auth-form');
const authEmail = document.querySelector('#auth-email');
const authPassword = document.querySelector('#auth-password');
const authSubmit = document.querySelector('#auth-submit');
const authMessage = document.querySelector('#auth-message');
const authSwitch = document.querySelector('#auth-switch');
let authMode = 'login';

function setAuthMode(mode) {
  authMode = mode;
  const signingUp = mode === 'signup';
  authTitle.textContent = signingUp ? 'Create your account' : 'Welcome back';
  authLead.textContent = signingUp ? 'Create an account to access ParkEase.' : 'Sign in to manage your parking facility.';
  authSubmit.textContent = signingUp ? 'Create account' : 'Sign in';
  authSwitch.innerHTML = signingUp
    ? 'Already have an account? <button type="button" data-mode="login">Sign in</button>'
    : 'New to ParkEase? <button type="button" data-mode="signup">Create an account</button>';
  authMessage.textContent = '';
}

function showApp() {
  authScreen.hidden = true;
  appShell.hidden = false;
  window.loadParkingData?.();
}

authSwitch.addEventListener('click', event => {
  if (event.target.dataset.mode) setAuthMode(event.target.dataset.mode);
});

authForm.addEventListener('submit', async event => {
  event.preventDefault();
  authMessage.textContent = '';
  authSubmit.disabled = true;
  authSubmit.textContent = authMode === 'signup' ? 'Creating account...' : 'Signing in...';
  try {
    const response = await fetch(apiUrl(`/api/auth/${authMode}`), {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: authEmail.value.trim(), password: authPassword.value })
    });
    const rawBody = await response.text();
    let data = {};
    try {
      data = rawBody ? JSON.parse(rawBody) : {};
    } catch {
      throw new Error('Authentication server is unavailable. Start the app with "npm start" and open http://localhost:3000.');
    }
    if (!response.ok) throw new Error(data.message || 'Unable to complete the request. Please try again.');
    if (!data.token || !data.user?.email) throw new Error('Authentication server returned an incomplete response.');
    if (authMode === 'signup') {
      authPassword.value = '';
      setAuthMode('login');
      authEmail.value = data.user.email;
      authMessage.classList.add('success-message');
      authMessage.textContent = 'Account created successfully. Please sign in to continue.';
      return;
    }

    sessionStorage.setItem('parkease_token', data.token);
    sessionStorage.setItem('parkease_user', data.user.email);
    showApp();
  } catch (error) {
    authMessage.classList.remove('success-message');
    authMessage.textContent = error.message;
  } finally {
    authSubmit.disabled = false;
    authSubmit.textContent = authMode === 'signup' ? 'Create account' : 'Sign in';
  }
});

if (sessionStorage.getItem('parkease_token')) showApp();
else setAuthMode('login');
