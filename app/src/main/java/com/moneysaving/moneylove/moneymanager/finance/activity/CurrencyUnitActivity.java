package com.moneysaving.moneylove.moneymanager.finance.activity;


import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import com.moneysaving.moneylove.moneymanager.finance.Utils.Utils;
import com.moneysaving.moneylove.moneymanager.finance.adapter.CurrencyUnitAdapter;
import com.moneysaving.moneylove.moneymanager.finance.base.AbsBaseActivity;
import com.moneysaving.moneylove.moneymanager.finance.databinding.ActivityCurrencyUnitBinding;


public class CurrencyUnitActivity extends AbsBaseActivity {

    public static final String EXTRA_FROM_SETTINGS = "extra_from_settings";


    CurrencyUnitAdapter currencyUnitAdapter;

    private ActivityCurrencyUnitBinding binding;

    @Override
    public void bind() {
        binding = ActivityCurrencyUnitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean fromSettings = getIntent().getBooleanExtra(EXTRA_FROM_SETTINGS, false);


        currencyUnitAdapter = new CurrencyUnitAdapter(this, Utils.getCurrencyUnit(), data -> {
            binding.ivSelect.setEnabled(true);
            binding.ivSelect.setAlpha(1.0f);

        });
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the list when text changes
                currencyUnitAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        binding.rvCurrencyUnit.setAdapter(currencyUnitAdapter);


        if (fromSettings) {
            binding.ivSelect.setEnabled(true);
            binding.ivSelect.setAlpha(1.0f);
        } else {
            binding.ivSelect.setEnabled(false);
            binding.ivSelect.setAlpha(0.3f);
        }

        binding.ivSelect.setOnClickListener(v -> {
            if(fromSettings) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                startActivity(new Intent(CurrencyUnitActivity.this, MainActivity.class));
            }

        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

