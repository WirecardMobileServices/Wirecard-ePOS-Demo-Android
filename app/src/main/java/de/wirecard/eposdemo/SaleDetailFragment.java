package de.wirecard.eposdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.wirecard.epos.model.sale.builder.CancelRequest;
import de.wirecard.epos.model.sale.builder.FailRequest;
import de.wirecard.epos.model.sale.builder.ModifyPurchaseRequest;
import de.wirecard.epos.model.sale.builder.ReferenceCaptureRequest;
import de.wirecard.epos.model.sale.builder.ReferencePurchaseRequest;
import de.wirecard.epos.model.sale.builder.ReferenceTerminalPreAuthorizationSupplementRequest;
import de.wirecard.epos.model.sale.builder.RefundRequest;
import de.wirecard.epos.model.sale.builder.ReturnRequest;
import de.wirecard.epos.model.sale.builder.ReverseRequest;
import de.wirecard.epos.model.sale.builder.payment.ARefundPayment;
import de.wirecard.epos.model.sale.builder.payment.CardCapturePayment;
import de.wirecard.epos.model.sale.builder.payment.CardReferenceRefundPayment;
import de.wirecard.epos.model.sale.builder.payment.CardReversePayment;
import de.wirecard.epos.model.sale.builder.payment.CardTerminalPreAuthorizationSupplementPayment;
import de.wirecard.epos.model.sale.builder.payment.CashPurchasePayment;
import de.wirecard.epos.model.sale.builder.payment.CashRefundPayment;
import de.wirecard.epos.model.sale.builder.payment.EftCardReferenceRefundPayment;
import de.wirecard.epos.model.sale.builder.payment.EftCardReversePayment;
import de.wirecard.epos.model.sale.builder.payment.ReversePayment;
import de.wirecard.epos.model.sale.builder.payment.TerminalPreAuthorizationSupplementPayment;
import de.wirecard.epos.model.sale.sales.Sale;
import de.wirecard.epos.model.sale.sales.SaleItem;
import de.wirecard.epos.model.sale.sales.SaleItemType;
import de.wirecard.epos.model.sale.sales.SaleStatus;
import de.wirecard.epos.model.sale.sales.SaleType;
import de.wirecard.epos.model.sale.sales.payment.AlipayAutoResolvePayment;
import de.wirecard.epos.model.sale.sales.payment.CapturePayment;
import de.wirecard.epos.model.sale.sales.payment.CardAutoResolvePayment;
import de.wirecard.epos.model.sale.sales.payment.CardPayment;
import de.wirecard.epos.model.sale.sales.payment.CashPayment;
import de.wirecard.epos.model.sale.sales.payment.CashReversePayment;
import de.wirecard.epos.model.sale.sales.payment.EftCardAutoResolvePayment;
import de.wirecard.epos.model.sale.sales.payment.EftCardPayment;
import de.wirecard.epos.model.sale.sales.payment.Payment;
import de.wirecard.epos.model.sale.sales.payment.PaymentStatus;
import de.wirecard.epos.model.sale.sales.payment.PurchasePayment;
import de.wirecard.epos.model.sale.sales.payment.TerminalAuthorizationPayment;
import de.wirecard.epos.model.sale.sales.payment.TerminalPreAuthorizationPayment;
import de.wirecard.epos.model.sale.sales.payment.WechatAutoResolvePayment;
import de.wirecard.epos.util.TaxUtils;
import de.wirecard.epos.util.TextUtils;
import de.wirecard.eposdemo.adapter.payments.PaymentsAdapter;
import de.wirecard.eposdemo.utils.DesignUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class SaleDetailFragment extends AbsFragment<View> implements PaymentsAdapter.OnPaymentActionListener {

    private TextView saleType, saleStatus, saleAmount, saleDateTime;
    private RecyclerView paymentsRecyclerView;
    private Group saleActionsGroup, paymentsGroup;
    private Spinner saleActionsSpinner;
    private Button saleActionButton;

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
        saleActionsSpinner = view.findViewById(R.id.saleActionsSpinner);
        saleActionButton = view.findViewById(R.id.saleActionButton);
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
        ArrayAdapter<SaleAction> saleActionsAdapter = new ArrayAdapter<>(requireContext(), R.layout.simple_spinner_item, availableSaleActions);
        saleActionsSpinner.setAdapter(saleActionsAdapter);
        saleActionButton.setOnClickListener(v -> {
            SaleAction saleAction = saleActionsAdapter.getItem(saleActionsSpinner.getSelectedItemPosition());
            if (saleAction != null)
                onSaleAction(saleAction, sale);
        });
        int saleStatusColor = DesignUtils.getSaleStatusColor(saleStatusName);
        saleStatus.setTextColor(saleStatusColor);
    }

    private void initPaymentsHolder(Sale sale) {
        List<Payment> payments = sale.getPayments();
        paymentsGroup.setVisibility((payments == null || payments.isEmpty()) ? View.INVISIBLE : View.VISIBLE);

        PaymentsAdapter paymentsAdapter = new PaymentsAdapter(requireContext(), sale, this, nf, formatter);
        paymentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        paymentsRecyclerView.setAdapter(paymentsAdapter);
    }

    /**
     * Rules in following method are very basic, not mandatory and used only for demo purpose.
     *
     * @param sale
     * @return list of sale available actions
     */
    private List<SaleAction> getAvailableSaleActions(Sale sale) {
        List<SaleAction> actions = new ArrayList<>();
        actions.add(SaleAction.SHOW_RECEIPT);
        if (sale != null && sale.getType() == SaleType.PURCHASE && sale.getMultitender() != null && sale.getMultitender()) {
            if (sale.getStatus() == SaleStatus.UNCONFIRMED) {
                actions.add(SaleAction.REFERENCE_PURCHASE);
                actions.add(SaleAction.MODIFY_SALE);
            }
            if ((sale.getStatus() == SaleStatus.COMPLETED || sale.getStatus() == SaleStatus.PARTIALLY_RETURNED)
                    && sale.getPayments() != null && sale.getPayments().size() == 1)
                actions.add(SaleAction.RETURN);

            int cashPurchaseCompletedReversed = Stream.of(sale.getPayments())
                    .filter(payment -> payment instanceof de.wirecard.epos.model.sale.sales.payment.CashPurchasePayment
                            && (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.REVERSED))
                    .collect(Collectors.toList())
                    .size();

            int cashReverseCompleted = Stream.of(sale.getPayments())
                    .filter(payment -> payment instanceof CashReversePayment && payment.getStatus() == PaymentStatus.COMPLETED)
                    .collect(Collectors.toList())
                    .size();

            List<Payment> allReferencedPayments = Stream.of(sale.getPayments())
                    .filter(payment ->
                            !(payment instanceof AlipayAutoResolvePayment
                                    || payment instanceof WechatAutoResolvePayment
                                    || payment instanceof CardAutoResolvePayment
                                    || payment instanceof EftCardAutoResolvePayment)
                    )
                    .filter(payment ->
                            !(payment instanceof CashPayment)
                                    && (payment instanceof PurchasePayment
                                    || payment instanceof CapturePayment
                                    || payment instanceof TerminalAuthorizationPayment
                                    || payment instanceof TerminalPreAuthorizationPayment
                                    || payment instanceof TerminalPreAuthorizationSupplementPayment)
                    ).collect(Collectors.toList());

            boolean allReferencedPaymentsReversed = Stream.of(allReferencedPayments)
                    .allMatch(payment -> payment.getStatus() == PaymentStatus.REVERSED);


            boolean allReferencedPaymentsReversedRefundedFailedRejected = Stream.of(allReferencedPayments)
                    .allMatch(payment -> payment.getStatus() == PaymentStatus.REVERSED || payment.getStatus() == PaymentStatus.REFUNDED
                            || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.FAILED_INTERVENE
                            || payment.getStatus() == PaymentStatus.REJECTED || payment.getStatus() == PaymentStatus.CAPTURED);

            if (sale.getStatus() == SaleStatus.UNCONFIRMED && sale.getPayments() != null
                    && (cashPurchaseCompletedReversed == cashReverseCompleted)
                    && allReferencedPaymentsReversed)
                actions.add(SaleAction.CLOSE_SALE_AS_CANCELED);

            if (sale.getStatus() == SaleStatus.UNCONFIRMED && sale.getPayments() != null
                    && (cashPurchaseCompletedReversed == cashReverseCompleted)
                    && allReferencedPaymentsReversedRefundedFailedRejected)
                actions.add(SaleAction.CLOSE_SALE_AS_FAILED);

            List<Payment> preAuthPayments =
                    Stream.of(sale.getPayments()).filter(p -> p instanceof TerminalPreAuthorizationPayment && p.getStatus() == PaymentStatus.COMPLETED).collect(Collectors.toList());
            if (sale.getStatus() == SaleStatus.UNCONFIRMED && preAuthPayments.size() == 1)
                actions.add(SaleAction.SUPPLEMENT);
        }
        return actions;
    }

    private void onSaleAction(SaleAction action, Sale sale) {
        switch (action) {
            case RETURN:
                showSaleActionParamsDialog(sale, SaleAction.RETURN);
                break;
            case SHOW_RECEIPT:
                showSaleReceipt(sale);
                break;
            case REFERENCE_PURCHASE:
                showSaleActionParamsDialog(sale, SaleAction.REFERENCE_PURCHASE);
                break;
            case MODIFY_SALE:
                showSaleActionParamsDialog(sale, SaleAction.MODIFY_SALE);
                break;
            case CLOSE_SALE_AS_CANCELED:
                closeSaleAsCanceled(sale);
                break;
            case CLOSE_SALE_AS_FAILED:
                closeSaleAsFailed(sale);
                break;
            case SUPPLEMENT:
                showSaleActionParamsDialog(sale, SaleAction.SUPPLEMENT);
                break;
            default:
                Toast.makeText(getContext(), "Not implemented sale action " + action.readableName, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPaymentAction(SaleDetailFragment.PaymentAction action, Payment payment, Sale sale) {
        switch (action) {
            case CAPTURE:
                showPaymentActionParamDialog(sale, payment, PaymentAction.CAPTURE);
                break;
            case REVERSE:
                doPaymentReverse(sale, payment);
                break;
            case REFUND:
                showPaymentActionParamDialog(sale, payment, PaymentAction.REFUND);
                break;
            default:
                Toast.makeText(getContext(), "Not implemented payment action " + action.readableName, Toast.LENGTH_SHORT).show();
                break;
        }

    }

    private void doPaymentCapture(Sale sale, Payment payment, BigDecimal captureAmount) {
        showLoading();
        CardCapturePayment cardCapturePayment = new CardCapturePayment(captureAmount, payment.getId());
        ReferenceCaptureRequest referenceCaptureRequest = new ReferenceCaptureRequest(sale.getId(), cardCapturePayment);
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

    private void doPaymentReverse(Sale sale, Payment payment) {
        ReversePayment reversePayment = null;
        if (payment instanceof CashPayment)
            reversePayment = new de.wirecard.epos.model.sale.builder.payment.CashReversePayment(payment.getId());
        else if (payment instanceof CardPayment)
            reversePayment = new CardReversePayment(payment.getId());
        else if (payment instanceof EftCardPayment)
            reversePayment = new EftCardReversePayment(payment.getId());

        if (reversePayment == null) {
            Toast.makeText(getContext(), "Reverse payment is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        ReverseRequest reverseRequest = new ReverseRequest(sale.getId(), reversePayment);

        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .operation()
                        .reverse(reverseRequest)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sale1 -> {
                            Toast.makeText(getContext(), "Reverse successful", Toast.LENGTH_SHORT).show();
                            loadSales();
                        }, showErrorInsteadContent())
        );
    }

    private void doPaymentRefund(Sale sale, Payment payment, BigDecimal refundAmount) {
        ARefundPayment refundPayment = null;

        if (payment instanceof CashPayment)
            refundPayment = new CashRefundPayment(refundAmount);
        else if (payment instanceof CardPayment)
            refundPayment = new CardReferenceRefundPayment(refundAmount, payment.getId());
        else if (payment instanceof EftCardPayment)
            refundPayment = new EftCardReferenceRefundPayment(refundAmount, payment.getId());

        if (refundPayment == null) {
            Toast.makeText(getContext(), "Refund payment is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        RefundRequest refundRequest = new RefundRequest(sale.getId(), refundPayment);

        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .operation()
                        .refund(refundRequest)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sale1 -> {
                            Toast.makeText(getContext(), "Reverse successful", Toast.LENGTH_SHORT).show();
                            loadSales();
                        }, showErrorInsteadContent())
        );
    }

    private void doSaleReferencePurchase(Sale sale, BigDecimal referencePurchaseAmount, String note) {
        showLoading();
        CashPurchasePayment cashPurchasePayment = new CashPurchasePayment(referencePurchaseAmount);
        ReferencePurchaseRequest referencePurchaseRequest = new ReferencePurchaseRequest(sale.getId(), cashPurchasePayment, null, note);
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

    private void doSaleReturn(Sale sale, BigDecimal returnAmount, String note) {
        showLoading();
        ARefundPayment refundPayment = null;
        if (sale.getPayments() != null && sale.getPayments().size() == 1) {
            Payment payment = sale.getPayments().get(0);
            if (payment instanceof CardPayment)
                refundPayment = new CardReferenceRefundPayment(returnAmount, payment.getId());
            else if (payment instanceof EftCardPayment)
                refundPayment = new EftCardReferenceRefundPayment(returnAmount, payment.getId());
            else if (payment instanceof CashPayment)
                refundPayment = new CashRefundPayment(returnAmount);
        }

        if (refundPayment == null) {
            Toast.makeText(getContext(), "Return payment is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        ReturnRequest returnRequest = new ReturnRequest(
                sale.getOriginalSaleId() != null ? sale.getOriginalSaleId() : sale.getId(),
                refundPayment,
                returnAmount,
                CURRENCY.getCurrencyCode(),
                null,
                note,
                true,
                Settings.getShopID(getActivity()),
                null,
                Settings.getCashRegisterId(getContext()),
                sale.getItems(),
                true
        );

        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .operation()
                        .returnn(returnRequest)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sale1 -> {
                            Toast.makeText(getContext(), "Return successful", Toast.LENGTH_SHORT).show();
                            loadSales();
                        }, showErrorInsteadContent())
        );
    }

    private void doModifySale(Sale sale, BigDecimal modifySaleAmount, String note) {
        showLoading();

        List<SaleItem> saleItem = Collections.singletonList(new SaleItem(
                SaleItemType.PURCHASE,
                "Demo modify sale item",
                modifySaleAmount,
                null,
                BigDecimal.ONE,
                TaxUtils.calculateTaxAmount(modifySaleAmount, new BigDecimal(19), sale.getUnitPricesIncludeTax(), FRACTION_DIGITS),
                modifySaleAmount,
                null,
                null,
                null,
                null
        ));

        ModifyPurchaseRequest modifyPurchaseRequest = new ModifyPurchaseRequest(
                sale.getId(),
                sale.getVersion(),
                note,
                sale.getCashRegisterId(),
                modifySaleAmount,
                saleItem,
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

    private void doSalePreAuthSupplement(Sale sale, BigDecimal supplementAmount, String note) {
        List<TerminalPreAuthorizationPayment> preAuthPayments = Stream.of(sale.getPayments())
                .filter(p -> p instanceof TerminalPreAuthorizationPayment && p.getStatus() == PaymentStatus.COMPLETED)
                .map(value -> ((TerminalPreAuthorizationPayment) value))
                .collect(Collectors.toList());

        List<de.wirecard.epos.model.sale.sales.payment.TerminalPreAuthorizationSupplementPayment> preAuthSupplementPayments = Stream.of(sale.getPayments())
                .filter(p -> p instanceof de.wirecard.epos.model.sale.sales.payment.TerminalPreAuthorizationSupplementPayment && p.getStatus() == PaymentStatus.COMPLETED)
                .map(value -> ((de.wirecard.epos.model.sale.sales.payment.TerminalPreAuthorizationSupplementPayment) value))
                .filter(p -> p.getInitialized() != null)
                .sorted((p1, p2) -> p1.getInitialized().compareTo(p2.getInitialized()))
                .collect(Collectors.toList());

        TerminalPreAuthorizationSupplementPayment payment = null;
        if (preAuthPayments.size() == 1) {
            Payment lastPaymentInPreAuthChain;
            if (!preAuthSupplementPayments.isEmpty()) {
                lastPaymentInPreAuthChain = preAuthSupplementPayments.get(preAuthSupplementPayments.size() - 1);
            }
            else {
                lastPaymentInPreAuthChain = preAuthPayments.get(0);
            }
            if (lastPaymentInPreAuthChain instanceof CardPayment) {
                payment = new CardTerminalPreAuthorizationSupplementPayment(
                        supplementAmount,
                        lastPaymentInPreAuthChain.getId(),
                        ((CardPayment) lastPaymentInPreAuthChain).getAuthorizationCode());
            }

            if (payment == null) {
                Toast.makeText(getContext(), "Supplement payment is null.", Toast.LENGTH_SHORT).show();
                return;
            }

            ReferenceTerminalPreAuthorizationSupplementRequest supplementRequest = new ReferenceTerminalPreAuthorizationSupplementRequest(sale.getId(), payment, null, note, null);
            showLoading();
            addDisposable(EposSdkApplication.getEposSdk()
                    .sales()
                    .operation()
                    .referencePurchase(supplementRequest)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(sale1 -> {
                        Toast.makeText(getContext(), "Terminal PreAuthorization Supplement completed", Toast.LENGTH_SHORT).show();
                        loadSales();
                    }, showErrorInsteadContent()));
        }
    }

    private void showSaleReceipt(Sale sale) {
        ReceiptFragment receiptFragment = ReceiptFragment.newInstance(sale.getId());
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeScreenWithBack(receiptFragment, receiptFragment.getClass().getSimpleName());
        }
    }

    private void closeSaleAsCanceled(Sale sale) {
        showLoading();
        CancelRequest cancelRequest = new CancelRequest(sale.getId());
        addDisposable(EposSdkApplication.getEposSdk()
                .sales()
                .operation()
                .cancel(cancelRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sale1 -> {
                    Toast.makeText(getContext(), "Close sale as canceled completed", Toast.LENGTH_SHORT).show();
                    loadSales();
                }, showErrorInsteadContent())
        );
    }

    private void closeSaleAsFailed(Sale sale) {
        showLoading();
        FailRequest failRequest = new FailRequest(sale.getId());
        addDisposable(EposSdkApplication.getEposSdk()
                .sales()
                .operation()
                .fail(failRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sale1 -> {
                    Toast.makeText(getContext(), "Close sale as failed completed", Toast.LENGTH_SHORT).show();
                    loadSales();
                }, showErrorInsteadContent())
        );
    }

    private void loadSales() {
        SalesFragment fragment = new SalesFragment();
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeScreen(fragment, fragment.getClass().getSimpleName());
        }
    }

    interface Action {
    }

    public enum PaymentAction implements Action {
        CAPTURE("Capture"),
        REVERSE("Reverse"),
        REFUND("Refund");

        private String readableName;

        PaymentAction(String readableName) {
            this.readableName = readableName;
        }

        @Override
        public String toString() {
            return readableName;
        }

    }

    public enum SaleAction implements Action {
        SHOW_RECEIPT("Show receipt"),
        REFERENCE_PURCHASE("Reference purchase"),
        RETURN("Return"),
        MODIFY_SALE("Modify sale"),
        SUPPLEMENT("Supplement"),
        CLOSE_SALE_AS_FAILED("Close sale as failed"),
        CLOSE_SALE_AS_CANCELED("Close sale as canceled");

        private String readableName;

        SaleAction(String readableName) {
            this.readableName = readableName;
        }

        @Override
        public String toString() {
            return readableName;
        }

    }

    public interface DialogResultListener {
        void onDialogPositiveButton(List<String> dialogFieldsValues);
    }

    private Dialog createDialogForAction(Action action, List<String> inputFields, DialogResultListener listener) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(lp);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        for (String field : inputFields) {
            EditText editText = new EditText(requireContext());
            editText.setLayoutParams(lp);
            editText.setHint(field);
            if (field.toLowerCase().contains("amount"))
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            linearLayout.addView(editText);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(linearLayout)
                .setTitle("Please enter " + action.toString().toLowerCase() + " parameters")
                .setPositiveButton("Perform " + action.toString(), (dialog, which) -> {
                    ArrayList<String> fieldValues = new ArrayList<>();
                    for (int i = 0; i < linearLayout.getChildCount(); i++) {
                        View childView = linearLayout.getChildAt(i);
                        if (childView instanceof EditText) {
                            fieldValues.add(((EditText) childView).getText().toString());
                        }
                    }
                    listener.onDialogPositiveButton(fieldValues);
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void showSaleActionParamsDialog(Sale sale, SaleAction action) {
        createDialogForAction(action, Arrays.asList(action.toString() + " amount", action.toString() + " note"), dialogFieldsValues -> {
            String amountString = dialogFieldsValues.get(0);
            String note = dialogFieldsValues.get(1);
            note = TextUtils.isEmpty(note) ? ("demo " + action.toString() + " note") : note;
            BigDecimal amount = parseAmount(amountString);
            if (amount != null) {
                switch (action) {
                    case REFERENCE_PURCHASE:
                        doSaleReferencePurchase(sale, amount, note);
                        break;
                    case MODIFY_SALE:
                        doModifySale(sale, amount, note);
                        break;
                    case SUPPLEMENT:
                        doSalePreAuthSupplement(sale, amount, note);
                        break;
                    case RETURN:
                        doSaleReturn(sale, amount, note);
                    default:
                        Toast.makeText(getContext(), "Dialog params not implemented for sale action " + action.readableName, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }).show();
    }

    private void showPaymentActionParamDialog(Sale sale, Payment payment, PaymentAction action) {
        createDialogForAction(action, Collections.singletonList(action.toString() + " amount"), dialogFieldsValues -> {
            String amountString = dialogFieldsValues.get(0);
            BigDecimal amount = parseAmount(amountString);
            if (amount != null) {
                switch (action) {
                    case REFUND:
                        doPaymentRefund(sale, payment, amount);
                        break;
                    case CAPTURE:
                        doPaymentCapture(sale, payment, amount);
                        break;
                    default:
                        Toast.makeText(getContext(), "Dialog params not implemented for payment action " + action.readableName, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }).show();
    }

    private BigDecimal parseAmount(String amountString) {
        try {
            return new BigDecimal(amountString.replace(',', '.')).setScale(CURRENCY.getDefaultFractionDigits(), RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            Toast.makeText(getContext(), "Entered amount has wrong format.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

}
