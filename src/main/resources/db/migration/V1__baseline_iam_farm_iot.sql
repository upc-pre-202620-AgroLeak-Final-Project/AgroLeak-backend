-- Baseline matches the preceding IAM/Farm release. IF NOT EXISTS supports
-- the explicit DEV baseline at version 0 without deleting Hibernate-created data.
CREATE TABLE IF NOT EXISTS users (
 id uuid PRIMARY KEY, first_name varchar(100) NOT NULL, last_name varchar(100) NOT NULL,
 email varchar(254) NOT NULL, password_hash varchar(255) NOT NULL, role varchar(20) NOT NULL,
 active boolean NOT NULL, created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL,
 CONSTRAINT uk_users_email UNIQUE(email), CHECK (role IN ('ADMIN','FARMER','TECHNICIAN'))
);
CREATE TABLE IF NOT EXISTS farms (
 id uuid PRIMARY KEY, name varchar(100) NOT NULL, location varchar(255) NOT NULL,
 area_hectares numeric(14,4) NOT NULL, owner_id uuid NOT NULL, created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL
);
CREATE TABLE IF NOT EXISTS fields (
 id uuid PRIMARY KEY, farm_id uuid NOT NULL, name varchar(100) NOT NULL, area_hectares numeric(14,4) NOT NULL, description varchar(1000)
);
CREATE TABLE IF NOT EXISTS sectors (
 id uuid PRIMARY KEY, field_id uuid NOT NULL, name varchar(100) NOT NULL, area_hectares numeric(14,4) NOT NULL, status varchar(30) NOT NULL
);
CREATE TABLE IF NOT EXISTS crops (
 id uuid PRIMARY KEY, sector_id uuid NOT NULL, name varchar(100) NOT NULL, variety varchar(100), planted_at date NOT NULL, status varchar(30) NOT NULL
);
CREATE TABLE IF NOT EXISTS devices (
 id uuid PRIMARY KEY, name varchar(100) NOT NULL, location varchar(120) NOT NULL, status varchar(30) NOT NULL,
 last_seen timestamptz, device_type varchar(30), battery_level integer, firmware_version varchar(100), installation_date date, sector_id uuid, owner_id uuid
);
CREATE TABLE IF NOT EXISTS sensor_readings (
 id uuid PRIMARY KEY, device_id uuid NOT NULL REFERENCES devices(id), sensor_type varchar(30) NOT NULL,
 value double precision NOT NULL, unit varchar(20) NOT NULL, recorded_at timestamptz NOT NULL
);
CREATE TABLE IF NOT EXISTS alerts (
 id uuid PRIMARY KEY, device_id uuid NOT NULL REFERENCES devices(id), type varchar(40) NOT NULL,
 severity varchar(20) NOT NULL, message varchar(255) NOT NULL, status varchar(20) NOT NULL, created_at timestamptz NOT NULL, resolved_at timestamptz
);
CREATE TABLE IF NOT EXISTS valve_commands (
 id uuid PRIMARY KEY, device_id uuid NOT NULL REFERENCES devices(id), action varchar(20) NOT NULL,
 status varchar(20) NOT NULL, requested_at timestamptz NOT NULL, confirmed_at timestamptz
);
CREATE TABLE IF NOT EXISTS pest_observations (
 id uuid PRIMARY KEY, device_id uuid NOT NULL REFERENCES devices(id), pest_count integer NOT NULL,
 confidence double precision NOT NULL, image_url varchar(500), recorded_at timestamptz NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_farm_ownerid ON farms(owner_id);
CREATE INDEX IF NOT EXISTS idx_field_farmid ON fields(farm_id);
CREATE INDEX IF NOT EXISTS idx_sector_fieldid ON sectors(field_id);
CREATE INDEX IF NOT EXISTS idx_crop_sectorid ON crops(sector_id);
CREATE INDEX IF NOT EXISTS idx_reading_device_sensor_time ON sensor_readings(device_id,sensor_type,recorded_at);
CREATE INDEX IF NOT EXISTS idx_alert_device_status ON alerts(device_id,status);
CREATE INDEX IF NOT EXISTS idx_valve_device_requested ON valve_commands(device_id,requested_at);
CREATE INDEX IF NOT EXISTS idx_pest_device_recorded ON pest_observations(device_id,recorded_at);
