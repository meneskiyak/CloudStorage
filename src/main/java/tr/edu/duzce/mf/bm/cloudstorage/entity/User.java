package tr.edu.duzce.mf.bm.cloudstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import tr.edu.duzce.mf.bm.cloudstorage.core.enums.Role;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    // Byte cinsinden — admin bu değeri ayarlayabilir
    @Column(name = "upload_limit_bytes", nullable = false)
    private Long uploadLimitBytes = 1073741824L; // default 1 GB

    @Column(name = "used_bytes", nullable = false)
    private Long usedBytes = 0L;

    // ADMIN (0) veya USER (1) - Ordinal ile tutulur
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    // Kullanıcının sahip olduğu klasörler — DAO sorgularında lazy yeterli
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Folder> folders;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileItem> files;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @Column(name = "updated_at")
    private Date updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }


    // Limit kontrolü için yardımcı — @Transient ile DB'ye yazılmaz
    @Transient
    public boolean isLimitExceeded(long additionalBytes) {
        return (usedBytes + additionalBytes) > uploadLimitBytes;
    }
}