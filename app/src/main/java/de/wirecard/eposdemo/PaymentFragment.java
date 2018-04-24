package de.wirecard.eposdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import de.wirecard.epos.EposSDK;
import de.wirecard.epos.model.sale.builder.SaleBuilder;
import de.wirecard.epos.model.sale.sales.SaleItem;
import de.wirecard.epos.model.sale.sales.SaleItemType;
import de.wirecard.epos.util.TaxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import kotlin.collections.CollectionsKt;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class PaymentFragment extends AbsFragment<View> {

    private BigDecimal amountValue;

    private TextView amountTextView;
    private Button cashButton, cardButton;

    private final NumberFormat nf;

    public PaymentFragment() {
        amountValue = BigDecimal.ZERO;
        nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(CURRENCY);
        nf.setMinimumFractionDigits(FRACTION_DIGITS);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.calculator_1).setOnClickListener(new CalcListener(1));
        view.findViewById(R.id.calculator_2).setOnClickListener(new CalcListener(2));
        view.findViewById(R.id.calculator_3).setOnClickListener(new CalcListener(3));
        view.findViewById(R.id.calculator_4).setOnClickListener(new CalcListener(4));
        view.findViewById(R.id.calculator_5).setOnClickListener(new CalcListener(5));
        view.findViewById(R.id.calculator_6).setOnClickListener(new CalcListener(6));
        view.findViewById(R.id.calculator_7).setOnClickListener(new CalcListener(7));
        view.findViewById(R.id.calculator_8).setOnClickListener(new CalcListener(8));
        view.findViewById(R.id.calculator_9).setOnClickListener(new CalcListener(9));
        view.findViewById(R.id.calculator_0).setOnClickListener(new CalcListener(0));
        view.findViewById(R.id.calculator_00).setOnClickListener(new CalcListener(-1));
        Button calcC = view.findViewById(R.id.calculator_c);
        calcC.setOnClickListener(new CalcListener(-2));
        calcC.setOnLongClickListener(v -> {
            amountValue = BigDecimal.ZERO;
            refreshAmount();
            return true;
        });

        amountTextView = view.findViewById(R.id.amount);
        refreshAmount();

        cashButton = view.findViewById(R.id.cash);
        cardButton = view.findViewById(R.id.card);

        cashButton.setOnClickListener(v -> {
            showLoading();
            final EposSDK eposSdk = EposSdkApplication.getEposSdk();
            if (EposSdkApplication.getCashRegisterId() == null) {
                Toast.makeText(getContext(), "You have to select cash register in settings", Toast.LENGTH_LONG).show();
                return;
            }
            addDisposable(
                    eposSdk.sales()
                            .pay(SaleBuilder.newBuilder()
                                    .setAmount(amountValue)
                                    .setCurrency(CURRENCY)
                                    .unitPricesIncludeTax()
                                    .addCashPayment(amountValue)
                                    .setSaleItems(CollectionsKt.listOf(new SaleItem(
                                            SaleItemType.PURCHASE,
                                            "Demo Item",
                                            amountValue,
                                            null,
                                            BigDecimal.ONE,
                                            TaxUtils.calculateTaxAmount(amountValue, new BigDecimal(19), true, FRACTION_DIGITS),
                                            amountValue,
                                            null,
                                            null,
                                            null,
                                            null
                                    )))
                                    .setCashierId(EposSdkApplication.getCashRegisterId())
                                    .build()
                            )
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnEvent((sale, throwable) -> loadingFinished())
                            .subscribe(saleId -> {
                                amountValue = BigDecimal.ZERO;
                                refreshAmount();
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .addToBackStack("receipt")
                                            .replace(R.id.contentFrameLayout, ReceiptFragment.newInstanceAfterSale(saleId))
                                            .commit();
                                }
                            }, showErrorInsteadContent())
            );
        });
    }

    @Override
    protected void showLoading() {
        super.showLoading();
        cashButton.setEnabled(false);
        cardButton.setEnabled(false);
    }

    @Override
    protected void loadingFinished() {
        super.loadingFinished();
        cashButton.setEnabled(true);
        cardButton.setEnabled(true);
    }

    private class CalcListener implements View.OnClickListener {

        private int change;

        private CalcListener(int change) {
            this.change = change;
        }

        @Override
        public void onClick(View v) {
            if (change == -2)
                removeDigit();
            else if (change == -1) {
                addDigit(0);
                addDigit(0);
            }
            else if (change >= 0)
                addDigit(change);
            refreshAmount();
        }
    }

    private void refreshAmount() {
        if (error != null && error.getVisibility() == View.VISIBLE)
            error.setVisibility(View.GONE);
        amountTextView.setText(nf.format(amountValue));
    }

    public void addDigit(int digit) {
        String current = amountValue.scaleByPowerOfTen(FRACTION_DIGITS).toPlainString();
        amountValue = new BigDecimal(current + digit).scaleByPowerOfTen(-FRACTION_DIGITS);
    }

    public void removeDigit() {
        if (amountValue.precision() > 1) {
            String current = amountValue.scaleByPowerOfTen(FRACTION_DIGITS).toPlainString();
            amountValue = new BigDecimal(current.substring(0, current.length() - 1)).scaleByPowerOfTen(-FRACTION_DIGITS);
        }
        else
            amountValue = new BigDecimal(0);
    }


}
