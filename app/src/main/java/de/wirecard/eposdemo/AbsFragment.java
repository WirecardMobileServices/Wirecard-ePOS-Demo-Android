package de.wirecard.eposdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import de.wirecard.epos.exceptions.EposException;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class AbsFragment<CONTENT extends View> extends Fragment {

    private CompositeDisposable disposables;

    protected ProgressBar loading;
    protected TextView error;
    protected CONTENT content;

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

    protected Consumer<? super Throwable> showErrorInsteadContent() {
        return throwable -> {
            throwable.printStackTrace();
            if (loading != null)
                loading.setVisibility(View.GONE);
            if (content != null)
                content.setVisibility(View.GONE);

            if (error != null) {
                error.setVisibility(View.VISIBLE);
                String errorMessage;
                if (throwable instanceof EposException && getContext() != null)
                    errorMessage = ((EposException) throwable).getMessage(getContext());
                else
                    errorMessage = throwable.toString();
                error.setText(errorMessage);
            }
            else
                Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_LONG).show();
        };
    }

    @Override
    public void onDestroyView() {
        disposables.dispose();
        super.onDestroyView();
    }
}
