package com.example.warehouse.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PersistenceUtil {

    private static EntityManagerFactory emf;

    public static void init() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("warehouseDB");
        }
    }

    public static EntityManager getEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("PersistenceUtil not initialized. Call init() first.");
        }
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
