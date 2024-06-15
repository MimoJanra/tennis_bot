package org.telegram.bot;

public class Button {
    private final String label;
    private final String callBack;

    public Button(String label, String callBack) {
        this.label = label;
        this.callBack = callBack;
    }

    public String getLabel() {
        return label;
    }

    public String getCallBack() {
        return callBack;
    }
}
