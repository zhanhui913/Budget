package com.zhan.budget.Etc;

import com.zhan.budget.Model.Realm.Category;
import com.zhan.budget.Util.Util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-10-28.
 */

public class CategoryXMLHandler extends DefaultHandler {
    private List<Category> categories;
    private String tempVal;
    private Category tempCategory;

    private boolean mCategory = false;
    private boolean mName = false;
    private boolean mType = false;
    private boolean mColor = false;
    private boolean mIcon = false;
    private boolean mIndex = false;
    private boolean mIsText = false;

    private static String CATEGORY = "category";
    private static String NAME = "name";
    private static String TYPE = "type";
    private static String COLOR = "color";
    private static String ICON = "icon";
    private static String INDEX = "index";
    private static String ISTEXT = "isText";

    private StringBuilder textContent;

    public CategoryXMLHandler() {
        categories = new ArrayList<>();
        textContent = new StringBuilder();
    }

    public List<Category> getCategories() {
        return categories;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // reset
        tempVal = "";
        textContent.setLength(0);

        if (qName.equalsIgnoreCase(CATEGORY)) {
            // create a new instance of Category
            tempCategory = new Category();
            tempCategory.setId(Util.generateUUID());

            mCategory = true;
        }else if(qName.equalsIgnoreCase(NAME)){
            mName = true;
        }else if(qName.equalsIgnoreCase(TYPE)){
            mType = true;
        }else if(qName.equalsIgnoreCase(COLOR)){
            mColor = true;
        }else if(qName.equalsIgnoreCase(ICON)){
            mIcon = true;
        }else if(qName.equalsIgnoreCase(INDEX)){
            mIndex = true;
        }else if(qName.equalsIgnoreCase(ISTEXT)){
            mIsText = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        textContent.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        tempVal = textContent.toString();

        if (mCategory && qName.equalsIgnoreCase(CATEGORY)) {
            // add it to the list
            categories.add(tempCategory);
            mCategory = false;
        } else if (mName && qName.equalsIgnoreCase(NAME)) {
            tempCategory.setName(tempVal);
            mName = false;
        } else if (mType && qName.equalsIgnoreCase(TYPE)) {
            tempCategory.setType(tempVal);
            mType = false;
        }else if (mColor && qName.equalsIgnoreCase(COLOR)) {
            tempCategory.setColor(tempVal);
            mColor = false;
        }else if (mIcon && qName.equalsIgnoreCase(ICON)) {
            tempCategory.setIcon(tempVal);
            mIcon = false;
        }else if (mIndex && qName.equalsIgnoreCase(INDEX)) {
            tempCategory.setIndex(Integer.parseInt(tempVal));
            mIndex = false;
        }else if (mIsText && qName.equalsIgnoreCase(ISTEXT)) {
            tempCategory.setText(Boolean.parseBoolean(tempVal));
            mIsText = false;
        }
    }
}
