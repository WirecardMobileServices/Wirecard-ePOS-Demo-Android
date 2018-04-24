package de.wirecard.eposdemo;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import de.wirecard.epos.model.cashregisters.CashRegisterLight;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class SettingsFragment extends AbsFragment<ScrollView> {

    private AlertDialog actualDialog;

    private TextView cashRegisterValue;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cash_register_selection).setOnClickListener(v -> {
            showLoading();
            addDisposable(EposSdkApplication.getEposSdk()
                    .cash()
                    .getCashRegistersLight()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cashRegisterLights -> {
                                loadingFinished();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.cash_register);

                                final List<String> registerNames = Stream.of(cashRegisterLights).map(it -> it.getName()).collect(Collectors.toList());
                                builder.setItems(registerNames.toArray(new CharSequence[registerNames.size()]), (dialog, which) -> {
                                    final CashRegisterLight cashRegisterLight = cashRegisterLights.get(which);
                                    EposSdkApplication.setCashRegister(cashRegisterLight.getId(), cashRegisterLight.getName());
                                    refreshCashRegister();
                                });

                                actualDialog = builder.create();

                                actualDialog.show();
                            }, showErrorInsteadContent()
                    ));
        });

        cashRegisterValue = view.findViewById(R.id.cash_register_value);

        refreshCashRegister();
    }

    private void refreshCashRegister() {
        if (EposSdkApplication.getCashRegisterId() != null && EposSdkApplication.getCashRegisterName() != null)
            cashRegisterValue.setText(EposSdkApplication.getCashRegisterName());
    }

    @Override
    protected void showLoading() {
        super.showLoading();
        if (actualDialog != null && actualDialog.isShowing())
            actualDialog.dismiss();
    }

}
