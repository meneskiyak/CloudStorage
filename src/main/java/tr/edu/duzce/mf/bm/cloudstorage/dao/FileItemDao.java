package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class FileItemDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    // 1. Yeni Dosya Kaydetme
    public void save(FileItem fileItem) {
        getSession().persist(fileItem);
    }

    // 2. ID'ye Göre Dosya Bulma (Silme veya indirme işlemi öncesi gerekir)
    public FileItem findById(Long id) {
        return getSession().get(FileItem.class, id);
    }

    // İSTER 1: Klasöre ve Sahibe Göre Listeleme (Zaten vardı, koruyoruz)
    public List<FileItem> findFilesByFolderAndOwner(Folder folder, User owner) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = builder.createQuery(FileItem.class);
        Root<FileItem> root = criteria.from(FileItem.class);

        Predicate ownerCondition = builder.equal(root.get("owner"), owner);
        Predicate notDeletedCondition = builder.isFalse(root.get("isDeleted")); // Sadece silinmemişler
        Predicate folderCondition = (folder == null)
                ? builder.isNull(root.get("folder"))
                : builder.equal(root.get("folder"), folder);

        criteria.select(root).where(builder.and(ownerCondition, notDeletedCondition, folderCondition));
        return session.createQuery(criteria).getResultList();
    }

    // İSTER 2: İsme veya Türe Göre Arama (Dynamic Criteria Query)
    public List<FileItem> searchFiles(User owner, String keyword, String mimeType) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = builder.createQuery(FileItem.class);
        Root<FileItem> root = criteria.from(FileItem.class);

        // Temel şartlar: Bu kullanıcıya ait ve silinmemiş olmalı
        Predicate finalPredicate = builder.and(
                builder.equal(root.get("owner"), owner),
                builder.isFalse(root.get("isDeleted"))
        );

        // Kullanıcı bir isim (keyword) girdiyse, dosya adında LIKE araması yap
        if (keyword != null && !keyword.trim().isEmpty()) {
            Predicate namePredicate = builder.like(
                    builder.lower(root.get("originalName")),
                    "%" + keyword.toLowerCase() + "%"
            );
            finalPredicate = builder.and(finalPredicate, namePredicate);
        }

        // Kullanıcı bir tür (örn: image/png, pdf vs.) seçtiyse MIME tipinde LIKE araması yap
        if (mimeType != null && !mimeType.trim().isEmpty()) {
            Predicate typePredicate = builder.like(
                    builder.lower(root.get("mimeType")),
                    "%" + mimeType.toLowerCase() + "%"
            );
            finalPredicate = builder.and(finalPredicate, typePredicate);
        }

        criteria.select(root).where(finalPredicate);
        return session.createQuery(criteria).getResultList();
    }

    // İSTER 3: Soft Delete (Veritabanından uçurmak yerine bayrağı true yapıyoruz)
    public void softDelete(FileItem fileItem) {
        fileItem.setIsDeleted(true);
        getSession().merge(fileItem); // Güncelleme işlemi
    }
}