package org.example.domain.customer.customers;

public abstract class Customer {
    protected int customerNumber;

    public Customer(int customerNumber) {
        this.customerNumber = customerNumber;
    }

    public int getCustomerNumber() { return customerNumber; }
}
