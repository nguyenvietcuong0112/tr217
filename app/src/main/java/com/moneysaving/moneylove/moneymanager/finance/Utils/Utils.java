package com.moneysaving.moneylove.moneymanager.finance.Utils;

import com.moneysaving.moneylove.moneymanager.finance.model.CurrencyUnitModel;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static ArrayList<CurrencyUnitModel> getCurrencyUnit() {
        List<ExtendedCurrency> currencies = ExtendedCurrency.getAllCurrencies();
        ArrayList<CurrencyUnitModel> currencyUnitModels = new ArrayList<>();
        for (ExtendedCurrency currency : currencies) {
            String symbol = currency.getSymbol();
            String name = currency.getName();
            String code = currency.getCode();
            int flagResId = currency.getFlag();
            currencyUnitModels.add(new CurrencyUnitModel(symbol,name, code, false,flagResId));
        }
        return  currencyUnitModels;
    }
}
