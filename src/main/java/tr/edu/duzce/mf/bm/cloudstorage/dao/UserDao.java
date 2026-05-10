package tr.edu.duzce.mf.bm.cloudstorage.dao;

import org.springframework.stereotype.Repository;

import tr.edu.duzce.mf.bm.cloudstorage.dao.base.BaseDao;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    // save, update, delete, findById, findAll — BaseDao'dan geliyor

    public User findByEmail(String email) {
        CriteriaBuilder builder = getCriteriaBuilder();
        CriteriaQuery<User> criteria = createCriteriaQuery();
        Root<User> root = getRoot(criteria);
        criteria.select(root).where(
                builder.equal(root.get("email"), email)
        );
        List<User> results = getSession().createQuery(criteria).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
