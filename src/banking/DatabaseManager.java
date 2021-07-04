package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Scanner;

public class DatabaseManager {

    private final Scanner scanner = new Scanner(System.in);

    Connection connect(String path) {
        String url = "jdbc:sqlite:" + path;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try {
            Connection con = dataSource.getConnection();
            System.out.println("Connected to database");
            return con;
        } catch (SQLException e) {
            System.out.println("Connection to database " + path + " failed");
            e.printStackTrace();
        }
        return null;
    }

    void createTableCard(Connection con) {
        String createTable = "CREATE TABLE IF NOT EXISTS card(" +
                "id INTEGER PRIMARY KEY ," +
                "number TEXT," +
                "pin TEXT," +
                "balance INTEGER DEFAULT 0)";
        try (PreparedStatement preparedStatement = con.prepareStatement(createTable)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Creating table card failed");
            e.printStackTrace();
        }
    }

    boolean checkNumberInDB(Connection con, String cardNumber) throws SQLException {
        String selectNumber = "SELECT * from card WHERE number = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(selectNumber)) {
            preparedStatement.setString(1, cardNumber);
            try (ResultSet cards = preparedStatement.executeQuery( )) {
                if (cards.next()) {
                    return true;
                }
            } catch (SQLException e) {
                System.out.println("Checking if account exists failed!");
                e.printStackTrace();
            }
        }
        return false;
    }

    boolean checkPinInDB(Connection con, String cardNumber, String pin) throws SQLException {
        String selectNumber_Pin = "SELECT * FROM card WHERE number = ? and pin = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(selectNumber_Pin)) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pin);
            try (ResultSet cards = preparedStatement.executeQuery()) {
                if (cards.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    void insertNumberInDB(Connection con, String cardNumber, String pin) throws SQLException {

        String insertAccount = "INSERT INTO card(number,pin) VALUES(?,?)";

        try (PreparedStatement preparedStatement = con.prepareStatement(insertAccount)) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pin);
            preparedStatement.executeUpdate();
        }
    }

    int getBalanceFromDB(Connection con, String cardNumber) throws SQLException {
        String selectNumber = "SELECT * from card WHERE number = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(selectNumber)) {
            preparedStatement.setString(1, cardNumber);
            try (ResultSet cards = preparedStatement.executeQuery()) {
                cards.next();
                return cards.getInt("balance");
            }
        }
    }

    void printBalance(Connection con, String cardNumber) throws SQLException {
        int balance= getBalanceFromDB(con,cardNumber);
        System.out.println("Balance: " + balance + "\n");
    }

    public void addIncomeInDB(Connection con, String cardNumber) throws SQLException {
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        int balance = this.getBalanceFromDB(con, cardNumber);
        int balance_income = income + balance;
        String updateCard = "UPDATE card SET balance = ? WHERE number = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(updateCard)) {
            preparedStatement.setInt(1, balance_income);
            preparedStatement.setString(2, cardNumber);
            preparedStatement.executeUpdate();
        }
        System.out.println("\nIncome was added!\n");
    }

    public void dropAccountFromDB(Connection con, String cardNumber) throws SQLException {
        String deleteAccount = "DELETE FROM card WHERE number = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(deleteAccount)) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.executeUpdate();
            System.out.println("\nThe account has been closed!\n");
        }
    }

    public void transferMoney(Connection con, String cardNumber, Bank bank) throws SQLException {
        System.out.println("Transfer\n");
        System.out.println("Enter card number:");
        String cardToTransfer = scanner.next();
        if (!bank.checkValidate(cardToTransfer)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
        } else {
            if (checkNumberInDB(con, cardToTransfer)) {
                System.out.println("Enter how much money you want to transfer:");
                int moneyToTransfer = scanner.nextInt();
                int availableMoney = getBalanceFromDB(con, cardNumber);
                if (moneyToTransfer > availableMoney) {
                    System.out.println("Not enough money!");
                } else {
                    con.setAutoCommit(false);

                    String updateBalance = "UPDATE card SET balance = ? WHERE number = ?";

                    int receiverMoney = getBalanceFromDB(con, cardToTransfer);

                    Savepoint savepoint = con.setSavepoint();

                    try (PreparedStatement updateOwnerBalance = con.prepareStatement(updateBalance);
                         PreparedStatement updateReceiverBalance = con.prepareStatement(updateBalance)) {

                        updateOwnerBalance.setInt(1, availableMoney - moneyToTransfer);
                        updateOwnerBalance.setString(2, cardNumber);
                        updateOwnerBalance.executeUpdate();

                        updateReceiverBalance.setInt(1, receiverMoney + moneyToTransfer);
                        updateReceiverBalance.setString(2, cardToTransfer);
                        updateReceiverBalance.executeUpdate();

                        con.commit();
                        System.out.println("Successful transfer!");

                    } catch (SQLException e) {
                        System.out.println("A rollback has occurred");
                        con.rollback(savepoint);
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Such a card does not exist.");
            }
        }
    }
}
