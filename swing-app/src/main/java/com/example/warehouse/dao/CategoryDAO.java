package com.example.warehouse.dao;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class CategoryDAO {

    public List<Category> findAll() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c ORDER BY c.name", Category.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Category findById(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.find(Category.class, id);
        } finally {
            em.close();
        }
    }

    public void save(Category category) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(category);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save category", e);
        } finally {
            em.close();
        }
    }

    public void update(Category category) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(category);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update category", e);
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Category c = em.find(Category.class, id);
            if (c != null) em.remove(c);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to delete category", e);
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
