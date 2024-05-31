package com.yourpackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ExpenseTrackerAppGUI extends JFrame {
    private ExpenseTrackerApp expenseTrackerApp;

    public ExpenseTrackerAppGUI() {
        expenseTrackerApp = new ExpenseTrackerApp();

        JButton loginButton = createButton("Login", this::showLoginDialog);
        JButton registerButton = createButton("Register User", this::showRegisterUserDialog);
        JButton addExpenseButton = createButton("Add Expense", this::showAddExpenseDialog);
        JButton displayExpensesButton = createButton("Display Expenses", this::showExpensesDialog);
        JButton setBudgetButton = createButton("Set Budget", this::showSetBudgetDialog);
        JButton deleteUserButton = createButton("Delete User", this::showDeleteUserDialog);

        JPanel panel = new JPanel(new GridLayout(6, 1));
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(addExpenseButton);
        panel.add(displayExpensesButton);
        panel.add(setBudgetButton);
        panel.add(deleteUserButton);

        getContentPane().add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Expense Tracker");
        setSize(300, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        return button;
    }

    private void showLoginDialog(ActionEvent e) {
        String username = getInput("Enter username:");
        String password = getInput("Enter password:");

        if (expenseTrackerApp.loginUser(username, password)) {
            showMessage("Login successful!");
        } else {
            showMessage("Invalid username or password. Please try again.");
        }
    }

    private void showRegisterUserDialog(ActionEvent e) {
        String username = getInput("Enter new username:");
        String password = getInput("Enter new password:");

        expenseTrackerApp.registerUser(username, password);

        showMessage("User registered successfully!");
    }

    private void showAddExpenseDialog(ActionEvent e) {
        if (expenseTrackerApp.getCurrentUser() == null) {
            showMessage("Please log in to add expenses.");
            return;
        }

        String date = getInput("Enter Date (MM/DD/YYYY):");
        String category = getInput("Enter Category:");
        String amountStr = getInput("Enter Amount:");
        String description = getInput("Enter Description:");

        try {
            double amount = Double.parseDouble(amountStr);
            expenseTrackerApp.addExpense(date, category, amount, description);
            showMessage("Expense added successfully!");
        } catch (NumberFormatException ex) {
            showMessage("Invalid amount. Please enter a valid number.");
        }
    }

    private void showExpensesDialog(ActionEvent e) {
        if (expenseTrackerApp.getCurrentUser() == null) {
            showMessage("Please log in to view expenses.");
            return;
        }

        List<Expense> expenses = expenseTrackerApp.getExpenses();
        if (expenses.isEmpty()) {
            showMessage("No expenses to display.");
            return;
        }

        StringBuilder expensesText = new StringBuilder("Expense List:\n");
        for (Expense expense : expenses) {
            expensesText.append(expense.toString()).append("\n");
        }

        JTextArea textArea = new JTextArea(expensesText.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);

        showMessageDialog(scrollPane, "Expenses");
    }

    private void showSetBudgetDialog(ActionEvent e) {
        if (expenseTrackerApp.getCurrentUser() == null) {
            showMessage("Please log in to set a budget.");
            return;
        }
    
        String category = getInput("Enter Category:");
        String budgetAmountStr = getInput("Enter Budget Amount:");
    
        try {
            double budgetAmount = Double.parseDouble(budgetAmountStr);
    
            if (budgetAmount < 0) { 
                showMessage("Invalid budget amount. Please enter a non-negative value.");
                return;
            }
    
            expenseTrackerApp.setBudget(category, budgetAmount);
            showMessage("Budget set successfully for category: " + category + " to $" + budgetAmount);
        } catch (NumberFormatException ex) {
            showMessage("Invalid budget amount. Please enter a valid number.");
        }
    }

    private void showDeleteUserDialog(ActionEvent e) {
        if (expenseTrackerApp.getCurrentUser() == null) {
            showMessage("No user logged in to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete your user profile?", "Delete User", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            String deleteMessage = expenseTrackerApp.deleteUserProfile(expenseTrackerApp.getCurrentUser().getUsername());
            showMessage(deleteMessage);
        }
    }

    private String getInput(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showMessageDialog(Component component, String title) {
        JOptionPane.showMessageDialog(this, component, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerAppGUI::new);
    }
}
