package com.example.warehouse.dao;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.model.Supplier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> findAll() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT s FROM Supplier s ORDER BY s.name", Supplier.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Supplier findById(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.find(Supplier.class, id);
        } finally {
            em.close();
        }
    }

    public void save(Supplier supplier) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(supplier);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to save supplier", e);
        } finally {
            em.close();
        }
    }

    public void update(Supplier supplier) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(supplier);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to update supplier", e);
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Supplier s = em.find(Supplier.class, id);
            if (s != null) em.remove(s);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to delete supplier", e);
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(s) FROM Supplier s", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
