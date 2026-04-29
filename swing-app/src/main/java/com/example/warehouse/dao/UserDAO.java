package com.example.warehouse.dao;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.find(User.class, username);
        } finally {
            em.close();
        }
    }

    public User authenticate(String username, String password) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            List<User> results = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :u AND u.password = :p",
                User.class)
                .setParameter("u", username)
                .setParameter("p", password)
                .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public void save(User user) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save user", e);
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
