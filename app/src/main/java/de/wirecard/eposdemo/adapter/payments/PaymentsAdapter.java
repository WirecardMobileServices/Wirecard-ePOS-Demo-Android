package de.wirecard.eposdemo.adapter.payments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.threeten.bp.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import de.wirecard.epos.model.sale.sales.Sale;
import de.wirecard.epos.model.sale.sales.SaleStatus;
import de.wirecard.epos.model.sale.sales.payment.AuthorizationPayment;
import de.wirecard.epos.model.sale.sales.payment.Payment;
import de.wirecard.epos.model.sale.sales.payment.PaymentStatus;
import de.wirecard.eposdemo.R;
import de.wirecard.eposdemo.SaleDetailFragment;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentViewHolder> {

    public interface OnPaymentActionListener {
        void onPaymentAction(SaleDetailFragment.PaymentAction action, Payment payment, Sale sale);
    }

    private List<Payment> data;
    private OnPaymentActionListener listener;
    private Sale sale;
    private NumberFormat nf;
    private DateTimeFormatter formatter;
    private Context context;

    public PaymentsAdapter(Context context, Sale sale, OnPaymentActionListener listener, NumberFormat nf, DateTimeFormatter formatter) {
        this.context = context;
        this.data = sale.getPayments() != null ? sale.getPayments() : new ArrayList<>();
        this.listener = listener;
        this.sale = sale;
        this.nf = nf;
        this.formatter = formatter;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sale_payment_row, viewGroup, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder paymentViewHolder, int i) {
        Payment payment = data.get(i);
        paymentViewHolder.paymentType.setText(payment.getClass().getSimpleName());
        paymentViewHolder.paymentStatus.setText(payment.getStatus().name());
        paymentViewHolder.paymentAmount.setText(nf.format(payment.getAmount()));
        paymentViewHolder.paymentDateTime.setText(payment.getInitialized().format(formatter));

        List<SaleDetailFragment.PaymentAction> availablePaymentActions = getAvailablePaymentActions(sale, payment);
        paymentViewHolder.paymentActionsGroup.setVisibility(availablePaymentActions.isEmpty() ? View.INVISIBLE : View.VISIBLE);

        for (SaleDetailFragment.PaymentAction action : availablePaymentActions) {
            Button button = new Button(context);
            button.setText(action.getReadableName());
            ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimaryDark)));
            button.setTextColor(Color.WHITE);
            button.setOnClickListener(v -> listener.onPaymentAction(action, payment, sale));
            paymentViewHolder.paymentActionButtons.addView(button);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private List<SaleDetailFragment.PaymentAction> getAvailablePaymentActions(Sale sale, Payment payment) {
        List<SaleDetailFragment.PaymentAction> actions = new ArrayList<>();
        if (sale != null &&
                sale.getMultitender() != null && sale.getMultitender() &&
                sale.getStatus() == SaleStatus.UNCONFIRMED &&
                payment instanceof AuthorizationPayment &&
                payment.getStatus() == PaymentStatus.COMPLETED)
            actions.add(SaleDetailFragment.PaymentAction.CAPTURE);
        return actions;
    }

    class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView paymentType;
        TextView paymentStatus;
        TextView paymentAmount;
        TextView paymentDateTime;
        LinearLayout paymentActionButtons;
        Group paymentActionsGroup;

        PaymentViewHolder(View itemView) {
            super(itemView);
            paymentType = itemView.findViewById(R.id.paymentType);
            paymentStatus = itemView.findViewById(R.id.paymentStatus);
            paymentAmount = itemView.findViewById(R.id.paymentAmount);
            paymentDateTime = itemView.findViewById(R.id.paymentDateTime);
            paymentActionButtons = itemView.findViewById(R.id.paymentActionButtons);
            paymentActionsGroup = itemView.findViewById(R.id.paymentActionsGroup);
        }

    }

}
