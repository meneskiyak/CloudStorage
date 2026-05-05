package com.BM470.dao;

import com.BM470.entity.Kullanici;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Bunu ekledik

@Repository
public class KullaniciDAO {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    // @Transactional anotasyonu veritabanına yazma/çizme izni verir
    @Transactional
    public void kaydet(Kullanici kullanici) {
        getSession().persist(kullanici);
    }
}