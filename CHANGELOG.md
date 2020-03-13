# Change Log
All notable changes to Wirecard ePOS SDK are documented in this file.

## [2.15.0] - 2020-01-08
### Added

- New payment methods: Terminal Authorization, Terminal Preauthorization, Terminal Preauthorization Supplement.
- New payment actions: Reverse, Refund
- New sale actions: Return, Close sale as failed, Close sale as canceled

### Fixed

- Handling of parallel events in payment process de/wirecard/eposdemo/PaymentMethodFragment.java:147

### Changed

- UI improvements mainly Sales section

## [2.14.1] - 2019-11-07
### Added

- EFT Card (SEPA) payments authorized by signature instead of PIN.

## [2.14.0] - 2019-10-21
### Added

- ~~EFT Card (SEPA) payments authorized by signature instead of PIN.~~
- Possibility to complete/modify an open sale during a different shift and/or using a different cash register.
- Added getMerchantShop / getMerchantShops methods to UserManager
- Added File Manager component (getFilesList, getFile, getFileRecord, getFilesCategories)

### Fixed

- It's possible to add cashRegisterId parameter to reference requests
- minor bug fixes

### Changed

- PAX A920 extension renamed to Paydroid extension
- A920PaxPrinterExtension class renamed to PaydroidPrinterExtension
- A920PaxTerminalExtension class renamed to PaydroidTerminalExtension

## [2.13.0] - 2019-09-06
### Added

- PAX A920 terminal support - Visa, MC, JCB, UPI certified

### Fixed
- minor bug fixes

## [2.12.0] - 2019-08-20
### Added
- New payment method: EFT Card payment
- Include Notification (Callback) URL in Sale Purchase request
- Printer Star SM-L200 support

### Fixed
- minor bug fixes

## [2.11.0] - 2019-05-29
### Added
- Receipt supports footer logo and small receipt optimalizations
- Card Authorisation and Card Capture as new payment methods
- Modify purchase sale as new sale operation

### Fixed
- minor bug fixes

## [2.10.0] - 2019-04-17
### Added
- Multitender support: Combining payment methods into one Sale
- Referenced sale request: References the Original Sale to process another payment
- Product Stock management

### Changed
- Constants changed password confirmation timeout for wechat payment method from 45s to 40s
- Constants added new tag INFO_TRANSACTION_TYPE
- TextUtils function isEmpty count with null param
- Updated model classes in de.wirecard.epos.model package:
    - ChipApplication model new property cardType
    - CardType new model data class
    - AuthenticationLog new model data class
    - ProductStock new model data class
    - ProductStockUpdate new model data class
    - Merchant new properties mccCode, status, defaultNetTaxation, notifyCallbackUrl, productStockEnabled
    - Merchant changed properties type(from BigDecimal to TaxCategory) serviceChargeTaxCategory, serviceChargeTaxCategory
    - Merchant.Status new enum class
    - MccCode new model data class
    - MccCodeRef new model data class
    - Shop removed property version, new proprety name
    - Partner removed properties serviceChargeTaxCategory, tipTaxCategory, country, new property metadata
    - MetadataEntry new model data class
    - ReceiptSaleStatus enum removed values APPROVED, PARTIALLY_REFUNDED, INCOMPLETE, new values FAILED, FAILED_INTERVENE, UNKNOWN
    - PasswordChange new model data class
    - PasswordResetRequest new model data class
    - UsernameRemindRequest new model data class
    - User new properties accountExpirationReference, email, fail
- Whole de.wirecard.epos.model.sale package refactoring to multitender support
    - de.wirecard.epos.model.sale.builder:
        - SaleBuilder new property emailForReceipt, changed property type(from Payment to PurchasePayment) payment, new function createPurchaseRequest()
        - new models:
            Sales(OperationRequests.kt):
                - new interfaces HasPayment, HasReference
                - new sealed class HasReference
                - new data classes PurchaseRequest, ReferencePurchaseRequest, ReturnRequest, ReverseRequest, RefundRequest, CancelRequest, FailRequest
            Items(Items.kt):
                - new sealed class SaleItem
        - de.wirecard.epos.model.sale.builder.method:
            - package removed (files Payment.kt, AlipayPayment.kt, CardPayment.kt, CashPayment.kt, WechatPayment.kt)
            - interface removed Payment
            - data classes removed AlipayPayment, CardPayment, CashPayment, WechatPayment
        - de.wirecard.epos.model.sale.builder.payment:
            - new package (file Payments.kt)
            - new models:
                Payments(Payments.kt):
                    - new interfaces Payment, PaymentType, PaymentMethod, PurchasePayment, ARefundPayment, RefundPayment, ReferenceRefundPayment, ReversePayment, CashPayment, CardPayment, AlipayPayment, WechatPayment
                    - new data classes CashPurchasePayment, CashRefundPayment, CashReversePayment, CardPurchasePayment, CardReferenceRefundPayment, CardReversePayment, AlipayPurchasePayment, AlipayReferenceRefundPayment, AlipayReversePayment, WechatPurchasePayment, WechatReferenceRefundPayment, WechatReversePayment
    - de.wirecard.epos.model.sale.enums:
        - new enum classes PaymentMethod, PaymentType
    - de.wirecard.epos.model.sale.request:
        - removed interfaces Sale, UpdateSale, MonetarySale, HasPayments, HasReference
        - removed data classes PurchaseSale, ReturnSale, DeclineSale, ConfirmSale, CancelSale
        - new models:
            Sale requests(RequestSaleOperations.kt):
                - new interfaces HasPayments, HasReference
                - new sealed classes Sale, UpdateSale
                - new data classes PurchaseSale, ReferencePurchaseSale, CancelSale, FailSale, ReturnSale, RefundSale, ReverseSale, ConfirmSale, DeclineSale
        - de.wirecard.epos.model.sale.request.payment:
            - de.wirecard.epos.model.sale.request.payment.alipay:
                - package removed (files AlipayPayment.kt, AlipayPurchasePayment.kt, AlipayReturnPayment.kt)
                - interface removed AlipayPayment
                - data classes removed AlipayPurchasePayment, AlipayReturnPayment
            - de.wirecard.epos.model.sale.request.payment.card:
                - package removed (files ACardPurchasePayment.kt, CardPayment.kt, CardAuthorizePayment.kt, CardConfirmPayment.kt, CardDeclinePayment.kt, CardPurchasePayment.kt, CardReturnPayment.kt, CardUpdatePayment.kt)
                - interface removed ACardPurchasePayment, CardPayment, CardUpdatePayment
                - data classes removed CardAuthorizePayment, CardConfirmPayment, CardDeclinePayment, CardPurchasePayment, CardReturnPayment
            - de.wirecard.epos.model.sale.request.payment.cash:
                - package removed (files CashPayment.kt, CashPurchasePayment.kt, CashReturnPayment.kt)
                - interface removed CashPayment
                - data classes removed CashPurchasePayment, CashReturnPayment
            - de.wirecard.epos.model.sale.request.payment.wechat:
                - package removed (files WechatPayment.kt)
                - interface removed WechatPayment, WechatUpdatePayment
                - data classes removed WechatPurchasePayment, WechatReturnPayment
                - classes removed WechatConfirmPayment, WechatDeclinePayment
            - new models:
                Request payments(RequestPayments.kt):
                    - new interfaces PaymentRequest, HasReference, PaymentTypeRequest, UpdatePaymentRequest, PurchasePaymentRequest, ARefundPaymentRequest, RefundPaymentRequest, ReferenceRefundPaymentRequest, ReturnPaymentRequest(deprecated, created only because of backward compatibility), ReversePaymentRequest, PaymentMethodRequest, CashPaymentRequest, CardPaymentRequest, AlipayPaymentRequest, WechatPaymentRequest, CardUpdatePaymentRequest, WechatUpdatePaymentRequest
                    - new data classes CashPurchasePaymentRequest, CashRefundPaymentRequest, CashReversePaymentRequest, CashReturnPaymentRequest(deprecated, created only because of backward compatibility), CardConfirmPaymentRequest, CardDeclinePaymentRequest, CardPurchasePaymentRequest, CardReferenceRefundPaymentRequest, CardReversePaymentRequest, CardReturnPaymentRequest(deprecated, created only because of backward compatibility), AlipayPurchasePaymentRequest, AlipayReferenceRefundPaymentRequest, AlipayReversePaymentRequest, AlipayReturnPaymentRequest(deprecated, created only because of backward compatibility), WechatPurchasePaymentRequest, WechatReferenceRefundPaymentRequest, WechatReversePaymentRequest, WechatReturnPaymentRequest(deprecated, created only because of backward compatibility)
                    - new singleton classes  WechatConfirmPaymentRequest, WechatDeclinePaymentRequest
    - de.wirecard.epos.model.sale.response:
        - TxStatusCode enum value property message is always empty now
        - removed interfaces ASaleResponse, UpdateSaleResponse, SaleNonErrorResponse, SaleErrorResponse,
        - removed data classes PurchaseSaleResponse, ReturnSaleResponse, DeclineSaleResponse, ConfirmSaleResponse, CancelSaleResponse
        - new models:
            Sale reponse(ResponseSaleOperations.kt):
                - new interfaces HasPayments
                - new sealed classes SaleResponse, UpdateSaleResponse
                - new data classes CancelSaleResponse, FailSaleResponse, PurchaseSaleResponse, ReferencePurchaseSaleResponse, ReturnSaleResponse, RefundSaleResponse, ReverseSaleResponse, ConfirmSaleResponse, DeclineSaleResponse
        - de.wirecard.epos.model.sale.response.payment:
            - removed interface APaymentResponse
            - de.wirecard.epos.model.sale.response.payment.alipay:
                - package removed (files AlipayPaymentResponse.kt, AlipayPurchasePaymentResponse.kt, AlipayQueryPaymentResponse.kt, AlipayRefundPaymentResponse.kt, AlipayResolvePaymentResponse.kt, AlipayReversalPaymentResponse.kt)
                - interface removed AlipayPaymentResponse
                - data classes removed AlipayPurchasePaymentResponse, AlipayQueryPaymentResponse, AlipayRefundPaymentResponse, AlipayResolvePaymentResponse, AlipayReversalPaymentResponse
            - de.wirecard.epos.model.sale.response.payment.card:
                - package removed (files CardPaymentResponse.kt, CardAuthorizePaymentResponse.kt, CardConfirmPaymentResponse.kt, CardDeclinePaymentResponse.kt, CardPurchasePaymentResponse.kt, CardQueryPaymentResponse.kt, CardRefundPaymentResponse.kt, CardResolvePaymentResponse.kt, CardReversalPaymentResponse.kt)
                - interface removed CardPaymentResponse
                - data classes removed CardAuthorizePaymentResponse, CardConfirmPaymentResponse, CardDeclinePaymentResponse, CardPurchasePaymentResponse, CardQueryPaymentResponse, CardRefundPaymentResponse, CardResolvePaymentResponse, CardReversalPaymentResponse
            - de.wirecard.epos.model.sale.response.payment.cash:
                - package removed (files CashPaymentResponse.kt, CashPurchasePaymentResponse.kt, CashRefundPaymentResponse.kt, CashReversalPamentResponse.kt)
                - interface removed CashPaymentResponse
                - data classes removed CashPurchasePaymentResponse, CashRefundPaymentResponse, CashReversalPamentResponse
            - de.wirecard.epos.model.sale.response.payment.wechat:
                - package removed (files Wechat.kt)
                - interface removed WechatPaymentResponse
                - data classes removed WechatConfirmPaymentResponse, WechatDeclinePaymentResponse, WechatPurchasePaymentResponse, WechatQueryPaymentResponse, WechatRefundPaymentResponse, WechatResolvePaymentResponse, WechatReversalPaymentResponse
            - new models:
                Response payments(ResponsePayments.kt):
                    - new interfaces PaymentResponse, PaymentTypeResponse, PurchasePaymentResponse, RefundPaymentResponse, ReferenceRefundPaymentResponse, ReversePaymentResponse, PaymentMethodResponse, CashPaymentResponse, CardPaymentResponse, AlipayPaymentResponse, WechatPaymentResponse
                    - new data classes CashPurchasePaymentResponse, CashRefundPaymentResponse, CashReversePaymentResponse, CardPurchasePaymentResponse, CardReferenceRefundPaymentResponse, CardReversePaymentResponse, CardAuthorizePaymentResponse, CardConfirmPaymentResponse, CardDeclinePaymentResponse, CardQueryPaymentResponse, CardResolvePaymentResponse, AlipayPurchasePaymentResponse, AlipayReferenceRefundPaymentResponse, AlipayReversePaymentResponse, AlipayQueryPaymentResponse, AlipayResolvePaymentResponse, WechatPurchasePaymentResponse, WechatReferenceRefundPaymentResponse, WechatReversePaymentResponse, WechatConfirmPaymentResponse, WechatDeclinePaymentResponse, WechatQueryPaymentResponse, WechatResolvePaymentResponse
    - de.wirecard.epos.model.sale.sales:
        - Sale new properties multitender, outstandingAmount
        - SaleType enum removed values CANCEL, CONFIRM, DECLINE
        - de.wirecard.epos.model.sale.sales.payment:
            - PaymentStatus enum removed values DECLINED, CAPTURED, NOT_NEEDED, SKIPPED
            - Payment removed property processedByGateway
            - de.wirecard.epos.model.sale.sales.payment.alipay:
                - package removed (files AlipayPayment.kt, AlipayAutoResolvePayment.kt, AlipayPurchasePayment.kt, AlipayRefundPayment.kt, AlipayReversalPayment.kt)
                - interface removed AlipayPayment
                - data classes removed AlipayAutoResolvePayment, AlipayPurchasePayment, AlipayRefundPayment, AlipayReversalPayment
            - de.wirecard.epos.model.sale.sales.payment.card:
                - package removed (files ACardPayment.kt, CardPayment.kt, CardAuthorizePayment.kt, CardAutoResolvePayment.kt, CardCapturePayment.kt, CardPurchasePayment.kt, CardRefundPayment.kt, CardReversalPayment.kt)
                - interface removed ACardPayment, CardPayment, CardRefundablePayment
                - data classes removed CardAuthorizePayment, CardAutoResolvePayment, CardCapturePayment, CardPurchasePayment, CardRefundPayment, CardReversalPayment
            - de.wirecard.epos.model.sale.sales.payment.cash:
                - package removed (files CashPayment.kt, CashPurchasePayment.kt, CashRefundPayment.kt, CashReversalPayment.kt)
                - interface removed CashPayment
                - data classes removed CashPurchasePayment, CashRefundPayment, CashReversalPayment
            - de.wirecard.epos.model.sale.sales.payment.wechat:
                - package removed (files WechatPayment.kt, WechatAutoResolvePayment.kt, WechatPurchasePayment.kt, WechatRefundPayment.kt, WechatReversalPayment.kt)
                - interface removed WechatPayment
                - data classes removed WechatAutoResolvePayment, WechatPurchasePayment, WechatRefundPayment, WechatReversalPayment
            - de.wirecard.epos.model.sale.sales.payment.coupon:
                - package removed (files CouponPayment.kt, CouponPurchasePayment.kt, CouponReversalPayment.kt)
                - interface removed CouponPayment
                - data classes removed CouponPurchasePayment, CouponReversalPayment
            - new models:
                Payments(Payment.kt):
                    - new interfaces PaymentType, PaymentMethod, PurchasePayment, RefundPayment, ReversePayment, CashPayment, CardPayment, AlipayPayment, WechatPayment, CouponPayment
                    - new data classes CashPurchasePayment, CashRefundPayment, CashReversePayment, CardPurchasePayment, CardRefundPayment, CardReversePayment, CardAutoResolvePayment, CardAuthorizePayment, CardCapturePayment, AlipayPurchasePayment, AlipayRefundPayment, AlipayReversePayment, AlipayAutoResolvePayment, WechatPurchasePayment, WechatRefundPayment, WechatReversePayment, WechatAutoResolvePayment, CouponPurchasePayment, CouponReversePayment
- de.wirecard.epos.model.sale.response.SaleNonErrorResponse class removed
- InventoryManager:
    - new functions:
        - getChanges(String catalogueId, OffsetDateTime dateTime)
        - getErpProductStock(@NotNull List<String> productExternalIds, @NotNull String shopId, @NotNull WithFields withFields)
        - getProductStock(@NotNull String catalogueId, @NotNull String productId, @NotNull WithFields withFields)
        - updateProductStock(@NotNull String catalogueId, @NotNull String productId, @NotNull List<ProductStockUpdate> stockUpdate, @NotNull WithFields withFields)
- SalesOperationsManager(new manager for Sale-based operations):
    - new functions:
        - purchase()
        - referencePurchase()
        - returnn()
        - reverse()
        - refund()
        - cancel()
        - fail()
- SaleManager:
    - changed functions:
        - saleReturn() function first parameter type changed(from de.wirecard.epos.model.sale.request.payment.AReturnPayment to de.wirecard.epos.model.sale.request.payment.ReturnPaymentRequest)
        - saleReverse() function return type changed(from Single<de.wirecard.epos.model.sale.response.SaleNonErrorResponse> to Single<de.wirecard.epos.model.sale.response.SaleResponse>)
    - new function:
        - operation() function returns SalesOperationsManager
- DeprecatedSince new annotation introduced, we ensures components marked with annotation stay alive minimally 2 major version after version listed inside the annotation(for example function marked with @DeprecatedSince(version = "2.10.0") will not be removed until 2.12.0 SDK version)
