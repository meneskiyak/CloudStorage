package tr.edu.duzce.mf.bm.cloudstorage.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

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

    // ADMIN veya USER
    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
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

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Long getUploadLimitBytes() { return uploadLimitBytes; }
    public void setUploadLimitBytes(Long uploadLimitBytes) { this.uploadLimitBytes = uploadLimitBytes; }

    public Long getUsedBytes() { return usedBytes; }
    public void setUsedBytes(Long usedBytes) { this.usedBytes = usedBytes; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<Folder> getFolders() { return folders; }
    public void setFolders(List<Folder> folders) { this.folders = folders; }

    public List<FileItem> getFiles() { return files; }
    public void setFiles(List<FileItem> files) { this.files = files; }

    // Limit kontrolü için yardımcı — @Transient ile DB'ye yazılmaz
    @Transient
    public boolean isLimitExceeded(long additionalBytes) {
        return (usedBytes + additionalBytes) > uploadLimitBytes;
    }
}