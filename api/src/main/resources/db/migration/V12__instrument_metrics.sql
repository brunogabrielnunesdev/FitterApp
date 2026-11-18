-- Event identity is optional for historical and anonymous requests without identifiers.
ALTER TABLE search_events ADD COLUMN visitor_id VARCHAR(128), ADD COLUMN idempotency_key VARCHAR(128), ADD COLUMN identity_key VARCHAR(160);
ALTER TABLE profile_view_events ADD COLUMN visitor_id VARCHAR(128), ADD COLUMN idempotency_key VARCHAR(128), ADD COLUMN identity_key VARCHAR(160);
ALTER TABLE contact_events ADD COLUMN visitor_id VARCHAR(128), ADD COLUMN idempotency_key VARCHAR(128), ADD COLUMN identity_key VARCHAR(160);
ALTER TABLE app_access_events ADD COLUMN visitor_id VARCHAR(128), ADD COLUMN idempotency_key VARCHAR(128), ADD COLUMN identity_key VARCHAR(160);
CREATE UNIQUE INDEX ux_search_event_intent ON search_events(identity_key,idempotency_key) WHERE identity_key IS NOT NULL AND idempotency_key IS NOT NULL;
CREATE UNIQUE INDEX ux_profile_view_event_intent ON profile_view_events(identity_key,idempotency_key) WHERE identity_key IS NOT NULL AND idempotency_key IS NOT NULL;
CREATE UNIQUE INDEX ux_contact_event_intent ON contact_events(identity_key,idempotency_key) WHERE identity_key IS NOT NULL AND idempotency_key IS NOT NULL;
CREATE UNIQUE INDEX ux_app_access_event_intent ON app_access_events(identity_key,idempotency_key) WHERE identity_key IS NOT NULL AND idempotency_key IS NOT NULL;
CREATE INDEX ix_app_access_identity ON app_access_events(identity_key,occurred_at);

CREATE TABLE metrics_tracking_start (started_at TIMESTAMPTZ NOT NULL);
INSERT INTO metrics_tracking_start(started_at) VALUES (CURRENT_TIMESTAMP);

CREATE TABLE profile_review_events (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  profile_id UUID NOT NULL REFERENCES personal_profiles(id),
  revision_id UUID NOT NULL REFERENCES personal_profile_revisions(id),
  event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('SUBMITTED','APPROVED','REJECTED')),
  occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_profile_review_events_time ON profile_review_events(event_type,occurred_at);
CREATE FUNCTION record_profile_review_event() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'UPDATE' AND NEW.status IS DISTINCT FROM OLD.status AND NEW.status IN ('PENDING_REVIEW','APPROVED','REJECTED') THEN
    INSERT INTO profile_review_events(profile_id, revision_id, event_type, occurred_at)
    VALUES (NEW.personal_id, NEW.id, CASE NEW.status WHEN 'PENDING_REVIEW' THEN 'SUBMITTED' ELSE NEW.status END, NEW.updated_at);
  END IF;
  RETURN NEW;
END;
$$;
CREATE TRIGGER trg_profile_review_metrics AFTER UPDATE OF status ON personal_profile_revisions FOR EACH ROW EXECUTE FUNCTION record_profile_review_event();
