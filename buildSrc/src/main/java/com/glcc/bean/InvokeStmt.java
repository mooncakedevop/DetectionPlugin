package com.glcc.bean;

public class InvokeStmt {
    String packageName;
    String InvokeMethod;
    String invokeClass;
    boolean isThird;
    String libName;
    String libVersion;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getInvokeMethod() {
        return InvokeMethod;
    }

    public void setInvokeMethod(String invokeMethod) {
        InvokeMethod = invokeMethod;
    }

    public String getInvokeClass() {
        return invokeClass;
    }

    public void setInvokeClass(String invokeClass) {
        this.invokeClass = invokeClass;
    }

    public boolean isThird() {
        return isThird;
    }

    public void setThird(boolean third) {
        isThird = third;
    }

    public String getLibName() {
        return libName;
    }

    public void setLibName(String libName) {
        this.libName = libName;
    }

    public String getLibVersion() {
        return libVersion;
    }

    public void setLibVersion(String libVersion) {
        this.libVersion = libVersion;
    }
}
