package com.example.campusexpensemanager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.DatabaseSQLite.ExpenseDB;
import com.example.campusexpensemanager.DatabaseSQLite.BudgetDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText addDescription;
    private EditText addDate;
    private EditText addAmount;
    private Button btnAdd;
    private Button btnBack;

    private ExpenseDB dbHelper;
    private BudgetDB budgetDB;
    private double currentBudget;

    private int expenseId = -1;
    private Calendar calendar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expense_activity);

        addDescription = findViewById(R.id.add_description);
        addDate = findViewById(R.id.add_date);
        addAmount = findViewById(R.id.add_amount);
        btnAdd = findViewById(R.id.button_add);
        btnBack = findViewById(R.id.add_back);

        dbHelper = new ExpenseDB(this);
        budgetDB = new BudgetDB(this);
        calendar = Calendar.getInstance();

        if (getIntent().hasExtra("expense_id")) {
            expenseId = getIntent().getIntExtra("expense_id", -1);
        }

        loadCurrentBudget();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpense();
            }
        });

        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddExpenseActivity.this, ExpenseTracking.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("Range")
    private void loadCurrentBudget() {
        SQLiteDatabase db = budgetDB.getReadableDatabase();
        Cursor cursor = db.query(BudgetDB.TABLE_BUDGET, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            currentBudget = cursor.getDouble(cursor.getColumnIndex(BudgetDB.COLUMN_AMOUNT));
        } else {
            currentBudget = 0;
        }
        cursor.close();
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            AddExpenseActivity.this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                addDate.setText(dateFormat.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveExpense() {
        String description = addDescription.getText().toString().trim();
        String date = addDate.getText().toString().trim();
        String amountStr = addAmount.getText().toString().trim();

        if (description.isEmpty() || date.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > currentBudget) {
            Toast.makeText(this, "Insufficient budget", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDB.COLUMM_DESCRIPTION, description);
        values.put(ExpenseDB.COLUMM_DATE, date);
        values.put(ExpenseDB.COLUMM_AMOUNT, amount);

        if (expenseId == -1) {
            db.insert(ExpenseDB.TABLE_EXPENSES, null, values);
            Toast.makeText(this, "Expense Saved", Toast.LENGTH_SHORT).show();
            updateBudget(currentBudget - amount);
            finish();
        } else {
            db.update(ExpenseDB.TABLE_EXPENSES, values, ExpenseDB.COLUMM_ID + " = ?", new String[]{String.valueOf(expenseId)});
            Toast.makeText(this, "Error Saving Expense", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBudget(double newBudget) {
        SQLiteDatabase db = budgetDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BudgetDB.COLUMN_AMOUNT, newBudget);
        db.delete(BudgetDB.TABLE_BUDGET, null, null);
        db.insert(BudgetDB.TABLE_BUDGET, null, values);
        currentBudget = newBudget;
    }
}