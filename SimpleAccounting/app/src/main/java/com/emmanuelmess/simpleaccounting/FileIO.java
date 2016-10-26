package com.emmanuelmess.simpleaccounting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Emmanuel
 *         on 2016-01-31, at 15:53.
 */
class FileIO extends SQLiteOpenHelper {

	static final String[] COLUMNS = new String[] { "DATE", "REFERENCE", "CREDIT", "DEBT"};

	private static final String NUMBER_COLUMN = "NUMBER";
	private static final int DATABASE_VERSION = 2;
	private static final String TABLE_NAME = "ACCOUNTING";
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + NUMBER_COLUMN + " INT, " + COLUMNS[0] + " INT, " + COLUMNS[1] +
			" TEXT, " + COLUMNS[2] + " REAL, " + COLUMNS[3] + " REAL);";
	private final ContentValues CV = new ContentValues();

	FileIO(Context context) {super(context, TABLE_NAME, null, DATABASE_VERSION);}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion == 1) {
			String sql = "ALTER TABLE " + TABLE_NAME + " DROP COLUMN " + "BALANCE";
			db.execSQL(sql);
		}
	}

	void newRow() {
		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN}, null, null, null,
				null, null);
		int i;

		if(c.getCount() == 0) {
			i = 0;
		} else {
			c.moveToLast();
			i = c.getInt(0) + 1;
			c.close();
		}

		CV.put(NUMBER_COLUMN, i);
		getWritableDatabase().insert(TABLE_NAME, null, CV);
		CV.clear();
	}

	void update(int row, String column, String data) {
		CV.put(column, data);
		getWritableDatabase().update(TABLE_NAME, CV, NUMBER_COLUMN + "=" + row, null);
		CV.clear();
	}

	String[][] getAll() {
		String [][] data;

		Cursor c = getReadableDatabase().query(TABLE_NAME, COLUMNS, null, null, null,
				null, null);

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

}
