package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class FolderDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    // 1. Yeni Klasör Oluşturma veya Taşıma (Parent Güncelleme)
    public void saveOrUpdate(Folder folder) {
        // saveOrUpdate mantığı ile çalışır, ID varsa günceller (taşıma işlemi), yoksa yeni açar
        getSession().merge(folder);
    }

    // 2. ID ile Klasör Bulma (İçine girmek veya taşınacak hedefi seçmek için)
    public Folder findById(Long id) {
        return getSession().get(Folder.class, id);
    }

    // 3. Kullanıcının Belirli Bir Dizindeki Alt Klasörlerini Getirme (Recursive Gezinme İçin)
    public List<Folder> findByParentAndOwner(Folder parent, User owner) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = builder.createQuery(Folder.class);
        Root<Folder> root = criteria.from(Folder.class);

        // Şartlar: Sadece bu kullanıcının ve silinmemiş klasörler
        Predicate ownerCondition = builder.equal(root.get("owner"), owner);
        Predicate notDeletedCondition = builder.isFalse(root.get("isDeleted"));
        Predicate parentCondition;

        // Parent null ise root (ana) dizindedir
        if (parent == null) {
            parentCondition = builder.isNull(root.get("parent"));
        } else {
            parentCondition = builder.equal(root.get("parent"), parent);
        }

        criteria.select(root).where(builder.and(ownerCondition, notDeletedCondition, parentCondition));
        return session.createQuery(criteria).getResultList();
    }
}