package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import tr.edu.duzce.mf.bm.cloudstorage.dao.base.BaseDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.*;
import java.util.List;

@Repository
public class FolderDao extends BaseDao<Folder> {

    public FolderDao() {
        super(Folder.class);
    }

    public void softDelete(Folder folder) {
        folder.setDeleted(true);
        update(folder);
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

    public List<Folder> findByParentAndOwnerInTrash(Folder parent, User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = createCriteriaQuery();
        Root<Folder> root = getRoot(criteria);

        Predicate ownerCondition = builder.equal(root.get("owner"), owner);
        Predicate deletedCondition = builder.isTrue(root.get("deleted"));
        Predicate parentCondition = (parent == null)
                ? builder.isNull(root.get("parent"))
                : builder.equal(root.get("parent"), parent);

        criteria.select(root).where(
                builder.and(ownerCondition, deletedCondition, parentCondition)
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

    public List<Folder> findDeletedByOwner(User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = createCriteriaQuery();
        Root<Folder> root = getRoot(criteria);
        
        // Üst hiyerarşiyi kontrol etmek için LEFT JOIN
        Join<Folder, Folder> parentJoin = root.join("parent", JoinType.LEFT);

        // Şart: (is_deleted == true) VE (üst klasörü yoksa VEYA üst klasörü silinmemişse)
        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isTrue(root.get("deleted")),
                        builder.or(
                                builder.isNull(root.get("parent")),
                                builder.isFalse(parentJoin.get("deleted"))
                        )
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<Folder> findStarredByOwner(User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = createCriteriaQuery();
        Root<Folder> root = getRoot(criteria);
        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isFalse(root.get("deleted")),
                        builder.isTrue(root.get("starred"))
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<Folder> findRecentByOwner(User owner, int limit) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Folder> criteria = createCriteriaQuery();
        Root<Folder> root = getRoot(criteria);

        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isFalse(root.get("deleted"))
                )
        ).orderBy(builder.desc(root.get("updatedAt")));

        return getSession().createQuery(criteria).setMaxResults(limit).getResultList();
    }
}
