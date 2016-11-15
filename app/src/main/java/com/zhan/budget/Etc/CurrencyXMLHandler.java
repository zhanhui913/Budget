package com.zhan.budget.Etc;

import com.zhan.budget.Model.Realm.BudgetCurrency;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-10-27.
 */

public class CurrencyXMLHandler extends DefaultHandler {
    private List<BudgetCurrency> currencies;
    private String tempVal;
    private BudgetCurrency tempBudgetCurrency;

    private boolean mCurrency = false;
    private boolean mCode = false;
    private boolean mName = false;

    private static String CURRENCY = "currency";
    private static String CODE = "code";
    private static String NAME = "name";

    private StringBuilder textContent;

    public CurrencyXMLHandler() {
        currencies = new ArrayList<>();
        textContent = new StringBuilder();
    }

    public List<BudgetCurrency> getCurrencies() {
        return currencies;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // reset
        tempVal = "";
        textContent.setLength(0);

        if (qName.equalsIgnoreCase(CURRENCY)) {
            // create a new instance of BudgetCurrency
            tempBudgetCurrency = new BudgetCurrency();
            mCurrency = true;
        }else if(qName.equalsIgnoreCase(CODE)){
            mCode = true;
        }else if(qName.equalsIgnoreCase(NAME)){
            mName = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        textContent.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        tempVal = textContent.toString();

        if (mCurrency && qName.equalsIgnoreCase(CURRENCY)) {
            // add it to the list
            currencies.add(tempBudgetCurrency);
            mCurrency = false;
        } else if (mCode && qName.equalsIgnoreCase(CODE)) {
            tempBudgetCurrency.setCurrencyCode(tempVal);
            mCode = false;
        } else if (mName && qName.equalsIgnoreCase(NAME)) {
            tempBudgetCurrency.setCurrencyName(tempVal);
            mName = false;
        }
    }
}
