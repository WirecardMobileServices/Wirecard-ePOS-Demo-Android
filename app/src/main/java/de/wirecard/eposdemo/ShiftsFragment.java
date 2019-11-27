package de.wirecard.eposdemo;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.util.List;

import de.wirecard.epos.model.cashregisters.cashoperations.CashOperationInit;
import de.wirecard.epos.model.cashregisters.cashoperations.CashOperationType;
import de.wirecard.epos.model.cashregisters.shift.CashRegisterShift;
import de.wirecard.epos.model.cashregisters.shift.CashRegisterShiftClose;
import de.wirecard.epos.model.cashregisters.shift.CashRegisterShiftOpen;
import de.wirecard.epos.model.cashregisters.shift.CashRegisterShiftStatus;
import de.wirecard.epos.model.with.With;
import de.wirecard.epos.model.with.WithPagination;
import de.wirecard.eposdemo.adapter.simple.SimpleItem;
import de.wirecard.eposdemo.adapter.simple.SimpleItemRecyclerViewAdapter;
import de.wirecard.eposdemo.utils.DesignUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;


public class ShiftsFragment extends AbsFragment<RecyclerView> {

    private List<CashRegisterShift> cashRegisterShifts;

    private Button openClose;
    private Button payIn, payOut;

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

        openClose = view.findViewById(R.id.open_close);
        openClose.setOnClickListener(v -> {
            if (Settings.getCashRegisterId(getContext()) == null) {
                Toast.makeText(getContext(), R.string.cash_register_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isLastShiftOpen()) {
                doOpen();
            }
            else {
                doClose();
            }
        });

        payIn = view.findViewById(R.id.pay_in);
        payIn.setOnClickListener(v -> {
            if (Settings.getCashRegisterId(getContext()) == null) {
                Toast.makeText(getContext(), R.string.cash_register_error, Toast.LENGTH_SHORT).show();
                return;
            }
            doPayInOut(true);

        });

        payOut = view.findViewById(R.id.pay_out);
        payOut.setOnClickListener(v -> {
            if (Settings.getCashRegisterId(getContext()) == null) {
                Toast.makeText(getContext(), R.string.cash_register_error, Toast.LENGTH_SHORT).show();
                return;
            }
            doPayInOut(false);
        });

        loadShifts();
    }

    private void doOpen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.open);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_open_close, null);
        builder.setView(dialogView);
        final EditText amountEditText = dialogView.findViewById(R.id.amount);
        final EditText noteEditText = dialogView.findViewById(R.id.note);
        builder.setPositiveButton(R.string.open, (dialog, which) -> {
            BigDecimal amount = BigDecimal.TEN;
            String note = "Demo shift";
            try {
                amount = new BigDecimal(amountEditText.getText().toString());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Wrong input, opening with 10 €", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            try {
                note = noteEditText.getText().toString();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Wrong input, changing note to 'Demo shift'", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            showLoading();
            addDisposable(
                    EposSdkApplication.getEposSdk()
                            .cash()
                            .openCashRegisterShift(Settings.getCashRegisterId(getContext()), new CashRegisterShiftOpen(amount, note))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(shift -> loadShifts(), showErrorInsteadContent())
            );
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
        });
        builder.show();
    }

    private void doClose() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.close);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_open_close, null);
        builder.setView(dialogView);
        final EditText amountEditText = dialogView.findViewById(R.id.amount);
        final EditText noteEditText = dialogView.findViewById(R.id.note);
        builder.setPositiveButton(R.string.close, (dialog, which) -> {
            BigDecimal amount = BigDecimal.TEN;
            String note = "Demo shift";
            try {
                amount = new BigDecimal(amountEditText.getText().toString());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Wrong input, opening with 10 €", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            try {
                note = noteEditText.getText().toString();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Wrong input, changing note to 'Demo shift'", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            showLoading();
            addDisposable(
                    EposSdkApplication.getEposSdk()
                            .cash()
                            .closeCashRegisterShift(Settings.getCashRegisterId(getContext()), new CashRegisterShiftClose(amount, note))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(shift -> loadShifts(), showErrorInsteadContent())
            );
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
        });
        builder.show();
    }

    private void doPayInOut(boolean payIn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(payIn ? R.string.pay_in : R.string.pay_out);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_open_close, null);
        builder.setView(dialogView);
        final EditText amountEditText = dialogView.findViewById(R.id.amount);
        final EditText noteEditText = dialogView.findViewById(R.id.note);
        builder.setPositiveButton(payIn ? R.string.pay_in : R.string.pay_out, (dialog, which) -> {
            BigDecimal amount = BigDecimal.TEN;
            String note = "Demo shift";
            try {
                amount = new BigDecimal(amountEditText.getText().toString());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Wrong input, amount changed to 10 €", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            try {
                note = noteEditText.getText().toString();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Wrong input, changing note to 'Demo shift'", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            showLoading();
            addDisposable(
                    EposSdkApplication.getEposSdk()
                            .cash()
                            .createCashOperation(Settings.getCashRegisterId(getContext()), new CashOperationInit(amount, CURRENCY, note, payIn ? CashOperationType.CASH_IN : CashOperationType.CASH_OUT))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(shift -> {
                                Toast.makeText(getContext(), String.format("%s was successful", payIn ? getString(R.string.pay_in) : getString(R.string.pay_out)), Toast.LENGTH_SHORT)
                                        .show();
                                loadShifts();
                            }, showErrorInsteadContent())
            );
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
        });
        builder.show();
    }

    SimpleItemRecyclerViewAdapter.Coloring coloring = new SimpleItemRecyclerViewAdapter.Coloring() {
        @Override
        public int getText1Color(String text) {
            return MainActivity.DEFAULT;
        }

        @Override
        public int getText2Color(String text) {
            return DesignUtils.getShiftStatusColor(text);
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

    private void refresh() {
        if (isLastShiftOpen()) {
            payIn.setEnabled(true);
            payOut.setEnabled(true);
            openClose.setText(R.string.close);
        }
        else {
            payIn.setEnabled(false);
            payOut.setEnabled(false);
            openClose.setText(R.string.open);
        }
    }

    private boolean isLastShiftOpen() {
        return cashRegisterShifts != null && cashRegisterShifts.size() != 0 && cashRegisterShifts.get(0).getStatus() == CashRegisterShiftStatus.OPEN;
    }

    @Override
    protected void showLoading() {
        super.showLoading();
        openClose.setEnabled(false);
        payIn.setEnabled(false);
        payOut.setEnabled(false);
    }

    @Override
    protected void showError(@Nullable String message) {
        super.showError(message);
        openClose.setEnabled(false);
        payIn.setEnabled(false);
        payOut.setEnabled(false);
    }

    @Override
    protected void loadingFinished() {
        super.loadingFinished();
        openClose.setEnabled(true);
        payIn.setEnabled(true);
        payOut.setEnabled(true);
    }

    private void loadShifts() {
        if (Settings.getCashRegisterId(getContext()) == null) {
            showError(getString(R.string.cash_register_error));
            return;
        }
        showLoading();
        WithPagination withPagination = With.pagination()
                .page(0)
                .size(20)
                .includeResponseFields("id", "openTime", "closeTime", "closingAmount", "status")
                .sort("closeTime", WithPagination.Order.DESC);
        addDisposable(
                EposSdkApplication.getEposSdk()
                        .cash()
                        .getCashRegisterShifts(Settings.getCashRegisterId(getContext()), withPagination)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(items -> {
                            cashRegisterShifts = items;
                            final List<SimpleItem> simpleItems = Stream.of(items)
                                    .map(item ->
                                            new SimpleItem(
                                                    formatter.format(item.getOpenTime()),
                                                    item.getStatus().toString(),
                                                    item.getClosingAmount() != null ? nf.format(item.getClosingAmount()) : getString(R.string.none),
                                                    item.getCloseTime() != null ? formatter.format(item.getCloseTime()) : null
                                            )
                                    ).collect(Collectors.toList());
                            loadingFinishedAndShowRecycler(new SimpleItemRecyclerViewAdapter(simpleItems, false, coloring));
                            refresh();
                        }, showErrorInsteadContent())
        );
    }
}
