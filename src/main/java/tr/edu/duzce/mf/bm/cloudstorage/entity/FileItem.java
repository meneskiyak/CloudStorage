package tr.edu.duzce.mf.bm.cloudstorage.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "file_items")
public class FileItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    // Disk/MinIO'daki benzersiz isim (UUID tabanlı)
    @Column(name = "stored_name", nullable = false, length = 255, unique = true)
    private String storedName;

    // Disk yolu veya MinIO bucket path
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    // image/png, application/pdf, vb. — tarayıcıda gösterim için kritik
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    // Dosyanın bulunduğu klasör — null ise root'ta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = true)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // Paylaşımlar
    @OneToMany(mappedBy = "file", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileShare> shares;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public List<FileShare> getShares() { return shares; }
    public void setShares(List<FileShare> shares) { this.shares = shares; }

    // Tarayıcıda inline gösterilebilir mi? (resim, PDF)
    @Transient
    public boolean isPreviewable() {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/") || mimeType.equals("application/pdf");
    }
}