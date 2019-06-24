package de.wirecard.eposdemo;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.wirecard.epos.model.sale.builder.ReferenceCaptureRequest;
import de.wirecard.epos.model.sale.builder.ReferencePurchaseRequest;
import de.wirecard.epos.model.sale.builder.ReturnRequest;
import de.wirecard.epos.model.sale.builder.payment.ARefundPayment;
import de.wirecard.epos.model.sale.builder.payment.CardCapturePayment;
import de.wirecard.epos.model.sale.builder.payment.CardReferenceRefundPayment;
import de.wirecard.epos.model.sale.builder.payment.CashPurchasePayment;
import de.wirecard.epos.model.sale.builder.payment.CashRefundPayment;
import de.wirecard.epos.model.sale.sales.Sale;
import de.wirecard.epos.model.sale.sales.SaleStatus;
import de.wirecard.epos.model.sale.sales.SaleType;
import de.wirecard.epos.model.sale.sales.payment.CardPayment;
import de.wirecard.epos.model.sale.sales.payment.Payment;
import de.wirecard.eposdemo.adapter.payments.PaymentsAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;


public class SaleDetailFragment extends AbsFragment<View> implements PaymentsAdapter.OnPaymentActionListener {

    private TextView saleType, saleStatus, saleAmount, saleDateTime;
    private LinearLayout saleActionButtons;
    private RecyclerView paymentsRecyclerView;
    private Group saleActionsGroup, paymentsGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saleType = view.findViewById(R.id.paymentType);
        saleStatus = view.findViewById(R.id.saleStatus);
        saleAmount = view.findViewById(R.id.paymentAmount);
        saleDateTime = view.findViewById(R.id.paymentDateTime);
        saleActionButtons = view.findViewById(R.id.paymentActionButtons);
        saleActionsGroup = view.findViewById(R.id.saleActionsGroup);
        paymentsGroup = view.findViewById(R.id.paymentsGroup);
        paymentsRecyclerView = view.findViewById(R.id.paymentsRecyclerView);

        Bundle arguments = getArguments();
        Sale sale = null;
        if (arguments != null) {
            sale = arguments.getParcelable(MainActivity.SALE);
        }

        if (sale != null) {
            initSaleHolder(sale);
            initPaymentsHolder(sale);
        }
        else
            showError("Missing sale");

        loadingFinished();

    }

    private void initSaleHolder(Sale sale) {
        saleType.setText(String.format("%s SALE", sale.getType().name()));
        String saleStatusName = sale.getStatus().toString();
        saleStatus.setText(saleStatusName);
        saleAmount.setText(nf.format(sale.getTotalAmount()));
        saleDateTime.setText(sale.getInitialized().format(formatter));
        List<SaleAction> availableSaleActions = getAvailableSaleActions(sale);
        saleActionsGroup.setVisibility(availableSaleActions.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        for (SaleAction action : availableSaleActions) {
            Button button = new Button(requireContext());
            ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)));
            button.setTextColor(Color.WHITE);
            button.setText(action.getReadableName());
            button.setOnClickListener(v -> onSaleAction(action, sale));
            saleActionButtons.addView(button);
        }

        if ("COMPLETED".equals(saleStatusName))
            saleStatus.setTextColor(MainActivity.GREEN);
        if ("FAILED".equals(saleStatusName))
            saleStatus.setTextColor(MainActivity.RED);
        if ("RETURNED".equals(saleStatusName))
            saleStatus.setTextColor(MainActivity.BLUE);
        if ("UNCONFIRMED".equals(saleStatusName))
            saleStatus.setTextColor(MainActivity.YELLOW);
    }

    private void initPaymentsHolder(Sale sale) {
        List<Payment> payments = sale.getPayments();
        paymentsGroup.setVisibility((payments == null || payments.isEmpty()) ? View.INVISIBLE : View.VISIBLE);

        PaymentsAdapter paymentsAdapter = new PaymentsAdapter(requireContext(), sale, this, nf, formatter);
        paymentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        paymentsRecyclerView.setAdapter(paymentsAdapter);
    }

    private List<SaleAction> getAvailableSaleActions(Sale sale) {
        List<SaleAction> actions = new ArrayList<>();
        actions.add(SaleAction.SHOW_RECEIPT);
        if (sale != null) {
            if (sale.getMultitender() != null && sale.getMultitender() && sale.getStatus() == SaleStatus.UNCONFIRMED) {
                actions.add(SaleAction.REFERENCE_PURCHASE);
                actions.add(SaleAction.MODIFY);
            }
            if ((sale.getStatus() == SaleStatus.COMPLETED || sale.getStatus() == SaleStatus.PARTIALLY_RETURNED) &&
                    sale.getType() == SaleType.PURCHASE &&
                    sale.getPayments() != null && sale.getPayments().size() == 1)
                actions.add(SaleAction.REFUND);
        }
        return actions;
    }

    private void onSaleAction(SaleAction action, Sale sale) {
        switch (action) {
            case REFUND:
                doRefund(sale);
                break;
            case SHOW_RECEIPT:
                showReceipt(sale);
                break;
            case REFERENCE_PURCHASE:
                doReferencePurchase(sale);
                break;
            case MODIFY:
                doModifySale(sale);
                break;
            default:
                showError("Unknown sale action");
                break;
        }
    }

    @Override
    public void onPaymentAction(SaleDetailFragment.PaymentAction action, Payment payment, Sale sale) {
        switch (action) {
            case CAPTURE:
                doCapture(sale, payment);
                break;
            default:
                showError("Unknown payment action");
                break;
        }

    }

    private void doCapture(Sale sale, Payment payment) {
        showLoading();
        CardCapturePayment cardCapturePayment = new CardCapturePayment(payment.getAmount(), payment.getId());
        ReferenceCaptureRequest referenceCaptureRequest = new ReferenceCaptureRequest(sale.getId(), cardCapturePayment, null);
        addDisposable(EposSdkApplication.getEposSdk()
                .sales()
                .operation()
                .referencePurchase(referenceCaptureRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sale1 -> {
                    Toast.makeText(getContext(), "Sale reference purchase completed", Toast.LENGTH_SHORT).show();
                    loadSales();
                }, showErrorInsteadContent())
        );
    }

    private void doReferencePurchase(Sale sale) {
        showLoading();
        BigDecimal outstandingAmount = sale.getOutstandingAmount();
        CashPurchasePayment cashPurchasePayment = new CashPurchasePayment(outstandingAmount);
        ReferencePurchaseRequest referencePurchaseRequest = new ReferencePurchaseRequest(sale.getId(), cashPurchasePayment, "demo reference purchase note");
        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .operation()
                        .referencePurchase(referencePurchaseRequest)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sale1 -> {
                            Toast.makeText(getContext(), "Sale reference purchase completed", Toast.LENGTH_SHORT).show();
                            loadSales();
                        }, showErrorInsteadContent())
        );
    }

    private void doRefund(Sale sale) {
        showLoading();
        ARefundPayment refundPayment;
        if (sale.getPayments() != null && sale.getPayments().get(0) instanceof CardPayment)
            refundPayment = new CardReferenceRefundPayment(sale.getTotalAmount(), sale.getPayments().get(0).getId());
        else
            refundPayment = new CashRefundPayment(sale.getTotalAmount());

        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .operation()
                        .returnn(
                                new ReturnRequest(
                                        sale.getOriginalSaleId() != null ? sale.getOriginalSaleId() : sale.getId(),
                                        refundPayment,
                                        sale.getTotalAmount(),
                                        CURRENCY.getCurrencyCode(),
                                        null,
                                        "saleNote",
                                        true,
                                        Settings.getShopID(getActivity()),
                                        null,
                                        Settings.getCashRegisterId(getContext()),
                                        sale.getItems(),
                                        true
                                )
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sale1 -> {
                            Toast.makeText(getContext(), "Return successful", Toast.LENGTH_SHORT).show();
                            loadSales();
                        }, showErrorInsteadContent())
        );
    }

    private void doModifySale(Sale sale) {
        ModifySaleFragment fragment = new ModifySaleFragment();
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SALE, sale);
        fragment.setArguments(args);
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeScreenWithBack(fragment, fragment.getClass().getSimpleName());
        }
    }

    private void showReceipt(Sale sale) {
        ReceiptFragment receiptFragment = ReceiptFragment.newInstance(sale.getId());
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeScreenWithBack(receiptFragment, receiptFragment.getClass().getSimpleName());
        }
    }

    private void loadSales() {
        SalesFragment fragment = new SalesFragment();
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeScreen(fragment, fragment.getClass().getSimpleName());
        }
    }

    public enum PaymentAction {
        CAPTURE("Capture");

        private String readableName;

        PaymentAction(String readableName) {
            this.readableName = readableName;
        }

        public String getReadableName() {
            return readableName;
        }

    }

    public enum SaleAction {
        SHOW_RECEIPT("Show receipt"),
        REFERENCE_PURCHASE("Reference purchase"),
        REFUND("Refund"),
        MODIFY("Modify");

        private String readableName;

        SaleAction(String readableName) {
            this.readableName = readableName;
        }

        public String getReadableName() {
            return readableName;
        }
    }
}
