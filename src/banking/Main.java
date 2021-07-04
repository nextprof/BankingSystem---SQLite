package banking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException {

        DatabaseManager DBManager = new DatabaseManager();
        Connection con = DBManager.connect(args[1]);

        DBManager.createTableCard(con);

        Bank bank = new Bank(DBManager, con);
        bank.menu();

        con.close();
    }


    static class Bank {

        private final DatabaseManager DBManager;
        private final Connection con;

        Bank(DatabaseManager dbManager, Connection con) {
            this.DBManager = dbManager;
            this.con = con;
        }

        public void menu() throws SQLException {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("1. Create an account");
                System.out.println("2. Log into account");
                System.out.println("0. Exit");

                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        this.createAccount();
                        break;
                    case 2:
                        this.logIntoAccount();
                        break;
                    case 0:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Wrong input!");
                        System.out.println();
                }
            }
        }

        boolean checkValidate(String number) {
            char[] temp = number.toCharArray();
            int[] intTemp = new int[16];
            for (int i = 0; i < 16; i++) {
                intTemp[i] = Integer.parseInt(String.valueOf(temp[i]));
            }
            for (int i = 0; i < 15; i++) {
                if (i % 2 == 0)
                    intTemp[i] *= 2;
            }
            for (int i = 0; i < 15; i++) {
                if (intTemp[i] > 9)
                    intTemp[i] -= 9;
            }
            double suma = 0;
            for (int i = 0; i < 16; i++) {
                suma += intTemp[i];
            }
            return suma % 10 == 0;
        }


        private String generateCardNumber() throws SQLException {
            Random random = new Random();
            StringBuilder number = new StringBuilder("400000");
            while (true) {
                for (int i = 0; i < 10; i++) {
                    number.append(random.nextInt(10));
                }
                if (DBManager.checkNumberInDB(con, number.toString()) || !checkValidate(number.toString())) {
                    number.delete(6, 17);
                } else {
                    break;
                }
            }
            return number.toString();
        }


        private String generateCardPin() {
            Random random = new Random();
            StringBuilder number = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                number.append(random.nextInt(10));
            }
            return number.toString();
        }


        private void createAccount() throws SQLException {
            String cardNumber = generateCardNumber();
            String pinNumber = generateCardPin();
            System.out.println("Your card has been created");
            System.out.println("Your card number:");
            System.out.println(cardNumber);
            System.out.println("Your card PIN:");
            System.out.println(pinNumber);


            DBManager.insertNumberInDB(con, cardNumber, pinNumber);
        }

        private void logIntoAccount() throws SQLException {
            System.out.println("Enter your card number:");

            Scanner scanner = new Scanner(System.in);
            String cardNumber = scanner.next();
            if (DBManager.checkNumberInDB(con, cardNumber)) {
                System.out.println("Enter your PIN:");
                String pin = scanner.next();
                if (DBManager.checkPinInDB(con, cardNumber, pin)) {
                    System.out.println("You have successfully logged in!\n");
                    Customer customer = new Customer(cardNumber, con,DBManager);
                    customer.menu(this);
                }
            } else {
                System.out.println("Bad number of card!");
            }
        }
    }

    static class Customer {

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
                        DBManager.checkBalanceInDB(con,this.cardNumber);
                        break;
                    case 2:
                        DBManager.addIncomeInDB(con,this.cardNumber);
                        System.out.println("\nIncome was added!\n");
                        break;
                    case 3:
                        DBManager.transferMoney(con,this.cardNumber,bank);
                        break;
                    case 4:
                        DBManager.dropAccountFromDB(con,this.cardNumber);
                        System.out.println("\nThe account has been closed!\n");
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
                        System.out.println("Wrong input!");
                        System.out.println();
                }
            }
        }
    }
}
