package tr.edu.duzce.mf.bm.cloudstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tr.edu.duzce.mf.bm.cloudstorage.core.enums.Permission;
import tr.edu.duzce.mf.bm.cloudstorage.core.enums.ResourceType;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "shares", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resource_type", "resource_id", "shared_with_user_id"})
})
@Getter
@Setter
public class Share implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // FILE veya FOLDER
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 10)
    private ResourceType resourceType;

    // FileItem ya da Folder'ın id'si
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private User sharedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 10)
    private Permission permission = Permission.READ;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}