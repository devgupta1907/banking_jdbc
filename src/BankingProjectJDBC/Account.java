package BankingProjectJDBC;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.*;
import java.time.LocalDate;

class Account {
    
    private Connection connection;
    private Scanner scanner;

    public Account(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }


    /**
     * Checks if an account with a given contact number exists without validating the security pin.
     * This method is useful during fund transfers as pin validation is not required for the recipient.
     *
     * @param contact The contact number to check for account existence.
     * @return true if an account exists with the provided contact number, false otherwise.
     */
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


    /**
     * Validates the existence of an account based on the provided contact number.
     * Additionally, it validates the security pin associated with the account.
     *
     * @param contact The registered contact number to validate.
     * @return true if the account exists and the pin is validated, false otherwise.
     */
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

    /**
     * Credits funds into a bank account after validating the user's contact number.
     *
     * @param contact The user's contact number.
     * @param amount The amount to credit into the account.
     * @return true if the credit operation is successful, false otherwise.
     */
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

    /**
     * Debits funds from a bank account after validating the user's contact number.
     * @param contact The user's contact number.
     * @param amount The amount to debit from the account.
     * @return true if the debit operation is successful, false otherwise.
     */
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


    /**
     * Transfers funds from one account to another, ensuring the atomicity of the transaction.
     * @param sender The contact number of the sender.
     * @param receiver The contact number of the recipient.
     * @param amount The amount to transfer.
     * @return true if the transfer is successful, false otherwise.
     */
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

    /**
     * Retrieves a list of transactions associated with a given contact number.
     *
     * @param contact The contact number to retrieve transactions for.
     * @return ArrayList of Transactions objects representing the transactions.
     */

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


    /**
     * Registers a transaction associated with the given contact number and amount.
     *
     * @param contact The contact number associated with the transaction.
     * @param amount The transaction amount.
     * @param isDebit Flag indicating whether the transaction is a debit or credit.
     */
    public void registerTransactions(long contact, double amount, boolean isDebit) {

        String query = isDebit ? "INSERT INTO transactions (transaction_date, contact, debit) VALUES (?, ?, ?)"
                : "INSERT INTO transactions (transaction_date, contact, credit) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            LocalDate today = LocalDate.now();

            preparedStatement.setDate(1, Date.valueOf(today));
            preparedStatement.setLong(2, contact);
            preparedStatement.setDouble(3, amount);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Retrieves the balance of a bank account associated with the given contact number.
     *
     * @param contact The contact number of the bank account.
     * @return The balance of the bank account.
     */
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


    /**
     * Generates a unique account number for a new account.
     *
     * @return The generated account number.
     */
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

    /**
     * Opens a bank account for a user with the given contact number and security pin.
     *
     * @param contact The contact number of the user.
     * @param pin The security pin for the account.
     * @return true if the account is successfully opened, false otherwise.
     */
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
