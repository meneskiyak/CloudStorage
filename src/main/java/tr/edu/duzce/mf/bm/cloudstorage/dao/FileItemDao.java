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

    // 2. Belirli bir klasördeki (veya ana dizindeki) "Silinmemiş" dosyaları getirme
    public List<FileItem> findFilesByFolderAndOwner(Folder folder, User owner) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = builder.createQuery(FileItem.class);
        Root<FileItem> root = criteria.from(FileItem.class);

        Predicate ownerCondition = builder.equal(root.get("owner"), owner);
        Predicate notDeletedCondition = builder.isFalse(root.get("isDeleted"));
        Predicate folderCondition;

        // Eğer folder null ise, kullanıcı ana dizindedir (Root)
        if (folder == null) {
            folderCondition = builder.isNull(root.get("folder"));
        } else {
            folderCondition = builder.equal(root.get("folder"), folder);
        }

        // Tüm şartları birleştiriyoruz (AND operatörü ile)
        criteria.select(root).where(builder.and(ownerCondition, notDeletedCondition, folderCondition));

        return session.createQuery(criteria).getResultList();
    }
}