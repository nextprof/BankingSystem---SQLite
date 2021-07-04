package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Scanner;

public class DatabaseManager {


    Connection connect(String path) throws SQLException {
        String url = "jdbc:sqlite:" + path;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        Connection conn = null;
        try{
            Connection con = dataSource.getConnection();
            // Statement creation
            System.out.println("Connected to database");
            return con;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    void createTableCard(Connection con) throws SQLException {
        try (Statement statement = con.createStatement()) {
            // Statement execution
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                    "id INTEGER PRIMARY KEY," +
                    "number TEXT," +
                    "pin TEXT," +
                    "balance INTEGER DEFAULT 0)");

        }
    }

    boolean checkNumberInDB(Connection con,String number) throws SQLException {
        try (Statement statement = con.createStatement()) {
            // Statement execution
            try (ResultSet cards = statement.executeQuery("Select * from card where number="+number+";")) {
                if (cards.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean checkPinInDB(Connection con,String number,String pin) throws SQLException {
        try (Statement statement = con.createStatement()) {
            // Statement execution
            try (ResultSet cards = statement.executeQuery("Select * from card where number=" +
                    number+" and pin=" + pin + ";")) {
                while (cards.next()) {
                    return true;
                }
            }
        }
        return false;
    }


    void insertNumberInDB(Connection con,String number,String pin) throws SQLException {
        try (Statement statement = con.createStatement()) {
            // Statement execution
            try {
                int rowsAffected = statement.executeUpdate("INSERT INTO card(number,pin) values("+
                        number + ","+ pin +");");
                // System.out.println(number + pin +" has been added to repository");
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void checkBalanceInDB(Connection con,String number) throws SQLException {
        try (Statement statement = con.createStatement()) {
            // Statement execution
            try (ResultSet cards = statement.executeQuery("Select * from card where number="+number+";")) {
                while (cards.next()) {
                    int balance = cards.getInt("balance");
                    System.out.println("Balance: "+balance+"\n");
                }
            }
        }
    }

    int getBalanceFromDB(Connection con,String number) throws SQLException {
        try (Statement statement = con.createStatement()) {
            // Statement execution
            try (ResultSet cards = statement.executeQuery("Select * from card where number="+number+";")) {
                cards.next();
                return cards.getInt("balance");
            }
        }
    }

    public void addIncomeInDB(Connection con, String cardNumber) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        int balance = this.getBalanceFromDB(con,cardNumber);
        int balance_income = income + balance;
        String updateOrigin = "UPDATE card SET balance = ? WHERE number = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(updateOrigin)) {
            preparedStatement.setInt(1, balance_income);
            preparedStatement.setString(2, cardNumber);
            preparedStatement.executeUpdate();
        }
    }

    public void dropAccountFromDB(Connection con, String cardNumber) throws SQLException {
        String deleteAccount = "DELETE FROM card WHERE number = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(deleteAccount)) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.executeUpdate();
        }
    }

    public void transferMoney(Connection con, String cardNumber, Main.Bank bank) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Transfer\n");
        System.out.println("Enter card number:");
        String cardToTransfer = scanner.next();
        if(!bank.checkValidate(cardToTransfer)){
            System.out.println("Probably you made a mistake in the card number. Please try again!");
        }
        else{
            if(checkNumberInDB(con,cardToTransfer))
            {
                System.out.println("Enter how much money you want to transfer:");
                int moneyToTransfer= scanner.nextInt();
                int availableMoney = getBalanceFromDB(con,cardNumber);
                if(moneyToTransfer>availableMoney)
                {
                    System.out.println("Not enough money!");
                }
                else{
                    con.setAutoCommit(false);
                    String updateBalance = "UPDATE card SET balance = ? WHERE number = ?";
                    int receiverMoney = getBalanceFromDB(con,cardToTransfer);
                    Savepoint savepoint = con.setSavepoint();

                    try (PreparedStatement updateOwnerBalance = con.prepareStatement(updateBalance);
                         PreparedStatement updateReceiverBalance = con.prepareStatement(updateBalance)) {

                        updateOwnerBalance.setInt(1, availableMoney-moneyToTransfer);
                        updateOwnerBalance.setString(2, cardNumber);
                        updateOwnerBalance.executeUpdate();

                        updateReceiverBalance.setInt(1, receiverMoney+moneyToTransfer);
                        updateReceiverBalance.setString(2, cardToTransfer);
                        updateReceiverBalance.executeUpdate();

                        con.commit();

                    }catch (SQLException e){
                        con.rollback(savepoint);
                        e.printStackTrace();
                    }
                }

            }
            else{
                System.out.println("Such a card does not exist.");
            }
        }
    }
}
