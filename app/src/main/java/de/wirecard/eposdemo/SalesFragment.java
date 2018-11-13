package de.wirecard.eposdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import de.wirecard.epos.model.sale.request.payment.cash.CashReturnPayment;
import de.wirecard.epos.model.sale.sales.Sale;
import de.wirecard.epos.model.sale.sales.SaleItem;
import de.wirecard.epos.model.sale.sales.SaleItemType;
import de.wirecard.epos.model.sale.sales.payment.Payment;
import de.wirecard.epos.model.sale.sales.payment.alipay.AlipayPayment;
import de.wirecard.epos.model.sale.sales.payment.card.CardPayment;
import de.wirecard.epos.model.sale.sales.payment.cash.CashPayment;
import de.wirecard.epos.model.sale.sales.payment.wechat.WechatPayment;
import de.wirecard.epos.model.with.With;
import de.wirecard.epos.model.with.WithPagination;
import de.wirecard.epos.util.TaxUtils;
import de.wirecard.eposdemo.adapter.SimpleItem;
import de.wirecard.eposdemo.adapter.SimpleItemRecyclerViewAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class SalesFragment extends AbsFragment<RecyclerView> {

    private List<Sale> saleLights;

    private Button receipt;
    private Button refund;

    public SalesFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        content.setLayoutManager(new LinearLayoutManager(getContext()));

        receipt = view.findViewById(R.id.receipt);
        receipt.setOnClickListener(v -> {
            final int selectedPosition = ((SimpleItemRecyclerViewAdapter) content.getAdapter()).getSelectedPosition();
            if (selectedPosition > RecyclerView.NO_POSITION) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("receipt")
                        .replace(R.id.contentFrameLayout, ReceiptFragment.newInstance(saleLights.get(selectedPosition).getId()))
                        .commit();
            }
            else
                Toast.makeText(getContext(), "You have to select something", Toast.LENGTH_SHORT).show();
        });

        refund = view.findViewById(R.id.refund);
        refund.setOnClickListener(v -> {
            doRefund();
        });

        loadSales();
    }

    private void loadSales() {
        WithPagination withPagination = With.pagination()
                .page(0)
                .size(20)
                .sort("initialized", WithPagination.Order.DESC);

        showLoading();
        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .getSales(withPagination)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(items -> {
                            saleLights = items;
                            final List<SimpleItem> simpleItems = Stream.of(items)
                                    .map(item -> new SimpleItem(
                                                    getCardholderNameOrType(item),
                                                    item.getStatus().toString(),
                                                    nf.format(item.getTotalAmount()),
                                                    item.getInitialized().format(formatter)
                                            )
                                    ).collect(Collectors.toList());
                            loadingFinishedAndShowRecycler(new SimpleItemRecyclerViewAdapter(simpleItems));
                        }, showErrorInsteadContent())
        );
    }

    private String getCardholderNameOrType(Sale sale) {
        final Payment payment = sale.getPayments().get(0);
        if (payment instanceof CardPayment)
            return "Card";
        if (payment instanceof CashPayment)
            return "Cash";
        if (payment instanceof AlipayPayment)
            return "Alipay";
        if (payment instanceof WechatPayment)
            return "Wechat";
        else return payment.getClass().getName();

    }

    private void doRefund() {
        showLoading();
        final int selectedPosition = ((SimpleItemRecyclerViewAdapter) content.getAdapter()).getSelectedPosition();
        if (selectedPosition > RecyclerView.NO_POSITION) {
            final Sale sale = saleLights.get(selectedPosition);
            final List<SaleItem> returnItems = Collections.singletonList(new SaleItem(
                    SaleItemType.PURCHASE,
                    "Demo Item",
                    sale.getTotalAmount(),
                    null,
                    BigDecimal.ONE,
                    TaxUtils.calculateTaxAmount(sale.getTotalAmount(), new BigDecimal(19), true, FRACTION_DIGITS),
                    sale.getTotalAmount(),
                    null,
                    null,
                    null,
                    null
            ));
            addDisposable(
                    EposSdkApplication.getEposSdk()
                            .sales()
                            .saleReturn(
                                    new CashReturnPayment(sale.getTotalAmount(), null),
                                    sale.getOriginalSaleId() != null ? sale.getOriginalSaleId() : sale.getId(),
                                    CURRENCY,
                                    returnItems,
                                    true,
                                    Settings.getCashRegisterId(getContext()),
                                    "saleNote",
                                    Settings.getShopID(getActivity())
                            )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(sale1 -> {
                                Toast.makeText(getContext(), "Return successful", Toast.LENGTH_SHORT).show();
                                loadSales();
                            }, showErrorInsteadContent())
            );
        }
        else
            Toast.makeText(getContext(), "You have to select something", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void showLoading() {
        super.showLoading();
        receipt.setEnabled(false);
        refund.setEnabled(false);
    }

    @Override
    protected void loadingFinished() {
        super.loadingFinished();
        receipt.setEnabled(true);
        refund.setEnabled(true);
    }
}
