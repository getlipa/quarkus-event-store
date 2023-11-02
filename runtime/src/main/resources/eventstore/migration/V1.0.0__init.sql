
CREATE TABLE event (
    "id" uuid NOT NULL,
    "position" BIGINT PRIMARY KEY,
    "seriesIndex" BIGINT NOT NULL,
    "seriesType" uuid NOT NULL,
    "seriesId" uuid NOT NULL,
    "type" uuid,
    "createdAt" TIMESTAMP WITH TIME ZONE NOT NULL,
    "correlationId" uuid NOT NULL,
    "causationId" uuid NOT NULL,
    "payload" bytea,
    CONSTRAINT event_id_unique UNIQUE ("id"),
    CONSTRAINT event_series_index_unique UNIQUE ("seriesIndex", "seriesType", "seriesId")
);

CREATE SEQUENCE event_position_seq;

CREATE TABLE event_series_index_sequence (
    "seriesType" uuid NOT NULL,
    "seriesId" uuid NOT NULL,
    "currentValue" BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT next_series_index_pk PRIMARY KEY ("seriesType", "seriesId")
);


CREATE OR REPLACE FUNCTION insert_event_trigger()
RETURNS TRIGGER AS $$
DECLARE
  "expectedIndex" BIGINT;
BEGIN
  -- Update the next_series_index value for the stream
  INSERT INTO event_series_index_sequence ("seriesType", "seriesId", "currentValue")
  VALUES (NEW."seriesType", NEW."seriesId", 0)
  ON CONFLICT("seriesId", "seriesType")
  DO UPDATE SET "currentValue" = event_series_index_sequence."currentValue" + 1
  RETURNING event_series_index_sequence."currentValue" INTO "expectedIndex";

  -- NOTE: it's important to set position here as otherwise,
  -- position order may not equal seriesIndex order within the same series
  NEW."position" := nextval('event_position_seq');

  -- -1 -> AnyIndex: Insert at the next index
  -- -2 -> AfterAnyIndex: Insert at the next index, as long as it is not the first one
  IF NEW."seriesIndex" = -1 OR (NEW."seriesIndex" = -2 AND "expectedIndex" > 0) THEN
    NEW."seriesIndex" := "expectedIndex";
  ELSIF NEW."seriesIndex" != "expectedIndex" THEN
    -- NOTE: the message format is important, as Hibernate depends on it to determine the constraint name
    -- (see: https://github.com/hibernate/hibernate-orm/blob/bc901f516265b0ee6ded0419f083d2c184662c6f/hibernate-core/src/main/java/org/hibernate/dialect/PostgreSQLDialect.java#L960)
    RAISE EXCEPTION 'seriesIndex "%" violates check constraint "consecutive_series_index"',
    NEW."seriesIndex" USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_event_trigger
BEFORE INSERT ON event
FOR EACH ROW
EXECUTE FUNCTION insert_event_trigger();