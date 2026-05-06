package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    // 1. Yeni Kullanıcı Kaydetme
    public void save(User user) {
        getSession().persist(user);
    }

    // 2. Email'e Göre Kullanıcı Bulma (Login İşlemi İçin Criteria API)
    public User findByEmail(String email) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteria = builder.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        // Kural: WHERE email = 'girilen_email'
        criteria.select(root).where(builder.equal(root.get("email"), email));

        List<User> results = session.createQuery(criteria).getResultList();

        // Kullanıcı varsa ilkini dön, yoksa null dön
        return results.isEmpty() ? null : results.get(0);
    }
}