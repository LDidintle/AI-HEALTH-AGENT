// ===== TRANSLATIONS =====
function changeTextA(){
    document.getElementById("heart").innerText = "Hartklop:";
    document.getElementById("temp").innerText = "Temperatuur:";
    document.getElementById("blood_p").innerText = "Bloeddruk:";

    document.getElementById("chart_heart").innerText = "● Hartklop";
    document.getElementById("chart_blood").innerText = "● Bloeddruk";
    document.getElementById("chart_temp").innerText = "● Temperatuur";

    document.getElementById("chat_ai").innerText = "Gesels met KI";

    document.getElementById("alert_p").innerText = "WAARSKUWING NA PARAMEDICI GESTUUR!";

    document.getElementById("causes").innerText = "Welstandvoorstelle";
   
    //Chat Message
    document.getElementById("chat_message").innerText = "Hallo! Hoe kan ek jou vandag met jou gesondheid help?";


}
function changeTextE(){
    document.getElementById("heart").innerText = "Heart Rate:";
    document.getElementById("blood_p").innerText = "Blood Pressure:";
    document.getElementById("temp").innerText = "Temperature:";

    document.getElementById("chart_heart").innerText = "● Heart Rate";
    document.getElementById("chart_blood").innerText = "● Blood Pressure";
    document.getElementById("chart_temp").innerText = "● Temperature";

    document.getElementById("chat_ai").innerText = "Chat with AI";

    document.getElementById("alert_p").innerText = "ALERT SENT TO PARAMEDICS!";

    document.getElementById("causes").innerText = "Wellness Suggestions";
    
    //Chat Message
    document.getElementById("chat_message").innerText = "Hello! How can I assist you with your health today?";

}
function changeTextI(){
    document.getElementById("heart").innerText = "Inhliziyo Ukushaya:";
    document.getElementById("heart").innerText = "Umfutho Wegaz:";
    document.getElementById("temp").innerText = "Izinga Lokushisa:";

    document.getElementById("chart_heart").innerText = "● Inhliziyo Ukushaya";
    document.getElementById("chart_blood").innerText = "● Umfutho Wegaz";
    document.getElementById("chart_temp").innerText = "● Izinga Lokushisa";

    document.getElementById("chat_ai").innerText = "Xoxa ne-AI";

    document.getElementById("alert_p").innerText = "ISIXWAYISO SITHUMELWE KUMAPHARAMEDIKI!";

    document.getElementById("causes").innerText = "Iziphakamiso Zempilo";

    
    //Chat Message
    document.getElementById("chat_message").innerText = "Sawubona! Ngingakusiza kanjani ngempilo yakho namuhla?";

}
const translations = {
    en: {
        cause1: "Review unusual readings with doctor/staff",
        cause2: "Keep monitoring symptoms and hydration",
        chat_greeting: "Hello! How can I assist you with your health today?"
    },
    es: {
        cause1: "Review unusual readings with doctor/staff",
        cause2: "Keep monitoring symptoms and hydration",
        chat_greeting: "¡Hola! ¿Cómo puedo ayudarte con tu salud hoy?"
    },
    af: {
        cause1: "Hersien ongewone lesings met dokter/personeel",
        cause2: "Hou simptome en hidrasie dop",
        chat_greeting: "Hallo! Hoe kan ek jou vandag met jou gesondheid help?"
    }
};

// ===== LANGUAGE SWITCH =====
function selectTab(button) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    button.classList.add('active');

    const lang = button.getAttribute('data-lang');

    document.querySelectorAll('[data-key]').forEach(el => {
        const key = el.getAttribute('data-key');
        if(translations[lang] && translations[lang][key]){
            el.textContent = translations[lang][key];
        }

    });
}

// ===== CHAT MODAL =====
function openChat() {
    document.getElementById('chatModal').style.display = 'flex';
    document.getElementById('chatInput').focus();
}

function closeChat() {
    document.getElementById('chatModal').style.display = 'none';
}

function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    
    if (message) {
        const messagesContainer = document.getElementById('chatMessages');
        
        const userBubble = document.createElement('div');
        userBubble.className = 'chat-bubble user';
        userBubble.textContent = message;
        messagesContainer.appendChild(userBubble);
        input.value = '';
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

        setTimeout(() => {
            const aiBubble = document.createElement('div');
            aiBubble.className = 'chat-bubble ai';
            aiBubble.textContent = buildAssistantReply(message);
            messagesContainer.appendChild(aiBubble);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }, 1000);
    }
}

function buildAssistantReply(message) {
    const lowerMessage = message.toLowerCase();
    const vitals = readCurrentVitals();

    if (lowerMessage.includes('llm') || lowerMessage.includes('model') || lowerMessage.includes('ai')) {
        return "This is a demo rule-based SmartHealth assistant, not a live LLM yet. It uses the readings shown on this page to give wellness suggestions only.";
    }

    if (lowerMessage.includes('connect') || lowerMessage.includes('watch') || lowerMessage.includes('sync')) {
        return "To connect the watch, pair the Galaxy Watch 5 with Samsung Health, allow Samsung Health to share with Health Connect, then sync from the Android app.";
    }

    if (lowerMessage.includes('heart') || lowerMessage.includes('pulse') || lowerMessage.includes('bpm')) {
        if (vitals.heartRate === null) {
            return "I do not have a heart rate reading yet. Sync the phone app first, then I can comment on the number shown here.";
        }

        if (vitals.heartRate < 50) {
            return `Your latest heart rate is ${vitals.heartRate} BPM, which is lower than the usual adult resting range. If you feel dizzy, weak, short of breath, or unwell, contact a healthcare professional urgently.`;
        }

        if (vitals.heartRate > 120) {
            return `Your latest heart rate is ${vitals.heartRate} BPM, which is high for a resting reading. Rest, recheck the reading, and contact doctor/staff urgently if you have chest pain, faintness, shortness of breath, or it stays high.`;
        }

        return `Your latest heart rate is ${vitals.heartRate} BPM. That is generally within the common adult resting range, but trends and symptoms matter, so keep monitoring it.`;
    }

    if (lowerMessage.includes('blood') || lowerMessage.includes('pressure')) {
        if (vitals.systolic === null || vitals.diastolic === null) {
            return "I do not have a blood pressure reading yet. Sync the phone app first, then I can comment on the number shown here.";
        }

        if (vitals.systolic >= 140 || vitals.diastolic >= 90) {
            return `Your latest blood pressure is ${vitals.systolic}/${vitals.diastolic} mmHg, which is above the usual target range. Recheck after resting and share it with doctor/staff if it remains high.`;
        }

        if (vitals.systolic < 90 || vitals.diastolic < 60) {
            return `Your latest blood pressure is ${vitals.systolic}/${vitals.diastolic} mmHg, which is low. If you feel dizzy, faint, confused, or weak, seek medical help.`;
        }

        return `Your latest blood pressure is ${vitals.systolic}/${vitals.diastolic} mmHg. It does not look severely abnormal from this single reading, but keep checking trends.`;
    }

    if (lowerMessage.includes('temp') || lowerMessage.includes('fever')) {
        if (vitals.temperature === null) {
            return "I do not have a temperature reading yet. Sync the phone app first, then I can comment on the number shown here.";
        }

        if (vitals.temperature >= 38) {
            return `Your latest temperature is ${vitals.temperature.toFixed(1)} °C, which may indicate a fever. Rest, hydrate, monitor symptoms, and contact doctor/staff if it persists or you feel very unwell.`;
        }

        if (vitals.temperature < 35.5) {
            return `Your latest temperature is ${vitals.temperature.toFixed(1)} °C, which is low. Warm up safely and seek help if you feel confused, very cold, or weak.`;
        }

        return `Your latest temperature is ${vitals.temperature.toFixed(1)} °C, which is within a common normal range. Keep monitoring if you have symptoms.`;
    }

    if (lowerMessage.includes('advice') || lowerMessage.includes('diagnose') || lowerMessage.includes('disease')) {
        return "I can give wellness suggestions based on the displayed readings, but I cannot diagnose disease or replace a clinician. For worrying symptoms, contact doctor/staff.";
    }

    return "I can help explain the heart rate, blood pressure, temperature, watch connection, and what the displayed readings may mean. I give suggestions only, not medical advice.";
}

function readCurrentVitals() {
    return {
        heartRate: parseNumberFromElement('heartRateValue'),
        temperature: parseNumberFromElement('temperatureValue'),
        systolic: 135,
        diastolic: 85
    };
}

function parseNumberFromElement(id) {
    const element = document.getElementById(id);
    if (!element) {
        return null;
    }
    const match = element.innerText.match(/-?\d+(\.\d+)?/);
    return match ? Number(match[0]) : null;
}

function handleKey(event) {
    if(event.key === "Enter") sendMessage();
}

// ===== CHART =====
document.addEventListener('DOMContentLoaded', () => {
    initChart();
});

function initChart() {
    const canvas = document.getElementById('healthChart');
    const ctx = canvas.getContext('2d');
    const container = canvas.parentElement;
    canvas.width = container.clientWidth;
    canvas.height = 120;

    const heartRateData = [98, 96, 97, 99, 95, 98, 100];
    const bloodPressureData = [135, 133, 134, 136, 132, 135, 137];
    const temperatureData = [37.8, 37.7, 37.9, 37.6, 37.8, 37.7, 37.9];

    const width = canvas.width, height = canvas.height, padding = 20;
    const chartWidth = width - padding*2, chartHeight = height - padding*2;

    ctx.clearRect(0,0,width,height);
    ctx.fillStyle = '#f8f9fa';
    ctx.fillRect(padding, padding, chartWidth, chartHeight);

    ctx.strokeStyle = '#e0e0e0';
    ctx.lineWidth = 1;
    for(let i=0;i<=4;i++){
        const y = padding + (chartHeight*i/4);
        ctx.beginPath(); ctx.moveTo(padding,y); ctx.lineTo(width-padding,y); ctx.stroke();
    }
    for(let i=0;i<=6;i++){
        const x = padding + (chartWidth*i/6);
        ctx.beginPath(); ctx.moveTo(x,padding); ctx.lineTo(x,height-padding); ctx.stroke();
    }

    drawLine(ctx, heartRateData, '#e53935', padding, chartHeight, width, height);
    drawLine(ctx, bloodPressureData, '#00897b', padding, chartHeight, width, height);
    drawLine(ctx, temperatureData, '#ff7043', padding, chartHeight, width, height);
}

function drawLine(ctx, data, color, padding, chartHeight, width, height) {
    ctx.strokeStyle = color; ctx.lineWidth = 3; ctx.lineJoin='round'; ctx.lineCap='round';
    ctx.beginPath();
    const step = (width - padding*2) / (data.length-1);

    data.forEach((value,index)=>{
        const x = padding + index*step;
        const normalizedValue = (value-90)/10;
        const y = height - padding - normalizedValue*chartHeight;
        if(index===0) ctx.moveTo(x,y); else ctx.lineTo(x,y);
    });
    ctx.stroke();

    ctx.fillStyle = color;
    data.forEach((value,index)=>{
        const x = padding + index*step;
        const normalizedValue = (value-90)/10;
        const y = height - padding - normalizedValue*chartHeight;
        ctx.beginPath(); ctx.arc(x,y,4,0,Math.PI*2); ctx.fill();
    });
}

// Close chat if click outside
window.addEventListener('click', e=>{
    if(e.target === document.getElementById('chatModal')) closeChat();
});
