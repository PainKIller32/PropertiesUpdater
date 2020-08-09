package main;

import main.properties.Address;
import main.properties.CompanyProperties;

import java.util.Scanner;

public class Loader {

    public static void main(String[] args) {
        CompanyProperties companyProperties = CompanyProperties.getInstance();
        if (args.length > 0) {
            companyProperties.setPath(args[0]);
            companyProperties.doRefresh();
        } else {
            companyProperties = CompanyProperties.getInstance();
        }
        print(companyProperties);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter number: \n1. Refresh properties\n2. Set path to properties file\n3.Exit");
            String command = scanner.nextLine();
            if (command.equals("1")) {
                companyProperties.doRefresh();
                print(companyProperties);
                System.out.println("Done\n");
            }
            if (command.equals("2")) {
                System.out.println("Enter path: ");
                String path = scanner.nextLine();
                companyProperties.setPath(path);
                System.out.println("Done\n");
            }
            if (command.equals("3")) {
                break;
            }
        }
    }

    private static void print(CompanyProperties companyProperties) {
        System.out.println("Company name: " + companyProperties.getMyCompanyName());
        System.out.println("Company owner: " + companyProperties.getMyCompanyOwner());
        System.out.println("Company years old: " + companyProperties.getYearsOld());
        System.out.println("Company cost: " + companyProperties.getCost());
        Address address = companyProperties.getAddress();
        if (address != null) {
            System.out.println("Company address:");
            System.out.println("Home: " + companyProperties.getAddress().getHome());
            System.out.println("Street: " + companyProperties.getAddress().getStreet());
        }
    }
}