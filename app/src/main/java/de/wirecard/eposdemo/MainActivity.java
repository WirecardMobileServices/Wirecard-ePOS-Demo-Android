package de.wirecard.eposdemo;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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

        username = findViewById(R.id.username);

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
                        .subscribe(
                                user -> username.setText(String.format("%s %s", user.getFirstName(), user.getLastName())),
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
