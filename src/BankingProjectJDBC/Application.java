package BankingProjectJDBC;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Application {
    private Connection connection;
    private Scanner scanner;
    private User user;
    private Account account;

    public Application(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
        this.user = new User(connection, scanner);
        this.account = new Account(connection, scanner);
    }

    public void createBankAccount() {
        System.out.print("Enter Email: ");
        String email = scanner.next();

        if (user.isUserExist(email)) {
            System.out.println("User already exist with this email!");
        }
        System.out.print("Enter Name: ");
        scanner.nextLine();
        String name = scanner.nextLine();

        System.out.print("Enter Contact Number: ");
        long contact = scanner.nextLong();

        if(!user.createUser(email, name, contact)) {
            System.out.println("Error Occurred!");
            return;
        }
        System.out.println("User created successfully!");

        System.out.print("Create new security pin (4-digit integer): ");
        int pin = scanner.nextInt();
        if(!account.openAccount(contact, pin)) {
            System.out.println("Error Occurred!");
            return;
        }
        System.out.println("Account created successfully!");

    }

    public void creditInBankAccount() {
        System.out.print("Enter your registered contact number: ");
        long contact = scanner.nextLong();

        if(!account.validateAccountAndPin(contact)) {
            return;
        }

        System.out.print("Enter amount to withdraw (in 💲): ");
        double amount = scanner.nextDouble();

        if (amount <= 0) {
            System.out.println("Negatives are not allowed at out bank!");
            return;
        }

        if(!account.cred(contact, amount)) {
            System.out.println("Something went wrong!");
            return;
        }
        System.out.print("Amount added successfully ✔!");

    }

    public void debitFromBankAccount() {
        System.out.print("Enter your registered contact number: ");
        long contact = scanner.nextLong();

        if(!account.validateAccountAndPin(contact)) {
            return;
        }

        System.out.print("Enter amount to withdraw (in 💲): ");
        double amount = scanner.nextDouble();

        if (amount <= 0 || account.getAccountBalance(contact) < amount) {
            System.out.println("Your account does not have this much balance!");
            return;
        }

        if(!account.debit(contact, amount)) {
            System.out.println("Something went wrong!");
            return;
        }
        System.out.print("Amount withdrawn successfully ✔!");
    }

    public void transferOfFunds() {

        System.out.print("Enter your registered contact number: ");
        long senderContact = scanner.nextLong();
        if (!account.validateAccountAndPin(senderContact)) {
            return;
        }
        System.out.print("Enter receiver's registered contact number: ");
        long receiverContact = scanner.nextLong();
        if(!account.isAccountExist(receiverContact)) {
            System.out.println("Account does not exist!");
            return;
        }

        System.out.print("Enter amount to transfer (in 💲): ");
        double amount = scanner.nextDouble();
        if (amount <= 0 || account.getAccountBalance(senderContact) < amount) {
            System.out.println("Your account does not have this much balance!");
            return;
        }
        if (!account.testTransfer(senderContact, receiverContact, amount)) {
            System.out.println("Error Occurred!");
            return;
        }
        System.out.println("Transfer Successful!");

    }

    public void printTransactions() {
        System.out.print("Enter your registered contact number: ");
        long contact = scanner.nextLong();

        if(!account.validateAccountAndPin(contact)) {
            return;
        }

        ArrayList<Transactions> transactionsList= account.getTransactions(contact);
        if(!transactionsList.isEmpty()){
            System.out.println("Txn_Date     Debit     Credit");
            for (Transactions t : transactionsList) {
                System.out.println(t.getTransactionDate() + "   " + t.getDebit() + "   " + t.getCredit());
            }
        }
    }

    public void displayAccountBalance() {
        System.out.print("Enter your registered contact number: ");
        long contact = scanner.nextLong();

        if(!account.validateAccountAndPin(contact)) {
            return;
        }

        System.out.println(account.getAccountBalance(contact));
    }
}
