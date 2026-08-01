package org.ByteWatch.model;

/**
 * Stage 1 (Account Context): canonical customer/account metadata used
 * to enrich risk investigations.
 */
public class Customer {

    private Long id;
    private String name;
    private String accNum;
    private String accType;
    private String bankName;
    private String currency;

    public Customer() {
    }

    public Customer(Long id, String name, String accNum, String accType, String bankName, String currency) {
        this.id = id;
        this.name = name;
        this.accNum = accNum;
        this.accType = accType;
        this.bankName = bankName;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccNum() {
        return accNum;
    }

    public void setAccNum(String accNum) {
        this.accNum = accNum;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
