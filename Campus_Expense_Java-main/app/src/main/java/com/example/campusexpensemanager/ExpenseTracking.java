package com.example.campusexpensemanager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanager.DatabaseSQLite.BudgetDB;
import com.example.campusexpensemanager.DatabaseSQLite.ExpenseDB;
import com.example.campusexpensemanager.Expense.Expense;
import com.example.campusexpensemanager.Expense.ExpenseAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExpenseTracking extends AppCompatActivity {

    private TextView textBudget;
    private EditText editBudget;
    private Button buttonSetBudget;
    private Button buttonAddExpense;
    private Button buttonBack;
    private TextView textTotalExpense;
    private RecyclerView recycleViewExpense;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;

    private ExpenseDB expenseDB;
    private BudgetDB budgetDB;
    private double currentBudget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expensetracking_activity);

        textBudget = findViewById(R.id.text_budget);
        editBudget = findViewById(R.id.edit_budget);
        buttonSetBudget = findViewById(R.id.button_set_budget);
        buttonAddExpense = findViewById(R.id.button_add_expense);
        buttonBack = findViewById(R.id.button_back);
        textTotalExpense = findViewById(R.id.text_total_expenses);
        recycleViewExpense = findViewById(R.id.recycler_view_expenses);

        expenseDB = new ExpenseDB(this);
        budgetDB = new BudgetDB(this);

        recycleViewExpense.setLayoutManager(new LinearLayoutManager(this));
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenseList);
        recycleViewExpense.setAdapter(expenseAdapter);

        loadBudget();
        loadExpense();

        buttonSetBudget.setOnClickListener(v -> setBudget());
        buttonAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseTracking.this, AddExpenseActivity.class);
            startActivity(intent);
        });
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseTracking.this, MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpense();
        loadBudget();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadExpense() {
        List<Expense> expenses = new ArrayList<>();
        double totalAmount = 0;

        SQLiteDatabase db = expenseDB.getReadableDatabase();
        Cursor cursor = db.query(
                ExpenseDB.TABLE_EXPENSES,
                null,
                null,
                null,
                null,
                null,
                null
        );
        expenseList.clear();

        if (cursor.getCount() == 0) {
            textTotalExpense.setText("Total: $0");
            recycleViewExpense.setVisibility(View.GONE);
        } else {
            recycleViewExpense.setVisibility(View.VISIBLE);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ExpenseDB.COLUMM_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDB.COLUMM_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDB.COLUMM_DATE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseDB.COLUMM_AMOUNT));

                Expense expense = new Expense(id, description, date, amount);
                expenses.add(expense);
                totalAmount += amount;
            }

            expenseAdapter.setExpenses(expenses);
            textTotalExpense.setText("Total: " + " $"+ totalAmount );
        }
        cursor.close();
    }

    @SuppressLint("Range")
    private void loadBudget() {
        SQLiteDatabase db = budgetDB.getReadableDatabase();
        Cursor cursor = db.query(BudgetDB.TABLE_BUDGET, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            currentBudget = cursor.getDouble(cursor.getColumnIndex(BudgetDB.COLUMN_AMOUNT));
        } else {
            currentBudget = 0;
        }
        textBudget.setText("Budget: " + " $"+ currentBudget);
        cursor.close();
    }

    private void setBudget() {
        String budgetStr = editBudget.getText().toString().trim();
        double budget;
        try {
            budget = Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid budget", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = budgetDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BudgetDB.COLUMN_AMOUNT, budget);

        db.delete(BudgetDB.TABLE_BUDGET, null, null); // Xóa ngân sách cũ
        db.insert(BudgetDB.TABLE_BUDGET, null, values);
        currentBudget = budget;
        textBudget.setText("Budget: " + " $"+ currentBudget);
        editBudget.setText(""); // Xóa nội dung sau khi đặt ngân sách
    }

    public void deleteExpense(int expenseId) {
        SQLiteDatabase db = expenseDB.getWritableDatabase();

        // Lấy số tiền của chi phí trước khi xóa
        double amount = getExpenseAmountById(expenseId);

        // Xóa chi phí
        db.delete(ExpenseDB.TABLE_EXPENSES, ExpenseDB.COLUMM_ID + " = ?", new String[]{String.valueOf(expenseId)});

        // Cập nhật ngân sách
        currentBudget += amount;  // Cộng lại số tiền đã xóa
        updateBudgetInDB(currentBudget);  // Cập nhật ngân sách trong DB
        loadExpense();  // Tải lại chi phí
        textBudget.setText("Budget: " + " $"+ currentBudget );  // Cập nhật hiển thị ngân sách
    }

    @SuppressLint("Range")
    private double getExpenseAmountById(int expenseId) {
        SQLiteDatabase db = expenseDB.getReadableDatabase();
        Cursor cursor = db.query(ExpenseDB.TABLE_EXPENSES,
                null,
                ExpenseDB.COLUMM_ID + " = ?",
                new String[]{String.valueOf(expenseId)},
                null, null, null);

        double amount = 0;
        if (cursor.moveToFirst()) {
            amount = cursor.getDouble(cursor.getColumnIndex(ExpenseDB.COLUMM_AMOUNT));
        }
        cursor.close();
        return amount;
    }

    private void updateBudgetInDB(double newBudget) {
        SQLiteDatabase db = budgetDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BudgetDB.COLUMN_AMOUNT, newBudget);
        db.update(BudgetDB.TABLE_BUDGET, values, null, null);
    }
}