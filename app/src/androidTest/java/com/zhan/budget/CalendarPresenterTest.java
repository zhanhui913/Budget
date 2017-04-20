package com.zhan.budget;

import android.support.test.espresso.core.deps.guava.collect.Lists;

import com.zhan.budget.Data.AppDataManager;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link CalendarPresenter}
 */
public class CalendarPresenterTest {

    private static List<Transaction> TRANSACTIONS;

    @Mock
    private AppDataManager mDataManager;


    @Mock
    private CalendarContract.View mCalendarView;

    private CalendarPresenter mCalendarPresenter;

    @Before
    public void setupCalendarPresenter(){
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

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

        TRANSACTIONS = Lists.newArrayList(transaction1);
    }

    @Test
    public void loadTransactionsFromRealmIntoView(){
        mCalendarPresenter.populateTransactionsForDate1(new Date());

        InOrder inOrder = inOrder(mCalendarView);
        inOrder.verify(mCalendarView).setLoadingIndicator(true);
        inOrder.verify(mCalendarView).setLoadingIndicator(false);


    }
}
