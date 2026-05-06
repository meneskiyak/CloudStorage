package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import tr.edu.duzce.mf.bm.cloudstorage.dao.base.BaseDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class FolderDao extends BaseDao<Folder> {

    public FolderDao() {
        super(Folder.class);
    }

    // BaseDao'dan gelenler — artık burada yazmaya gerek yok:
    // save, update, delete, findById, findAll

    // saveOrUpdate → BaseDao'daki update ile aynı iş, kaldırıldı
    // softDelete — Folder'a özgü, burada kalıyor
    public void softDelete(Folder folder) {
        folder.setDeleted(true);
        update(folder); // BaseDao'daki merge
    }

    public List<Folder> findByParentAndOwner(Folder parent, User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = createCriteriaQuery();
        Root<Folder> root = getRoot(criteria);

        Predicate ownerCondition = builder.equal(root.get("owner"), owner);
        Predicate notDeletedCondition = builder.isFalse(root.get("deleted"));
        Predicate parentCondition = (parent == null)
                ? builder.isNull(root.get("parent"))
                : builder.equal(root.get("parent"), parent);

        criteria.select(root).where(
                builder.and(ownerCondition, notDeletedCondition, parentCondition)
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<Folder> findAllByOwner(User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = createCriteriaQuery();
        Root<Folder> root = getRoot(criteria);

        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isFalse(root.get("deleted"))
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<Folder> searchByName(String keyword, User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = createCriteriaQuery();
        Root<Folder> root = getRoot(criteria);

        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.like(
                                builder.lower(root.get("name")),
                                "%" + keyword.toLowerCase() + "%"
                        ),
                        builder.isFalse(root.get("deleted"))
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }
}