
CREATE TABLE event (
    "uuid" uuid NOT NULL,
    "position" BIGINT PRIMARY KEY,
    "logIndex" BIGINT NOT NULL,
    "logContext" text NOT NULL, --- TODO: normalize?
    "logDomainUuid" uuid NOT NULL,
    "logId" uuid NOT NULL,
    "type" uuid,
    "createdAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "correlationId" uuid NOT NULL,
    "causationId" uuid NOT NULL,
    "payload" bytea,
    CONSTRAINT event_id_unique UNIQUE ("uuid"),
    CONSTRAINT event_log_index_unique UNIQUE ("logIndex", "logDomainUuid", "logId")
);

CREATE SEQUENCE event_position_seq;

CREATE TABLE event_log_index_sequence (
    "logDomainUuid" uuid NOT NULL,
    "logId" uuid NOT NULL,
    "currentValue" BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT next_log_index_pk PRIMARY KEY ("logDomainUuid", "logId")
);


CREATE OR REPLACE FUNCTION insert_event_trigger()
RETURNS TRIGGER AS $$
DECLARE
  "expectedIndex" BIGINT;
BEGIN
  -- Update the next_log_index value for the stream
  INSERT INTO event_log_index_sequence ("logDomainUuid", "logId", "currentValue")
  VALUES (NEW."logDomainUuid", NEW."logId", 0)
  ON CONFLICT("logId", "logDomainUuid")
  DO UPDATE SET "currentValue" = event_log_index_sequence."currentValue" + 1
  RETURNING event_log_index_sequence."currentValue" INTO "expectedIndex";

  -- NOTE: it's important to set position here as otherwise,
  -- position order may not equal logIndex order within the same series
  NEW."position" := nextval('event_position_seq');

  -- -1 -> AnyIndex: Insert at the next index
  -- -2 -> AfterAnyIndex: Insert at the next index, as long as it is not the first one
  IF NEW."logIndex" = -1 OR (NEW."logIndex" = -2 AND "expectedIndex" > 0) THEN
    NEW."logIndex" := "expectedIndex";
  ELSIF NEW."logIndex" != "expectedIndex" THEN
    -- NOTE: the message format is important, as Hibernate depends on it to determine the constraint name
    -- (see: https://github.com/hibernate/hibernate-orm/blob/bc901f516265b0ee6ded0419f083d2c184662c6f/hibernate-core/src/main/java/org/hibernate/dialect/PostgreSQLDialect.java#L960)
    RAISE EXCEPTION 'logIndex "%" violates check constraint "consecutive_log_index"',
    NEW."logIndex" USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_event_trigger
BEFORE INSERT ON event
FOR EACH ROW
EXECUTE FUNCTION insert_event_trigger();