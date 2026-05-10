package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.springframework.stereotype.Repository;

import tr.edu.duzce.mf.bm.cloudstorage.dao.base.BaseDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.*;
import java.util.List;

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
}
