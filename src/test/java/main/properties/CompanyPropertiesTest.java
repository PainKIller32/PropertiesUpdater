package main.properties;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CompanyPropertiesTest {

    @Test
    void getInstance() {
        CompanyProperties companyPropertiesFirstInstance = CompanyProperties.getInstance();
        CompanyProperties companyPropertiesSecondInstance = CompanyProperties.getInstance();
        assertEquals(companyPropertiesFirstInstance, companyPropertiesSecondInstance);
    }

    @Test
    void doRefresh() {
        CompanyProperties companyProperties = CompanyProperties.getInstance();

        String initNameValue = companyProperties.getMyCompanyName();
        String initOwnerValue = companyProperties.getMyCompanyOwner();
        Address initAddressValue = companyProperties.getAddress();
        Integer initYearsOldValue = companyProperties.getYearsOld();
        Double initCostValue = companyProperties.getCost();

        companyProperties.setPath("test1.properties");
        companyProperties.doRefresh();

        assertNotEquals(initNameValue, companyProperties.getMyCompanyName());
        assertEquals(initOwnerValue, companyProperties.getMyCompanyOwner());
        assertNotEquals(initAddressValue, companyProperties.getAddress());
        assertNotEquals(initYearsOldValue,  companyProperties.getYearsOld());
        assertNotEquals(initCostValue, companyProperties.getCost());
    }

    @Test
    void doMultithreadingRefresh() {
        CompanyProperties company = CompanyProperties.getInstance();
        ExecutorService pool = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 10; i++) {
            pool.submit(()-> {
                company.setPath("test1.properties");
                company.doRefresh();
            });
            pool.submit(()-> {
                company.setPath("test2.properties");
                company.doRefresh();
            });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean inCompanyFirstValues = company.getMyCompanyName().contains("1")
                && company.getYearsOld().equals(1)
                && company.getCost().equals(1.0)
                && company.getAddress().getHome().equals(1)
                && company.getAddress().getStreet().contains("1");
        boolean inCompanySecondValues = company.getMyCompanyName().contains("2")
                && company.getYearsOld().equals(2)
                && company.getCost().equals(2.0)
                && company.getAddress().getHome().equals(2)
                && company.getAddress().getStreet().contains("2");
        assertTrue(inCompanyFirstValues || inCompanySecondValues);
    }
}