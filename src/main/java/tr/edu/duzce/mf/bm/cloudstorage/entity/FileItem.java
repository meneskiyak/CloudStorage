package tr.edu.duzce.mf.bm.cloudstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "file_items")
@Getter
@Setter
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
    private boolean deleted = false;

    @Column(name = "is_starred", nullable = false)
    private boolean starred = false;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // DİKKAT: @OneToMany(mappedBy = "file") kısmı KESİNLİKLE kaldırıldı
    // Çünkü Share sınıfında 'file' adında bir değişken yok, 'resourceId' var.

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Tarayıcıda inline gösterilebilir mi? (resim, PDF)
    @Transient
    public boolean canPreview() {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/") || mimeType.equals("application/pdf");
    }
}