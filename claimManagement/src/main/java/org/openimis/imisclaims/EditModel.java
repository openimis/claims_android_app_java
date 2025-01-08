package org.openimis.imisclaims;
public class EditModel {
    private String code;
    private String name;
    private String price;
    private String qty;
    private String qtyMax;
    private String type;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getQtyMax() {
        return qtyMax;
    }
    public void setQtyMax(String qtyMax) {
        this.qtyMax = qtyMax;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
    public String getQty() {
        return qty;
    }
    public void setQty(String qty) {
        this.qty = qty;
    }
}