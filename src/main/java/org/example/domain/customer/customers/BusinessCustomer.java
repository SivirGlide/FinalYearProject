package org.example.domain.customer.customers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BusinessCustomer extends Customer {
    private String companyName;
    private int companiesHouseNumber;
    private LocalDate dateOfIncorperation;
    private List<PersonalCustomer> beneficialOwners;
    private int annualTurnover;
    private int sicCode;
    private String knowYourBusinessActivities;
    private List<String> expectedCountriesOfOutwardPayments;
    private List<String> getExpectedCountriesOfInwardPayments;
    private int expectedCashTurnover;
    private int countryOfIncorperation;
    private Map<Integer, List<String>> addresses;

    public BusinessCustomer(int customerNumber, String companyName, int companiesHouseNumber, LocalDate dateOfIncorperation, List<PersonalCustomer> beneficialOwners, int annualTurnover, int sicCode, String knowYourBusinessActivities, List<String> expectedCountriesOfOutwardPayments, List<String> getExpectedCountriesOfInwardPayments, int expectedCashTurnover, int countryOfIncorperation, Map<Integer, List<String>> addresses) {
        super(customerNumber);
        this.companyName = companyName;
        this.companiesHouseNumber = companiesHouseNumber;
        this.dateOfIncorperation = dateOfIncorperation;
        this.beneficialOwners = beneficialOwners;
        this.annualTurnover = annualTurnover;
        this.sicCode = sicCode;
        this.knowYourBusinessActivities = knowYourBusinessActivities;
        this.expectedCountriesOfOutwardPayments = expectedCountriesOfOutwardPayments;
        this.getExpectedCountriesOfInwardPayments = getExpectedCountriesOfInwardPayments;
        this.expectedCashTurnover = expectedCashTurnover;
        this.countryOfIncorperation = countryOfIncorperation;
        this.addresses = addresses;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getCompaniesHouseNumber() {
        return companiesHouseNumber;
    }

    public void setCompaniesHouseNumber(int companiesHouseNumber) {
        this.companiesHouseNumber = companiesHouseNumber;
    }

    public LocalDate getDateOfIncorperation() {
        return dateOfIncorperation;
    }

    public void setDateOfIncorperation(LocalDate dateOfIncorperation) {
        this.dateOfIncorperation = dateOfIncorperation;
    }

    public List<PersonalCustomer> getBeneficialOwners() {
        return beneficialOwners;
    }

    public void setBeneficialOwners(List<PersonalCustomer> beneficialOwners) {
        this.beneficialOwners = beneficialOwners;
    }

    public int getAnnualTurnover() {
        return annualTurnover;
    }

    public void setAnnualTurnover(int annualTurnover) {
        this.annualTurnover = annualTurnover;
    }

    public int getSicCode() {
        return sicCode;
    }

    public void setSicCode(int sicCode) {
        this.sicCode = sicCode;
    }

    public String getKnowYourBusinessActivities() {
        return knowYourBusinessActivities;
    }

    public void setKnowYourBusinessActivities(String knowYourBusinessActivities) {
        this.knowYourBusinessActivities = knowYourBusinessActivities;
    }

    public List<String> getExpectedCountriesOfOutwardPayments() {
        return expectedCountriesOfOutwardPayments;
    }

    public void setExpectedCountriesOfOutwardPayments(List<String> expectedCountriesOfOutwardPayments) {
        this.expectedCountriesOfOutwardPayments = expectedCountriesOfOutwardPayments;
    }

    public List<String> getGetExpectedCountriesOfInwardPayments() {
        return getExpectedCountriesOfInwardPayments;
    }

    public void setGetExpectedCountriesOfInwardPayments(List<String> getExpectedCountriesOfInwardPayments) {
        this.getExpectedCountriesOfInwardPayments = getExpectedCountriesOfInwardPayments;
    }

    public int getExpectedCashTurnover() {
        return expectedCashTurnover;
    }

    public void setExpectedCashTurnover(int expectedCashTurnover) {
        this.expectedCashTurnover = expectedCashTurnover;
    }

    public int getCountryOfIncorperation() {
        return countryOfIncorperation;
    }

    public void setCountryOfIncorperation(int countryOfIncorperation) {
        this.countryOfIncorperation = countryOfIncorperation;
    }

    public Map<Integer, List<String>> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<Integer, List<String>> addresses) {
        this.addresses = addresses;
    }
}
