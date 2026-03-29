package org.example.domain.customer.services;

import org.example.domain.customer.customers.BusinessCustomer;
import org.example.domain.customer.customers.PersonalCustomer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CustomerFactory {

    // Creates a PersonalCustomer
    public static PersonalCustomer createPersonalCustomer(
            int id,
            String firstName,
            String middleName,
            String lastName,
            LocalDate dateOfBirth,
            Map<Integer, List<String>> addresses,
            String nationality,
            String countryOfBirth,
            int expectedCashTurnover,
            List<String> expectedCountriesOfOutwardPayments,
            List<String> expectedCountriesOfInwardPayments
    ) {

        return new PersonalCustomer(
                id,
                firstName,
                middleName,
                lastName,
                dateOfBirth,
                addresses,
                nationality,
                countryOfBirth,
                expectedCashTurnover,
                expectedCountriesOfOutwardPayments,
                expectedCountriesOfInwardPayments
        );
    }

    // Creates a BusinessCustomer
    public static BusinessCustomer createBusinessCustomer(
            int customerNumber,
            String companyName,
            int companiesHouseNumber,
            LocalDate dateOfIncorporation,
            List<PersonalCustomer> beneficialOwners,
            int annualTurnover,
            int sicCode,
            String knowYourBusinessActivities,
            List<String> expectedCountriesOfOutwardPayments,
            List<String> expectedCountriesOfInwardPayments,
            int expectedCashTurnover,
            int countryOfIncorporation,
            Map<Integer, List<String>> addresses
    ) {

        if (beneficialOwners == null || beneficialOwners.isEmpty()) {
            throw new IllegalArgumentException("A business customer must have at least one beneficial owner.");
        }

        return new BusinessCustomer(
                customerNumber,
                companyName,
                companiesHouseNumber,
                dateOfIncorporation,
                beneficialOwners,
                annualTurnover,
                sicCode,
                knowYourBusinessActivities,
                expectedCountriesOfOutwardPayments,
                expectedCountriesOfInwardPayments,
                expectedCashTurnover,
                countryOfIncorporation,
                addresses
        );
    }
}
