package com.emmanuelmess.simpleaccounting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.String.format;

/**
 * @author Emmanuel
 *         on 2016-01-31, at 15:53.
 */
class FileIO extends SQLiteOpenHelper {

	static final String[] COLUMNS = new String[] { "DATE", "REFERENCE", "CREDIT", "DEBT", "MONTH", "YEAR"};

	private static final String NUMBER_COLUMN = "NUMBER";
	private static final int DATABASE_VERSION = 3;
	private static final String TABLE_NAME = "ACCOUNTING";
	private static final String TABLE_CREATE = format("CREATE TABLE %1$s" +
			" (%2$s INT, %3$s INT, %4$s TEXT, %5$s REAL, %6$s REAL, %7$s INT, %8$s INT);",
			TABLE_NAME, NUMBER_COLUMN, COLUMNS[0], COLUMNS[1], COLUMNS[2], COLUMNS[3], COLUMNS[4], COLUMNS[5]);
	private final ContentValues CV = new ContentValues();

	FileIO(Context context) {super(context, TABLE_NAME, null, DATABASE_VERSION);}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql;
		switch(oldVersion){
			case 1:
				sql = "CREATE TEMPORARY TABLE temp(" + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + ");" +
						"INSERT INTO temp SELECT " + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + " FROM " + TABLE_NAME + ";" +
						"DROP TABLE " + TABLE_NAME + ";" +
						"CREATE TABLE " + TABLE_NAME + "(" + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + ");" +
						"INSERT INTO " + TABLE_NAME + " SELECT " + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + " FROM temp;" +
						"DROP TABLE temp;";
				db.execSQL(sql);//"copy, drop table, create new table, copy back" technique bc ALTER...DROP COLUMN isn't in SQLite
			case 2:
				sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMNS[4] + " INT;";
				db.execSQL(sql);

				sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMNS[5] + " INT;";
				db.execSQL(sql);

				Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{COLUMNS[0]},
						null, null, null, null, null);

				c.moveToLast();

				int last = -1;
				int month = Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(new Date()))-1,
						//YEARS ALREADY START IN 0!!!
						year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()));

				for (int i = c.getCount(); i >= 0; i--) {
					if(last <= c.getInt(0)) {// TODO: 12/11/2016 test
						if (month >= 0)
							month--;
						else {
							month = 12-1;
							year--;
						}
					}

					update(i, COLUMNS[4], String.valueOf(month));
					update(i, COLUMNS[4], String.valueOf(year));
					last = c.getInt(0);

					c.moveToPrevious();
				}

				c.close();
		}
	}

	void newRowInMonth(int month, int year) {
		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
				null, null, null, null, null);
		int i;

		if(c.getCount() == 0) {
			i = 0;
		} else {
			c.moveToLast();
			i = c.getInt(0) + 1;
			c.close();
		}

		CV.put(NUMBER_COLUMN, i);
		CV.put(COLUMNS[4], month);
		CV.put(COLUMNS[5], year);
		getWritableDatabase().insert(TABLE_NAME, null, CV);
		CV.clear();
	}

	void update(int row, String column, String data) {
		CV.put(column, data);
		getWritableDatabase().update(TABLE_NAME, CV, NUMBER_COLUMN + "=" + row, null);
		CV.clear();
	}

	int[][] getMonthsWithData() {
		int[][] data;

		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {COLUMNS[4], COLUMNS[5]},
				null, null, COLUMNS[4], null, null);

		if (c != null) {
			c.moveToFirst();
		} else return new int[0][0];

		data = new int[c.getCount()][2];
		for(int x = 0; x < data.length; x++) {
			if(c.getString(0) != null)
				data[x]= new int[]{c.getInt(0), c.getInt(1)};
			else data[x] = new int[]{-1, -1};
			c.moveToNext();
		}
		c.close();

		return data;
	}

	String[][] getAllForMonth(int month, int year) {
		String [][] data;

		Cursor c = getReadableDatabase().query(TABLE_NAME, COLUMNS,
				COLUMNS[4] + "=" + month + " AND " + COLUMNS[5] + "=" + year,
				null, null, null, COLUMNS[0]);

		if (c != null) {
			c.moveToFirst();
		} else return new String[0][0];

		data = new String[c.getCount()][COLUMNS.length];
		for(int x = 0; x < data.length; x++) {
			for(int y = 0; y < COLUMNS.length; y++){
				data[x][y] = c.getString(y);
			}
			c.moveToNext();
		}
		c.close();

		return data;
	}

	int[] getIndexesForMonth(int month, int year) {
		int[] data;

		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
				format("%1$s = %2$s AND %3$s = %4$s", COLUMNS[4], month, COLUMNS[5], year),
				null, null, null, COLUMNS[0]);

		if (c != null) {
			c.moveToFirst();
		} else return new int[0];

		data = new int[c.getCount()];
		for(int x = 0; x < data.length; x++) {
			data[x] = c.getInt(0);
			c.moveToNext();
		}
		c.close();

		return data;
	}

	int getLastIndex() {
		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
				format("%1$s = (SELECT MAX(%1$s) FROM %2$s)", NUMBER_COLUMN, TABLE_NAME),
				null, null, null, COLUMNS[0]);
		c.moveToFirst();
		int data = c.getInt(0);
		c.close();
		return data;
	}

}
