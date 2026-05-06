package tr.edu.duzce.mf.bm.cloudstorage.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "folders")
public class Folder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // Klasörü oluşturan kullanıcı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Üst klasör — null ise root klasördür
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private Folder parent;

    // Alt klasörler — taşıma/kopyalama işlemlerinde recursive traverse için
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Folder> children;

    // Bu klasörün içindeki dosyalar
    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileItem> files;

    // Soft delete — silinen klasörler hemen DB'den kaldırılmaz
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Folder getParent() { return parent; }
    public void setParent(Folder parent) { this.parent = parent; }

    public List<Folder> getChildren() { return children; }
    public void setChildren(List<Folder> children) { this.children = children; }

    public List<FileItem> getFiles() { return files; }
    public void setFiles(List<FileItem> files) { this.files = files; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Root kontrolü
    @Transient
    public boolean isRoot() { return parent == null; }
}