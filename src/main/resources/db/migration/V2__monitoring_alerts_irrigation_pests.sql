ALTER TABLE alerts ADD COLUMN acknowledged_at timestamptz;
ALTER TABLE alerts ADD COLUMN acknowledged_by uuid;
ALTER TABLE alerts ADD COLUMN resolved_by uuid;
ALTER TABLE alerts ADD COLUMN sector_id uuid REFERENCES sectors(id);
ALTER TABLE alerts ALTER COLUMN device_id DROP NOT NULL;
-- Hibernate's previous enum constraints must evolve too. The legacy pressure
-- value remains readable; new rules emit LOW_PRESSURE/HIGH_PRESSURE.
ALTER TABLE alerts DROP CONSTRAINT IF EXISTS alerts_type_check;
ALTER TABLE alerts DROP CONSTRAINT IF EXISTS alerts_status_check;
ALTER TABLE alerts DROP CONSTRAINT IF EXISTS alerts_severity_check;
ALTER TABLE alerts ADD CONSTRAINT alerts_type_check CHECK(type IN ('LEAK','OBSTRUCTION','PRESSURE_OUT_OF_RANGE','LOW_PRESSURE','HIGH_PRESSURE','DEVICE_OFFLINE','PEST_DETECTED'));
ALTER TABLE alerts ADD CONSTRAINT alerts_status_check CHECK(status IN ('ACTIVE','ACKNOWLEDGED','RESOLVED'));
ALTER TABLE alerts ADD CONSTRAINT alerts_severity_check CHECK(severity IN ('LOW','MEDIUM','HIGH','CRITICAL'));
ALTER TABLE alerts ADD CONSTRAINT alerts_source_check CHECK(device_id IS NOT NULL OR sector_id IS NOT NULL);

ALTER TABLE pest_observations ADD COLUMN sector_id uuid REFERENCES sectors(id);
ALTER TABLE pest_observations ADD COLUMN pest_type varchar(100);
ALTER TABLE pest_observations ALTER COLUMN device_id DROP NOT NULL;
ALTER TABLE pest_observations ADD CONSTRAINT pests_source_check CHECK(device_id IS NOT NULL OR sector_id IS NOT NULL);
UPDATE pest_observations SET pest_type='UNSPECIFIED' WHERE pest_type IS NULL;
-- Historical rows keep device-based scope: their past sector cannot be inferred safely.
CREATE TABLE irrigation_settings (
 device_id uuid PRIMARY KEY REFERENCES devices(id), mode varchar(20) NOT NULL,
 CONSTRAINT irrigation_mode_check CHECK(mode IN ('MONITOR_ONLY','MANUAL','AUTO_SAFE'))
);
CREATE INDEX idx_devices_sector ON devices(sector_id);
CREATE INDEX idx_devices_status_seen ON devices(status,last_seen);
CREATE INDEX idx_reading_device_time ON sensor_readings(device_id,recorded_at);
CREATE INDEX idx_pests_sector_time ON pest_observations(sector_id,recorded_at);
CREATE INDEX idx_alert_sector_status ON alerts(sector_id,status);
