package com.example.warehouse.service;

import com.example.warehouse.dao.SupplierDAO;
import com.example.warehouse.model.Supplier;
import java.util.List;

public class SupplierService {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<Supplier> findAll() {
        return supplierDAO.findAll();
    }

    public Supplier findById(Long id) {
        return supplierDAO.findById(id);
    }

    public void save(Supplier supplier) {
        supplierDAO.save(supplier);
    }

    public void update(Supplier supplier) {
        supplierDAO.update(supplier);
    }

    public void delete(Long id) {
        supplierDAO.delete(id);
    }

    public long count() {
        return supplierDAO.count();
    }
}
