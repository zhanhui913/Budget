package com.zhan.budget.Activity;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.Context;
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

import com.zhan.budget.Fragment.ColorPickerCategoryFragment;
import com.zhan.budget.Model.Realm.Location;
import com.zhan.budget.R;
import com.zhan.budget.Util.BudgetPreference;
import com.zhan.budget.Util.CategoryUtil;
import com.zhan.budget.Util.Colors;
import com.zhan.budget.Util.Util;
import com.zhan.library.CircularView;

import org.parceler.Parcels;

import java.util.HashSet;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;

public class LocationInfoActivity extends BaseActivity implements
        ColorPickerCategoryFragment.OnColorPickerCategoryFragmentInteractionListener{

    private static final String TAG = "Location_INFO";

    public static final String NEW_LOCATION = "New Location";

    public static final String EDIT_LOCATION_ITEM = "Edit Location Item";

    public static final String RESULT_LOCATION = "Result Location";

    public static final String DELETE_LOCATION = "Delete Location";

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

    private HashSet<String> locationHash;

    public static Intent createIntentForNewLocation(Context context) {
        Intent intent = new Intent(context, LocationInfoActivity.class);
        intent.putExtra(NEW_LOCATION, true);
        return intent;
    }

    public static Intent createIntentToEditLocation(Context context, Location location) {
        Intent intent = new Intent(context, LocationInfoActivity.class);
        intent.putExtra(NEW_LOCATION, false);

        Parcelable wrapped = Parcels.wrap(location);
        intent.putExtra(EDIT_LOCATION_ITEM, wrapped);

        return intent;
    }

    @Override
    protected int getActivityLayout(){
        return R.layout.activity_location_info;
    }

    @Override
    protected void init(){
        instance = this;

        isNewLocation = (getIntent().getExtras()).getBoolean(NEW_LOCATION);

        locationHash = new HashSet<>();

        Log.d(TAG, "isNewLocation "+isNewLocation);

        if(!isNewLocation){
            location = Parcels.unwrap((getIntent().getExtras()).getParcelable(EDIT_LOCATION_ITEM));

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
        locationCircularView.setStrokeColor(location.getColor());

        if(!isNewLocation){
            locationCircularView.setText(""+Util.getFirstCharacterFromString(location.getName().toUpperCase().trim()));
        }

        getSupportFragmentManager().beginTransaction().add(R.id.colorFragment, colorPickerCategoryFragment).commit();

        createToolbar();
        addListeners();
        getAllLocations();
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
                getSupportActionBar().setTitle(getString(R.string.new_location));
            }else{
                getSupportActionBar().setTitle(getString(R.string.edit_location));
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

    private void getAllLocations(){
        final Realm myRealm = Realm.getDefaultInstance();

        RealmResults<Location> locationRealmResults = myRealm.where(Location.class).findAllAsync();
        locationRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Location>>() {
            @Override
            public void onChange(RealmResults<Location> element) {
                element.removeChangeListener(this);

                for(int i = 0; i < element.size(); i++){
                    locationHash.add(element.get(i).getName());
                }

                myRealm.close();
            }
        });
    }

    private void changeName(){
        View promptView = View.inflate(instance, R.layout.alertdialog_generic_edittext, null);

        TextView genericTitle = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        final EditText input = (EditText) promptView.findViewById(R.id.genericEditText);

        genericTitle.setText(getString(R.string.name));
        input.setText(locationNameTextView.getText());
        input.setHint(getString(R.string.location));

        AlertDialog.Builder builder = new AlertDialog.Builder(instance)
                .setView(promptView)
                .setPositiveButton(getString(R.string.dialog_button_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String inputValue = input.getText().toString().trim();

                        //If there already exist a location with that name and it isnt its own
                        if(locationHash.contains(inputValue) && !inputValue.equalsIgnoreCase(locationNameTextView.getText().toString().trim())){
                            Util.createSnackbar(getApplicationContext(), toolbar, getString(R.string.location_exist));
                        }else{
                            locationNameTextView.setText(inputValue);

                            location.setName(inputValue);

                            //update the text in the circular view to reflect the new name
                            locationCircularView.setText(""+Util.getFirstCharacterFromString(inputValue));
                            locationCircularView.setTextColor(Colors.getHexColorFromAttr(instance, R.attr.themeColor));
                        }

                    }
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

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(getString(R.string.dialog_title_delete));
        message.setText(R.string.warning_delete_location);

        new AlertDialog.Builder(this)
                .setView(promptView)
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

                        intent.putExtra(DELETE_LOCATION, true); //deleting location
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

    /**
     * If there is no location name, a dialog will popup to remind the user.
     */
    private void notificationForLocation(){
        View promptView = View.inflate(getBaseContext(), R.layout.alertdialog_generic_message, null);

        TextView title = (TextView) promptView.findViewById(R.id.alertdialogTitle);
        TextView message = (TextView) promptView.findViewById(R.id.genericMessage);

        title.setText(R.string.location);
        message.setText(R.string.warning_location_valid_name);

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

    private void save(){
        Intent intent = new Intent();

        Location loc;

        Realm myRealm = Realm.getDefaultInstance();
        if(!isNewLocation){

            //If added a name that isnt in the hash, it means it wont be in the db as well
            if(!locationHash.contains(location.getName())){
                myRealm.beginTransaction();
                loc = myRealm.createObject(Location.class);
            }else{
                loc = myRealm.where(Location.class).equalTo("name", location.getName()).findFirst();
                myRealm.beginTransaction();
            }
        } else{
            myRealm.beginTransaction();
            loc = myRealm.createObject(Location.class);
        }

        loc.setName(locationNameTextView.getText().toString().trim());
        loc.setColor(location.getColor());
        myRealm.commitTransaction();

        Log.d(TAG, "-----Results-----");
        Log.d(TAG, "Account name : "+loc.getName());
        Log.d(TAG, "color : " + loc.getColor());
        Log.d(TAG, "-----Results-----");

        Parcelable wrapped = Parcels.wrap(loc);
        myRealm.close();

        if(!isNewLocation){
            intent.putExtra(DELETE_LOCATION, false); //not deleting location
        }

        intent.putExtra(RESULT_LOCATION, wrapped);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void updateCircularColor(){
        locationCircularView.setCircleColor(selectedColor);
        locationCircularView.setStrokeColor(selectedColor);
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
            if(Util.isNotNullNotEmptyNotWhiteSpaceOnlyByJava(location.getName())){
                save();
            }else{
                notificationForLocation();
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
        location.setColor(selectedColor);
        updateCircularColor();
    }
}
