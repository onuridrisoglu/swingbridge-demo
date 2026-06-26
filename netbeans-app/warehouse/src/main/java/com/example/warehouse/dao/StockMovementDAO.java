package com.example.warehouse.dao;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.model.StockMovement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

public class StockMovementDAO {

    public List<StockMovement> findAll() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT m FROM StockMovement m JOIN FETCH m.product p JOIN FETCH p.category ORDER BY m.movementDate DESC",
                StockMovement.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<StockMovement> findByProduct(Long productId) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT m FROM StockMovement m JOIN FETCH m.product p WHERE p.id = :pid ORDER BY m.movementDate DESC",
                StockMovement.class)
                .setParameter("pid", productId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public List<StockMovement> findRecent(int limit) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT m FROM StockMovement m JOIN FETCH m.product ORDER BY m.movementDate DESC",
                StockMovement.class)
                .setMaxResults(limit)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(StockMovement movement) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            movement.setMovementDate(LocalDateTime.now());
            em.persist(movement);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save stock movement", e);
        } finally {
            em.close();
        }
    }
}
