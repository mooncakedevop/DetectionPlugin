package com.glcc.bean;

public class InvokeStmt {
    String name;

    String pattern;

    String desc;

    boolean sensitive;
    String packageName;
    String InvokeMethod;
    String invokeClass;
    boolean isThird;
    String libName;
    String libVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

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
