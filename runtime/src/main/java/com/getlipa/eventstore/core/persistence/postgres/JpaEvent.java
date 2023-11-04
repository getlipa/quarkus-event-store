package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.EventMetadata;
import com.getlipa.eventstore.core.proto.Payload;
import com.google.protobuf.Message;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
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
    /*@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_position_seq")
    @SequenceGenerator(
            name = "event_position_seq",
            sequenceName = "event_position_seq",
            allocationSize = 1 // FIXME - default is 50 -> change SQL? Why does 1 lead to increments by 2?!
    )

     */
    private long position;

    @Column(nullable = false)
    private String logDomain;

    @Column(nullable = false)
    private UUID logDomainUuid;

    @Column(nullable = false)
    private UUID logId;

    @GeneratedValue(strategy = GenerationType.TABLE)
    @TableGenerator(
            name = "gugus"
    )
    @Column(nullable = false)
    private long logIndex;

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
                .type(event.getPayload().getTypeId())
                .payload(event.getPayload().get().toByteArray());
    }

    public static JpaEvent findLatest(UUID logDomain, UUID logId) {
        return find(
                "logDomain = :logDomain AND logId = :logId",
                Sort.descending("position"),
                Parameters.with("logDomain", logDomain)
                        .and("logId", logId)
        ).firstResult();
    }

    public Event<Message> toPersistedEvent() {
        return Event.from(this)
                .withPayload(Payload.create(type, payload));
    }
}
