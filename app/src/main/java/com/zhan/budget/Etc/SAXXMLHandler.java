package com.zhan.budget.Etc;

import android.util.Log;

import com.zhan.budget.Model.Realm.BudgetCurrency;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanyap on 2016-10-27.
 */

public class SAXXMLHandler extends DefaultHandler {
    private List<BudgetCurrency> employees;
    private String tempVal;
    private BudgetCurrency tempEmp;

    public SAXXMLHandler() {
        employees = new ArrayList<BudgetCurrency>();
    }

    public List<BudgetCurrency> getEmployees() {
        return employees;
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // reset
        tempVal = "";
        if (qName.equalsIgnoreCase("currency")) {
            // create a new instance of employee
            tempEmp = new BudgetCurrency();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("currency")) {
            // add it to the list
            employees.add(tempEmp);
            Log.d("XMLXX", "currency : "+tempEmp);
        } else if (qName.equalsIgnoreCase("code")) {
            tempEmp.setCurrencyCode(tempVal);
            Log.d("XMLXX", "code : "+tempEmp);
        } else if (qName.equalsIgnoreCase("country")) {
            tempEmp.setCountry(tempVal);
            Log.d("XMLXX", "country : "+tempEmp);
        }else if (qName.equalsIgnoreCase("symbol")) {
            tempEmp.setSymbol(tempVal);
            Log.d("XMLXX", "symbol : "+tempEmp);
        }
    }
}
