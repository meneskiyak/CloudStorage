package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.springframework.stereotype.Repository;

import tr.edu.duzce.mf.bm.cloudstorage.dao.base.BaseDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.*;
import java.util.List;
import java.util.Map;

@Repository
public class FileItemDao extends BaseDao<FileItem> {

    public FileItemDao() {
        super(FileItem.class);
    }

    // save, update, delete, findById, findAll — BaseDao'dan geliyor

    public List<FileItem> findFilesByFolderAndOwner(Folder folder, User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);

        Predicate ownerCondition = builder.equal(root.get("owner"), owner);
        Predicate notDeletedCondition = builder.isFalse(root.get("deleted"));
        Predicate folderCondition = (folder == null)
                ? builder.isNull(root.get("folder"))
                : builder.equal(root.get("folder"), folder);

        criteria.select(root).where(
                builder.and(ownerCondition, notDeletedCondition, folderCondition)
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<FileItem> searchFiles(User owner, String keyword, String mimeType) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);

        Predicate finalPredicate = builder.and(
                builder.equal(root.get("owner"), owner),
                builder.isFalse(root.get("deleted"))
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            finalPredicate = builder.and(finalPredicate,
                    builder.like(
                            builder.lower(root.get("originalName")),
                            "%" + keyword.toLowerCase() + "%"
                    )
            );
        }

        if (mimeType != null && !mimeType.trim().isEmpty()) {
            finalPredicate = builder.and(finalPredicate,
                    builder.like(
                            builder.lower(root.get("mimeType")),
                            "%" + mimeType.toLowerCase() + "%"
                    )
            );
        }

        criteria.select(root).where(finalPredicate);
        return getSession().createQuery(criteria).getResultList();
    }

    public void softDelete(FileItem fileItem) {
        fileItem.setDeleted(true);
        update(fileItem);
    }

    public List<FileItem> findAllByOwner(User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);
        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isFalse(root.get("deleted"))
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<FileItem> findDeletedByOwner(User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);
        
        // Klasör hiyerarşisini kontrol etmek için LEFT JOIN kullanıyoruz
        Join<FileItem, Folder> folderJoin = root.join("folder", JoinType.LEFT);

        // Şart: (is_deleted == true) VE (klasörü yoksa VEYA klasörü silinmemişse)
        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isTrue(root.get("deleted")),
                        builder.or(
                                builder.isNull(root.get("folder")),
                                builder.isFalse(folderJoin.get("deleted"))
                        )
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<FileItem> findByFolderAndOwnerInTrash(Folder folder, User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);

        Predicate ownerCondition = builder.equal(root.get("owner"), owner);
        Predicate deletedCondition = builder.isTrue(root.get("deleted"));
        Predicate folderCondition = (folder == null)
                ? builder.isNull(root.get("folder"))
                : builder.equal(root.get("folder"), folder);

        criteria.select(root).where(
                builder.and(ownerCondition, deletedCondition, folderCondition)
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public List<FileItem> findStarredByOwner(User owner) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);
        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isFalse(root.get("deleted")),
                        builder.isTrue(root.get("starred"))
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }

    public Map<String, Long> countFilesByMimeTypeGroup() {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Object[]> criteria = builder.createQuery(Object[].class);
        Root<FileItem> root = criteria.from(FileItem.class);

        // MIME tiplerini i18n anahtarlarına göre grupluyoruz
        Expression<String> groupExpression = builder.selectCase()
                .when(builder.like(root.get("mimeType"), "image/%"), "common.type.image")
                .when(builder.like(root.get("mimeType"), "video/%"), "common.type.video")
                .when(builder.equal(root.get("mimeType"), "application/pdf"), "common.type.document")
                .when(builder.like(root.get("mimeType"), "%word%"), "common.type.document")
                .when(builder.like(root.get("mimeType"), "%excel%"), "common.type.document")
                .when(builder.like(root.get("mimeType"), "%sheet%"), "common.type.document")
                .otherwise("common.type.other").as(String.class);

        criteria.multiselect(groupExpression, builder.count(root));
        criteria.groupBy(groupExpression);

        List<Object[]> results = getSession().createQuery(criteria).getResultList();
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    public List<FileItem> findRecentByOwner(User owner, int limit) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);

        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("owner"), owner),
                        builder.isFalse(root.get("deleted"))
                )
        ).orderBy(builder.desc(root.get("updatedAt")));

        return getSession().createQuery(criteria).setMaxResults(limit).getResultList();
    }

    public List<FileItem> findGlobalRecentItems(int limit) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<FileItem> criteria = createCriteriaQuery();
        Root<FileItem> root = getRoot(criteria);

        criteria.select(root).orderBy(builder.desc(root.get("updatedAt")));

        return getSession().createQuery(criteria).setMaxResults(limit).getResultList();
    }
}
