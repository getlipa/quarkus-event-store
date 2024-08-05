package com.getlipa.eventstore.persistence.postgres;

import com.getlipa.eventstore.event.EphemeralEvent;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.event.EventMetadata;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.payload.Payload;
import com.google.protobuf.Message;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
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

    public static final String EVENT_TYPE_NAMESPACE = "$payload-type";

    public static final String DOMAIN_UUID_NAMESPACE = "$event-series-type";

    @Column(columnDefinition = "uuid", nullable = false)
    private UUID uuid;


    @jakarta.persistence.Id
    /*@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_position_seq")
    @SequenceGenerator(
            name = "event_position_seq",
            sequenceName = "event_position_seq",
            allocationSize = 1 // FIXME - default is 50 -> change SQL? Why does 1 lead to increments by 2?!
    )

     */
    private long position;

    @Column(nullable = false)
    private String logContext;

    @Column(nullable = false)
    private UUID logDomainUuid;

    @Column(name = "logId", nullable = false)
    private UUID logUuid;

    @Column(nullable = false)
    private long logIndex;

    @Column
    private UUID type;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "correlationId", nullable = false)
    private UUID correlationUuid;

    @Column(name = "causationId", nullable = false)
    private UUID causationUuid;

    @Column(nullable = false)
    private byte[] payload;

    @Transient
    @Override
    public Id getId() {
        return Id.from(uuid);
    }

    @Transient
    @Override
    public Id getCorrelationId() {
        return Id.from(correlationUuid);
    }

    @Transient
    @Override
    public Id getCausationId() {
        return Id.from(causationUuid);
    }

    @Transient
    @Override
    public Id getLogId() {
        return Id.from(logUuid);
    }

    public static <T extends Message> JpaEvent.JpaEventBuilder builder(EphemeralEvent<T> event) {
        return new JpaEventBuilder()
                .uuid(event.getId().toUuid())
                .createdAt(event.getCreatedAt())
                .correlationUuid(event.getCorrelationId().toUuid())
                .causationUuid(event.getCausationId().toUuid())
                .type(event.getPayload().getTypeId().toUuid())
                .payload(event.getPayload().get().toByteArray());
    }

    public static UUID createTypeId(String type) {
        return Id.derive(EVENT_TYPE_NAMESPACE, type)
                .toUuid();
    }

    public static UUID createLogDomainId(String logDomain) {
        return Id.derive(DOMAIN_UUID_NAMESPACE, logDomain)
                .toUuid();
    }

    public Event<Message> toPersistedEvent() {
        return Event.from(this)
                .withPayload(Payload.encoded(Id.from(type), payload));
    }
}
