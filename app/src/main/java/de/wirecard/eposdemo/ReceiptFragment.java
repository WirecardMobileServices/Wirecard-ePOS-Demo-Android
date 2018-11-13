package de.wirecard.eposdemo;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.wirecard.epos.extension.printer.model.PrintableAddress;
import de.wirecard.epos.extension.printer.model.PrintableDetail;
import de.wirecard.epos.extension.printer.model.PrintablePayment;
import de.wirecard.epos.extension.printer.model.PrintableReceipt;
import de.wirecard.epos.extension.printer.model.PrintableSaleItem;
import de.wirecard.epos.extension.printer.model.TaxItem;
import de.wirecard.epos.model.sale.sales.Sale;
import de.wirecard.epos.model.sale.sales.SaleItem;
import de.wirecard.epos.model.sale.sales.SaleItemType;
import de.wirecard.epos.model.sale.sales.SaleStatus;
import de.wirecard.epos.model.sale.sales.SaleType;
import de.wirecard.epos.model.sale.sales.payment.Payment;
import de.wirecard.epos.model.sale.sales.payment.card.CardPurchasePayment;
import de.wirecard.epos.model.sale.sales.payment.cash.CashPurchasePayment;
import de.wirecard.epos.model.sale.sales.payment.coupon.CouponPurchasePayment;
import de.wirecard.epos.model.tax.TaxSection;
import de.wirecard.epos.util.Constants;
import de.wirecard.epos.util.ReceiptUtil;
import de.wirecard.epos.util.ReceiptUtils;
import de.wirecard.epos.util.TaxUtils;
import io.github.binaryfoo.DecodedData;
import io.github.binaryfoo.EmvTags;
import io.github.binaryfoo.RootDecoder;
import io.github.binaryfoo.tlv.Tag;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static de.wirecard.eposdemo.EposSdkApplication.FRACTION_DIGITS;


public class ReceiptFragment extends AbsFragment<WebView> {

    public static final String ARG_SALE_ID = "sale_id";
    public static final String ARG_AFTER_SALE = "after_sale";

    private String saleId;
    private boolean afterSale;

    private static final boolean CUSTOMER_RECEIPT = false;

    private View loadingReceiptAfterSale;
    private View print_fab;
    private PrintableReceipt printableReceipt;

    public static ReceiptFragment newInstance(String saleId) {
        return newInstance(saleId, false);
    }

    public static ReceiptFragment newInstanceAfterSale(String saleId) {
        return newInstance(saleId, true);
    }

    private static ReceiptFragment newInstance(String saleId, boolean afterSale) {
        ReceiptFragment fragment = new ReceiptFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_SALE_ID, saleId);
        bundle.putBoolean(ARG_AFTER_SALE, afterSale);
        fragment.setArguments(bundle);
        return fragment;
    }

    public ReceiptFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_SALE_ID)) {
            saleId = getArguments().getString(ARG_SALE_ID);
            afterSale = getArguments().getBoolean(ARG_AFTER_SALE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingReceiptAfterSale = view.findViewById(R.id.loading_receipt);

        print_fab = view.findViewById(R.id.print_fab);
        print_fab.setOnClickListener(v -> {
            if (printableReceipt != null) {
                addDisposable(EposSdkApplication.getEposSdk()
                        .printer()
                        .print(printableReceipt)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> Toast.makeText(getContext(), "Print successful", Toast.LENGTH_LONG).show(), showErrorInsteadContent())
                );
            }
        });

        addDisposable(
                EposSdkApplication.getEposSdk()
                        .sales()
                        .getSale(saleId)
                        .flatMap(this::preparePrintableReceipt)
                        .flatMap(this::prepareImageReceipt)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(receiptHtml -> {
                            content.loadDataWithBaseURL("file:///android_asset/", receiptHtml, "text/html", "utf-8", null);
                            loadingFinished();
                        }, showErrorInsteadContent())
        );

        if (!afterSale)
            loadingReceiptAfterSale.setVisibility(View.GONE);
    }

    @Override
    protected void showLoading() {
        super.showLoading();
        if (afterSale)
            loadingReceiptAfterSale.setVisibility(View.VISIBLE);
        print_fab.setVisibility(View.GONE);
    }

    @Override
    protected void loadingFinished() {
        super.loadingFinished();
        if (afterSale)
            loadingReceiptAfterSale.setVisibility(View.GONE);
        print_fab.setVisibility(View.VISIBLE);
    }

    private Single<PrintableReceipt> preparePrintableReceipt(Sale sale) {
        return Single.fromCallable(() -> {
            PrintableReceipt pr = new PrintableReceipt();

            //header
            pr.setHeaderLabel("RECEIPT");

            // merchant name
            if (!TextUtils.isEmpty(sale.getMerchant().getName()))
                pr.setMerchantName(sale.getMerchant().getName());

            // merchant address
            pr.setAddress(new PrintableAddress(
                    sale.getMerchant().getAddress().getStreet1(),
                    sale.getMerchant().getAddress().getStreet2(),
                    sale.getMerchant().getAddress().getCity(),
                    sale.getMerchant().getAddress().getPostalCode(),
                    sale.getMerchant().getAddress().getStateOrProvince(),
                    sale.getMerchant().getAddress().getCountry().getName()
                    )
            );

            // merchant tax number
            if (!TextUtils.isEmpty(sale.getMerchant().getTaxNumber()))
                pr.setTaxNumber(sale.getMerchant().getTaxNumber());

            // receipt type: SALE or REFUND
            if (sale.getType() == SaleType.RETURN)
                pr.setReceiptType("Refund");
            else if (sale.getStatus() == SaleStatus.CANCELED)
                pr.setReceiptType("Reverse");
            else
                pr.setReceiptType("Sale");

            pr.setSaleStatus(sale.getStatus().name());

            // receipt number
            pr.setReceiptNumberLabel("Receipt number");
            pr.setReceiptNumber(String.valueOf(sale.getMerchantReceiptId()));

            //shop
            pr.setShopLabel("Shop");
            pr.setShop(sale.getShop() != null ? sale.getShop().getExternalId() : null);

            //cashier
            pr.setCashierIdLabel("Cashier ID");
            pr.setCashierId(sale.getExternalCashierId());

            //date & time
            pr.setProcessedDateLabel("Processed");
            pr.setProcessedDate(formatter.format(sale.getInitialized()));

            // split sale items to categories regarding type
            List<SaleItem> purchaseSaleItems = new ArrayList<>();
            SaleItem serviceCharge = null;
            SaleItem tip = null;

            if (sale.getItems() != null)
                for (SaleItem item : sale.getItems()) {
                    if (item.getType() == SaleItemType.PURCHASE) {
                        purchaseSaleItems.add(item);
                    }
                    else if (item.getType() == SaleItemType.SERVICE_CHARGE)
                        serviceCharge = item;
                    else if (item.getType() == SaleItemType.TIP)
                        tip = item;
                }

            boolean taxInclusive = sale.getUnitPricesIncludeTax();

            // items
            if (!sale.getItems().isEmpty()) {
                // sale items
                pr.setSaleItems(getPrintableSaleItems(purchaseSaleItems, EposSdkApplication.CURRENCY, Locale.getDefault()));
                // taxation info row
                if (taxInclusive)
                    pr.setUnitPricesTaxation("Included");
                else
                    pr.setUnitPricesTaxation("Excluded");
                // tax items
                pr.setTaxPercentage("Tax %");
                pr.setNetto("Netto");
                pr.setBrutto("Brutto");
                pr.setTax("Tax");
                pr.setTaxItems(getPrintableTaxItems(
                        sale.getItems(),
                        taxInclusive,
                        EposSdkApplication.CURRENCY,
                        Locale.getDefault(),
                        "Sum"));
            }

            // subtotal
            BigDecimal saleDiscount;
            if (sale.getItems().isEmpty())
                saleDiscount = BigDecimal.ZERO;
            else
                saleDiscount = Stream.of(sale.getItems())
                        .filter(saleItem -> SaleItemType.PURCHASE.equals(saleItem.getType()))
                        .filter(saleItem -> saleItem.getUnitPriceModified() != null)
                        .map(saleItem -> saleItem.getUnitPrice()
                                .subtract(saleItem.getUnitPriceModified())
                                .multiply(saleItem.getQuantity())
                                .setScale(FRACTION_DIGITS, RoundingMode.HALF_UP)
                        )
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

            pr.setSubTotalAmountLabel("Sub total");
            if (saleDiscount != null && saleDiscount.compareTo(BigDecimal.ZERO) != 0)
                pr.setSubTotalAmount(nf.format(sale.getTotalAmount().add(saleDiscount)));
            else
                pr.setSubTotalAmount(nf.format(sale.getTotalAmount()));

            //service charge
            if (serviceCharge != null) {
                BigDecimal scTotalAmount = serviceCharge.getItemTotal();
                // service charge
                pr.setServiceChargeLabel("Service charge");
                pr.setServiceChargeAmount(nf.format(scTotalAmount));
                if(serviceCharge.getUnitTax() != null && serviceCharge.getUnitTax().compareTo(BigDecimal.ZERO) > 0) {
                    pr.setServiceChargeTax(String.format("%s %s %s", "Included", nf.format(serviceCharge.getUnitTax()), "Tax"));
                } else {
                    pr.setServiceChargeTax("No tax");
                }
            }

            // discount
            if (saleDiscount != null && saleDiscount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal discountValue;
                if (sale.getItems().get(0).getUnitPriceModified() == null)
                    discountValue = BigDecimal.ZERO;
                else {
                    BigDecimal discountRate = sale.getItems().get(0).getUnitPriceModified().divide(sale.getItems().get(0).getUnitPrice(), FRACTION_DIGITS, RoundingMode.HALF_UP);
                    BigDecimal invertedRate = BigDecimal.ONE.subtract(discountRate);
                    discountValue = invertedRate.multiply(BigDecimal.valueOf(100));
                }
                pr.setDiscountLabel(String.format("Discount %1$s", discountValue.setScale(0, RoundingMode.DOWN) + "%"));
                pr.setDiscountAmount(nf.format(saleDiscount.negate()));
            }


            // tip
            if (tip != null) {
                BigDecimal tipAmount = tip.getItemTotal();
                // tip inclusive tax
                if (tip.getUnitTax() != null) {
                    String tipInclusive = "Tip inclusive %s  Tax";
                    tipInclusive = tipInclusive.replace("%s", tip.getUnitTax().toString() + "%");
                    pr.setTipInclusiveTaxAmount(tipInclusive);
                } else {
                    pr.setTipInclusiveTaxAmount("");
                }
                pr.setTipLabel("Tip");
                pr.setTipTax(nf.format(tip.getUnitTax()));
            }

            // total amount
            pr.setTotalAmountLabel("Total amount");
            pr.setTotalAmount(nf.format(sale.getTotalAmount()));

            //details (list of payments details)
            pr.setDetailsTitle("Details");
            List<PrintablePayment> payments = new ArrayList<>();
            for (Payment t : sale.getPayments()) {
                List<PrintableDetail> details = new ArrayList<>();
                details.add(new PrintableDetail("Payment type", t.getClass().getSimpleName()));
                details.add(new PrintableDetail("Payment status", t.getStatus().name()));
                details.add(new PrintableDetail("Payment amount", nf.format(t.getAmount())));
                details.add(new PrintableDetail("Payment date", formatter.format(t.getInitialized())));

                if (t instanceof CouponPurchasePayment) {
                    Timber.d("No special details for Coupon payment");
                    payments.add(new PrintablePayment(details, ""));
                }
                else if (t instanceof CashPurchasePayment) {
                    Timber.d("No special details for Cash payment");
                    payments.add(new PrintablePayment(details, ""));
                }
                else if (t instanceof CardPurchasePayment) {
                    CardPurchasePayment payment = (CardPurchasePayment) t;
                    List<DecodedData> tlv;
                    Map<String, String> decodedTags = new HashMap<>();
                    List<DecodedData> tlvUpdate;
                    Map<String, String> decodedUpdatedTags = new HashMap<>();

                    try {
                        RootDecoder decoder = new RootDecoder();
                        if (payment.getEmvData() != null) {
                            tlv = decoder.decode(payment.getEmvData(), "EMV", "constructed");
                            parseEmv(decodedTags, tlv);
                        }

                        if (payment.getEmvUpdateData() != null) {
                            tlvUpdate = decoder.decode(payment.getEmvUpdateData(), "EMV", "constructed");
                            parseEmv(decodedUpdatedTags, tlvUpdate);
                        }
                    } catch (Error | Exception e) {
                        Timber.e("Error during parse of emv:");
                        Timber.e(e);
                    }

                    // AOSA (only for VISA) and printed receipt
                    if (CUSTOMER_RECEIPT && payment.getApplicationLabel() != null && payment.getApplicationLabel().toLowerCase().contains("visa")) {
                        BigDecimal aosa;
                        String tagValue = decodedUpdatedTags.get(Constants.INFO_APPLICATION_CAPABILITIES_INFORMATION);
                        String value = null;
                        if (tagValue != null)
                            value = tagValue.replaceFirst("^0+(?!$)", "");
                        aosa = TextUtils.isEmpty(value) ? null : new BigDecimal(value).movePointLeft(FRACTION_DIGITS);
                        if (aosa != null)
                            details.add(new PrintableDetail("AOSA", nf.format(aosa)));
                    }
                    String maskedCardNumber = "";
                    if (!TextUtils.isEmpty(payment.getMaskedCardNumber()) && !payment.getMaskedCardNumber().equalsIgnoreCase("null")) {
                        String maskChar = "*";
                        String number = payment.getMaskedCardNumber().substring(payment.getMaskedCardNumber().length() - 4, payment.getMaskedCardNumber().length());
                        StringBuilder maskedNumber = new StringBuilder("");
                        for (int i = 0; i < payment.getMaskedCardNumber().length() - 4; i++) {
                            maskedNumber.append(maskChar);
                        }
                        maskedNumber.append(number);
                        maskedCardNumber = maskedNumber.toString();
                    }
                    details.add(new PrintableDetail("Card number", maskedCardNumber));
                    details.add(new PrintableDetail("Cardholder name", payment.getCardHolderName()));
                    details.add(new PrintableDetail("Card type", payment.getApplicationLabel()));
                    details.add(new PrintableDetail("AID", payment.getAid()));
                    details.add(new PrintableDetail("Terminal id", payment.getTid()));
                    details.add(new PrintableDetail("Mid", payment.getMid()));
                    details.add(new PrintableDetail("Approval code", payment.getAuthorizationCode()));
                    details.add(new PrintableDetail("TC", getTcValue(decodedTags, decodedUpdatedTags)));
                    if (!CUSTOMER_RECEIPT) { // disabled printing of signature
                        pr.setSignatureLabel("Signature");
                        payments.add(new PrintablePayment(details, payment.getSignatureImage()));
                    } else {
                        payments.add(new PrintablePayment(details, ""));
                    }
                }
            }
            pr.setPayments(payments);

            // footer
            pr.setRetainLabel("");
            pr.setClosingLabel("");
            pr.setIssuedByLabel(String.format("%s %s", "Payment issued by", getContext().getString(R.string.app_name)));
            pr.setBarcodeData(String.valueOf(sale.getMerchantReceiptId()));
            pr.setDisplayBarcodeLabel(true);

            printableReceipt = pr;
            return pr;
        });
    }

    private static String getTagValueFromData(String tag, Map<String, String> emvDataParser, Map<String, String> emvUpdateDataParser) {
        String value = emvUpdateDataParser.get(tag);
        return value == null ? emvDataParser.get(tag) : value;
    }

    private static String getTcValue(Map<String, String> emvDataParser, Map<String, String> emvUpdateDataParser) {
        StringBuilder sb = new StringBuilder("");
//        EmvTag APP_CRYPTOGRAM = new TagImpl("9f26", TagValueType.BINARY, "Application Cryptogram", "Cryptogram returned by the CHIP in response of the GENERATE AC command");
        String appCryptogram = getTagValueFromData(Constants.INFO_APP_CRYPTOGRAM, emvDataParser, emvUpdateDataParser);
//        EmvTag CRYPTOGRAM_INFORMATION_DATA = new TagImpl("9f27", TagValueType.BINARY, "Cryptogram Information Data", "Indicates the type of cryptogram and the actions to be performed by the terminal");
        String cid = getTagValueFromData(Constants.INFO_CRYPTOGRAM_INFORMATION_DATA, emvDataParser, emvUpdateDataParser);
//        EmvTag APP_TRANSACTION_COUNTER = new TagImpl("9f36", TagValueType.BINARY, "Application Payment Counter (ATC)", "Counter maintained by the application in the CHIP (incrementing the ATC is managed by the CHIP)");
        String atc = getTagValueFromData(Constants.INFO_APP_TRANSACTION_COUNTER, emvDataParser, emvUpdateDataParser);
        if (!TextUtils.isEmpty(appCryptogram) && !appCryptogram.isEmpty()) {
            sb.append("9f26");
            sb.append("08");
            sb.append(appCryptogram);
        }
        if (!TextUtils.isEmpty(cid) && !cid.isEmpty()) {
            sb.append("9f27");
            sb.append("01");
            sb.append(cid);
        }
        if (!TextUtils.isEmpty(atc) && !atc.isEmpty()) {
            sb.append("9f36");
            sb.append("02");
            sb.append(atc);
        }
        return sb.toString();
    }

    private static void parseEmv(Map<String, String> info, List<DecodedData> emvData) {
        Map<String, Tag> tagYouWant = new HashMap<>();
        tagYouWant.put(Constants.INFO_APPLICATION_CAPABILITIES_INFORMATION, Tag.fromHex("9f5d")); // 9f5d AOSA
        tagYouWant.put(Constants.INFO_APP_CRYPTOGRAM, EmvTags.APPLICATION_CRYPTOGRAM); // 9f26
        tagYouWant.put(Constants.INFO_CRYPTOGRAM_INFORMATION_DATA, EmvTags.CRYPTOGRAM_INFORMATION_DATA); // 9f27
        tagYouWant.put(Constants.INFO_APP_TRANSACTION_COUNTER, EmvTags.APPLICATION_TRANSACTION_COUNTER); // 9f36
        tagYouWant.put(Constants.INFO_CVM_RESULTS, EmvTags.CVM_RESULTS); // 9F34
        tagYouWant.put(Constants.INFO_CARD_TRANSACTION_QUALIFIERS, Tag.fromHex("9f6c")); // 9f6c

        for (Map.Entry<String, Tag> entry : tagYouWant.entrySet()) {
            for (DecodedData decodedData : emvData) {
                if (decodedData.getTag() != null && decodedData.getTag().equals(entry.getValue())) {
                    info.put(entry.getKey(), decodedData.getTlv() == null ? decodedData.getDecodedData() : decodedData.getTlv().getValueAsHexString());
                    break;
                }
            }
        }
    }

    private static List<PrintableSaleItem> getPrintableSaleItems(List<SaleItem> saleItems, Currency currency, Locale locale) {
        List<PrintableSaleItem> printable = new ArrayList<>(saleItems.size());
        // if item description is empty use itemNoDescription value

        printable.addAll(
                Stream.of(saleItems)
                        .filter(item -> item.getType().equals(SaleItemType.PURCHASE))
                        .map(item -> new PrintableSaleItem(
                                TextUtils.isEmpty(item.getDescription()) ? "No description" : item.getDescription(), // if item description is empty use itemNoDescription value
                                ReceiptUtils.bigDecimalToCurrencyString(item.getUnitPrice(), currency, locale),
                                item.getUnitPriceModified() == null ? null :
                                        ReceiptUtils.bigDecimalToCurrencyString((item.getUnitPriceModified().subtract(item.getUnitPrice()).multiply(item.getQuantity())), currency, locale),
                                item.getQuantity(),
                                item.getUnitTax().toString() + "%",
                                ReceiptUtils.bigDecimalToCurrencyString(item.getUnitPriceModified() == null ? item.getItemTotal() : (item.getUnitPrice().multiply(item.getQuantity())), currency, locale),
                                getDiscountPercentage(item.getUnitPrice(), item.getUnitPriceModified())
                        ))
                        .collect(Collectors.toList())
        );

        return printable;
    }

    private static String getDiscountPercentage(BigDecimal unitPrice, BigDecimal unitPriceModified){
        if (unitPriceModified == null)
            return "";

        return unitPrice.subtract(unitPriceModified).multiply(new BigDecimal(100)).divide(unitPrice, RoundingMode.UP).toString();
    }

    private static List<TaxItem> getPrintableTaxItems(List<SaleItem> saleItems, boolean unitPricesIncludeTax, Currency currency, Locale locale, String sumLabel) {
        List<TaxItem> items = new ArrayList<>();
        List<TaxSection> taxSections = TaxUtils.getTaxSections(saleItems, unitPricesIncludeTax, currency);
        BigDecimal netTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (TaxSection taxSection : taxSections) {
            String taxValue = String.format("%s%%", String.valueOf(taxSection.getValue()));
            netTotal = netTotal.add(taxSection.getNetto());
            taxTotal = taxTotal.add(taxSection.getTax());
            total = total.add(taxSection.getTotal());

            String nettoAmount = ReceiptUtils.bigDecimalToCurrencyString(taxSection.getNetto(), currency, locale);
            String taxAmountString = ReceiptUtils.bigDecimalToCurrencyString(taxSection.getTax(), currency, locale);
            String totalAmount = ReceiptUtils.bigDecimalToCurrencyString(taxSection.getTotal(), currency, locale);
            items.add(new TaxItem(taxValue, nettoAmount, taxAmountString, totalAmount));
        }

        // Sum row
        items.add(new TaxItem(
                sumLabel,
                ReceiptUtils.bigDecimalToCurrencyString(netTotal, currency, locale),
                ReceiptUtils.bigDecimalToCurrencyString(taxTotal, currency, locale),
                ReceiptUtils.bigDecimalToCurrencyString(total, currency, locale)
        ));
        return items;
    }

    private Single<String> prepareImageReceipt(PrintableReceipt receipt) {
        return Single.fromCallable(() -> ReceiptUtil.buildHtmlPrintableReceipt(getContext(),CUSTOMER_RECEIPT, receipt));
    }
}
