-- Scalar UUID associations stay scalar in Java; database constraints prevent
-- orphaned rows during concurrent parent deletion and child creation.
ALTER TABLE farms ADD CONSTRAINT fk_farm_owner FOREIGN KEY(owner_id) REFERENCES users(id);
ALTER TABLE fields ADD CONSTRAINT fk_field_farm FOREIGN KEY(farm_id) REFERENCES farms(id);
ALTER TABLE sectors ADD CONSTRAINT fk_sector_field FOREIGN KEY(field_id) REFERENCES fields(id);
ALTER TABLE crops ADD CONSTRAINT fk_crop_sector FOREIGN KEY(sector_id) REFERENCES sectors(id);
ALTER TABLE devices ADD CONSTRAINT fk_device_sector FOREIGN KEY(sector_id) REFERENCES sectors(id);
-- Device.owner_id intentionally has no FK: earlier demo rows used a synthetic
-- internal identity; no user or device rows are deleted during this transition.
