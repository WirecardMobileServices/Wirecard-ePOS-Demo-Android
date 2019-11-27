package de.wirecard.eposdemo;

public enum AppPayment {
    CASH("Cash"),
    CARD("Card"),
    TERMINAL_AUTHORIZATION("Terminal authorization"),
    TERMINAL_PREAUTHORIZATION("Terminal preauthorization"),
    EFT_CARD("Eft card")
    ;

    private String readableName;

    AppPayment(String readableName) {
        this.readableName = readableName;
    }
    public String getReadableName() {
        return readableName;
    }

}


