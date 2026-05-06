package tr.edu.duzce.mf.bm.cloudstorage.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "file_shares")
public class FileShare implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileItem file;

    // Paylaşılan kullanıcı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    // READ veya WRITE
    @Column(name = "permission", nullable = false, length = 10)
    private String permission = "READ";

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FileItem getFile() { return file; }
    public void setFile(FileItem file) { this.file = file; }

    public User getSharedWithUser() { return sharedWithUser; }
    public void setSharedWithUser(User sharedWithUser) { this.sharedWithUser = sharedWithUser; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}