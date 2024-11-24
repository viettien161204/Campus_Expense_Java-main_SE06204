package com.example.campusexpensemanager.DatabaseSQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExpenseDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMM_ID = "_id";
    public static final String COLUMM_DESCRIPTION = "description";
    public static final String COLUMM_DATE = "date";
    public static final String COLUMM_AMOUNT = "amount";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_EXPENSES + " (" +
                    COLUMM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMM_DESCRIPTION + " TEXT, " +
                    COLUMM_DATE + " TEXT, " +
                    COLUMM_AMOUNT + " REAL " +
                    ");";

    public ExpenseDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        onCreate(db);
    }
}