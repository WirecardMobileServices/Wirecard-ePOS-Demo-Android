package de.wirecard.eposdemo;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.wirecard.eposdemo.adapter.SimpleItem;
import de.wirecard.eposdemo.adapter.SimpleItemRecyclerViewAdapter;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class ShiftsFragment extends AbsFragment<RecyclerView> {

    public ShiftsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shifts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        content.setLayoutManager(new LinearLayoutManager(getContext()));
        addDisposable(
                Single.fromCallable(() -> {
                    List<SimpleItem> shiftItems = new ArrayList<>();
                    shiftItems.add(new SimpleItem("1: left", "center", "right"));
                    shiftItems.add(new SimpleItem("2: left", "center", "right"));
                    shiftItems.add(new SimpleItem("3: left", "center", "right"));
                    shiftItems.add(new SimpleItem("4: left", "center", "right"));
                    shiftItems.add(new SimpleItem("5: left", "center", "right"));
                    shiftItems.add(new SimpleItem("6: left", "center", "right"));
                    shiftItems.add(new SimpleItem("7: left", "center", "right"));
                    shiftItems.add(new SimpleItem("8: left", "center", "right"));
                    shiftItems.add(new SimpleItem("9: left", "center", "right"));
                    shiftItems.add(new SimpleItem("10: left", "center", "right"));
                    shiftItems.add(new SimpleItem("11: left", "center", "right"));
                    shiftItems.add(new SimpleItem("12: left", "center", "right"));
                    shiftItems.add(new SimpleItem("13: left", "center", "right"));
                    shiftItems.add(new SimpleItem("14: left", "center", "right"));
                    shiftItems.add(new SimpleItem("15: left", "center", "right"));
                    return shiftItems;
                })
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(shiftItems -> loadingFinishedAndShowRecycler(new SimpleItemRecyclerViewAdapter(shiftItems)), showErrorInsteadContent())
        );
    }
}
