from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "smarthealth-erd.png"

W, H = 2800, 2000
BG = "#F8FAFC"
TEXT = "#0F172A"
MUTED = "#475569"
LINE = "#CBD5E1"
CARD = "#FFFFFF"


def font(size, bold=False):
    candidates = [
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/Library/Fonts/Arial Bold.ttf" if bold else "/Library/Fonts/Arial.ttf",
        "/System/Library/Fonts/Supplemental/Helvetica Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Helvetica.ttf",
    ]
    for candidate in candidates:
        try:
            return ImageFont.truetype(candidate, size=size)
        except OSError:
            continue
    return ImageFont.load_default()


F_TITLE = font(54, True)
F_SUBTITLE = font(28)
F_TABLE_TITLE = font(25, True)
F_GROUP = font(16, True)
F_ROW = font(20)
F_BADGE = font(16, True)
F_LEGEND = font(19)


tables = {
    "users": {
        "title": "users",
        "group": "Patient Core",
        "x": 1030,
        "y": 500,
        "w": 430,
        "color": "#0B1628",
        "cols": [
            "PK id",
            "title, first_name, surname",
            "dob, gender, marital_status",
            "email UNIQUE",
            "cell_number, address",
            "id_number",
            "emergency_contact_name",
            "emergency_contact_number",
            "blood_group",
            "known_allergies",
            "chronic_conditions",
            "is_verified, created_at",
        ],
    },
    "user_auth": {
        "title": "user_auth",
        "group": "Login",
        "x": 140,
        "y": 250,
        "w": 330,
        "color": "#293B59",
        "cols": ["PK auth_id", "FK user_id", "password_hash", "created_at"],
    },
    "language_settings": {
        "title": "language_settings",
        "group": "Preference",
        "x": 140,
        "y": 540,
        "w": 330,
        "color": "#293B59",
        "cols": ["PK language_id", "FK user_id UNIQUE", "language"],
    },
    "voice_logs": {
        "title": "voice_logs",
        "group": "Interaction",
        "x": 140,
        "y": 800,
        "w": 330,
        "color": "#293B59",
        "cols": ["PK voice_id", "FK user_id", "command_text", "command_time"],
    },
    "devices": {
        "title": "devices",
        "group": "Watch / Phone",
        "x": 610,
        "y": 340,
        "w": 340,
        "color": "#006D5B",
        "cols": ["PK device_id", "FK user_id", "device_type", "manufacturer", "device_model", "platform", "active, created_at"],
    },
    "device_sync_events": {
        "title": "device_sync_events",
        "group": "Sync Audit",
        "x": 610,
        "y": 780,
        "w": 380,
        "color": "#006D5B",
        "cols": ["PK sync_id", "FK user_id", "FK device_id", "source_platform", "external_record_id", "synced_for", "sync_status"],
    },
    "pulse_readings": {
        "title": "pulse_readings",
        "group": "Vitals",
        "x": 1570,
        "y": 260,
        "w": 390,
        "color": "#B42318",
        "cols": ["PK pulse_id", "FK user_id", "FK device_id", "bpm, status", "source, external_record_id", "recorded_at", "measured_at, synced_at"],
    },
    "blood_pressure_readings": {
        "title": "blood_pressure_readings",
        "group": "Vitals",
        "x": 1570,
        "y": 680,
        "w": 390,
        "color": "#0E9F6E",
        "cols": ["PK bp_id", "FK user_id", "FK device_id", "systolic, diastolic", "status, source", "external_record_id", "recorded_at, measured_at, synced_at"],
    },
    "temperature_readings": {
        "title": "temperature_readings",
        "group": "Vitals",
        "x": 1570,
        "y": 1110,
        "w": 390,
        "color": "#F97316",
        "cols": ["PK temp_id", "FK user_id", "FK device_id", "temperature, status", "source, external_record_id", "recorded_at", "measured_at, synced_at"],
    },
    "health_sync_sections": {
        "title": "health_sync_sections",
        "group": "Mobile Summary",
        "x": 2190,
        "y": 620,
        "w": 430,
        "color": "#7C3AED",
        "cols": [
            "PK section_id",
            "FK user_id",
            "FK device_id",
            "source",
            "window_start, window_end",
            "heart_rate latest/min/max/avg/count",
            "temperature latest/min/max/avg/count",
            "systolic_latest, diastolic_latest",
            "blood_pressure_count",
        ],
    },
    "emergency_alerts": {
        "title": "emergency_alerts",
        "group": "Risk / Alert",
        "x": 1040,
        "y": 1340,
        "w": 370,
        "color": "#DC2626",
        "cols": ["PK alert_id", "FK user_id", "bpm", "alert_status", "countdown_seconds", "created_at"],
    },
    "ambulance_notifications": {
        "title": "ambulance_notifications",
        "group": "Response",
        "x": 1530,
        "y": 1540,
        "w": 390,
        "color": "#DC2626",
        "cols": ["PK notification_id", "FK alert_id", "sent_time", "response_status"],
    },
    "health_advice": {
        "title": "health_advice",
        "group": "Advice Content",
        "x": 120,
        "y": 1480,
        "w": 360,
        "color": "#4B5563",
        "cols": ["PK advice_id", "advice_type", "message"],
    },
}


relations = [
    ("users", "user_auth", "#293B59", "auth"),
    ("users", "language_settings", "#293B59", "language"),
    ("users", "voice_logs", "#293B59", "voice log"),
    ("users", "devices", "#006D5B", "owns"),
    ("devices", "device_sync_events", "#006D5B", "sync source"),
    ("users", "device_sync_events", "#006D5B", "sync logs"),
    ("users", "pulse_readings", "#B42318", "heart rate"),
    ("users", "blood_pressure_readings", "#0E9F6E", "blood pressure"),
    ("users", "temperature_readings", "#F97316", "temperature"),
    ("devices", "pulse_readings", "#64748B", "measured by"),
    ("devices", "blood_pressure_readings", "#64748B", "measured by"),
    ("devices", "temperature_readings", "#64748B", "measured by"),
    ("users", "health_sync_sections", "#7C3AED", "section summary"),
    ("devices", "health_sync_sections", "#7C3AED", "from device"),
    ("users", "emergency_alerts", "#DC2626", "triggers"),
    ("emergency_alerts", "ambulance_notifications", "#DC2626", "notifies"),
]


def table_h(table):
    return 138 + len(table["cols"]) * 36


def rect(name):
    t = tables[name]
    return t["x"], t["y"], t["w"], table_h(t)


def anchor(name, side):
    x, y, w, h = rect(name)
    if side == "left":
        return x, y + h / 2
    if side == "right":
        return x + w, y + h / 2
    if side == "top":
        return x + w / 2, y
    if side == "bottom":
        return x + w / 2, y + h
    raise ValueError(side)


def best_sides(src, dst):
    sx, sy, sw, sh = rect(src)
    dx, dy, dw, dh = rect(dst)
    if dx > sx + sw:
        return "right", "left"
    if sx > dx + dw:
        return "left", "right"
    if dy > sy + sh:
        return "bottom", "top"
    return "top", "bottom"


def draw_arrow(draw, points, color):
    draw.line(points, fill=color, width=4, joint="curve")
    p1, p2 = points[-2], points[-1]
    x1, y1 = p1
    x2, y2 = p2
    size = 12
    if abs(x2 - x1) >= abs(y2 - y1):
        direction = 1 if x2 > x1 else -1
        tri = [(x2, y2), (x2 - direction * size, y2 - size / 2), (x2 - direction * size, y2 + size / 2)]
    else:
        direction = 1 if y2 > y1 else -1
        tri = [(x2, y2), (x2 - size / 2, y2 - direction * size), (x2 + size / 2, y2 - direction * size)]
    draw.polygon(tri, fill=color)


def draw_relation(draw, src, dst, color, label):
    src_side, dst_side = best_sides(src, dst)
    a = anchor(src, src_side)
    b = anchor(dst, dst_side)
    if src_side in ("left", "right"):
        mid_x = (a[0] + b[0]) / 2
        points = [a, (mid_x, a[1]), (mid_x, b[1]), b]
        label_pos = (mid_x + 8, (a[1] + b[1]) / 2 - 12)
    else:
        mid_y = (a[1] + b[1]) / 2
        points = [a, (a[0], mid_y), (b[0], mid_y), b]
        label_pos = ((a[0] + b[0]) / 2 + 8, mid_y - 28)
    draw_arrow(draw, points, color)
    pad = 8
    bbox = draw.textbbox(label_pos, label, font=F_BADGE)
    draw.rounded_rectangle(
        (bbox[0] - pad, bbox[1] - 5, bbox[2] + pad, bbox[3] + 5),
        radius=10,
        fill=BG,
        outline=color,
        width=2,
    )
    draw.text(label_pos, label, fill=color, font=F_BADGE)


def draw_table(draw, table):
    x, y, w, h = table["x"], table["y"], table["w"], table_h(table)
    color = table["color"]
    draw.rounded_rectangle((x + 8, y + 10, x + w + 8, y + h + 10), radius=18, fill="#E2E8F0")
    draw.rounded_rectangle((x, y, x + w, y + h), radius=18, fill=CARD, outline=LINE, width=2)
    draw.rounded_rectangle((x, y, x + w, y + 62), radius=18, fill=color)
    draw.rectangle((x, y + 44, x + w, y + 62), fill=color)
    draw.text((x + 24, y + 18), table["title"], fill="#FFFFFF", font=F_TABLE_TITLE)
    draw.text((x + 24, y + 78), table["group"].upper(), fill=color, font=F_GROUP)
    row_y = y + 112
    for col in table["cols"]:
        fill = TEXT
        if col.startswith("PK"):
            fill = color
        elif col.startswith("FK"):
            fill = "#334155"
        draw.text((x + 24, row_y), col, fill=fill, font=F_ROW)
        row_y += 36


img = Image.new("RGB", (W, H), BG)
draw = ImageDraw.Draw(img)

draw.text((90, 60), "SmartHealth Database ERD", fill=TEXT, font=F_TITLE)
draw.text(
    (92, 128),
    "A presentation-friendly layout showing patient profile data, connected devices, synced vitals, and emergency alert flow.",
    fill=MUTED,
    font=F_SUBTITLE,
)

for src, dst, color, label in relations:
    draw_relation(draw, src, dst, color, label)

for table in tables.values():
    draw_table(draw, table)

legend_x, legend_y = 92, 1885
legend = [
    ("PK", "Primary key"),
    ("FK", "Foreign key"),
    ("UNIQUE", "Unique constraint"),
    ("Vitals", "Watch / phone measurements"),
    ("Alerts", "Risk response workflow"),
]
draw.rounded_rectangle((legend_x - 20, legend_y - 20, 1200, 1960), radius=20, fill="#FFFFFF", outline=LINE, width=2)
x = legend_x
for badge, text in legend:
    badge_w = max(54, draw.textbbox((0, 0), badge, font=F_BADGE)[2] + 24)
    draw.rounded_rectangle((x, legend_y, x + badge_w, legend_y + 34), radius=12, fill="#E2E8F0")
    draw.text((x + 12, legend_y + 8), badge, fill=TEXT, font=F_BADGE)
    draw.text((x + badge_w + 10, legend_y + 7), text, fill=MUTED, font=F_LEGEND)
    x += badge_w + 10 + draw.textlength(text, font=F_LEGEND) + 42

img.save(OUTPUT)
print(OUTPUT)
