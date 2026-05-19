# SmartHealth ERD

This ERD reflects the current SmartHealth database design, including Galaxy Watch 5 readings that flow through Samsung Health, Health Connect, the Android app, and the Java backend.

Additional diagram exports are stored in `docs/diagrams/`:

- `smarthealth-live-eerd.mmd` for the Mermaid source
- `smarthealth-live-eerd-link.txt` for the Mermaid Live editor link
- `smarthealth-top-down-eerd.drawio` for editable Draw.io source
- `smarthealth-top-down-eerd.pdf` for a portable diagram export

```mermaid
erDiagram
    users ||--o{ user_auth : has
    users ||--o{ devices : owns
    users ||--o{ pulse_readings : has
    users ||--o{ temperature_readings : has
    users ||--o{ blood_pressure_readings : has
    users ||--o{ device_sync_events : syncs
    users ||--o{ emergency_alerts : triggers
    users ||--o| language_settings : prefers
    users ||--o{ voice_logs : creates

    devices ||--o{ pulse_readings : records
    devices ||--o{ temperature_readings : records
    devices ||--o{ blood_pressure_readings : records
    devices ||--o{ device_sync_events : logs

    emergency_alerts ||--o{ ambulance_notifications : sends

    users {
        int id PK
        varchar title
        varchar first_name
        varchar surname
        date dob
        varchar gender
        varchar marital_status
        varchar email UK
        varchar cell_number
        text address
        timestamp created_at
    }

    user_auth {
        int auth_id PK
        int user_id FK
        varchar password_hash
        timestamp created_at
    }

    devices {
        int device_id PK
        int user_id FK
        varchar device_type
        varchar manufacturer
        varchar device_model
        varchar platform
        boolean active
        timestamp created_at
    }

    pulse_readings {
        int pulse_id PK
        int user_id FK
        int device_id FK
        int bpm
        varchar status
        varchar source
        varchar external_record_id
        timestamp measured_at
        timestamp synced_at
        timestamp recorded_at
    }

    temperature_readings {
        int temp_id PK
        int user_id FK
        int device_id FK
        decimal temperature
        varchar status
        varchar source
        varchar external_record_id
        timestamp measured_at
        timestamp synced_at
        timestamp recorded_at
    }

    blood_pressure_readings {
        int bp_id PK
        int user_id FK
        int device_id FK
        int systolic
        int diastolic
        varchar status
        varchar source
        varchar external_record_id
        timestamp measured_at
        timestamp synced_at
        timestamp recorded_at
    }

    device_sync_events {
        int sync_id PK
        int user_id FK
        int device_id FK
        varchar source_platform
        varchar external_record_id
        timestamp synced_for
        varchar sync_status
    }

    emergency_alerts {
        int alert_id PK
        int user_id FK
        int bpm
        varchar alert_status
        int countdown_seconds
        timestamp created_at
    }

    ambulance_notifications {
        int notification_id PK
        int alert_id FK
        timestamp sent_time
        varchar response_status
    }

    language_settings {
        int language_id PK
        int user_id FK
        varchar language
    }

    voice_logs {
        int voice_id PK
        int user_id FK
        text command_text
        timestamp command_time
    }

    health_advice {
        int advice_id PK
        varchar advice_type
        text message
    }
```

## Watch Sync Flow

```text
Galaxy Watch 5 -> Samsung Health -> Health Connect -> Android app -> MobileHealthSyncServlet -> database
```

Health Connect may provide manufacturer/model metadata. When it does, the backend stores that information in `devices` and links each synced reading to the device through `device_id`.
