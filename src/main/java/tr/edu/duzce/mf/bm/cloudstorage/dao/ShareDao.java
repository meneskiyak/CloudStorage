package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.springframework.stereotype.Repository;

import tr.edu.duzce.mf.bm.cloudstorage.core.enums.ResourceType;
import tr.edu.duzce.mf.bm.cloudstorage.dao.base.BaseDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Share;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class ShareDao extends BaseDao<Share> {

    public ShareDao() {
        super(Share.class);
    }

    // save, delete, findById — BaseDao'dan geliyor

    // "Benimle paylaşılanlar" listesi
    public List<Share> findSharedWithUser(User user) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Share> criteria = createCriteriaQuery();
        Root<Share> root = getRoot(criteria);
        criteria.select(root).where(
                builder.equal(root.get("sharedWithUser"), user)
        );
        return getSession().createQuery(criteria).getResultList();
    }

    // "Benim paylaştıklarım" listesi
    public List<Share> findSharedByUser(User user) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Share> criteria = createCriteriaQuery();
        Root<Share> root = getRoot(criteria);
        criteria.select(root).where(
                builder.equal(root.get("sharedBy"), user)
        );
        return getSession().createQuery(criteria).getResultList();
    }

    // Belirli bir kaynak için tüm paylaşımları getir
    // Örn: Bu dosyayı kimlerle paylaştım?
    public List<Share> findByResource(ResourceType resourceType, Long resourceId) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Share> criteria = createCriteriaQuery();
        Root<Share> root = getRoot(criteria);
        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("resourceType"), resourceType),
                        builder.equal(root.get("resourceId"), resourceId)
                )
        );
        return getSession().createQuery(criteria).getResultList();
    }

    // Belirli bir kullanıcının belirli bir kaynağa erişimi var mı?
    // Erişim kontrolü için — service katmanında kullanılacak
    public Share findByResourceAndUser(ResourceType resourceType, Long resourceId, User user) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<Share> criteria = createCriteriaQuery();
        Root<Share> root = getRoot(criteria);
        criteria.select(root).where(
                builder.and(
                        builder.equal(root.get("resourceType"), resourceType),
                        builder.equal(root.get("resourceId"), resourceId),
                        builder.equal(root.get("sharedWithUser"), user)
                )
        );
        List<Share> results = getSession().createQuery(criteria).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}