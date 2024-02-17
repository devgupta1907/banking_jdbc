package BankingProjectJDBC;

import java.sql.*;
import java.util.Scanner;


public class Main {

    private static final String URL = "jdbc:postgresql://localhost:5432/javabanking";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "sarita";


    public static void main(String[] args) {

        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Scanner scanner = new Scanner(System.in);

            User user = new User(connection, scanner );
            Account account = new Account(connection, scanner);

            System.out.println("\nWelcome to Anna Banking Services - Your happiness is our assets");

            while (true) {
                System.out.println("\nWhat you want to do today?\n");
                System.out.println("1. Create a bank account");
                System.out.println("2. Login to your account");
                System.out.println("3. Add money to your account");
                System.out.println("4. Debit money from your account");
                System.out.println("5. Check your account balance");
                System.out.println("6. Transfer Funds");

                int userInput = scanner.nextInt();

                switch (userInput) {

                    case 1 -> {
                        long contact = user.createUser();
                        if (contact != 0) { account.openAccount(contact);}
                    }

                    case 2 -> { System.out.println("System Under Maintenance 😎"); }

                    case 3 -> {
                        System.out.print("Enter your registered contact number: ");
                        long contact = scanner.nextLong();
                        account.creditAmount(contact);
                    }

                    case 4 -> {
                        System.out.print("Enter your registered contact number: ");
                        long contact = scanner.nextLong();
                        account.transferAmount(contact);
                    }

                    case 5 -> { System.out.println(account.checkAccountBalance()); }
                    case 6 -> {
                        account.debitCredit();
                    }
                    case 0 -> {
                        System.out.println("Thank you for banking with us! Tada 👋");
                        return;
                    }
                    default -> { System.out.println("Oops! That doesn't seem one of the options 🤔"); }


                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Done");
    }

}
