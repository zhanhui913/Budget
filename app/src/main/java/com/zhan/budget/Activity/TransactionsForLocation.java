package com.zhan.budget.Activity;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.pdfjet.Text;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.R;
import com.zhan.budget.Util.DateUtil;

import java.util.Date;

public class TransactionsForLocation extends BaseRealmActivity {

    private Toolbar toolbar;
    private Date currentMonth;
    private String location;
    private TextView locationTextView, costTextView;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_transactions_for_location;
    }

    @Override
    protected void init(){
        //Get intents from caller activity
        currentMonth = DateUtil.convertStringToDate((getIntent().getExtras()).getString(Constants.REQUEST_ALL_TRANSACTION_FOR_LOCATION_MONTH));
        location = getIntent().getExtras().getString(Constants.REQUEST_ALL_TRANSACTION_FOR_LOCATION_LOCATION);

        locationTextView = (TextView)findViewById(R.id.locationName);
        costTextView = (TextView)findViewById(R.id.transactionLocationBalance);

        locationTextView.setText(location);

        createToolbar();
        addListeners();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_nav_back);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(DateUtil.convertDateToStringFormat2(currentMonth));
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
