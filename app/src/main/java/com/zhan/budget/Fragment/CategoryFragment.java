package com.zhan.budget.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.zhan.budget.Activity.CategoryInfo;
import com.zhan.budget.Activity.TransactionInfoActivity;
import com.zhan.budget.Adapter.CategoryListAdapter;
import com.zhan.budget.Database.Database;
import com.zhan.budget.Etc.Constants;
import com.zhan.budget.Model.Category;
import com.zhan.budget.Model.Transaction;
import com.zhan.budget.R;
import com.zhan.budget.Util.Util;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CategoryFragment.OnCategoryInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    private OnCategoryInteractionListener mListener;
    private View view;
    private FloatingActionButton fab;

    private SwipeMenuListView categoryListView;
    private CategoryListAdapter categoryAdapter;

    private ArrayList<Category> categoryList;
    private Database db;

    private int categoryIndexEditted;//The index of the category that the user just finished editted.

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CategoryFragment.
     */
    public static CategoryFragment newInstance() {
        CategoryFragment fragment = new CategoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_category, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        init();
        addListener();
        createSwipeMenu();
    }

    private void init(){
        openDatabase();

        fab = (FloatingActionButton) view.findViewById(R.id.addCategoryFAB);
        categoryListView = (SwipeMenuListView) view.findViewById(R.id.categoryListView);

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryListAdapter(getActivity(), categoryList);
        categoryListView.setAdapter(categoryAdapter);

        populateCategory();
    }

    private void addListener(){
        fab.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                displayPrompt();
            }
        });
    }

    /**
     * Displays prompt for user to type new category.
     */
    private void displayPrompt(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        //It is ok to put null as the 2nd parameter as this custom layout is being attached to a
        //AlertDialog, where it not necessary to know what the parent is.
        View promptView = layoutInflater.inflate(R.layout.zz_mission_prompt, null);

        final EditText input = (EditText) promptView.findViewById(R.id.editTextCategory);

        new AlertDialog.Builder(getActivity())
                .setView(promptView)
                .setCancelable(true)
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Category c = new Category();
                        c.setName(input.getText().toString());
                        c.setBudget(100.0f);
                        c.setCost(2.0f);


                        db.createCategory(c);

                        categoryList.add(c);
                        categoryAdapter.refreshList(categoryList);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }


    @Override
    public void onResume(){
        super.onResume();
        Log.d("ZHAN", "on resume");
    }

    private void populateCategory(){

        categoryList = db.getAllCategory();

        Log.d("ZHAN", "There are " + categoryList.size() + " categories");

        for(int i = 0; i < categoryList.size(); i++){
            Log.d("ZHAN", i+"->"+categoryList.get(i).getName());
        }


        categoryAdapter.refreshList(categoryList);
    }

    /**
     * Add swipe capability on list view to delete that item.
     * From 3rd party library.
     */
    private void createSwipeMenu() {
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "edit" item
                SwipeMenuItem editItem = new SwipeMenuItem(getContext());
                editItem.setBackground(R.color.colorPrimary);// set item background
                editItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                editItem.setIcon(R.drawable.ic_mode_edit);// set a icon
                menu.addMenuItem(editItem);// add to menu

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext());
                deleteItem.setBackground(R.color.red);// set item background
                deleteItem.setWidth(Util.dp2px(getContext(), 90));// set item width
                deleteItem.setIcon(R.drawable.ic_delete);// set a icon
                menu.addMenuItem(deleteItem);// add to menu
            }
        };
        //set creator
        categoryListView.setMenuCreator(creator);

        // step 2. listener item click event
        categoryListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //edit
                        categoryIndexEditted = position;

                        Intent editCategory = new Intent(getContext(), CategoryInfo.class);

                        //This is edit mode
                        editCategory.putExtra(Constants.REQUEST_EDIT_CATEGORY, categoryList.get(position));
                        startActivityForResult(editCategory, Constants.RETURN_EDIT_CATEGORY);

                        break;

                    case 1:
                        //delete
                        db.deleteCategory(categoryList.get(position));
                        categoryList.remove(position);
                        categoryAdapter.refreshList(categoryList);

                        break;
                }
                //False: Close the menu
                //True: Did not close the menu
                return false;
            }
        });

        // set SwipeListener
        categoryListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), "Item clicked : " + categoryList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if(requestCode == Constants.RETURN_EDIT_CATEGORY){

                Log.i("ZHAN", "----------- onActivityResult ----------");

                Category category = data.getExtras().getParcelable(Constants.RESULT_EDIT_CATEGORY);

                Log.d("ZHAN", "category name is "+category.getName());
                Log.i("ZHAN", "----------- onActivityResult ----------");

                db.updateCategory(category);

                categoryList.set(categoryIndexEditted, category);
                categoryAdapter.refreshList(categoryList);
            }
        }
    }

    public void openDatabase(){
        db = new Database(getActivity().getApplicationContext());
    }

    public void closeDatabase(){
        db.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCategoryInteractionListener) {
            mListener = (OnCategoryInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        closeDatabase();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCategoryInteractionListener {
        void onCategoryInteraction(String value);
    }
}
