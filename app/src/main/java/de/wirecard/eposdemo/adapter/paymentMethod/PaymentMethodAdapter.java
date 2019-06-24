package de.wirecard.eposdemo.adapter.paymentMethod;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import de.wirecard.eposdemo.AppPayment;
import de.wirecard.eposdemo.R;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {

    private List<AppPayment> data;
    private OnPaymentMethodClickListener listener;

    public PaymentMethodAdapter(List<AppPayment> supportedPaymentMethods, OnPaymentMethodClickListener listener) {
        this.data = supportedPaymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.payment_method_row, viewGroup, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder paymentMethodViewHolder, int i) {
        AppPayment appPayment = data.get(i);
        paymentMethodViewHolder.buttonPaymentMethod.setText(appPayment.getReadableName());
        paymentMethodViewHolder.buttonPaymentMethod.setOnClickListener(v -> {
            listener.onPaymentMethodClicked(appPayment);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        Button buttonPaymentMethod;

        PaymentMethodViewHolder(View itemView) {
            super(itemView);
            buttonPaymentMethod = itemView.findViewById(R.id.buttonPaymentMethod);
        }

    }

}
