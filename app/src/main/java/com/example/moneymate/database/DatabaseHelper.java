package com.example.moneymate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import com.example.moneymate.models.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MoneyMate.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_TRANSACTIONS = "transactions";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";

    private final Executor dbExecutor = Executors.newFixedThreadPool(4);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_AMOUNT + " REAL NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_TYPE + " TEXT NOT NULL CHECK (" + COLUMN_TYPE + " IN ('income', 'expense')),"
                + COLUMN_CATEGORY + " TEXT NOT NULL,"
                + COLUMN_DATE + " TEXT NOT NULL"
                + ")";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    public interface DatabaseCallback<T> {
        void onComplete(T result);
    }

    // Add transaction with async support
    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, dateFormat.format(transaction.getDate()));

        long id = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return id;
    }

    public void addTransactionAsync(Transaction transaction, DatabaseCallback<Long> callback) {
        dbExecutor.execute(() -> {
            long id = addTransaction(transaction);
            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(id));
        });
    }

    // Get all transactions with async support
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS
                + " ORDER BY " + COLUMN_DATE + " DESC, " + COLUMN_ID + " DESC";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(selectQuery, null)) {

            if (cursor.moveToFirst()) {
                do {
                    Transaction transaction = new Transaction();
                    transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));

                    try {
                        transaction.setDate(dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))));
                    } catch (ParseException e) {
                        transaction.setDate(new Date());
                    }

                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
        }
        return transactions;
    }

    public void getAllTransactionsAsync(DatabaseCallback<List<Transaction>> callback) {
        dbExecutor.execute(() -> {
            List<Transaction> transactions = getAllTransactions();
            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(transactions));
        });
    }

    // Get transactions for specific month
    public List<Transaction> getTransactionsForMonth(int year, int month) {
        List<Transaction> transactions = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        String startDate = dateFormat.format(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(cal.getTime());

        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_DATE + " BETWEEN ? AND ? ORDER BY " + COLUMN_DATE + " DESC";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(selectQuery, new String[]{startDate, endDate})) {

            if (cursor.moveToFirst()) {
                do {
                    Transaction transaction = new Transaction();
                    transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));

                    try {
                        transaction.setDate(dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))));
                    } catch (ParseException e) {
                        transaction.setDate(new Date());
                    }

                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
        }
        return transactions;
    }

    // Dalam DatabaseHelper.java
    public double getCurrentBalance() {
        double income = getTotalIncome();
        double expense = getTotalExpense();
        return income - expense;
    }

    private double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE type='income'", null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0;
    }

    private double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE type='expense'", null);
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0;
    }

    // Update transaction
    public boolean updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, dateFormat.format(transaction.getDate()));

        int result = db.update(TABLE_TRANSACTIONS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});
        db.close();

        return result > 0;
    }

    // Delete transaction
    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTIONS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();

        return result > 0;
    }

    // Get transactions count
    public int getTransactionsCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(countQuery, null)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    // Get category spending for chart
    public List<CategorySpending> getCategorySpending(int year, int month) {
        List<CategorySpending> categorySpending = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        String startDate = dateFormat.format(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(cal.getTime());

        String selectQuery = "SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TYPE + " = 'expense' AND " + COLUMN_DATE + " BETWEEN ? AND ? GROUP BY " + COLUMN_CATEGORY;

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(selectQuery, new String[]{startDate, endDate})) {

            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    double amount = cursor.getDouble(1);
                    categorySpending.add(new CategorySpending(category, amount));
                } while (cursor.moveToNext());
            }
        }
        return categorySpending;
    }

    // Inner class for category spending
    public static class CategorySpending {
        private final String category;
        private final double amount;

        public CategorySpending(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }
}