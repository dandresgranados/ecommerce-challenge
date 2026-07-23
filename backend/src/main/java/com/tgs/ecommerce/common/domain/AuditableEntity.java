package com.tgs.ecommerce.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Clase base para todas las entidades del dominio.
 *
 * <p>{@code @MappedSuperclass} indica que esta clase NO es una tabla propia,
 * pero sus campos se copian en cada entidad hija. Así evitamos repetir los
 * campos de auditoría (createdAt, updatedAt, createdBy, updatedBy) en cada
 * una de las 8 entidades del sistema.
 *
 * <p>{@code @EntityListeners(AuditingEntityListener.class)} engancha el
 * mecanismo de JPA Auditing: cuando guardas o actualizas una entidad hija,
 * Spring rellena automáticamente los 4 campos.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    /** Momento en que se creó el registro. Nunca se actualiza. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Momento de la última modificación. Se actualiza en cada save. */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Username del usuario que creó el registro (resuelto por AuditorAware). */
    @CreatedBy
    @Column(name = "created_by", length = 64, updatable = false)
    private String createdBy;

    /** Username del usuario que hizo la última modificación. */
    @LastModifiedBy
    @Column(name = "updated_by", length = 64)
    private String updatedBy;
}
