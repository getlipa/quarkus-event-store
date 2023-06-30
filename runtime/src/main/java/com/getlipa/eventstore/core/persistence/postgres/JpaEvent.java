package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.EventMetadata;
import com.getlipa.eventstore.core.proto.PayloadParser;
import com.google.protobuf.Message;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Builder
@Entity(name = "event")
@AllArgsConstructor
@NoArgsConstructor
public class JpaEvent extends PanacheEntityBase implements EventMetadata {

    @Column(nullable = false)
    private UUID id;

    @Id
    private long position;

    @Column(nullable = false)
    private UUID seriesType;

    @Column(nullable = false)
    private UUID seriesId;

    @Column(nullable = false)
    private long seriesIndex;

    @Column
    private UUID type;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private UUID correlationId;

    @Column(nullable = false)
    private UUID causationId;

    @Column(nullable = false)
    private byte[] payload;

    public static <T extends Message> JpaEvent.JpaEventBuilder builder(EphemeralEvent<T> event) {
        return new JpaEventBuilder()
                .id(event.getId())
                .createdAt(event.getCreatedAt())
                .correlationId(event.getCorrelationId())
                .causationId(event.getCausationId())
                .payload(event.payload().toByteArray());
    }

    public static JpaEvent findLatest(UUID seriesType, UUID seriesId) {
        return find(
                "seriesType = :seriesType AND seriesId = :seriesId",
                Sort.descending("position"),
                Parameters.with("seriesType", seriesType)
                        .and("seriesId", seriesId)
        ).firstResult();
    }

    public Event<Message> toPersistedEvent(PayloadParser parser) {
        return Event.from(this)
                .payload(() -> parser.parse(type, payload))
                .build();
    }
}
