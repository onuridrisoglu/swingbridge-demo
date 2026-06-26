package com.example.warehouse.service;

import com.example.warehouse.dao.PurchaseOrderDAO;
import com.example.warehouse.model.PurchaseOrder;
import java.util.List;

public class PurchaseOrderService {

    private final PurchaseOrderDAO orderDAO = new PurchaseOrderDAO();

    public List<PurchaseOrder> findAll() {
        return orderDAO.findAll();
    }

    public PurchaseOrder findById(Long id) {
        return orderDAO.findById(id);
    }

    public void save(PurchaseOrder order) {
        orderDAO.save(order);
    }

    public void update(PurchaseOrder order) {
        orderDAO.update(order);
    }

    public void delete(Long id) {
        orderDAO.delete(id);
    }

    public long countPending() {
        return orderDAO.countByStatus(PurchaseOrder.OrderStatus.PENDING);
    }

    public List<Object[]> countByAllStatuses() {
        return orderDAO.countByAllStatuses();
    }
}
