package com.example.warehouse.dao;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.model.PurchaseOrder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class PurchaseOrderDAO {

    public List<PurchaseOrder> findAll() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT o FROM PurchaseOrder o JOIN FETCH o.supplier LEFT JOIN FETCH o.items ORDER BY o.orderDate DESC",
                PurchaseOrder.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<PurchaseOrder> findByStatus(PurchaseOrder.OrderStatus status) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT o FROM PurchaseOrder o JOIN FETCH o.supplier LEFT JOIN FETCH o.items " +
                "WHERE o.status = :status ORDER BY o.orderDate DESC",
                PurchaseOrder.class)
                .setParameter("status", status)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public PurchaseOrder findById(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            List<PurchaseOrder> results = em.createQuery(
                "SELECT DISTINCT o FROM PurchaseOrder o JOIN FETCH o.supplier LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id",
                PurchaseOrder.class)
                .setParameter("id", id)
                .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public void save(PurchaseOrder order) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(order);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save purchase order", e);
        } finally {
            em.close();
        }
    }

    public void update(PurchaseOrder order) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(order);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update purchase order", e);
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PurchaseOrder o = em.find(PurchaseOrder.class, id);
            if (o != null) em.remove(o);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to delete purchase order", e);
        } finally {
            em.close();
        }
    }

    public long countByStatus(PurchaseOrder.OrderStatus status) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT COUNT(o) FROM PurchaseOrder o WHERE o.status = :status",
                Long.class)
                .setParameter("status", status)
                .getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Object[]> countByAllStatuses() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT o.status, COUNT(o) FROM PurchaseOrder o GROUP BY o.status",
                Object[].class).getResultList();
        } finally {
            em.close();
        }
    }
}
