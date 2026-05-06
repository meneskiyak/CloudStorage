package tr.edu.duzce.mf.bm.cloudstorage.dao.base;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Repository
public abstract class BaseDao<T> {

    @Autowired
    private SessionFactory sessionFactory;

    private final Class<T> entityClass;

    protected BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected CriteriaQuery<T> createCriteriaQuery() {
        return getSession().getCriteriaBuilder().createQuery(entityClass);
    }

    protected CriteriaBuilder getCriteriaBuilder() {
        return getSession().getCriteriaBuilder();
    }

    protected Root<T> getRoot(CriteriaQuery<T> criteria) {
        return criteria.from(entityClass);
    }

    // Ortak CRUD metodları
    public void save(T entity) {
        getSession().persist(entity);
    }

    public void update(T entity) {
        getSession().merge(entity);
    }

    public void delete(T entity) {
        getSession().remove(entity);
    }

    public T findById(Long id) {
        return getSession().get(entityClass, id);
    }

    public List<T> findAll() {
        CriteriaQuery<T> criteria = createCriteriaQuery();
        criteria.from(entityClass);
        return getSession().createQuery(criteria).getResultList();
    }
}
