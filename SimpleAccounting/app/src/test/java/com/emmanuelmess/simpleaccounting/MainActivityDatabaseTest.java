package com.emmanuelmess.simpleaccounting;

import android.database.sqlite.SQLiteDatabase;
import android.widget.TableRow;

import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;
import com.emmanuelmess.simpleaccounting.utils.SimpleBalanceFormatter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.emmanuelmess.simpleaccounting.utils.database.SQLiteHelper.setUpDatabase;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityDatabaseTest extends MainActivityTest {

    private SQLiteDatabase database;
    private TableGeneral tableGeneral;
    private TableMonthlyBalance tableMonthlyBalance;
    private File databasePath;

    @Override
    protected void endSetUp() {
        databasePath = RuntimeEnvironment.application.getDatabasePath("database.db");

        database = SQLiteDatabase.openOrCreateDatabase(databasePath, null);

        tableGeneral = setUpDatabase(new TableGeneral(context), database);
        tableMonthlyBalance = setUpDatabase(new TableMonthlyBalance(context), database);
    }

    @Test
    public void testDisplayNewRow() {
        String c = "200", d = "100";

        BigDecimal credit = new BigDecimal(c);
        BigDecimal debit = new BigDecimal(d);
        String total = new SimpleBalanceFormatter().format(credit.subtract(debit).setScale(1));

        Date date = new Date();
        int month = Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(date)) - 1;
        int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));

        fakePastRow(month, year, "", 2, "test", credit, debit);

        super.endSetUp();

        LedgerRow balance = (LedgerRow) table.getChildAt(1);

        assertEquals(1, activity.getFirstRealRow());
        assertEquals(total, balance.getBalanceText());
    }

    @Test
    public void testSaveNewRow() {
        super.endSetUp();

        String c = "200", d = "100";

        TableRow row = createNewRow(c, d);

        BigDecimal credit = new BigDecimal(c);
        BigDecimal debit = new BigDecimal(d);
        String total = new SimpleBalanceFormatter().format(credit.subtract(debit).setScale(1));

        Date date = new Date();
        String day = new SimpleDateFormat("dd", Locale.getDefault()).format(date);

        int month = Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(date)) - 1;
        int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));


        getDataForMonth(month, year, "", (databaseResult) -> {
            assertEquals(day, databaseResult.date);
            assertEquals(null, databaseResult.reference);
            assertEquals(c + ".0", databaseResult.credit);
            assertEquals(d + ".0", databaseResult.debit);
            assertEquals(credit.subtract(debit).doubleValue(), databaseResult.monthResult, 0.01);
        });
    }

    @Test
    public void testLastBalance() {
        String c = "200", d = "100";

        BigDecimal credit = new BigDecimal(c);
        BigDecimal debit = new BigDecimal(d);
        String total = new SimpleBalanceFormatter().format(credit.subtract(debit));

        Date date = new Date();
        int month = Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(date)) - 1;
        int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));

        fakePastRow(month - 1, year, "", 2, "test", credit, debit);

        super.endSetUp();

        LedgerRow lastBalance = (LedgerRow) table.getChildAt(1);

        assertEquals(2, activity.getFirstRealRow());
        assertEquals(total, lastBalance.getBalanceText());
    }

    @Test
    public void testLastBalanceSkippingMonths() {
        String c = "200", d = "100";

        BigDecimal credit = new BigDecimal(c);
        BigDecimal debit = new BigDecimal(d);
        String total = new SimpleBalanceFormatter().format(credit.subtract(debit));

        Date date = new Date();
        int month = Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(date)) - 1;
        int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));

        fakePastRow(month - 2, year, "", 2, "test", credit, debit);

        super.endSetUp();

        LedgerRow lastBalance = (LedgerRow) table.getChildAt(1);

        assertEquals(2, activity.getFirstRealRow());
        assertEquals(total, lastBalance.getBalanceText());
    }

    @Test
    public void testSaveLastBalanceMonth() {
        super.endSetUp();

        String c = "200", d = "100";

        TableRow row = createNewRow(c, d);

        BigDecimal credit = new BigDecimal(c);
        BigDecimal debit = new BigDecimal(d);
        String total = new SimpleBalanceFormatter().format(credit.subtract(debit).setScale(1));

        Date date = new Date();
        String day = new SimpleDateFormat("dd", Locale.getDefault()).format(date);

        int month = Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(date)) - 1;
        int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));

        getLastBalanceForMonth(month+2, year, "", (lastBalance) -> {
            assertEquals(credit.subtract(debit).doubleValue(), lastBalance, 0.01);
        });
    }

    private void fakePastRow(int month, int year, String currency, int date, String reference,
                             BigDecimal credit, BigDecimal debit) {
        tableGeneral.newRowInMonth(month, year, currency);
        tableGeneral.update(0, TableGeneral.COLUMNS[0], date+"");
        tableGeneral.update(0, TableGeneral.COLUMNS[1], reference);
        tableGeneral.update(0, TableGeneral.COLUMNS[2], credit.toPlainString());
        tableGeneral.update(0, TableGeneral.COLUMNS[3], debit.toPlainString());
        tableMonthlyBalance.updateMonth(month, year, currency, credit.subtract(debit).doubleValue());
    }

    private void getDataForMonth(int month, int year, String currency, Consumer<DatabaseResult> callback) {
        String[][] wholeMonth = tableGeneral.getAllForMonth(month, year, currency);
        String[] lastRowInMonth = wholeMonth[wholeMonth.length-1];

        Double resultForMonth = tableMonthlyBalance.getBalanceLastMonthWithData(month+1, year, currency);

        callback.accept(new DatabaseResult(resultForMonth, lastRowInMonth[0], lastRowInMonth[1], lastRowInMonth[2], lastRowInMonth[3]));
    }

    private void getLastBalanceForMonth(int month, int year, String currency, Consumer<Double> callback) {
        Double resultForMonth = tableMonthlyBalance.getBalanceLastMonthWithData(month+1, year, currency);

        callback.accept(resultForMonth);
    }

    private static class DatabaseResult {
        public final double monthResult;
        public final String date, reference, credit, debit;

        private DatabaseResult(double monthResult, String date, String reference, String credit, String debit) {
            this.monthResult = monthResult;
            this.date = date;
            this.reference = reference;
            this.credit = credit;
            this.debit = debit;
        }
    }

}
