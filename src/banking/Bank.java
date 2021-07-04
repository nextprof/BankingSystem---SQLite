package banking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

class Bank {

    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();
    private final DatabaseManager DBManager;
    private final Connection con;

    Bank(DatabaseManager dbManager, Connection con) {
        this.DBManager = dbManager;
        this.con = con;
    }

    public void menu() throws SQLException {
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