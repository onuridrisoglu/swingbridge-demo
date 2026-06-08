package com.example.warehouse.service;

import com.example.warehouse.dao.SupplierDAO;
import com.example.warehouse.model.Supplier;
import java.nio.charset.StandardCharsets;
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

    public byte[] exportToCsv() {
        StringBuilder sb = new StringBuilder("ID,Supplier Name,Contact Person,Email,Phone,Address\r\n");
        for (Supplier s : findAll()) {
            sb.append(csvEscape(s.getId().toString())).append(',')
              .append(csvEscape(s.getName())).append(',')
              .append(csvEscape(s.getContactName())).append(',')
              .append(csvEscape(s.getEmail())).append(',')
              .append(csvEscape(s.getPhone())).append(',')
              .append(csvEscape(s.getAddress())).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String csvEscape(String val) {
        if (val == null) return "";
        String v = val.replace("\"", "\"\"");
        return (v.contains(",") || v.contains("\"") || v.contains("\n")) ? "\"" + v + "\"" : v;
    }
}
