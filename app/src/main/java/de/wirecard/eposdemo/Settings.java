package de.wirecard.eposdemo;

import android.content.Context;
import android.preference.PreferenceManager;

import de.wirecard.epos.model.user.User;

public final class Settings {

    private static User loggedUser;

    private Settings() {
    }

    public static void setLoggedUser(User user) {
        loggedUser = user;
    }

    public static boolean isCashRegisterRequired() {
        return loggedUser != null && loggedUser.getMerchant().getCashRegistersRequired();
    }

    public static void setCashRegister(Context context, String cashRegisterId, String cashRegisterName) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("CASH_REGISTER_ID", cashRegisterId)
                .putString("CASH_REGISTER_NAME", cashRegisterName)
                .apply();
    }

    public static void setShopId(Context context,String shopId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("SHOP_ID", shopId)
                .apply();
    }

    public static String getShopID(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString("SHOP_ID", null);
    }

    public static String getCashRegisterId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("CASH_REGISTER_ID", null);
    }

    public static String getCashRegisterName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("CASH_REGISTER_NAME", null);
    }

}
