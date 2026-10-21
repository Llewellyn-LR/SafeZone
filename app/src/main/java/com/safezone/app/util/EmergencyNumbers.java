package com.safezone.app.util;

public final class EmergencyNumbers {
    private EmergencyNumbers() {}

    public static final class Entry {
        public final String dial;
        public final String display;
        public final String source;
        Entry(String dial, String display, String source) {
            this.dial = dial; this.display = display; this.source = source;
        }
    }

    public static final Entry SAPS_EMERGENCY = new Entry("10111", "10111",
            "SAPS – to be re-verified on saps.gov.za");

    public static final Entry CRIME_STOP = new Entry("0860010111", "08600 10111",
            "SAPS media statements (saps.gov.za/newsroom), 2025");

    public static final Entry GBV_COMMAND_CENTRE = new Entry("0800428428", "0800 428 428",
            "sanews.gov.za (2018, 2020) and gov.za media statements");

    public static final Entry GBV_CALL_ME_BACK = new Entry("*120*7867#", "*120*7867#",
            "sanews.gov.za (2018, 2020) and gov.za media statements");
}
