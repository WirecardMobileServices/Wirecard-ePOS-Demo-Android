package de.wirecard.eposdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import de.wirecard.epos.exceptions.EposException;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static de.wirecard.eposdemo.EposSdkApplication.CURRENCY;
import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;

public class AbsFragment<CONTENT extends View> extends Fragment {

    private CompositeDisposable disposables;

    protected ProgressBar loading;
    protected TextView error;
    protected CONTENT content;

    protected final NumberFormat nf;
    protected final DateTimeFormatter formatter;

    public AbsFragment() {
        nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(CURRENCY);
        nf.setMinimumFractionDigits(FRACTION_DIGITS);

        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }

    protected void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        disposables = new CompositeDisposable();
        loading = view.findViewById(R.id.loading);
        error = view.findViewById(R.id.error);
        content = view.findViewById(R.id.content);
    }

    protected void showLoading() {
        if (loading != null)
            loading.setVisibility(View.VISIBLE);
        if (error != null)
            error.setVisibility(View.GONE);
        if (content != null) {
            content.setVisibility(View.GONE);
        }
    }

    protected void loadingFinished() {
        if (loading != null)
            loading.setVisibility(View.GONE);
        if (error != null)
            error.setVisibility(View.GONE);
        if (content != null) {
            content.setVisibility(View.VISIBLE);
        }
    }

    protected void loadingFinishedAndShowRecycler(RecyclerView.Adapter adapter) {
        loadingFinished();
        if (content != null && content instanceof RecyclerView) {
            ((RecyclerView) content).setAdapter(adapter);
        }
        else
            Toast.makeText(getContext(), "Couldn't find recycler view", Toast.LENGTH_LONG).show();
    }

    protected void showError(@Nullable String message) {
        if (loading != null)
            loading.setVisibility(View.GONE);
        if (content != null)
            content.setVisibility(View.GONE);
        if (error != null) {
            if (!TextUtils.isEmpty(message))
                error.setText(message);
            error.setVisibility(View.VISIBLE);
        }
    }

    protected Consumer<? super Throwable> showErrorInsteadContent() {
        return throwable -> {
            throwable.printStackTrace();
            showError(null);

            if (error != null) {
                error.setVisibility(View.VISIBLE);
                error.setText(getErrorMessage(throwable));
            }
            else
                Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_LONG).show();
        };
    }

    public String getErrorMessage(Throwable throwable) {
        throwable.printStackTrace();
        String errorMessage;
        if (throwable instanceof EposException && getContext() != null)
            errorMessage = ((EposException) throwable).getMessage(getContext());
        else if (throwable.getCause() instanceof EposException && getContext() != null)
            errorMessage = ((EposException) throwable.getCause()).getMessage(getContext());
        else
            errorMessage = throwable.toString();
        return errorMessage;
    }

    @Override
    public void onDestroyView() {
        disposables.dispose();
        super.onDestroyView();
    }
}
