package com.zhan.budget;

import android.content.Context;

import com.zhan.budget.Data.AppDataManager;
import com.zhan.budget.Data.Realm.RealmHelper;
import com.zhan.budget.Fragment.CalendarContract;
import com.zhan.budget.Fragment.CalendarPresenter;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Util.Util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit tests for the implementation of {@link CalendarPresenter}
 */

/*
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@SuppressStaticInitializationFor("io.realm.internal.Util")
@PrepareForTest({Realm.class, RealmLog.class})*/
public class CalendarPresenterTest {

    private static List<Transaction> TRANSACTIONS;

    @Mock
    private Context mContext;

   // @Mock
    private AppDataManager mDataManager;

    @Mock
    private CalendarContract.View mCalendarView;


    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<RealmHelper.LoadTransactionsCallback> mLoadTransactionsCallbackCaptor;

    private CalendarPresenter mCalendarPresenter;

    @Before
    public void setupCalendarPresenter(){
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        //Realm testRealm = RealmMock.mockRealm();

/*

        RealmConfiguration testConfig =
                new RealmConfiguration.Builder(mContext)
                .schemaVersion(1)
                .inMemory()
                .name("test-realm")
                .build();
        Realm.setDefaultConfiguration(testConfig);
        //Realm testRealm = Realm.getInstance(testConfig);*/


        // Setup Realm to be mocked. The order of these matters

        //Realm mockRealm = RealmMock.mockRealm();








       // mDataManager = new AppDataManager(mockRealm);

        mCalendarView.setContext(mContext);

        //Get a reference to the class under test
        mCalendarPresenter = new CalendarPresenter(mDataManager, mCalendarView);

        //The presenter won't update the view unless its active
        when(mCalendarView.isActive()).thenReturn(true);

        Category expenseCategory = new Category();
        expenseCategory.setId(Util.generateUUID());
        expenseCategory.setName("Grocery");
        expenseCategory.setColor("#ffffcc00");
        expenseCategory.setIcon("c_food");
        expenseCategory.setBudget(100);
        expenseCategory.setType(BudgetType.EXPENSE.toString());
        expenseCategory.setIndex(0);
        expenseCategory.setText(false);

        Category incomeCategory = new Category();
        incomeCategory.setId(Util.generateUUID());
        incomeCategory.setName("Salary");
        incomeCategory.setColor("#ffbe90d4");
        incomeCategory.setIcon("c_gift");
        incomeCategory.setBudget(0);
        incomeCategory.setType(BudgetType.INCOME.toString());
        incomeCategory.setIndex(0);
        incomeCategory.setText(false);

        Account account = new Account();
        account.setId(Util.generateUUID());
        account.setName("Visa");
        account.setIsDefault(true);
        account.setColor("#ff332345");

        Location location = new Location();
        location.setName("Costco");
        location.setColor("#ff990032");

        //Have Category, Account, Location, Note
        Transaction transaction1 = new Transaction();
        transaction1.setId(Util.generateUUID());
        transaction1.setCategory(expenseCategory);
        transaction1.setAccount(account);
        transaction1.setLocation(location);
        transaction1.setNote("Note 1");
        transaction1.setPrice(99.99);
        transaction1.setDayType(DayType.COMPLETED.toString());
        transaction1.setDate(new Date());

        TRANSACTIONS = Arrays.asList(transaction1);
    }

    @Test
    public void loadTransactionsFromRealmIntoView(){
        final Date selectedDate = new Date();
        /*mCalendarPresenter.populateTransactionsForDate1(selectedDate, new RealmHelper.RealmOperationCallback() {
            @Override
            public void onComplete() {
               // mCalendarPresenter.updateDecorations();

            }
        });


        InOrder inOrder = inOrder(mCalendarView);
        inOrder.verify(mCalendarView).setLoadingIndicator(true);
        inOrder.verify(mCalendarView).setLoadingIndicator(false);
*/

        mCalendarPresenter.populateTransactionsForDate1(selectedDate);

        //callback is captured and invoked with stubbed tasks
        verify(mDataManager).getTransactions(selectedDate, mLoadTransactionsCallbackCaptor.capture());
        mLoadTransactionsCallbackCaptor.getValue().onTransactionsLoaded(TRANSACTIONS);

        ArgumentCaptor<List> showTransactionsArgumentCaptor = ArgumentCaptor.forClass(List.class);


        (mCalendarView).updateTransactions(showTransactionsArgumentCaptor.capture());

        assertTrue(showTransactionsArgumentCaptor.getValue().size() == 2);

    }
}
