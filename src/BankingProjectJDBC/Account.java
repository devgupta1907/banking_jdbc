package BankingProjectJDBC;

import javax.xml.transform.Result;
import java.util.Scanner;
import java.sql.*;

public class Account {

    private Connection connection;
    private Scanner scanner;

    public Account(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }


//    This method takes registered contact number as argument and checks for account existence.
//    If account exists with the given contact, then it will validate security pin.
    public boolean validateAccountAndPin(long contact) {
        String query = "SELECT contact, security_pin FROM accounts WHERE contact = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, contact);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                int actualPin = resultSet.getInt("security_pin");
                System.out.print("Enter your security pin: ");
                int enteredPin = scanner.nextInt();
                if (enteredPin != actualPin) {
                    System.out.println("Security Pin does not match!");
                    return false;
                }
                resultSet.close();
                return true;
            } else {
                System.out.println("No bank account is registered with this contact.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    public boolean creditAmount(long contact) {

        String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE contact = ?";

        try {
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(updateQuery);

            if (!validateAccountAndPin(contact)) {
                return false;
            }

            System.out.print("Enter amount to add (in 💲): ");
            double amount = scanner.nextDouble();
            if (amount <= 0) {
                System.out.println("Negative amounts are not allowed at our bank.");
                return false;
            }
            preparedStatementUpdate.setLong(2, contact);
            preparedStatementUpdate.setDouble(1, amount);

            int returnedRow = preparedStatementUpdate.executeUpdate();
            if (returnedRow > 0) {
                System.out.println("Amount added successfully ✔! Current Balance: " + checkAccountBalance(contact));
                return true;
            }
            System.out.println("Process Failed ❌! Your money is safe 🙂!");
            return false;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }


    public boolean transferAmount(long contact) {

        String query = "UPDATE accounts SET balance = balance - ? WHERE contact = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (!validateAccountAndPin(contact)) {
                return false;
            }

            System.out.print("Enter amount to withdraw (in 💲): ");
            double amount = scanner.nextDouble();
            if (amount <= 0) {
                System.out.println("Negative amounts are not allowed at our bank.");
                return false;
            }
            preparedStatement.setLong(2, contact);
            preparedStatement.setDouble(1, amount);

            int returnedRow = preparedStatement.executeUpdate();

            if (returnedRow > 0) {
                System.out.print("Amount withdrawn successfully ✔! Current Balance: " + checkAccountBalance(contact));
                return true;
            }
            System.out.println("Process Failed ❌! Your money is safe 🙂!");
            return false;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    public boolean isAccountExist(long contact) {
        String query = "SELECT contact FROM accounts WHERE contact = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, contact);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public void debitCredit() {
        String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE contact = ?";
        String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE contact = ?";

        try {
            connection.setAutoCommit(false);

            PreparedStatement preparedStatementCredit = connection.prepareStatement(creditQuery);
            PreparedStatement preparedStatementDebit = connection.prepareStatement(debitQuery);

            System.out.print("Enter your registered contact number: ");
            long senderContact = scanner.nextLong();
            if (!validateAccountAndPin(senderContact)) {
                return;
            }
            System.out.print("Enter receiver's registered contact number: ");
            long receiverContact = scanner.nextLong();
            if(!isAccountExist(receiverContact)) {
                System.out.println("Account does not exist!");
                return;
            }

            System.out.println("Enter transfer amount: ");
            double transferAmount = scanner.nextDouble();

            preparedStatementCredit.setDouble(1, transferAmount);
            preparedStatementCredit.setLong(2, receiverContact);

            preparedStatementDebit.setDouble(1, transferAmount);
            preparedStatementDebit.setLong(2, senderContact);

            int returnedRowsCredit = preparedStatementCredit.executeUpdate();
            int returnedRowsDebit = preparedStatementDebit.executeUpdate();

            if(returnedRowsCredit > 0 && returnedRowsDebit > 0) {
                connection.commit();
                System.out.println("Transfer Successful!");
            } else {
                connection.rollback();
                System.out.println("Transaction Failed!");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public double checkAccountBalance(long contact) {

        String query = "SELECT balance FROM accounts WHERE contact = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, contact);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            }
            resultSet.close();
            System.out.print("Something went wrong! ");
            return Double.NaN;

        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        return Double.NaN;
    }


    public double checkAccountBalance() {

        System.out.print("Enter your registered contact number: ");
        long contact = scanner.nextLong();
        if (!validateAccountAndPin(contact)) {
            return Double.NaN;
        }

        return checkAccountBalance(contact);
    }

    public long createAccountNumber() {
        String query = "SELECT account_number FROM accounts ORDER BY account_number DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                long prevAccountNumber = resultSet.getLong("account_number");
                return prevAccountNumber + 1;
            }
            resultSet.close();
            return 76860101;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 76860101;
    }


    public void openAccount(long contact) {

        String query = "INSERT INTO accounts (account_number, contact, security_pin, balance) VALUES(?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            System.out.print("Create new security pin (4-digit integer): ");
            preparedStatement.setInt(3, scanner.nextInt());

            System.out.print("Add opening balance: ");
            preparedStatement.setDouble(4, scanner.nextDouble());

            preparedStatement.setLong(2, contact);
            preparedStatement.setLong(1, createAccountNumber());


            int returnedRow = preparedStatement.executeUpdate();

            if (returnedRow > 0) System.out.println("\nSUCCESS, account created!");
            else System.out.println("\nFAILED! account not created!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }


}
