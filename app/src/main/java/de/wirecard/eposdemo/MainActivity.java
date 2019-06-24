package de.wirecard.eposdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.util.concurrent.TimeUnit;

import de.wirecard.epos.exceptions.UnauthorizedException;
import de.wirecard.epos.model.user.UserCredentials;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String AMOUNT = "AMOUNT";
    public static final String SALE = "SALE";

    public static final int GREEN = Color.parseColor("#009933");
    public static final int RED = Color.parseColor("#FF0000");
    public static final int BLUE = Color.parseColor("#0000FF");
    public static final int YELLOW = Color.parseColor("#FF7F0E");

    private int selectedMenu = -1;

    private TextView username;

    private boolean doubleClick = false;

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(getTitle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        username = findViewById(R.id.usernameLayout);

        if (savedInstanceState != null)
            selectedMenu = savedInstanceState.getInt("selectedMenu");
        else
            selectedMenu = R.id.payment;

        navigationView.setCheckedItem(selectedMenu);
        changeScreen(selectedMenu);
        disposables.add(
                EposSdkApplication.getEposSdk()
                        .user()
                        .getCurrentUser()
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorResumeNext(err -> Single.create(source -> {
                            View view = getLayoutInflater().inflate(R.layout.dialog_login, null);
                            ((TextInputLayout) view.findViewById(R.id.usernameLayout)).setError("Invalid username or password");
                            ((TextInputLayout) view.findViewById(R.id.passwordLayout)).setError("Invalid username or password");
                            new AlertDialog.Builder(this)
                                    .setView(view)
                                    .setCancelable(false)
                                    .setPositiveButton("Login", (dialog, which) -> {
                                        dialog.dismiss();
                                        EposSdkApplication.getEposSdk().other()
                                                .updateUserCredentials(new UserCredentials(
                                                        ((TextInputEditText) view.findViewById(R.id.username)).getText().toString(),
                                                        ((TextInputEditText) view.findViewById(R.id.password)).getText().toString()
                                                ));
                                        source.onError(err);
                                    }).create()
                                    .show();

                        }))
                        //unauthorized or empty credentials
                        .retry(Long.MAX_VALUE, err -> err instanceof UnauthorizedException || err instanceof MismatchedInputException)
                        .subscribe(
                                user -> {
                                    username.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
                                    if (user.getMerchantShops() != null && !user.getMerchantShops().isEmpty())
                                        Settings.setShopId(this, user.getMerchantShops().get(0).getId());
                                    Settings.setLoggedUser(user);
                                },
                                error -> Log.e("MainActivity", error.toString())
                        )
        );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (getSupportFragmentManager().getBackStackEntryCount() == 0 && isTaskRoot()) {
            if (doubleClick) {
                super.onBackPressed();
                return;
            }
            this.doubleClick = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            disposables.add(Completable.complete().delay(2, TimeUnit.SECONDS).subscribe(() -> doubleClick = false, Timber::e));
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedMenu = savedInstanceState.getInt("selectedMenu");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("selectedMenu", selectedMenu);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        changeScreen(id);
        selectedMenu = id;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
            drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeScreen(@IdRes int id) {
        Fragment fragment = null;
        if (id == R.id.payment) {
            fragment = new PaymentFragment();
        }
        else if (id == R.id.sales) {
            fragment = new SalesFragment();
        }
        else if (id == R.id.shifts) {
            fragment = new ShiftsFragment();
        }
        else if (id == R.id.products) {
            fragment = new ProductsFragment();
        }
        else if (id == R.id.settings) {
            fragment = new SettingsFragment();
        }
        else {
            fragment = new NotFoundFragment();
        }

        changeScreen(fragment, fragment.getClass().getSimpleName());
    }

    public void changeScreenWithBack(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(tag)
                .replace(R.id.contentFrameLayout, fragment)
                .commit();
    }

    public void changeScreen(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrameLayout, fragment)
                .commit();
    }

    @Override
    protected void onStop() {
        disposables.dispose();
        super.onStop();
    }
}
