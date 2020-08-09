package main.properties;

import main.annotation.Property;
import main.refresher.Refresher;

public class CompanyProperties {
    private static CompanyProperties instance;
    private String path;
    private static final Refresher refresher = new Refresher();

    @Property(propertyName = "com.mycompany.name")
    private String myCompanyName;
    @Property(propertyName = "com.mycompany.owner", defaultValue = "I am owner")
    private String myCompanyOwner;
    @Property(propertyName = "com.mycompany.address")
    private Address address;
    @Property(propertyName = "com.mycompany.years.old")
    private Integer yearsOld;
    @Property(propertyName = "com.mycompany.cost")
    private Double cost;

    private CompanyProperties() {
    }

    public synchronized void doRefresh() {
        refresher.doRefresh(path, instance);
    }

    public static CompanyProperties getInstance() {
        CompanyProperties localInstance = instance;
        if (localInstance == null) {
            synchronized (CompanyProperties.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CompanyProperties();
                    instance.doRefresh();
                }
            }
        }
        return localInstance;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMyCompanyName() {
        return myCompanyName;
    }

    public void setMyCompanyName(String myCompanyName) {
        this.myCompanyName = myCompanyName;
    }

    public String getMyCompanyOwner() {
        return myCompanyOwner;
    }

    public void setMyCompanyOwner(String myCompanyOwner) {
        this.myCompanyOwner = myCompanyOwner;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Integer getYearsOld() {
        return yearsOld;
    }

    public void setYearsOld(Integer yearsOld) {
        this.yearsOld = yearsOld;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }
}