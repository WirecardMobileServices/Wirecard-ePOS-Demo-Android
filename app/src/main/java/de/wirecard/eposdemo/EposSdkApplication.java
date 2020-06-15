package de.wirecard.eposdemo;

import android.text.TextUtils;

import java.util.Currency;

import androidx.multidex.MultiDexApplication;
import de.wirecard.epos.EposSDK;
import de.wirecard.epos.EposSdkBuilder;
import de.wirecard.epos.extension.datecs.DatecsPrinterExtension;
import de.wirecard.epos.extension.pax.PaydroidTerminalExtension;
import de.wirecard.epos.extension.spire.spm2.Spm2SpireTerminalExtension;
import de.wirecard.epos.model.backend.Env;
import de.wirecard.epos.model.user.UserCredentials;
import timber.log.Timber;

public class EposSdkApplication extends MultiDexApplication {

    private static EposSDK eposSDK;

    private final static String USERNAME = BuildConfig.username;
    private final static String PASSWORD = BuildConfig.password;

    public static final Currency CURRENCY = Currency.getInstance("EUR");
    public static final int FRACTION_DIGITS = CURRENCY.getDefaultFractionDigits();

    @Override
    public void onCreate() {
        super.onCreate();

        if (TextUtils.isEmpty(USERNAME) || TextUtils.isEmpty(PASSWORD)) {
            throw new IllegalStateException("" +
                    "You have to specify username & password in 'user.properties' file! \n" +
                    "Credentials should be provided by Wirecard Epos support. \n" +
                    "Example of 'user.properties' file: \n" +
                    "username = \"yourUsername\"\n" +
                    "password = \"yourPassword\"");
        }
        else {
            eposSDK = new EposSdkBuilder(this, Env.SWITCH_TEST)
                    .setCredentials(new UserCredentials(USERNAME, PASSWORD))
                    .addExtension(Spm2SpireTerminalExtension.getInstance())
                    .addExtension(DatecsPrinterExtension.getInstance())
                    .addExtension(PaydroidTerminalExtension.getInstance())
                    .build();
        }

        Timber.plant(new Timber.DebugTree());
    }

    public static EposSDK getEposSdk() {
        return eposSDK;
    }

}
