package de.wirecard.eposdemo;

import android.support.multidex.MultiDexApplication;

import java.util.Currency;

import de.wirecard.epos.EposSDK;
import de.wirecard.epos.EposSdkBuilder;
import de.wirecard.epos.extension.datecs.DatecsPrinterExtension;
import de.wirecard.epos.extension.spire.spm2.Spm2SpireTerminalExtension;
import de.wirecard.epos.model.backend.Env;
import de.wirecard.epos.model.user.UserCredentials;
import timber.log.Timber;

public class EposSdkApplication extends MultiDexApplication {

    private static EposSDK eposSDK;

    private final static String USERNAME = "EposDemoUser";
    private final static String PASSWORD = "Demo12345678!!!!!";

    public static final Currency CURRENCY = Currency.getInstance("EUR");
    public static final int FRACTION_DIGITS = CURRENCY.getDefaultFractionDigits();

    @Override
    public void onCreate() {
        super.onCreate();
        eposSDK = new EposSdkBuilder(this, Env.SWITCH_TEST)
                .setCredentials(new UserCredentials(USERNAME, PASSWORD))
                .addExtension(Spm2SpireTerminalExtension.getInstance())
                .addExtension(DatecsPrinterExtension.getInstance())
                .build();

        Timber.plant(new Timber.DebugTree());
    }

    public static EposSDK getEposSdk() {
        return eposSDK;
    }

}
