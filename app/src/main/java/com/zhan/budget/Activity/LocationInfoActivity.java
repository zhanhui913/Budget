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
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import org.parceler.Parcels;

import io.realm.Realm;

public class LocationInfoActivity extends BaseActivity implements
        ColorPickerCategoryFragment.OnColorPickerCategoryFragmentInteractionListener{

    private static final String TAG = "Location_INFO";

    private Activity instance;
    private Toolbar toolbar;
    private TextView locationNameTextView;
    private ImageButton deleteLocationtBtn, changeNameBtn;
    private CircularView locationCircularView;

    //Fragments
    private ColorPickerCategoryFragment colorPickerCategoryFragment;

    private boolean isNewLocation;
    private Location location;

    //Selected color
    private String selectedColor;

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_location_info;
    }

    @Override
    protected void init(){
        instance = this;

        isNewLocation = (getIntent().getExtras()).getBoolean(Constants.REQUEST_NEW_LOCATION);

        Log.d(TAG, "isNewLocation "+isNewLocation);

        if(!isNewLocation){
            location = Parcels.unwrap((getIntent().getExtras()).getParcelable(Constants.REQUEST_EDIT_LOCATION));

            Log.d(TAG, "received location name : " +location.getName());
            Log.d(TAG, "received location color : " +location.getColor());

        }else{
            //Give default location values
            location = new Location();
            location.setColor(CategoryUtil.getDefaultCategoryColor(this));
        }

        colorPickerCategoryFragment = ColorPickerCategoryFragment.newInstance(location.getColor());

        locationCircularView = (CircularView)findViewById(R.id.locationCircularView);

        deleteLocationtBtn = (ImageButton)findViewById(R.id.deleteLocationBtn);
        changeNameBtn = (ImageButton)findViewById(R.id.changeNameBtn);

        locationNameTextView = (TextView)findViewById(R.id.locationNameTextView);
        locationNameTextView.setText(location.getName());

        if(isNewLocation){
            deleteLocationtBtn.setVisibility(View.GONE);
        }else{
            deleteLocationtBtn.setVisibility(View.VISIBLE);//Cant delete location for now
        }



        //default color selected
        selectedColor = location.getColor();
        locationCircularView.setCircleColor(location.getColor());
        locationCircularView.setTextSizeInDP(30);

        if(!isNewLocation){
            locationCircularView.setText(""+Util.getFirstCharacterFromString(location.getName().toUpperCase()));
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
            if(isNewLocation){
                getSupportActionBar().setTitle("Add Location");
            }else{
                getSupportActionBar().setTitle("Edit Location");
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

        locationNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });

        deleteLocationtBtn.setOnClickListener(new View.OnClickListener() {
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

        genericTitle.setText("Location Name");
        input.setText(locationNameTextView.getText());
        input.setHint("Location");

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        locationNameTextView.setText(input.getText().toString());

                        //location.setName(input.getText().toString());

                        //update the text in the circular view to reflect the new name
                        locationCircularView.setText(""+Util.getFirstCharacterFromString(input.getText().toString().toUpperCase()));
                        locationCircularView.setTextColor(Colors.getHexColorFromAttr(instance, R.attr.themeColor));                    }
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
        message.setText(R.string.warning_delete_location);

        new AlertDialog.Builder(this)
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_button_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Toast.makeText(getApplicationContext(), "DELETE...", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        Realm myRealm = Realm.getDefaultInstance();
                        Location loc = myRealm.where(Location.class).equalTo("name", location.getName()).findFirst();
                        myRealm.beginTransaction();
                        loc.deleteFromRealm();
                        myRealm.commitTransaction();
                        myRealm.close();

                        intent.putExtra(Constants.RESULT_DELETE_LOCATION, true); //deleting location
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

        Location loc;

        Realm myRealm = Realm.getDefaultInstance(); BudgetPreference.addRealmCache(this);
        if(!isNewLocation){
            loc = myRealm.where(Location.class).equalTo("name", location.getName()).findFirst();
            myRealm.beginTransaction();
        } else{
            myRealm.beginTransaction();
            loc = myRealm.createObject(Location.class);
        }

        loc.setName(locationNameTextView.getText().toString());
        loc.setColor(location.getColor());
        myRealm.commitTransaction();

        Log.d(TAG, "-----Results-----");
        Log.d(TAG, "Account name : "+loc.getName());
        Log.d(TAG, "color : " + loc.getColor());
        Log.d(TAG, "-----Results-----");

        Parcelable wrapped = Parcels.wrap(loc);
        myRealm.close();BudgetPreference.removeRealmCache(this);

        if(!isNewLocation){
            intent.putExtra(Constants.RESULT_DELETE_LOCATION, false); //not deleting location
            intent.putExtra(Constants.RESULT_EDIT_LOCATION, wrapped);
        }else{
            intent.putExtra(Constants.RESULT_NEW_LOCATION, wrapped);
        }

        setResult(RESULT_OK, intent);

        finish();
    }

    private void updateCircularColor(){
        locationCircularView.setCircleColor(selectedColor);
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
            save();
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
        location.setColor(selectedColor);
        updateCircularColor();
    }
}
