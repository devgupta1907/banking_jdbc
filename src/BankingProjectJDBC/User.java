package BankingProjectJDBC;

import java.sql.*;
import java.util.Scanner;

public class User {

    private Connection connection;
    private Scanner scanner;

    public User(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public boolean isUserExist(String email) {
        String query = "SELECT email FROM users WHERE email = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    public long createUser()  {
        String query = "INSERT INTO users (name, email, contact) VALUES(?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            System.out.print("Enter Email: ");
            String email = scanner.next();

            if (isUserExist(email)) {
                System.out.println("User already exist with this email!");
                return 0;
            }

            System.out.print("Enter Name: ");
            String name = scanner.next();

            System.out.print("Enter Contact Number: ");
            long contact = scanner.nextLong();

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setLong(3, contact);

            int returnedRows = preparedStatement.executeUpdate();

            if (returnedRows > 0) {
                System.out.println("\nSUCCESS, user created!");
                return contact;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("\nFAILED! user not created!");
        return 0;

    }

}