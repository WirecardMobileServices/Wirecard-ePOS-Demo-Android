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

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;

import de.wirecard.epos.EposSDK;
import de.wirecard.epos.model.sale.builder.SaleBuilder;
import de.wirecard.epos.model.sale.sales.SaleItem;
import de.wirecard.epos.model.sale.sales.SaleItemType;
import de.wirecard.epos.util.TaxUtils;
import de.wirecard.epos.util.events.Event;
import de.wirecard.epos.util.events.TerminalEvent;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class PaymentFragment extends AbsFragment<View> {

    private BigDecimal amountValue;

    private TextView amountTextView, update;
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
        update = view.findViewById(R.id.update);

        refreshAmount();

        cashButton = view.findViewById(R.id.cash);
        cardButton = view.findViewById(R.id.card);

        Relay<Event> eventRelay = BehaviorRelay.create();

        addDisposable(eventRelay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof TerminalEvent) {
                        if (event instanceof TerminalEvent.SignatureRequest) {
                            //I'll just send any image encoded in base64
                            ((TerminalEvent.SignatureRequest) event).signatureEntered(signatureImageInBase64.getBytes());
                        }
                        else if (event instanceof TerminalEvent.AppSignatureConfirmation) {
                            //I'll confirm signature
                            ((TerminalEvent.AppSignatureConfirmation) event).signatureConfirmed();
                        }
                        else if (event instanceof TerminalEvent.SignatureConfirmation) {
                            //do nothing, terminal shows confirmation of signature
                            update.setText("signature confirmation on terminal side");
                        }
                        else if (event instanceof Event.Update) {
                            update.setText(((Event.Update) event).getMessage(getContext()));
                        }
                        else if (event instanceof TerminalEvent.PaymentInfo) {
                            update.setText(((TerminalEvent.PaymentInfo) event).getPanTag());
                        }
                        else {
                            update.setText("Unknown event: " + event.toString());
                        }
                    }
                    else if (event instanceof Event.Update) {
                        update.setText(((Event.Update) event).getMessage(getContext()));
                    }
                    else if (event instanceof Event.PasswordConfirmation) {
                        update.setText("Waiting for password confirmation");    //only wechat
                    }
                    else {
                        update.setText("Unknown event: " + event.toString());
                    }
                }, showErrorInsteadContent()));

        cashButton.setOnClickListener(v -> doPayment(true, eventRelay));
        cardButton.setOnClickListener(v -> doPayment(false, eventRelay));
    }

    private void doPayment(boolean isCash, Relay<Event> eventRelay) {
        showLoading();
        final EposSDK eposSdk = EposSdkApplication.getEposSdk();

        update.setVisibility(View.VISIBLE);

        final SaleBuilder.PaymentMethodStep paymentMethodStep = SaleBuilder.newBuilder()
                .setAmount(amountValue)
                .setCurrency(CURRENCY)
                .unitPricesIncludeTax();
        final SaleBuilder.OptionalStep optionalStep;
        if (isCash)
            optionalStep = paymentMethodStep.addCashPayment(amountValue);
        else
            optionalStep = paymentMethodStep.addCardPayment(amountValue);

        if (Settings.isCashRegisterRequired())
            if (Settings.getCashRegisterId(getContext()) == null) {
                Toast.makeText(getContext(), R.string.cash_register_error, Toast.LENGTH_LONG).show();
                loadingFinished();
                return;
            }else
                optionalStep.setCashRegisterId(Settings.getCashRegisterId(getContext()));

        final SaleBuilder saleBuilder = optionalStep.setSaleItems(Collections.singletonList(new SaleItem(
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
                .build();

        addDisposable(
                eposSdk.sales()
                        .pay(saleBuilder)
                        .subscribeParallel(eventRelay)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnEvent((sale, throwable) -> loadingFinished())
                        .subscribe(saleId -> {
                            update.setVisibility(View.GONE);
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
        update.setVisibility(View.GONE);
    }

    @Override
    protected void showError(@Nullable String message) {
        super.showError(message);
        update.setVisibility(View.GONE);
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


    private String signatureImageInBase64 = "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";

}
