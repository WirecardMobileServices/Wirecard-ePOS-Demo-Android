package de.wirecard.eposdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import de.wirecard.epos.model.sale.sales.Filter;
import de.wirecard.epos.model.sale.sales.Order;
import de.wirecard.epos.model.sale.sales.SaleItem;
import de.wirecard.epos.model.sale.sales.SaleItemType;
import de.wirecard.epos.model.sale.sales.SaleLight;
import de.wirecard.epos.model.sale.sales.payment.PaymentLight;
import de.wirecard.epos.util.TaxUtils;
import de.wirecard.eposdemo.adapter.SimpleItem;
import de.wirecard.eposdemo.adapter.SimpleItemRecyclerViewAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class SalesFragment extends AbsFragment<RecyclerView> {

    private List<SaleLight> saleLights;

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
        showLoading();
        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .getSales(new Filter(20, new Order().date().desc()))
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

    private String getCardholderNameOrType(SaleLight saleLight) {
        final PaymentLight paymentLight = saleLight.getPayments().get(0);
        if (!TextUtils.isEmpty(paymentLight.getCardHolderName()))
            return paymentLight.getCardHolderName();
        else {
            final String type = paymentLight.getType();
            String typeEscaped = null;
            try {
                typeEscaped = type.substring(0, type.indexOf("_"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (typeEscaped != null ? typeEscaped : type).substring(0, 1).toUpperCase() + (typeEscaped != null ? typeEscaped : type).substring(1).toLowerCase();
        }
    }

    private void doRefund() {
        showLoading();
        final int selectedPosition = ((SimpleItemRecyclerViewAdapter) content.getAdapter()).getSelectedPosition();
        if (selectedPosition > RecyclerView.NO_POSITION) {
            final SaleLight saleLight = saleLights.get(selectedPosition);
            final List<SaleItem> returnItems = Collections.singletonList(new SaleItem(
                    SaleItemType.PURCHASE,
                    "Demo Item",
                    saleLight.getTotalAmount(),
                    null,
                    BigDecimal.ONE,
                    TaxUtils.calculateTaxAmount(saleLight.getTotalAmount(), new BigDecimal(19), true, FRACTION_DIGITS),
                    saleLight.getTotalAmount(),
                    null,
                    null,
                    null,
                    null
            ));
            addDisposable(
                    EposSdkApplication.getEposSdk()
                            .sales()
                            .saleReturn(
                                    Collections.singletonList(new CashReturnPayment(saleLight.getTotalAmount(), null)),
                                    saleLight.getOriginalSaleId() != null ? saleLight.getOriginalSaleId() : saleLight.getId(),
                                    CURRENCY,
                                    returnItems,
                                    true,
                                    Settings.getCashRegisterId(getContext())
                            )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(sale -> {
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
