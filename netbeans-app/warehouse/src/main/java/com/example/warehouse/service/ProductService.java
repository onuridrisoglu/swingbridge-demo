package com.example.warehouse.service;

import com.example.warehouse.AppSession;
import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.dao.ProductDAO;
import com.example.warehouse.dao.StockMovementDAO;
import com.example.warehouse.model.Product;
import com.example.warehouse.model.StockMovement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();

    public List<Product> findAll() {
        return productDAO.findAll();
    }

    public List<Product> findLowStock() {
        return productDAO.findLowStock();
    }

    public List<Object[]> findStockByCategory() {
        return productDAO.findStockByCategory();
    }

    public Product findById(Long id) {
        return productDAO.findById(id);
    }

    public void save(Product product) {
        productDAO.save(product);
    }

    public void update(Product product) {
        productDAO.update(product);
    }

    public void delete(Long id) {
        productDAO.delete(id);
    }

    public long count() {
        return productDAO.count();
    }

    public long countLowStock() {
        return productDAO.countLowStock();
    }

    public BigDecimal totalInventoryValue() {
        return productDAO.totalInventoryValue();
    }

    public void adjustStock(Product product, StockMovement.MovementType type, int quantity, String reason) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product managed = em.find(Product.class, product.getId());
            if (type == StockMovement.MovementType.IN) {
                managed.setQuantity(managed.getQuantity() + quantity);
            } else if (type == StockMovement.MovementType.OUT) {
                managed.setQuantity(Math.max(0, managed.getQuantity() - quantity));
            } else {
                managed.setQuantity(quantity);
            }
            StockMovement movement = new StockMovement(managed, type, quantity, reason, AppSession.getUsername());
            em.persist(movement);
            tx.commit();
            product.setQuantity(managed.getQuantity());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Failed to adjust stock", e);
        } finally {
            em.close();
        }
    }
}
