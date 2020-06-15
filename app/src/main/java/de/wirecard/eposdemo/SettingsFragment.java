package de.wirecard.eposdemo;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import de.wirecard.epos.PrinterManager;
import de.wirecard.epos.TerminalManager;
import de.wirecard.epos.extension.EposPublicExtension;
import de.wirecard.epos.extension.printer.UninitializedPrinterDevice;
import de.wirecard.epos.extension.terminal.UninitializedTerminalDevice;
import de.wirecard.epos.model.cashregisters.CashRegister;
import de.wirecard.epos.model.with.With;
import de.wirecard.epos.model.with.WithPagination;
import de.wirecard.epos.util.events.Event;
import de.wirecard.epos.util.events.acceptance.AvailablePrinterTypesEvent;
import de.wirecard.epos.util.events.acceptance.AvailablePrintersEvent;
import de.wirecard.epos.util.events.acceptance.AvailableTerminalTypesEvent;
import de.wirecard.epos.util.events.acceptance.AvailableTerminalsEvent;
import de.wirecard.epos.util.events.general.UpdatePrinterEvent;
import de.wirecard.epos.util.events.general.UpdateTerminalEvent;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;


public class SettingsFragment extends AbsFragment<LinearLayout> {

    private AlertDialog actualDialog;

    private View terminalSelection;
    private TextView terminalValue;
    private View printerSelection;
    private TextView printerValue;
    private View cashRegisterSelection;
    private TextView cashRegisterValue;

    private Toast actualToast;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        terminalSelection = view.findViewById(R.id.terminal_selection);
        terminalSelection.setOnClickListener(v -> {
            showLoading();
            Relay<Event> eventObservable = BehaviorRelay.create();
            addDisposable(EposSdkApplication.getEposSdk()
                    .terminal()
                    .discoverDevices()
                    .subscribeParallel(eventObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(terminalDevice -> loadingFinished(), throwable -> showError(getErrorMessage(throwable))
                    )
            );

            addDisposable(eventObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                        if (event instanceof AvailableTerminalTypesEvent) {
                            final AvailableTerminalTypesEvent availableDeviceTypesEvent = (AvailableTerminalTypesEvent) event;
                            if (availableDeviceTypesEvent.getExtensions().size() == 1)
                                availableDeviceTypesEvent.select(0);
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.terminal);

                                final List<String> extensionNames = Stream.of(availableDeviceTypesEvent.getExtensions())
                                        .map(EposPublicExtension::getExtensionName)
                                        .collect(Collectors.toList());
                                builder.setItems(extensionNames.toArray(new CharSequence[extensionNames.size()]), (dialog, which) -> availableDeviceTypesEvent.select(which));
                                builder.setOnCancelListener(dialog -> availableDeviceTypesEvent.cancel());

                                actualDialog = builder.create();

                                actualDialog.show();
                            }
                        }
                        else if (event instanceof AvailableTerminalsEvent) {
                            final AvailableTerminalsEvent availableTerminalsEvent = (AvailableTerminalsEvent) event;
                            if (availableTerminalsEvent.getDevices().size() == 1)
                                availableTerminalsEvent.select(0);
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.terminal);

                                final List<String> extensionNames = Stream.of(availableTerminalsEvent.getDevices())
                                        .map(UninitializedTerminalDevice::getDisplayName)
                                        .collect(Collectors.toList());
                                builder.setItems(extensionNames.toArray(new CharSequence[extensionNames.size()]), (dialog, which) -> availableTerminalsEvent.select(which));
                                builder.setOnCancelListener(dialog -> availableTerminalsEvent.cancel());

                                actualDialog = builder.create();

                                actualDialog.show();
                            }
                        }
                        else if (event instanceof UpdateTerminalEvent) {
                            terminalValue.setText(((UpdateTerminalEvent) event).getMessage(getContext()));
                        }
                        else {
                            Timber.d("Event: %s", event.toString());
                        }

                    })
            );
        });
        terminalSelection.setOnLongClickListener(v -> {
            EposSdkApplication.getEposSdk().terminal().resetSelectedDevice().blockingAwait();
            refresh();
            return true;
        });

        printerSelection = view.findViewById(R.id.printer_selection);
        printerSelection.setOnClickListener(v -> {
            showLoading();
            Relay<Event> eventObservable = BehaviorRelay.create();
            addDisposable(EposSdkApplication.getEposSdk()
                    .printer()
                    .discoverDevices()
                    .subscribeParallel(eventObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(printerDevice -> loadingFinished(), throwable -> showError(getErrorMessage(throwable))
                    )
            );

            addDisposable(eventObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                        if (event instanceof AvailablePrinterTypesEvent) {
                            final AvailablePrinterTypesEvent availableDeviceTypesEvent = (AvailablePrinterTypesEvent) event;
                            if (availableDeviceTypesEvent.getExtensions().size() == 1)
                                availableDeviceTypesEvent.select(0);
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.printer);

                                final List<String> extensionNames = Stream.of(availableDeviceTypesEvent.getExtensions())
                                        .map(EposPublicExtension::getExtensionName)
                                        .collect(Collectors.toList());
                                builder.setItems(extensionNames.toArray(new CharSequence[extensionNames.size()]), (dialog, which) -> availableDeviceTypesEvent.select(which));
                                builder.setOnCancelListener(dialog -> availableDeviceTypesEvent.cancel());

                                actualDialog = builder.create();

                                actualDialog.show();
                            }
                        }
                        else if (event instanceof AvailablePrintersEvent) {
                            final AvailablePrintersEvent availablePrintersEvent = (AvailablePrintersEvent) event;
                            if (availablePrintersEvent.getDevices().size() == 1)
                                availablePrintersEvent.select(0);
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.printer);

                                final List<String> extensionNames = Stream.of(availablePrintersEvent.getDevices())
                                        .map(UninitializedPrinterDevice::getDisplayName)
                                        .collect(Collectors.toList());
                                builder.setItems(extensionNames.toArray(new CharSequence[extensionNames.size()]), (dialog, which) -> availablePrintersEvent.select(which));
                                builder.setOnCancelListener(dialog -> availablePrintersEvent.cancel());

                                actualDialog = builder.create();

                                actualDialog.show();
                            }
                        }
                        else if (event instanceof UpdatePrinterEvent) {
                            printerValue.setText(((UpdatePrinterEvent) event).getMessage(getContext()));
                        }
                        else {
                            Timber.d("Event: %s", event.toString());
                        }

                    })
            );
        });
        printerSelection.setOnLongClickListener(v -> {
            EposSdkApplication.getEposSdk().printer().resetSelectedDevice().blockingAwait();
            refresh();
            return true;
        });

        cashRegisterSelection = view.findViewById(R.id.cash_register_selection);
        final WithPagination withPagination = With.pagination()
                .includeResponseFields(
                        "id",
                        "name"
                );
        cashRegisterSelection.setOnClickListener(v -> {
            showLoading();
            addDisposable(EposSdkApplication.getEposSdk()
                    .cash()
                    .getCashRegisters(withPagination)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cashRegisters -> {
                                loadingFinished();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.cash_register);

                                final List<String> registerNames = Stream.of(cashRegisters).map(CashRegister::getName).collect(Collectors.toList());
                                builder.setItems(registerNames.toArray(new CharSequence[registerNames.size()]), (dialog, which) -> {
                                    final CashRegister cashRegisterLight = cashRegisters.get(which);
                                    Settings.setCashRegister(getContext(), cashRegisterLight.getId(), cashRegisterLight.getName());
                                    refresh();
                                });

                                actualDialog = builder.create();

                                actualDialog.show();
                            }, throwable -> showError(getErrorMessage(throwable))
                    ));
        });
        cashRegisterSelection.setOnLongClickListener(v -> {
            Settings.setCashRegister(getContext(), null, null);
            refresh();
            return true;
        });

        terminalValue = view.findViewById(R.id.terminal_value);
        printerValue = view.findViewById(R.id.printer_value);
        cashRegisterValue = view.findViewById(R.id.cash_register_value);

        refresh();
    }

    private void refresh() {
        final TerminalManager terminalEpos = EposSdkApplication.getEposSdk().terminal();
        if (terminalEpos.isInitialized().blockingGet())
            terminalValue.setText(terminalEpos.getSelectedDevice().blockingGet().getDisplayName());
        else
            terminalValue.setText(R.string.none);

        final PrinterManager printerEpos = EposSdkApplication.getEposSdk().printer();
        if (printerEpos.isInitialized().blockingGet())
            printerValue.setText(printerEpos.getSelectedDevice().blockingGet().getDisplayName());
        else
            printerValue.setText(R.string.none);

        if (Settings.getCashRegisterId(getContext()) != null && Settings.getCashRegisterName(getContext()) != null)
            cashRegisterValue.setText(Settings.getCashRegisterName(getContext()));
        else
            cashRegisterValue.setText(R.string.none);
    }

    @Override
    protected void showLoading() {
        super.showLoading();
        if (content != null)
            content.setVisibility(View.VISIBLE);
        if (terminalSelection != null)
            terminalSelection.setEnabled(false);
        if (printerSelection != null)
            printerSelection.setEnabled(false);
        if (cashRegisterSelection != null)
            cashRegisterSelection.setEnabled(false);
        if (actualDialog != null && actualDialog.isShowing())
            actualDialog.dismiss();
    }

    @Override
    protected void loadingFinished() {
        super.loadingFinished();
        if (terminalSelection != null)
            terminalSelection.setEnabled(true);
        if (printerSelection != null)
            printerSelection.setEnabled(true);
        if (cashRegisterSelection != null)
            cashRegisterSelection.setEnabled(true);
        if (actualDialog != null && actualDialog.isShowing())
            actualDialog.dismiss();
        refresh();
    }

    @Override
    protected void showError(@Nullable String message) {
        loadingFinished();
        if (actualToast != null)
            actualToast.cancel();
        actualToast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        actualToast.show();
    }
}
