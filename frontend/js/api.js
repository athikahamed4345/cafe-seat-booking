const BASE = '/api';

// ── Auth helpers ──────────────────────────────────────────────────────────────
function getToken()   { return localStorage.getItem('token'); }
function getUser()    { return JSON.parse(localStorage.getItem('user') || 'null'); }
function isLoggedIn() { return !!getToken(); }
function isAdmin()    { const u = getUser(); return u && u.role === 'ADMIN'; }

function requireAuth() {
  if (!isLoggedIn()) { location.href = '/auth.html'; return false; }
  return true;
}
function requireAdmin() {
  if (!isAdmin()) { location.href = '/index.html'; return false; }
  return true;
}

// ── API call helper ───────────────────────────────────────────────────────────
async function apiCall(method, path, body, params) {
  let url = BASE + path;
  if (params) url += '?' + new URLSearchParams(params);

  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  const token = getToken();
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  if (body)  opts.body = JSON.stringify(body);

  const res = await fetch(url, opts);
  if (!res.ok) {
    let msg = await res.text().catch(() => '');
    try { const j = JSON.parse(msg); msg = j.error || j.message || msg; } catch {}
    throw new Error(msg || 'Request failed (' + res.status + ')');
  }
  return res.status === 204 ? null : res.json();
}

// ── API endpoints ─────────────────────────────────────────────────────────────
const api = {
  auth: {
    login:          d                 => apiCall('POST', '/auth/login',            d),
    register:       d                 => apiCall('POST', '/auth/register',         d),
    verifyEmail:    token             => apiCall('GET',  '/auth/verify-email', null, { token }),
    forgotPassword: email             => apiCall('POST', '/auth/forgot-password',  { email }),
    resetPassword:  (token, password) => apiCall('POST', '/auth/reset-password',   { token, password }),
  },
  user: {
    changePassword: (currentPassword, newPassword) =>
      apiCall('PATCH', '/user/password', { currentPassword, newPassword }),
    deleteAccount: () => apiCall('DELETE', '/user/me'),
  },
  tables: {
    all:       ()                    => apiCall('GET',  '/tables'),
    available: (date, time, guests)  => apiCall('GET',  '/tables/available', null, { date, time, partySize: guests }),
  },
  bookings: {
    create: d  => apiCall('POST',  '/bookings',          d),
    my:     () => apiCall('GET',   '/bookings/my'),
    cancel: id => apiCall('PATCH', `/bookings/${id}/cancel`),
  },
  admin: {
    bookings:     ()               => apiCall('GET',    '/admin/bookings'),
    updateStatus: (id, status)     => apiCall('PATCH',  `/admin/bookings/${id}/status`, null, { status }),
    addTable:     d                => apiCall('POST',   '/admin/tables', d),
    toggleTable:  (id, available)  => apiCall('PATCH',  `/admin/tables/${id}/availability`, null, { available }),
    deleteTable:  id               => apiCall('DELETE', `/admin/tables/${id}`),
  },
};

// ── Toast notifications ───────────────────────────────────────────────────────
function showToast(msg, type = 'success', duration = 4500) {
  let box = document.getElementById('toast-container');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toast-container';
    document.body.appendChild(box);
  }
  const t = document.createElement('div');
  t.className = `toast toast-${type}`;
  t.innerHTML = msg;
  t.onclick = () => { t.classList.remove('toast-visible'); setTimeout(() => t.remove(), 350); };
  box.appendChild(t);
  requestAnimationFrame(() => requestAnimationFrame(() => t.classList.add('toast-visible')));
  setTimeout(() => { t.classList.remove('toast-visible'); setTimeout(() => t.remove(), 400); }, duration);
}

// ── Utilities ─────────────────────────────────────────────────────────────────
function bookingRef(id) {
  return 'CB-' + String(id).padStart(5, '0');  // e.g. CB-00001
}

function formatTime12(t) {
  if (!t) return t;
  const [h, m] = t.split(':').map(Number);
  return (h % 12 || 12) + ':' + String(m).padStart(2, '0') + (h >= 12 ? ' PM' : ' AM');
}

const STATUS_BADGE = {
  CONFIRMED: 'badge-green', PENDING: 'badge-yellow',
  CANCELLED: 'badge-red',   COMPLETED: 'badge-blue', NO_SHOW: 'badge-gray'
};
function statusBadge(s) {
  return `<span class="badge ${STATUS_BADGE[s] || 'badge-gray'}">${s}</span>`;
}

// ── Navbar ────────────────────────────────────────────────────────────────────
function renderNav() {
  const el = document.getElementById('main-nav');
  if (!el) return;
  el.innerHTML =
    (isLoggedIn()  ? `<a href="/book.html">Reservations</a>` : '') +
    (isLoggedIn()  ? `<a href="/my-bookings.html">My Bookings</a>` : '') +
    (isAdmin()     ? `<a href="/admin.html">Admin</a>` : '') +
    (!isLoggedIn() ? `<a href="/auth.html">Sign In</a>` : '') +
    (!isLoggedIn() ? `<a href="/auth.html?tab=register">Register</a>` : '') +
    (isLoggedIn()  ? `<button class="btn-logout" onclick="logout()">Sign Out</button>` : '');

  const nav    = el.closest('.navbar');
  const toggle = nav && nav.querySelector('.nav-toggle');
  if (toggle) {
    toggle.onclick = () => el.classList.toggle('nav-open');
    document.addEventListener('click', e => {
      if (!nav.contains(e.target)) el.classList.remove('nav-open');
    });
  }
}

function logout() { localStorage.clear(); location.href = '/auth.html'; }

// ── Footer ────────────────────────────────────────────────────────────────────
function renderFooter() {
  const el = document.getElementById('site-footer');
  if (!el) return;
  el.innerHTML = `
    <footer class="site-footer">
      <div class="footer-grid">
        <div class="footer-brand">
          <a href="/index.html" class="logo">&#9749; CafeBook</a>
          <p>Reserve your perfect table in seconds. No wait, no surprises — just great coffee and the seat you love.</p>
        </div>
        <div class="footer-col">
          <h4>Quick Links</h4>
          <a href="/index.html">Home</a>
          ${isLoggedIn() ? '<a href="/book.html">Make a Reservation</a>' : ''}
          ${isLoggedIn() ? '<a href="/my-bookings.html">My Reservations</a>' : ''}
          ${!isLoggedIn() ? '<a href="/auth.html">Sign In</a>' : ''}
          ${!isLoggedIn() ? '<a href="/auth.html?tab=register">Create Account</a>' : ''}
        </div>
        <div class="footer-col">
          <h4>Information</h4>
          <a href="#">Opening Hours</a>
          <a href="#">Location</a>
          <a href="#">Contact Us</a>
          ${isAdmin() ? '<a href="/admin.html">Admin Dashboard</a>' : ''}
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; ${new Date().getFullYear()} CafeBook. All rights reserved.</p>
      </div>
    </footer>`;
}

// ── Inline alert ──────────────────────────────────────────────────────────────
function showAlert(containerId, msg, type) {
  const el = document.getElementById(containerId);
  if (el) el.innerHTML = `<div class="alert alert-${type}">${msg}</div>`;
}
