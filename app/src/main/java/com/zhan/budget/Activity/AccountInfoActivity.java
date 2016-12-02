package com.zhan.budget.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Fragment.ColorPickerCategoryFragment;
import com.zhan.budget.Model.Realm.Account;
import com.zhan.budget.R;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import org.parceler.Parcels;

import io.realm.Realm;

public class AccountInfoActivity extends BaseActivity implements
        ColorPickerCategoryFragment.OnColorPickerCategoryFragmentInteractionListener{

    private Activity instance;
    private Toolbar toolbar;
    private TextView accountNameTextView;
    private ImageButton deleteAccountBtn, changeNameBtn;
    private CircularView accountCircularView;


    //Fragments
    private ColorPickerCategoryFragment colorPickerCategoryFragment;

    private boolean isNewAccount;
    private Account account;

    //Selected color
    private String selectedColor;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_account_info;
    }

    @Override
    protected void init(){
        instance = this;

        isNewAccount = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_ACCOUNT);

        Log.d("ACCOUNT_INFO", "isNewAccount "+isNewAccount);

        if(!isNewAccount){
            account = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_EDIT_ACCOUNT));

            Log.d("ACCOUNT_INFO", "received account name : " +account.getName());
            Log.d("ACCOUNT_INFO", "received account id : " +account.getId());
            Log.d("ACCOUNT_INFO", "received account color : " +account.getColor());
        }else{
            //Give default account values
            account = new Account();
            account.setId(Util.generateUUID());
            account.setColor(CategoryUtil.getDefaultCategoryColor(this));
            account.setIsDefault(false);
        }

        colorPickerCategoryFragment = ColorPickerCategoryFragment.newInstance(account.getColor());

        accountCircularView = (CircularView)findViewById(R.id.accountCircularView);

        deleteAccountBtn = (ImageButton)findViewById(R.id.deleteAccountBtn);
        changeNameBtn = (ImageButton)findViewById(R.id.changeNameBtn);

        accountNameTextView = (TextView)findViewById(R.id.accountNameTextView);
        accountNameTextView.setText(account.getName());

        if(isNewAccount){
            deleteAccountBtn.setVisibility(View.GONE);
        }else{
            deleteAccountBtn.setVisibility(View.VISIBLE);
        }

        //default color selected
        selectedColor = account.getColor();
        accountCircularView.setCircleColor(account.getColor());
        accountCircularView.setTextSizeInDP(30);

        if(!isNewAccount){
            accountCircularView.setText(""+Util.getFirstCharacterFromString(account.getName().toUpperCase()));
        }

        getSupportFragmentManager().beginTransaction().add(R.id.colorFragment, colorPickerCategoryFragment).commit();

        createToolbar();
        addListeners();
    }

    /**
     * Create toolbar
     */
    private void createToolbar(){
        //Create the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.svg_ic_clear);

        if(getSupportActionBar() != null){
            if(isNewAccount){
                getSupportActionBar().setTitle(getString(R.string.add_account));
            }else{
                getSupportActionBar().setTitle(getString(R.string.edit_account));
            }
        }
    }

    private void addListeners(){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });

        accountNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });

        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    private void changeName(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic, null);

        TextView genericTitle = (TextView) promptView.findViewById(R.id.genericTitle);
        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);

        genericTitle.setText(getString(R.string.name));
        input.setText(accountNameTextView.getText());
        input.setHint(getString(R.string.account));

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        accountNameTextView.setText(input.getText().toString());

                        account.setName(input.getText().toString());

                        //update the text in the circular view to reflect the new name
                        accountCircularView.setText(""+Util.getFirstCharacterFromString(input.getText().toString().toUpperCase()));
                        accountCircularView.setTextColor(Colors.getHexColorFromAttr(instance, R.attr.themeColor));                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog nameDialog = builder.create();
        nameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        nameDialog.show();
    }

    private void confirmDelete(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_account);

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        Realm myRealm = Realm.getDefaultInstance();
                        Account acc = myRealm.where(Account.class).equalTo("id", account.getId()).findFirst();
                        myRealm.beginTransaction();
                        acc.deleteFromRealm();
                        myRealm.commitTransaction();
                        myRealm.close();

                        intent.putExtra(Constants.RESULT_DELETE_ACCOUNT, true); //deleting account
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void save(){
        Intent intent = new Intent();

        Account acc;

        Realm myRealm = Realm.getDefaultInstance();
        if(!isNewAccount){
            acc = myRealm.where(Account.class).equalTo("id", account.getId()).findFirst();
            myRealm.beginTransaction();
        } else{
            myRealm.beginTransaction();
            acc = myRealm.createObject(Account.class);
            acc.setId(account.getId());
            acc.setIsDefault(account.isDefault());
        }

        acc.setName(accountNameTextView.getText().toString());
        acc.setColor(account.getColor());
        myRealm.commitTransaction();

        Log.d("ACCOUNT_INFO_ACTIVITY", "-----Results-----");
        Log.d("ACCOUNT_INFO_ACTIVITY", "Account name : "+acc.getName());
        Log.d("ACCOUNT_INFO_ACTIVITY", "id : " + acc.getId());
        Log.d("ACCOUNT_INFO_ACTIVITY", "color : " + acc.getColor());
        Log.d("ACCOUNT_INFO_ACTIVITY", "-----Results-----");
        Log.d("ACCOUNT_INFO_ACTIVITY", "collor 2 "+account.getColor());

        Parcelable wrapped = Parcels.wrap(account);
        myRealm.close();

        if(!isNewAccount){
            intent.putExtra(Constants.RESULT_DELETE_ACCOUNT, false); //not deleting account
            intent.putExtra(Constants.RESULT_EDIT_ACCOUNT, wrapped);
        }else{
            intent.putExtra(Constants.RESULT_NEW_ACCOUNT, wrapped);
        }

        setResult(RESULT_OK, intent);

        finish();
    }

    private void updateCircularColor(){
        accountCircularView.setCircleColor(selectedColor);
    }

    /**
     * If there is no Account name, a dialog will popup to remind the user.
     */
    private void notificationForAccount(){
        View promptView = View.inflate(getBaseContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.genericTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(R.string.account);
        message.setText(R.string.warning_account_valid_name);

        new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Menu
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.formSaveBtn) {

            if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(account.getName())){
                save();
            }else{
                notificationForAccount();

            }
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Fragment Listener
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onColorCategoryClick(String color){
        Log.d("ACCOUNT_INFO", "click on color : "+color);
        selectedColor = color;
        account.setColor(selectedColor);
        updateCircularColor();
    }
}
