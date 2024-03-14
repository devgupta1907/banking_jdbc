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

            User user = new User(connection, scanner);
            Account account = new Account(connection, scanner);
            Application app = new Application(connection, scanner);

            System.out.println("\nWelcome to Anna Banking Services - Your happiness is our assets");

            while (true) {
                System.out.println("\nWhat you want to do today?\n");
                System.out.println("1. Create A Bank Account");
                System.out.println("2. Deposit In Your Account");
                System.out.println("3. Withdraw From Your Account");
                System.out.println("4. Transfer Funds");
                System.out.println("5. Check Your Account Balance");
                System.out.println("6. Print Account Statement");

                int userInput = scanner.nextInt();
                switch (userInput) {
                    case 1 -> app.createBankAccount();
                    case 2 -> app.creditInBankAccount();
                    case 3 -> app.debitFromBankAccount();
                    case 4 -> app.transferOfFunds();
                    case 5 -> app.displayAccountBalance();
                    case 6 -> app.printTransactions();
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
