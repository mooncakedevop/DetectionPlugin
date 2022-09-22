package com.glcc.bean;

public class DetectionPoint {
    PrivacyRule rule;
    InvokeStmt invokeStmt;

    public PrivacyRule getRule() {
        return rule;
    }

    public void setRule(PrivacyRule rule) {
        this.rule = rule;
    }

    public InvokeStmt getInvokeStmt() {
        return invokeStmt;
    }

    public void setInvokeStmt(InvokeStmt invokeStmt) {
        this.invokeStmt = invokeStmt;
    }
}

