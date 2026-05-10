const copy = {
    en: {
        heart: "Heart Rate:",
        blood: "Blood Pressure:",
        temp: "Temperature:",
        chartHeart: "● Heart Rate",
        chartBlood: "● Blood Pressure",
        chartTemp: "● Temperature",
        chat: "Chat with AI",
        alert: "ALERT SENT TO PARAMEDICS!",
        causes: "Wellness Suggestions",
        cause1: "Review unusual readings with doctor/staff",
        cause2: "Keep monitoring symptoms and hydration",
        warningAlert: "WARNING PATTERN DETECTED",
        criticalAlert: "CRITICAL ALERT SENT TO HOSPITAL STAFF",
        greeting: "Hello! How can I assist you with your health today?"
    },
    zu: {
        heart: "Ukushaya kwenhliziyo:",
        blood: "Umfutho wegazi:",
        temp: "Izinga lokushisa:",
        chartHeart: "● Ukushaya kwenhliziyo",
        chartBlood: "● Umfutho wegazi",
        chartTemp: "● Izinga lokushisa",
        chat: "Xoxa ne-AI",
        alert: "ISIXWAYISO SITHUNYELWE KUMAPHARAMEDIKI!",
        causes: "Iziphakamiso Zempilo",
        cause1: "Xoxa nodokotela/abasebenzi ngezilinganiso ezingajwayelekile",
        cause2: "Qhubeka uqapha izimpawu nokuphuza amanzi",
        warningAlert: "KUTHOLAKELE IPHETHINI LESIXWAYISO",
        criticalAlert: "ISIXWAYISO ESIBUCAYI SITHUNYELWE ABASEBENZI",
        greeting: "Sawubona! Ngingakusiza kanjani ngempilo yakho namuhla?"
    },
    af: {
        heart: "Hartklop:",
        blood: "Bloeddruk:",
        temp: "Temperatuur:",
        chartHeart: "● Hartklop",
        chartBlood: "● Bloeddruk",
        chartTemp: "● Temperatuur",
        chat: "Gesels met KI",
        alert: "WAARSKUWING NA PARAMEDICI GESTUUR!",
        causes: "Welstandvoorstelle",
        cause1: "Bespreek ongewone lesings met dokter/personeel",
        cause2: "Hou simptome en hidrasie dop",
        warningAlert: "WAARSKUWINGSPATROON OPGEMERK",
        criticalAlert: "KRITIEKE WAARSKUWING NA PERSONEEL GESTUUR",
        greeting: "Hallo! Hoe kan ek jou vandag met jou gesondheid help?"
    }
};

const LIVE_REFRESH_INTERVAL_MS = 5000;
const MAX_CHART_POINTS = 12;
const LIVE_FEED_STALE_AFTER_MS = 75000;
const chartSeries = {
    heartRate: [],
    systolic: [],
    temperature: []
};

document.addEventListener('DOMContentLoaded', () => {
    applyLanguage('en');
    initChart();
    loadLatestReadings();
    setInterval(loadLatestReadings, LIVE_REFRESH_INTERVAL_MS);
});

function selectTab(button) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    button.classList.add('active');
    applyLanguage(button.dataset.lang || 'en');
}

function applyLanguage(lang) {
    const text = copy[lang] || copy.en;
    setText('heart', text.heart);
    setText('blood_p', text.blood);
    setText('temp', text.temp);
    setText('chart_heart', text.chartHeart);
    setText('chart_blood', text.chartBlood);
    setText('chart_temp', text.chartTemp);
    setText('chat_ai', text.chat);
    setText('alert_p', text.alert);
    setText('causes', text.causes);
    setText('chat_message', text.greeting);
    document.querySelector('[data-key="cause1"]').textContent = text.cause1;
    document.querySelector('[data-key="cause2"]').textContent = text.cause2;
}

function toggleMenu() {
    const menu = document.getElementById('dropdownMenu');
    menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
}

function openProfile() {
    document.getElementById('dropdownMenu').style.display = 'none';
    document.getElementById('profileModal').style.display = 'flex';
    loadProfile();
}

function closeProfile() {
    document.getElementById('profileModal').style.display = 'none';
}

function loadProfile() {
    const details = document.getElementById('profileDetails');
    details.innerHTML = '<p>Loading profile...</p>';

    fetch('api/mobile/me', { headers: { Accept: 'application/json' } })
        .then(response => response.ok ? response.json() : Promise.reject())
        .then(data => details.innerHTML = data.success ? renderProfile(data.user) : '<p>Profile could not be loaded.</p>')
        .catch(() => details.innerHTML = '<p>Profile could not be loaded. Please sign in again.</p>');
}

function renderProfile(user) {
    const rows = [
        ['Name', `${safe(user.title)} ${safe(user.firstName)} ${safe(user.surname)}`.trim()],
        ['Email', user.email],
        ['Gender', user.gender],
        ['Cell Number', user.cellNumber],
        ['Patient ID', user.id]
    ];
    return `<dl>${rows.map(([label, value]) => `<div><dt>${label}</dt><dd>${safe(value) || 'Not saved'}</dd></div>`).join('')}</dl>`;
}

function openChat() {
    document.getElementById('chatModal').style.display = 'flex';
    document.getElementById('chatInput').focus();
}

function closeChat() {
    document.getElementById('chatModal').style.display = 'none';
}

function handleKey(event) {
    if (event.key === 'Enter') sendMessage();
}

function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    if (!message) return;

    appendChat('user', message);
    input.value = '';
    askAssistant(message);
}

function askAssistant(message) {
    appendChat('ai', 'Thinking...');
    const thinkingBubble = document.querySelector('#chatMessages .chat-bubble.ai:last-child');

    fetch('AIChatServlet.do', {
        method: 'POST',
        headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: `message=${encodeURIComponent(message)}&vitals=${encodeURIComponent(JSON.stringify(readCurrentVitals()))}&history=${encodeURIComponent(readChatHistory())}`
    })
        .then(response => response.ok ? response.json() : Promise.reject())
        .then(data => thinkingBubble.textContent = data.reply || buildAssistantReply(message))
        .catch(() => thinkingBubble.textContent = buildAssistantReply(message));
}

function appendChat(type, text) {
    const messages = document.getElementById('chatMessages');
    const bubble = document.createElement('div');
    bubble.className = `chat-bubble ${type}`;
    bubble.textContent = text;
    messages.appendChild(bubble);
    messages.scrollTop = messages.scrollHeight;
}

function readChatHistory() {
    return Array.from(document.querySelectorAll('#chatMessages .chat-bubble'))
        .slice(-8)
        .map(bubble => `${bubble.classList.contains('user') ? 'User' : 'Assistant'}: ${bubble.textContent}`)
        .join('\n');
}

function buildAssistantReply(message) {
    const lower = message.toLowerCase();
    const vitals = readCurrentVitals();

    if (lower.includes('llm') || lower.includes('model') || lower.includes('ai')) return "The page now tries the server LLM endpoint first. If no LLM key is configured, I use this local wellness assistant as a fallback.";
    if (lower.includes('connect') || lower.includes('watch') || lower.includes('sync')) return "Pair the Galaxy Watch with Samsung Health, allow Samsung Health to share with Health Connect, then sync from the Android app.";
    if (lower.includes('heart') || lower.includes('pulse') || lower.includes('bpm')) return describeHeartRate(vitals.heartRate);
    if (lower.includes('blood') || lower.includes('pressure')) return describeBloodPressure(vitals.systolic, vitals.diastolic);
    if (lower.includes('temp') || lower.includes('fever')) return describeTemperature(vitals.temperature);
    if (lower.includes('advice') || lower.includes('diagnose') || lower.includes('disease')) return "I can give wellness suggestions based on displayed readings, but I cannot diagnose disease or replace a clinician.";
    return "I can explain heart rate, blood pressure, temperature, watch connection, and displayed readings. These are wellness suggestions only.";
}

function describeHeartRate(value) {
    if (value === null) return "I do not have a heart rate reading yet. Sync the phone app first.";
    if (value < 50) return `Your latest heart rate is ${value} BPM, which is low for many adults. Seek help urgently if you feel dizzy, weak, short of breath, or unwell.`;
    if (value > 120) return `Your latest heart rate is ${value} BPM, which is high for a resting reading. Rest, recheck it, and contact doctor/staff if it stays high or symptoms appear.`;
    return `Your latest heart rate is ${value} BPM. It is generally within a common adult resting range, but trends and symptoms matter.`;
}

function describeBloodPressure(systolic, diastolic) {
    if (systolic === null || diastolic === null) return "I do not have a blood pressure reading yet. Sync the phone app first.";
    if (systolic >= 140 || diastolic >= 90) return `Your latest blood pressure is ${systolic}/${diastolic} mmHg, which is above the usual target range. Recheck after resting and share it with doctor/staff if it remains high.`;
    if (systolic < 90 || diastolic < 60) return `Your latest blood pressure is ${systolic}/${diastolic} mmHg, which is low. Seek help if you feel dizzy, faint, confused, or weak.`;
    return `Your latest blood pressure is ${systolic}/${diastolic} mmHg. It does not look severely abnormal from this single reading, but keep checking trends.`;
}

function describeTemperature(value) {
    if (value === null) return "I do not have a temperature reading yet. Sync the phone app first.";
    if (value >= 38) return `Your latest temperature is ${value.toFixed(1)} C, which may indicate a fever. Rest, hydrate, and contact doctor/staff if it persists or you feel very unwell.`;
    if (value < 35.5) return `Your latest temperature is ${value.toFixed(1)} C, which is low. Warm up safely and seek help if you feel confused, very cold, or weak.`;
    return `Your latest temperature is ${value.toFixed(1)} C, which is within a common normal range. Keep monitoring if you have symptoms.`;
}

function readCurrentVitals() {
    const bloodPressureMatch = getText('bloodPressureValue').match(/(\d+)\s*\/\s*(\d+)/);
    return {
        heartRate: parseNumberFromElement('heartRateValue'),
        temperature: parseNumberFromElement('temperatureValue'),
        systolic: bloodPressureMatch ? Number(bloodPressureMatch[1]) : null,
        diastolic: bloodPressureMatch ? Number(bloodPressureMatch[2]) : null
    };
}

function initChart() {
    redrawChart();
}

function redrawChart() {
    const canvas = document.getElementById('healthChart');
    const ctx = canvas.getContext('2d');
    const width = canvas.parentElement.clientWidth;
    const height = 120;
    canvas.width = width;
    canvas.height = height;
    drawChart(ctx, width, height, [
        { color: '#b42318', values: chartSeries.heartRate },
        { color: '#00995d', values: chartSeries.systolic },
        { color: '#ff8a5c', values: chartSeries.temperature }
    ]);
}

function drawChart(ctx, width, height, series) {
    const padding = 18;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = '#f7fbf9';
    ctx.fillRect(padding, padding, chartWidth, chartHeight);
    ctx.strokeStyle = '#d7deea';
    ctx.lineWidth = 1;

    for (let i = 0; i <= 4; i++) drawGridLine(ctx, padding, padding + chartHeight * i / 4, width - padding, padding + chartHeight * i / 4);
    for (let i = 0; i <= 6; i++) drawGridLine(ctx, padding + chartWidth * i / 6, padding, padding + chartWidth * i / 6, height - padding);

    if (!series.some(item => item.values.length > 1)) {
        drawEmptyChartMessage(ctx, width, height);
        return;
    }

    const laneHeight = chartHeight / series.length;
    series.forEach((item, index) => {
        drawLine(ctx, item.values, item.color, padding, laneHeight, width, height, padding + laneHeight * index);
    });
}

function drawEmptyChartMessage(ctx, width, height) {
    ctx.fillStyle = '#5a6680';
    ctx.font = '600 14px Poppins, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('Waiting for synced section readings', width / 2, height / 2);
}

function drawGridLine(ctx, x1, y1, x2, y2) {
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
}

function drawLine(ctx, data, color, padding, laneHeight, width, height, laneTop) {
    if (data.length < 2) return;
    const step = (width - padding * 2) / (data.length - 1);
    const min = Math.min(...data);
    const max = Math.max(...data);
    const range = max - min;
    const innerLaneHeight = Math.max(laneHeight - 10, 12);
    ctx.strokeStyle = color;
    ctx.lineWidth = 3;
    ctx.lineJoin = 'round';
    ctx.lineCap = 'round';
    ctx.beginPath();
    data.forEach((value, index) => {
        const x = padding + index * step;
        const normalized = range <= 0.01 ? 0.5 : (value - min) / range;
        const y = laneTop + 5 + (1 - normalized) * innerLaneHeight;
        if (index === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    });
    ctx.stroke();
}

function loadLatestReadings() {
    fetch('ReadingServlet.do', { headers: { Accept: 'application/json' } })
        .then(response => response.json())
        .then(data => {
            if (!data.success) {
                clearLiveReadings();
                return updateSyncStatus('Sign in and sync a phone health section to see readings.');
            }
            if (!isLiveFeedFresh(data)) {
                clearLiveReadings();
                return updateSyncStatus('No recent phone health section found. Sync the latest section from the mobile app.');
            }

            setText('heartRateValue', data.heartRate === null ? '--' : data.heartRate);
            setText('bloodPressureValue', data.bloodPressure === null ? '--' : data.bloodPressure);
            setText('temperatureValue', data.temperature === null ? '--' : data.temperature);
            updateChartSeries(data);
            updateAlertBanner(data);
            updateSyncStatus(`Latest synced section refreshed. Last checked ${new Date().toLocaleTimeString()}.`);
        })
        .catch(() => {
            clearLiveReadings();
            updateSyncStatus('Unable to load synced section readings from the server.');
        });
}

function isLiveFeedFresh(data) {
    if (!data.latestSyncedAt) return false;
    const syncedTime = new Date(data.latestSyncedAt).getTime();
    return Number.isFinite(syncedTime) && Date.now() - syncedTime <= LIVE_FEED_STALE_AFTER_MS;
}

function updateChartSeries(data) {
    if (data.heartRate !== null) appendChartPoint(chartSeries.heartRate, Number(data.heartRate));

    const bloodPressureMatch = String(data.bloodPressure || '').match(/(\d+)\s*\/\s*(\d+)/);
    if (bloodPressureMatch) appendChartPoint(chartSeries.systolic, Number(bloodPressureMatch[1]));

    if (data.temperature !== null) appendChartPoint(chartSeries.temperature, Number(data.temperature));

    redrawChart();
}

function appendChartPoint(series, value) {
    if (!Number.isFinite(value)) return;
    const last = series[series.length - 1];
    if (last === value) return;
    series.push(value);
    while (series.length > MAX_CHART_POINTS) series.shift();
}

function clearLiveReadings() {
    setText('heartRateValue', '--');
    setText('bloodPressureValue', '--');
    setText('temperatureValue', '--');
    chartSeries.heartRate = [];
    chartSeries.systolic = [];
    chartSeries.temperature = [];
    redrawChart();
    updateAlertBanner({ heartRate: null, temperature: null });
}

function updateAlertBanner(data) {
    const banner = document.getElementById('alertBanner');
    const alertText = document.getElementById('alert_p');
    const activeLanguage = document.querySelector('.tab.active')?.dataset.lang || 'en';
    const text = copy[activeLanguage] || copy.en;

    if (data.activeAlert) {
        alertText.innerText = data.activeAlert.status === 'CRITICAL' ? text.criticalAlert : text.warningAlert;
        banner.style.display = 'flex';
        return;
    }

    const heartRate = data.heartRate;
    const temperature = data.temperature === null ? null : parseFloat(data.temperature);
    const bloodPressureMatch = String(data.bloodPressure || '').match(/(\d+)\s*\/\s*(\d+)/);
    const systolic = bloodPressureMatch ? Number(bloodPressureMatch[1]) : null;
    const diastolic = bloodPressureMatch ? Number(bloodPressureMatch[2]) : null;
    const shouldWarn = (heartRate !== null && (heartRate < 50 || heartRate > 120)) ||
        (temperature !== null && temperature > 38) ||
        (systolic !== null && diastolic !== null && (systolic >= 140 || diastolic >= 90));

    alertText.innerText = text.alert;
    banner.style.display = shouldWarn ? 'flex' : 'none';
}

function updateSyncStatus(message) {
    setText('syncStatus', message);
}

function parseNumberFromElement(id) {
    const match = getText(id).match(/-?\d+(\.\d+)?/);
    return match ? Number(match[0]) : null;
}

function getText(id) {
    const element = document.getElementById(id);
    return element ? element.innerText : '';
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) element.innerText = value;
}

function safe(value) {
    return value === null || value === undefined ? '' : String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
}

window.addEventListener('click', event => {
    if (!event.target.closest('.user-menu')) document.getElementById('dropdownMenu').style.display = 'none';
    if (event.target === document.getElementById('chatModal')) closeChat();
    if (event.target === document.getElementById('profileModal')) closeProfile();
});
