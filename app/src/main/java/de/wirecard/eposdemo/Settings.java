package de.wirecard.eposdemo;

import android.content.Context;
import android.preference.PreferenceManager;

public final class Settings {

    private Settings() {
    }

    public static void setCashRegister(Context context, String cashRegisterId, String cashRegisterName) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("CASH_REGISTER_ID", cashRegisterId)
                .putString("CASH_REGISTER_NAME", cashRegisterName)
                .apply();
    }

    public static String getCashRegisterId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("CASH_REGISTER_ID", null);
    }

    public static String getCashRegisterName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("CASH_REGISTER_NAME", null);
    }

}
