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

import org.threeten.bp.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.wirecard.epos.model.sale.request.payment.cash.CashReturnPayment;
import de.wirecard.epos.model.sale.sales.Filter;
import de.wirecard.epos.model.sale.sales.Order;
import de.wirecard.epos.model.sale.sales.SaleLight;
import de.wirecard.eposdemo.adapter.SimpleItem;
import de.wirecard.eposdemo.adapter.SimpleItemRecyclerViewAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class SalesFragment extends AbsFragment<RecyclerView> {

    private final NumberFormat nf;
    private final DateTimeFormatter formatter;

    private List<SaleLight> saleLights;

    private Button receipt;
    private Button refund;

    public SalesFragment() {
        nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(CURRENCY);
        nf.setMinimumFractionDigits(FRACTION_DIGITS);

        formatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        content.setLayoutManager(new LinearLayoutManager(getContext()));

        loadSales();

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
    }

    private void loadSales() {
        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .getSales(new Filter(30, new Order().date().desc()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(items -> {
                            saleLights = items;
                            final List<SimpleItem> simpleItems = Stream.of(items)
                                    .map(item ->
                                            new SimpleItem(
                                                    item.getInitialized().format(formatter),
                                                    String.format("%s, %s", item.getType().toString(), item.getStatus()),
                                                    nf.format(item.getTotalAmount())
                                            )
                                    ).collect(Collectors.toList());
                            loadingFinishedAndShowRecycler(new SimpleItemRecyclerViewAdapter(simpleItems));
                        }, showErrorInsteadContent())
        );
    }

    private void doRefund() {
        showLoading();
        final int selectedPosition = ((SimpleItemRecyclerViewAdapter) content.getAdapter()).getSelectedPosition();
        if (selectedPosition > RecyclerView.NO_POSITION) {
            final SaleLight saleLight = saleLights.get(selectedPosition);
            addDisposable(
                    EposSdkApplication.getEposSdk()
                            .sales()
                            .saleReturn(
                                    Collections.singletonList(new CashReturnPayment(saleLight.getTotalAmount(), null)),
                                    saleLight.getOriginalSaleId() != null ? saleLight.getOriginalSaleId() : saleLight.getId(),
                                    CURRENCY,
                                    null,
                                    true,
                                    null
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
