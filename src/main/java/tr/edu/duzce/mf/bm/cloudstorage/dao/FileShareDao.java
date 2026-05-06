package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Share;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class FileShareDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    // 1. Yeni Bir Paylaşım İzni Kaydetme
    public void save(Share share) {
        getSession().persist(share);
    }

    // 2. "Benimle Paylaşılanlar" Listesini Getirme
    public List<Share> findSharedWithUser(User user) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Share> criteria = builder.createQuery(Share.class);
        Root<Share> root = criteria.from(Share.class);

        // Kural: sharedWithUser alanı giriş yapan kullanıcıya eşit olanları getir
        criteria.select(root).where(builder.equal(root.get("sharedWithUser"), user));

        return session.createQuery(criteria).getResultList();
    }
}