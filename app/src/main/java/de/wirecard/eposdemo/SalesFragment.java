package de.wirecard.eposdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import de.wirecard.epos.model.sale.sales.Sale;
import de.wirecard.epos.model.with.With;
import de.wirecard.epos.model.with.WithPagination;
import de.wirecard.eposdemo.adapter.simple.OnItemClickListener;
import de.wirecard.eposdemo.adapter.simple.SimpleItem;
import de.wirecard.eposdemo.adapter.simple.SimpleItemRecyclerViewAdapter;
import de.wirecard.eposdemo.utils.DesignUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SalesFragment extends AbsFragment<RecyclerView> implements OnItemClickListener {

    private List<Sale> saleLights;

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
        loadSales();
    }

    SimpleItemRecyclerViewAdapter.Coloring coloring = new SimpleItemRecyclerViewAdapter.Coloring() {
        @Override
        public int getText1Color(String text) {
            return MainActivity.DEFAULT;
        }

        @Override
        public int getText2Color(String text) {
            return DesignUtils.getSaleStatusColor(text);
        }

        @Override
        public int getText3Color(String text) {
            return MainActivity.DEFAULT;
        }

        @Override
        public int getText4Color(String text) {
            return MainActivity.DEFAULT;
        }
    };

    @Override
    public void onItemClick(View view, int position) {
        final Sale sale = saleLights.get(position);
        SaleDetailFragment saleDetailFragment = new SaleDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.SALE, sale);
        saleDetailFragment.setArguments(args);
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).changeScreenWithBack(saleDetailFragment, saleDetailFragment.getClass().getSimpleName());
        }
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
                                            String.format("%s SALE", item.getType().name()),
                                                    item.getStatus().toString(),
                                                    nf.format(item.getTotalAmount()),
                                                    item.getInitialized().format(formatter)
                                            )
                                    ).collect(Collectors.toList());
                            loadingFinishedAndShowRecycler(new SimpleItemRecyclerViewAdapter(simpleItems, this, coloring));
                        }, showErrorInsteadContent())
        );
    }
}
