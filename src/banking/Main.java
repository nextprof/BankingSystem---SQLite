package banking;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DatabaseManager DBManager = new DatabaseManager();
        try (Connection con = DBManager.connect(args[1])) {
            DBManager.createTableCard(con);
            Bank bank = new Bank(DBManager, con);
            bank.menu();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
