package de.wirecard.eposdemo.utils;

import androidx.annotation.Nullable;
import de.wirecard.epos.model.cashregisters.shift.CashRegisterShiftStatus;
import de.wirecard.epos.model.sale.sales.SaleStatus;
import de.wirecard.epos.model.sale.sales.payment.PaymentStatus;
import de.wirecard.eposdemo.MainActivity;

public class DesignUtils {

    public static int getSaleStatusColor(@Nullable String saleStatusText) {
        if (saleStatusText == null || saleStatusText.isEmpty())
            return MainActivity.DEFAULT;

        switch (SaleStatus.valueOf(saleStatusText)) {
            case COMPLETED:
                return MainActivity.GREEN;
            case FAILED:
                return MainActivity.RED;
            case RETURNED:
            case PARTIALLY_RETURNED:
                return MainActivity.BLUE;
            case UNCONFIRMED:
                return MainActivity.YELLOW;
            default:
                return MainActivity.DEFAULT;
        }
    }

    public static int getShiftStatusColor(@Nullable String shiftStatusText) {
        if (shiftStatusText == null || shiftStatusText.isEmpty())
            return MainActivity.DEFAULT;

        switch (CashRegisterShiftStatus.valueOf(shiftStatusText)) {
            case OPEN:
                return MainActivity.GREEN;
            case CLOSED:
                return MainActivity.YELLOW;
            default:
                return MainActivity.DEFAULT;
        }
    }


    public static int getPaymentStatusColor(@Nullable String paymentStatusText) {
        if (paymentStatusText == null || paymentStatusText.isEmpty())
            return MainActivity.DEFAULT;

        switch (PaymentStatus.valueOf(paymentStatusText)) {
            case COMPLETED:
            case CAPTURED:
            case APPROVED:
                return MainActivity.GREEN;
            case FAILED:
            case REJECTED:
            case FAILED_INTERVENE:
                return MainActivity.RED;
            case REFUNDED:
            case REVERSED:
                return MainActivity.BLUE;
            case INCOMPLETE:
            case USERPAYING:
            case INITIALIZED:
                return MainActivity.YELLOW;
            default:
                return MainActivity.DEFAULT;
        }
    }
}
