package com.zhan.budget;

import android.content.Context;

import com.zhan.budget.Data.Realm.RealmHelper;
import com.zhan.budget.Data.Realm.TransactionCaller;
import com.zhan.budget.Data.Realm.TransactionDAO;
import com.zhan.budget.Model.BudgetType;
import com.zhan.budget.Model.DayType;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.Model.Realm.Transaction;
import com.zhan.budget.Util.Util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//import static org.mockito.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class SimpleTest {

    @Mock
    Context mContext;

    @Captor
    ArgumentCaptor<RealmHelper.LoadTransactionsCallback> loadTransactionsCaptor;

    @Mock
    TransactionDAO transactionDAO;

    //class under test
    private TransactionCaller transactionCaller;

    private static List<Transaction> TRANSACTIONS;

    private Date today;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        transactionCaller = new TransactionCaller(transactionDAO);

        today = new Date();

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
        transaction1.setDate(today);

        TRANSACTIONS = Arrays.asList(transaction1);


    }

    @Test
    public void getTransactions(){
        //lets call the method under test
        transactionCaller.getTransactions(today);

        verify(transactionDAO).getTransactions(eq(today), loadTransactionsCaptor.capture());

        //Some assertion about the state before the callback is called
        assertThat(transactionCaller.getTransactionResults().isEmpty(), is(true));

        //Once you're satisfied, trigger the reply on loadTransactionsCaptor.getValue().
        loadTransactionsCaptor.getValue().onTransactionsLoaded(TRANSACTIONS);

        assertThat(transactionCaller.getTransactionResults(), is(equalTo(TRANSACTIONS)));
    }

    @Test
    public void getSimpleTransactions(){
        when(transactionDAO.getTransactions(today)).thenReturn(TRANSACTIONS);

        List<Transaction> list = transactionDAO.getTransactions(today);
        Assert.assertEquals(list.size(), 1);
    }
}
