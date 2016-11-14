package com.emmanuelmess.simpleaccounting.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static java.lang.String.format;

/**
 * @author Emmanuel
 *         on 14/11/2016, at 16:52.
 */
public class DBMonthlyBalance extends DBs {
	private static final String[] COLUMNS = new String[] {"MONTH", "YEAR", "BALANCE"};

	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "MONTHLY_BALANCE";
	private static final String TABLE_CREATE = format("CREATE TABLE %1$s(%2$s INT, %3$s INT, %4$s INT, %5$s REAL);",
			TABLE_NAME, NUMBER_COLUMN, COLUMNS[0], COLUMNS[1], COLUMNS[2]);

	public DBMonthlyBalance(Context context) {super(context, TABLE_NAME, null, DATABASE_VERSION);}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void createMonth(int month, int year) {
		boolean exists = false;
		{
			Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {NUMBER_COLUMN},
					SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
							format("%1$s=%2$s" , COLUMNS[1], year)),
					null, null, null, null);

			exists = c.getCount() == 0;

			c.close();

		}

		if(!exists) {
			Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
					null, null, null, null, null);
			int i;

			if (c.getCount() == 0) {
				i = 0;
			} else {
				c.moveToLast();
				i = c.getInt(0) + 1;
				c.close();
			}

			CV.put(NUMBER_COLUMN, i);
			CV.put(COLUMNS[0], month);
			CV.put(COLUMNS[1], year);
			CV.put(COLUMNS[2], 0);
			getWritableDatabase().insert(TABLE_NAME, null, CV);
			CV.clear();
		}
	}

	public void updateMonth(int month, int year, double balance) {
		CV.put(COLUMNS[2], balance);
		getWritableDatabase().update(TABLE_NAME, CV, SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
				format("%1$s=%2$s" , COLUMNS[1], year)), null);
		CV.clear();
	}

	public double getBalance(int month, int year) {
		double data = 0;

		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {COLUMNS[2]},
				SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
						format("%1$s=%2$s" , COLUMNS[1], year)),
				null, null, null, null);

		if(c.getCount() == 0)
			throw new IllegalStateException("Non-existent month: " + (month+1) + " in year " + year);
		else data = c.getDouble(0);

		c.close();

		return data;
	}

}
