const state = { vehicles: [], history: [], revenue: 0 };
const pageInfo = {
  dashboard: ["Dashboard", "A quick view of your parking facility."],
  park: ["Park a vehicle", "Register a vehicle and allocate the next available slot."],
  vehicles: ["Active vehicles", "Vehicles currently parked in the facility."],
  history: ["Parking history", "Completed parking visits and collected fees."]
};

const escapeHtml = value => String(value).replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
const formatTime = date => new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
const minutesSince = date => Math.max(0, Math.floor((Date.now() - date.getTime()) / 60000));

function changePage(page) {
  document.querySelectorAll('.page').forEach(el => el.classList.toggle('active', el.id === page));
  document.querySelectorAll('.nav-link').forEach(el => el.classList.toggle('active', el.dataset.page === page));
  document.querySelector('#page-title').textContent = pageInfo[page][0];
  document.querySelector('#page-subtitle').textContent = pageInfo[page][1];
  render();
}

function row(vehicle, includeAction = false) {
  const action = includeAction ? `<td><button class="button checkout" data-checkout="${vehicle.id}">Check out</button></td>` : '';
  return `<tr><td>${escapeHtml(vehicle.number)}</td><td>${escapeHtml(vehicle.owner)}</td><td>${vehicle.type}</td><td>${vehicle.slot}</td><td>${formatTime(vehicle.entry)}</td><td>${minutesSince(vehicle.entry)} min</td>${action}</tr>`;
}

function fillTable(id, content, columns) {
  document.querySelector(id).innerHTML = content || `<tr><td class="empty" colspan="${columns}">No records to display.</td></tr>`;
}

function render() {
  const twoW = state.vehicles.filter(v => v.type === '2W').length;
  const fourW = state.vehicles.length - twoW;
  document.querySelector('#active-count').textContent = state.vehicles.length;
  document.querySelector('#two-wheeler-slots').textContent = 100 - twoW;
  document.querySelector('#four-wheeler-slots').textContent = 100 - fourW;
  document.querySelector('#revenue').textContent = `Rs. ${state.revenue.toFixed(2)}`;
  fillTable('#dashboard-table', state.vehicles.slice(0, 5).map(v => row(v)).join(''), 6);
  const term = document.querySelector('#vehicle-search').value.trim().toUpperCase();
  const filtered = state.vehicles.filter(v => v.number.includes(term));
  fillTable('#active-table', filtered.map(v => row(v, true)).join(''), 7);
  fillTable('#history-table', state.history.map(v => `<tr><td>${escapeHtml(v.number)}</td><td>${escapeHtml(v.owner)}</td><td>${v.type}</td><td>${v.slot}</td><td>${formatTime(v.exit)}</td><td>Rs. ${v.fee.toFixed(2)}</td></tr>`).join(''), 6);
}

function notify(message) {
  const toast = document.querySelector('#toast'); toast.textContent = message; toast.classList.add('show-toast');
  setTimeout(() => toast.classList.remove('show-toast'), 3200);
}

document.querySelectorAll('.nav-link').forEach(button => button.addEventListener('click', () => changePage(button.dataset.page)));
document.querySelectorAll('[data-navigate]').forEach(button => button.addEventListener('click', () => changePage(button.dataset.navigate)));
document.querySelector('#vehicle-search').addEventListener('input', render);
document.querySelector('#parking-form').addEventListener('submit', event => {
  submitParking(event);
});

async function submitParking(event) {
  event.preventDefault();
  const number = document.querySelector('#vehicle-number').value.trim().toUpperCase();
  const owner = document.querySelector('#owner-name').value.trim();
  const type = document.querySelector('#vehicle-type').value;
  try {
    const response = await api('/api/parking', { method: 'POST', body: JSON.stringify({ number, owner, type }) });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message);
    event.target.reset(); await loadParkingData(); notify(`${data.vehicle.vehicle_number} allocated ${data.vehicle.vehicle_type} slot ${data.vehicle.slot_no}.`); changePage('vehicles');
  } catch (error) { notify(error.message || 'Unable to park this vehicle.'); }
}

document.addEventListener('click', async event => {
  const id = event.target.dataset.checkout; if (!id) return;
  try {
    const response = await api(`/api/parking/${id}/checkout`, { method: 'POST' });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message);
    await loadParkingData(); notify(`${data.vehicle.vehicle_number} checked out. Fee: Rs. ${Number(data.vehicle.fee).toFixed(2)}.`);
  } catch (error) { notify(error.message || 'Unable to check out this vehicle.'); }
});

function apiUrl(path) {
  return `${String(window.PARKEASE_API_URL || '').replace(/\/$/, '')}${path}`;
}

function api(url, options = {}) {
  return fetch(apiUrl(url), { ...options, headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${sessionStorage.getItem('parkease_token') || ''}`, ...(options.headers || {}) } });
}

async function loadParkingData() {
  const token = sessionStorage.getItem('parkease_token');
  if (!token) return;
  try {
    const response = await api('/api/parking');
    if (response.status === 401) { sessionStorage.removeItem('parkease_token'); return; }
    const data = await response.json();
    if (!response.ok) throw new Error(data.message);
    state.vehicles = data.active.map(vehicle => ({ id: vehicle.id, number: vehicle.vehicle_number, owner: vehicle.owner_name, type: vehicle.vehicle_type, slot: vehicle.slot_no, entry: new Date(vehicle.entry_time) }));
    state.history = data.history.map(vehicle => ({ id: vehicle.id, number: vehicle.vehicle_number, owner: vehicle.owner_name, type: vehicle.vehicle_type, slot: vehicle.slot_no, entry: new Date(vehicle.entry_time), exit: new Date(vehicle.exit_time), fee: Number(vehicle.fee) }));
    state.revenue = Number(data.revenue); render();
  } catch (error) { notify(error.message || 'Unable to load saved parking data.'); }
}

window.loadParkingData = loadParkingData;
render();
