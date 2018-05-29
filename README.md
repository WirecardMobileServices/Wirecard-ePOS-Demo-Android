# Wirecard ePOS SDK

<img src="https://raw.githubusercontent.com/WirecardMobileServices/Wirecard-ePOS-iOS/master/docs/logo.png" alt="Wirecard-ePOS-iOS" width=400 height=64>

[![Download](https://api.bintray.com/packages/wirecardmobileservices/Wirecard-ePOS/EposSDK/images/download.svg) ](https://bintray.com/wirecardmobileservices/Wirecard-ePOS)


## Overview
The library enables cashless payment processing with selected mPOS terminals (magStripe and Chip and PIN) via the fully-licensed Wirecard Bank, Wirecard Retail Services which allows acceptance of many different cards, including Visa, MasterCard and American Express. In addition alternative payment method Alipay is also available.

The SDK and Wirecard Payment infrastructure supports:

### Card present transactions
Debit and credit cards are standard

* VISA, Mastercard
* Rupay
* American Express

### Alternative payment methods

* Alipay
* WeChat

### Payment operations

* Purchase
* Authorisation and Pre-Authorisation
* Capture
* Reversal
* Refund

## ePOS backend - Switch
Switch system is mobile Cashier backend sale system for merchants, which provides the following base features:

* Management of Partners, Merchants, Users, Cashiers, Cash registers, mPOS Terminals and Merchant's Product catalogues
* Processing Sales with combined payment methods (currently support for: Card, Cash and Gift card payments, but API is open to adding of new payment methods)
* Card payments can be routed to the right Payment gateway based on rules configured for Merchant
* Merchant's ERP system integration (Sale notifications, Products stock and Cashiers activity tracking).
  Currently cloud SAP (Business by Design) is supported.
* Loyalty (Voucher) system services - providing GiftCards selling and GiftCard payment method for attracting customers.
* Sales history with the possibility to invoke actions (like Reversal, Refund, Receipt generation, etc.)
* Possibility to communicate with HSM module for data decryption and reencryption
* Terminals supported: Spire SPm2

Find more information about Switch in the [Switch Fact Sheet](https://github.com/WirecardMobileServices/Wirecard-ePOS-iOS/blob/master/docs/Fact-Sheet-ePOS.pdf) and [Switch ePOS backend overview](https://github.com/WirecardMobileServices/Wirecard-ePOS-iOS/blob/master/docs/SWITCH-Overview.pdf)

## Whitelabel solution
Wirecard Technologies is using the Wirecard-ePOS in their Whitelabel application which is fully integrated professional mPOS solution. The **Whitelabel** app is **VISA and Mastercard certified** and utilises the Wirecard infrastructure for card payment processing.

[<img src="https://raw.githubusercontent.com/WirecardMobileServices/Wirecard-ePOS-iOS/master/docs/sdkarchv09.png" alt="Whitelabel architecture" width=400 height=422>](./docs/sdkarchv09.png "Whitelabel Architecture")

## Payment UI
The SDK provides minimalistic Payment UI consiting of the following screens:

- Amount Entry
- Payment Method selection
- Signature Capture
- Payment Process
- Cash Register selection
- Card application selection
- Open/Close Shift
- Payment QRCode scanner

Please refer to eClear example application to see the implementation details.

## Installation

There are two ways how to install the SDK.

1. Set up with Gradle. See [Developer Portal](https://wirecardmobileservices.github.io/Wirecard-ePOS-Developer/int-setup-android-gradle/)
2. Set up Manually . See [Developer Portal](https://wirecardmobileservices.github.io/Wirecard-ePOS-Developer/int-setup-android-manual/)

Read more about integrating the Wirecard ePOS SDK into your application on our [Developer Portal](https://wirecardmobileservices.github.io/Wirecard-ePOS-Developer/)

## Contact

Get in touch with [Wirecard ePOS development team](mailto:mpos-svk@wirecard.com "Wirecard-ePOS") for Wirecard ePOS support and ePOS Whitelabel solution

Get in touch with [Wirecard ePOS retail team](mailto:retail.mpos@wirecard.com "mPOS Retails") for Wirecard payment processing services


## Documentation

All the necessary information can be found on [Wirecard ePOS Developer Portal](https://wirecardmobileservices.github.io/Wirecard-ePOS-Developer "Developer Portal")

The Switch ePOS backend documentation:

* [Switch REST tests](https://switch-test.wirecard.com/mswitch-server/swagger/index.html)
* [Switch Sale API](https://switch-test.wirecard.com/mswitch-server/doc/api-doc-sale.html)


## Requirements

* Computer running Windows, Linux, OSX
* Android Studio
* Device running Android > 4.0 (API 14 - Ice Cream Sandwich)
* One of Wirecard approved terminals, printers, cash drawers, barcode scanners
	* Spire [SPm2](http://www.spirepayments.com/product/spm2/ "SPm2")
	* Datecs printer [DPP-250](http://www.datecs.bg/en/products/DPP-250/2/175 "DPP-250")

## Authors

   Wirecard Technologies Slovakia,  mpos-svk@wirecard.com

## License

Wirecard-ePOS is available under the MIT license. See the LICENSE file for more info.
