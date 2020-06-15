package de.wirecard.eposdemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class PaymentFragment extends AbsFragment<View> {

    private BigDecimal amountValue;

    private TextView amountTextView;
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

        amountTextView = view.findViewById(R.id.amount);
        Button nextButton = view.findViewById(R.id.next);

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

        nextButton.setOnClickListener(v -> {
            PaymentMethodFragment paymentMethodFragment = new PaymentMethodFragment();
            Bundle args = new Bundle();
            args.putString(MainActivity.AMOUNT, amountValue.toString());
            paymentMethodFragment.setArguments(args);
            FragmentActivity activity = getActivity();
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).changeScreenWithBack(paymentMethodFragment, paymentMethodFragment.getClass().getSimpleName());
                amountValue = BigDecimal.ZERO;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAmount();
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
