package BankingProjectJDBC;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.*;
import java.time.LocalDate;

public class Account {
    
    private Connection connection;
    private Scanner scanner;

    public Account(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }


//   This method is used to check if the account with a certain contact number exists or not.
//   It does not ask or validates for security pin.
//   This method is useful during transfer of funds as we do not ask for pin from the receiver.
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
                    System.out.println("Security pin does not match!");
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
            return false;
        }

    }

//    This method is used to credit funds in bank account.
//    It takes user's contact number as an argument and validates it before crediting funds.
    public boolean cred(long contact, double amount) {

        String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE contact = ?";

        try (PreparedStatement preparedStatementUpdate = connection.prepareStatement(updateQuery)) {

            preparedStatementUpdate.setLong(2, contact);
            preparedStatementUpdate.setDouble(1, amount);
            int returnedRow = preparedStatementUpdate.executeUpdate();

            if (returnedRow > 0) {
                registerTransactions(contact, amount, false);
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //    This method is used to debit funds from bank account.
//    It is different from transfer of funds.
    public boolean debit(long contact, double amount) {
        String query = "UPDATE accounts SET balance = balance - ? WHERE contact = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(2, contact);
            preparedStatement.setDouble(1, amount);
            int returnedRow = preparedStatement.executeUpdate();

            if (returnedRow > 0) {
                registerTransactions(contact, amount, true);
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    //    This method is used to transfer funds from one account to another.
//    It also uses commit and rollback functionality of JDBC.
    public boolean testTransfer(long sender, long receiver, double amount) {

        try {
            connection.setAutoCommit(false);

            if(debit(sender, amount) && cred(receiver, amount)) {
                connection.commit();
            } else {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            connection.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    public ArrayList<Transactions> getTransactions(long contact) {
        String query = "SELECT transaction_date, debit, credit FROM transactions WHERE contact = ?";

        ArrayList<Transactions> transactions = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, contact);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                do {
                    Transactions transaction = new Transactions();
                    transaction.setTransactionDate(resultSet.getDate("transaction_date"));
                    transaction.setDebit(resultSet.getDouble("debit"));
                    transaction.setCredit(resultSet.getDouble("credit"));
                    transactions.add(transaction);
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return transactions;
    }

    public void registerTransactions(long contact, double amount, boolean isDebit) {

        String debitQuery = "INSERT INTO transactions (transaction_date, contact, debit) VALUES (?, ?, ?)";
        String creditQuery = "INSERT INTO transactions (transaction_date, contact, credit) VALUES (?, ?, ?)";

        try {
            LocalDate today = LocalDate.now();

            PreparedStatement debitStatement = connection.prepareStatement(debitQuery);
            PreparedStatement creditStatement = connection.prepareStatement(creditQuery);

            if (isDebit) {
                debitStatement.setLong(2, contact);
                debitStatement.setDate(1, Date.valueOf(today));
                debitStatement.setDouble(3, amount);
                debitStatement.executeUpdate();
            } else {
                creditStatement.setLong(2, contact);
                creditStatement.setDate(1, Date.valueOf(today));
                creditStatement.setDouble(3, amount);
                creditStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


//    This method takes the contact number as argument to check the balance.
//    This method assumes that the passed contact number is already validated.
//    It is useful when we print the updated balance after a transaction.
    public double getAccountBalance(long contact) {

        String query = "SELECT balance FROM accounts WHERE contact = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, contact);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            }
            resultSet.close();
            return 0.0;

        } catch (SQLException e) {
            System.out.print(e.getMessage());
            return 0.0;
        }

    }


//    This method creates a unique account number.
//    This method is accessed by openAccount() method to create an account number for the user.
    private long createAccountNumber() {
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

//    This method takes the contact of user and open an account for that user.
    public boolean openAccount(long contact, int pin) {

        String query = "INSERT INTO accounts (account_number, contact, security_pin, balance) VALUES(?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(3, pin);
            preparedStatement.setDouble(4, 0.0);
            preparedStatement.setLong(2, contact);
            preparedStatement.setLong(1, this.createAccountNumber());

            int returnedRow = preparedStatement.executeUpdate();
            if (returnedRow > 0) return true;
            return false;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }


}
