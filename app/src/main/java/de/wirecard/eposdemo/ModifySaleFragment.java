package de.wirecard.eposdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import de.wirecard.epos.model.sale.builder.ModifyPurchaseRequest;
import de.wirecard.epos.model.sale.sales.Sale;
import de.wirecard.epos.model.sale.sales.SaleItem;
import de.wirecard.epos.model.sale.sales.SaleItemType;
import de.wirecard.epos.util.TaxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class ModifySaleFragment extends AbsFragment<View> {

    private Button modify;
    private EditText saleNote, saleAmount;
    private Sale sale;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modify_sale, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        modify = view.findViewById(R.id.modify);
        saleNote = view.findViewById(R.id.saleNoteEditText);
        saleAmount = view.findViewById(R.id.amountEditText);

        Bundle arguments = getArguments();
        if (arguments != null) {
            sale = arguments.getParcelable(MainActivity.SALE);
        }
        loadingFinished();

        modify.setOnClickListener(v -> {
            if (sale != null) {
                showLoading();

                BigDecimal modifiedAmount = new BigDecimal(saleAmount.getText().toString().replace(',', '.')).setScale(CURRENCY.getDefaultFractionDigits(), RoundingMode.HALF_UP);

                List<SaleItem> saleItem = Collections.singletonList(new SaleItem(
                        SaleItemType.PURCHASE,
                        "Demo modify sale item",
                        modifiedAmount,
                        null,
                        BigDecimal.ONE,
                        TaxUtils.calculateTaxAmount(modifiedAmount, new BigDecimal(19), sale.getUnitPricesIncludeTax(), FRACTION_DIGITS),
                        modifiedAmount,
                        null,
                        null,
                        null,
                        null
                ));

                ModifyPurchaseRequest modifyPurchaseRequest = new ModifyPurchaseRequest(sale.getId(), sale.getVersion(), saleNote.getText().toString(), null, modifiedAmount, saleItem,
                        sale.getUnitPricesIncludeTax());

                addDisposable(EposSdkApplication.getEposSdk()
                        .sales()
                        .operation()
                        .modify(modifyPurchaseRequest)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sale1 -> {
                            Toast.makeText(getContext(), "Sale modify completed", Toast.LENGTH_SHORT).show();
                            loadSales();
                        }, showErrorInsteadContent()));
            }


        });
    }

    private void loadSales() {
        SalesFragment fragment = new SalesFragment();
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeScreen(fragment, fragment.getClass().getSimpleName());
        }
    }


}
