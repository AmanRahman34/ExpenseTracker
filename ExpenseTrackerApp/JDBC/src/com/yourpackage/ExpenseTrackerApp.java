package com.yourpackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.io.*;

class Expense {
    private String date;
    private String category;
    double amount;
    private String description;

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Expense(String date, String category, double amount, String description) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Date: " + date +
                "\nCategory: " + category +
                "\nAmount: $" + this.amount +
                "\nDescription: " + description;
    }
}

class Budget {
    private String category;
    private double budgetAmount;
    private double expenses;

    public Budget(String category, double budgetAmount) {
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.expenses = 0.0;
    }

    public String getCategory() {
        return category;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public double getExpenses() {
        return expenses;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public void addExpense(double amount) {
        expenses += amount;
    }

    public double getRemainingBudget() {
        return budgetAmount - expenses;
    }
}

class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

class ExpenseTrackerApp {
    private Map<String, User> users;
    private List<Expense> expenses;
    private List<Budget> budgets;
    private User currentUser;
    private Scanner scanner;
    private Connection connection;

    public ExpenseTrackerApp() {
        users = new HashMap<>();
        expenses = new ArrayList<>();
        budgets = new ArrayList<>();
        scanner = new Scanner(System.in);

        // Initialize the Oracle database connection
        String jdbcURL = "jdbc:oracle:thin:@localhost:1521/XE";
        String username = "system";
        String password = "password";

        try {
            connection = DriverManager.getConnection(jdbcURL, username, password);
            createTables();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Please check your connection settings.");
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Check if the "users" table already exists
            // ResultSet resultSetUsers = statement.executeQuery("SELECT count(*) FROM
            // user_tables WHERE table_name = 'USERS'");
            ResultSet resultSetUsers = statement
                    .executeQuery("SELECT count(*) FROM all_tables WHERE owner = 'system' AND table_name = 'USERS'");

            resultSetUsers.next();
            int usersTableCount = resultSetUsers.getInt(1);

            if (usersTableCount == 0) {
                // The "users" table doesn't exist, so create it
                String createUserTableSQL = "CREATE TABLE users (username VARCHAR2(255) PRIMARY KEY, password VARCHAR2(255))";
                statement.execute(createUserTableSQL);
                System.out.println("Table 'users' created successfully.");
            } else {
                System.out.println("Table 'users' already exists.");
            }

            // Check if the "expenses" table already exists
            // ResultSet resultSetExpenses = statement.executeQuery("SELECT count(*) FROM
            // user_tables WHERE table_name = 'EXPENSES'");
            ResultSet resultSetExpenses = statement
                    .executeQuery("SELECT count(*) FROM all_tables WHERE owner = 'system' AND table_name = 'EXPENSES'");

            resultSetExpenses.next();
            int expensesTableCount = resultSetExpenses.getInt(1);

            if (expensesTableCount == 0) {
                // The "expenses" table doesn't exist, so create it
                String createExpenseTableSQL = "CREATE TABLE expenses (" +
                        "expense_date VARCHAR2(255), " +
                        "category VARCHAR2(255), " +
                        "amount NUMBER, " +
                        "description VARCHAR2(255))";
                statement.execute(createExpenseTableSQL);
                System.out.println("Table 'expenses' created successfully.");
            } else {
                System.out.println("Table 'expenses' already exists.");
            }

            // Check if the "budgets" table already exists
            // ResultSet resultSetBudgets = statement.executeQuery("SELECT count(*) FROM
            // user_tables WHERE table_name = 'BUDGETS'");
            ResultSet resultSetBudgets = statement
                    .executeQuery("SELECT count(*) FROM all_tables WHERE owner = 'system' AND table_name = 'BUDGETS'");

            resultSetBudgets.next();
            int budgetsTableCount = resultSetBudgets.getInt(1);

            if (budgetsTableCount == 0) {
                // The "budgets" table doesn't exist, so create it
                String createBudgetTableSQL = "CREATE TABLE budgets (category VARCHAR2(255) PRIMARY KEY, budgetAmount NUMBER, expenses NUMBER)";
                statement.execute(createBudgetTableSQL);
                System.out.println("Table 'budgets' created successfully.");
            } else {
                System.out.println("Table 'budgets' already exists.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void registerUser(String username, String password) {
        if (users.containsKey(username)) {
            System.out.println("Username already exists. Please choose a different one.\n");
            return;
        }

        try (PreparedStatement insertUser = connection
                .prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            insertUser.setString(1, username);
            insertUser.setString(2, password);
            insertUser.executeUpdate();
            System.out.println("User registered successfully!\n");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to register the user.\n");
        }
    }

    public boolean loginUser(String username, String password) {
        if (currentUser != null) {
            System.out.println("You are already logged in. Please log out first.\n");
            return false;
        }

        try (PreparedStatement selectUser = connection
                .prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            selectUser.setString(1, username);
            selectUser.setString(2, password);

            ResultSet resultSet = selectUser.executeQuery();

            if (resultSet.next()) {
                currentUser = new User(username, password);
                System.out.println("Login successful!\n");
                return true;
            } else {
                System.out.println("Invalid username or password. Please try again.\n");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Login failed. Please try again.\n");
            return false;
        }
    }

    public void addExpense(String date, String category, double amount, String description) {
        if (currentUser == null) {
            System.out.println("Please log in to add expenses.\n");
            return;
        }

        if (amount <= 0) {
            System.out.println("Invalid amount. Please enter a valid amount.\n");
            return;
        }

        Expense expense = new Expense(date, category, amount, description);
        expenses.add(expense);

        boolean budgetFound = false;
        for (Budget budget : budgets) {
            if (budget.getCategory().equals(category)) {
                budget.addExpense(amount);
                budgetFound = true;
                break;
            }
        }

        if (!budgetFound) {
            System.out.println(
                    "No budget found for the category '" + category + "'. You may want to create a budget for it.\n");
        }

        try {
            // Assuming 'connection' is your active database connection
            String sql = "INSERT INTO expenses (expense_date, category, amount, description) VALUES (?, ?, ?, ?)";
            PreparedStatement insertExpense = connection.prepareStatement(sql);
            
            // Bind the parameters to the PreparedStatement
            insertExpense.setString(1, date);
            insertExpense.setString(2, category);
            insertExpense.setDouble(3, amount);
            insertExpense.setString(4, description);
            
            // Execute the update
            insertExpense.executeUpdate();
            System.out.println("Expense added successfully!\n");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to add the expense.\n");
        }
    }

    public void displayExpenses() {
        if (currentUser == null) {
            System.out.println("Please log in to view expenses.\n");
            return;
        }

        if (expenses.isEmpty()) {
            System.out.println("No expenses to display.\n");
        } else {
            System.out.println("Expense List:");
            for (Expense e : expenses) {
                System.out.println(e);
            }
        }
    }

    public void setBudget(String category, double budgetAmount) {
        if (currentUser == null) {
            System.out.println("Please log in to set a budget.\n");
            return;
        }

        if (budgetAmount < 0) {
            System.out.println("Invalid budget amount. Please enter a valid non-negative value.\n");
            return;
        }

        // Check if the budget already exists for the category
        for (Budget budget : budgets) {
            if (budget.getCategory().equals(category)) {
                budget.setBudgetAmount(budgetAmount);
                System.out.println("Budget updated successfully for category: " + category + " to $" + budgetAmount + "\n");
                return;
            }
        }

        // If the budget doesn't exist, create a new one
        Budget newBudget = new Budget(category, budgetAmount);
        budgets.add(newBudget);
        System.out.println("Budget set successfully for category: " + category + " to $" + budgetAmount + "\n");
    }

    public void generateReports() {
        if (currentUser == null) {
            System.out.println("Please log in to generate reports.\n");
            return;
        }

        System.out.println("Generating reports...");

        double totalExpenses = 0.0;
        double totalBudgets = 0.0;

        for (Expense expense : expenses) {
            totalExpenses += expense.amount;
        }

        for (Budget budget : budgets) {
            totalBudgets += budget.getBudgetAmount();
            System.out.println("Budget for category '" + budget.getCategory() + "': $" + budget.getBudgetAmount());
            System.out.println("Expenses for category '" + budget.getCategory() + "': $" + budget.getExpenses());
            System.out.println(
                    "Remaining budget for category '" + budget.getCategory() + "': $" + budget.getRemainingBudget());
        }

        System.out.println("Total Expenses: $" + totalExpenses);
        System.out.println("Total Budgets: $" + totalBudgets);
    }

    public void saveDataToFile() {
        if (currentUser == null) {
            System.out.println("Please log in to save data.\n");
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(currentUser.getUsername() + "_expenses.csv");
            PrintWriter writer = new PrintWriter(fileWriter);

            for (Expense expense : expenses) {
                // Use a CSV format to write each expense
                writer.println(expense.getDate() + "," + expense.getCategory() + "," + expense.getAmount() + ","
                        + expense.getDescription());
            }

            writer.close();
            System.out.println("Data saved to file successfully!\n");
        } catch (IOException e) {
            System.out.println("An error occurred while saving data to the file.\n");
            e.printStackTrace();
        }
    }

    public void loadDataFromFile() {
        if (currentUser == null) {
            System.out.println("Please log in to load data from a file.\n");
            return;
        }

        try {
            String filename = currentUser.getUsername() + "_expenses.csv";
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("No data file found for the current user.\n");
                return;
            }

            try (Scanner fileScanner = new Scanner(file)) {
                expenses.clear();

                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] parts = line.split(",");

                    if (parts.length == 4) {
                        String date = parts[0].trim();
                        String category = parts[1].trim();
                        double amount = Double.parseDouble(parts[2].trim());
                        String description = parts[3].trim();

                        Expense expense = new Expense(date, category, amount, description);
                        expenses.add(expense);
                    } else {
                        System.out.println("Invalid data format in the file.\n");
                        return;
                    }
                }

                fileScanner.close();
            } catch (NumberFormatException e) {

                e.printStackTrace();
            }
            System.out.println("Data loaded successfully from the file!\n");
        } catch (IOException e) {
            System.out.println("An error occurred while loading data from the file.\n");
            e.printStackTrace();
        }
    }

    public String deleteUserProfile(String username) {
        if (currentUser == null) {
            return "Please log in to delete a user profile.";
        }

        if (currentUser.getUsername().equals(username)) {
            users.remove(username);
            currentUser = null;
            return "User profile deleted successfully!";
        } else {
            return "You can only delete your own user profile.";
        }
    }

    public void logout() {
        currentUser = null;
        System.out.println("Logged out successfully!\n");
    }

    public void start() {
        try {
            while (true) {
                displayMenu();
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter Username: ");
                        String newUser = scanner.nextLine();
                        System.out.print("Enter Password: ");
                        String newPassword = scanner.nextLine();

                        registerUser(newUser, newPassword);
                        break;
                    case 2:
                        System.out.print("Enter Username: ");
                        String loginUsername = scanner.nextLine();
                        System.out.print("Enter Password: ");
                        String loginPassword = scanner.nextLine();

                        loginUser(loginUsername, loginPassword);
                        break;
                    case 3:
                        if (currentUser != null) {
                            System.out.print("Enter Date (MM/DD/YYYY): ");
                            String date = scanner.nextLine();
                            System.out.print("Enter Category: ");
                            String category = scanner.nextLine();
                            System.out.print("Enter Amount: $");
                            double amount = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.print("Enter Description: ");
                            String description = scanner.nextLine();
                            
                            addExpense(date, category, amount, description);
                        } else {
                            System.out.println("Please log in to add expenses.\n");
                        }
                        break;
                    case 4:
                        displayExpenses();
                        break;
                    case 5:
                        if (currentUser != null) {
                            System.out.print("Enter Category: ");
                            String budgetCategory = scanner.nextLine();
                            System.out.print("Enter Budget Amount: $");
                            double budgetAmount = scanner.nextDouble();
                            scanner.nextLine();
                            setBudget(budgetCategory, budgetAmount);
                        } else {
                            System.out.println("Please log in to set a budget.\n");
                        }
                        break;
                    case 6:
                        generateReports();
                        break;
                    case 7:
                        saveDataToFile();
                        break;
                    case 8:
                        loadDataFromFile();
                        break;
                    case 9:
                        deleteUserProfile(currentUser.getUsername());
                        break;
                    case 10:
                        System.out.println("Exiting Expense Tracker. Goodbye!");
                        scanner.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please select a valid option.\n");
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.\n");
            scanner.nextLine(); // consume the invalid input
        } finally {

            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayMenu() {
        System.out.println("Expense Tracker Menu:");
        System.out.println("1. Register User");
        System.out.println("2. Login");
        System.out.println("3. Add an Expense");
        System.out.println("4. Display Expenses");
        System.out.println("5. Set Budget");
        System.out.println("6. Generate Reports");
        System.out.println("7. Save Data to File");
        System.out.println("8. Load Data from File");
        System.out.println("9. Delete User Profile");
        System.out.println("10. Logout");
        System.out.println("11. Exit");
        System.out.print("Select an option: ");
    }

    public static void main(String[] args) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            ExpenseTrackerApp app = new ExpenseTrackerApp();
            app.start();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
