package org.example.domain.customer.customers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PersonalCustomer extends Customer {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Map<Integer, List<String>> addresses;
    //follow 2 character ISO
    private String nationality;
    private String countryOfBirth;
    private int expectedCashTurnover;
    private List<String> expectedCountriesOfOutwardPayments;
    private List<String> getExpectedCountriesOfInwardPayments;
    //add financial details later on

    public PersonalCustomer(int id, String firstName, String middleName, String lastName, LocalDate dateOfBirth, Map<Integer, List<String>> addresses, String nationality, String countryOfBirth, int expectedCashTurnover, List<String> expectedCountriesOfOutwardPayments, List<String> getExpectedCountriesOfInwardPayments) {
        super(id);
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.addresses = addresses;
        this.nationality = nationality;
        this.countryOfBirth = countryOfBirth;
        this.expectedCashTurnover = expectedCashTurnover;
        this.expectedCountriesOfOutwardPayments = expectedCountriesOfOutwardPayments;
        this.getExpectedCountriesOfInwardPayments = getExpectedCountriesOfInwardPayments;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Map<Integer, List<String>> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<Integer, List<String>> addresses) {
        this.addresses = addresses;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getCountryOfBirth() {
        return countryOfBirth;
    }

    public void setCountryOfBirth(String countryOfBirth) {
        this.countryOfBirth = countryOfBirth;
    }

    public int getExpectedCashTurnover() {
        return expectedCashTurnover;
    }

    public void setExpectedCashTurnover(int expectedCashTurnover) {
        this.expectedCashTurnover = expectedCashTurnover;
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
}