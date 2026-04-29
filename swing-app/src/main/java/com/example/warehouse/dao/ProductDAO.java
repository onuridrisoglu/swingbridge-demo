package com.example.warehouse.dao;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.util.List;

public class ProductDAO {

    public List<Product> findAll() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.supplier ORDER BY p.name",
                Product.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Product> findLowStock() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.supplier " +
                "WHERE p.quantity <= p.reorderLevel ORDER BY p.quantity ASC",
                Product.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> findStockByCategory() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT c.name, SUM(p.quantity) FROM Product p JOIN p.category c GROUP BY c.name ORDER BY c.name",
                Object[].class).getResultList();
        } finally {
            em.close();
        }
    }

    public Product findById(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    public void save(Product product) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(product);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save product", e);
        } finally {
            em.close();
        }
    }

    public void update(Product product) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(product);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update product", e);
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = em.find(Product.class, id);
            if (p != null) em.remove(p);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to delete product", e);
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countLowStock() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.quantity <= p.reorderLevel",
                Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public BigDecimal totalInventoryValue() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            BigDecimal result = em.createQuery(
                "SELECT SUM(p.unitPrice * p.quantity) FROM Product p",
                BigDecimal.class).getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }
}
