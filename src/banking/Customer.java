package banking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

class Customer {

    private final String cardNumber;
    private final Connection con;
    private final DatabaseManager DBManager;

    Customer(String cardNumber, Connection con,DatabaseManager dbManager) {
        this.cardNumber = cardNumber;
        this.con = con;
        this.DBManager = dbManager;
    }

    void menu(Bank bank) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    DBManager.printBalance(con,cardNumber);
                    break;
                case 2:
                    DBManager.addIncomeInDB(con,cardNumber);
                    break;
                case 3:
                    DBManager.transferMoney(con,cardNumber,bank);
                    break;
                case 4:
                    DBManager.dropAccountFromDB(con,cardNumber);
                    bank.menu();
                    break;
                case 5:
                    System.out.println("You have successfully logged out!");
                    bank.menu();
                    break;
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Wrong input!\n");
            }
        }
    }
}