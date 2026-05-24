package com.bytebridges.anytop.domain.eloadengine.enums;

public enum MytelAmount {

    MMK_1000("1000", "1"),
    MMK_2000("2000", "2"),
    MMK_3000("3000", "3"),
    MMK_5000("5000", "4"),
    MMK_10000("10000", "5");

    private final String amount;
    private final String code;

    MytelAmount(String amount, String code) {
        this.amount = amount;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static String fromAmount(String amount) {
        for (MytelAmount a : values()) {
            if (a.amount.equals(amount)) {
                return a.code;
            }
        }
        throw new IllegalArgumentException("Invalid Mytel amount: " + amount);
    }
}
