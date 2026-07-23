package com.tgs.ecommerce.user.domain;

import com.tgs.ecommerce.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario del sistema. Hereda de {@link AuditableEntity} para tener
 * createdAt/updatedAt/createdBy/updatedBy automáticos.
 *
 * <p>Un usuario puede tener varios roles (relación ManyToMany), materializada
 * en la tabla intermedia {@code user_roles}.
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario para login (único). */
    @Column(nullable = false, length = 64)
    private String username;

    /** Email (único). */
    @Column(nullable = false, length = 128)
    private String email;

    /** Hash BCrypt de la contraseña. Nunca en texto plano. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** Nombre completo mostrado en la UI. */
    @Column(name = "full_name", length = 128)
    private String fullName;

    /** Si el usuario puede autenticarse (soft-delete). */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Roles asignados. FetchType.EAGER porque siempre los necesitamos al
     * cargar el usuario (para construir los authorities en Spring Security).
     * En el ManyToMany usamos Set (no List) para evitar duplicados.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Helpers para mantener la relación consistente en ambos lados.
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
