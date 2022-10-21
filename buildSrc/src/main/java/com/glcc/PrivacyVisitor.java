package com.glcc;

import com.glcc.bean.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PrivacyVisitor extends ClassVisitor {
    private String className;
    private String packageName;
    ScanResult result = new ScanResult();

    public PrivacyVisitor(ClassVisitor classVisitor, String className, String packageName, ScanResult result) {
        super(Opcodes.ASM5, classVisitor);
        this.className = className;
        this.packageName = packageName;
        this.result = result;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new MyAdapter(Opcodes.ASM5, methodVisitor, access, name, desc, className, packageName, result);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }
}

class MyAdapter extends AdviceAdapter {
    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api    the ASM API version implemented by this visitor. Must be one
     * of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    private HashMap<String, InvokeStmt> API;
    private String className;
    private String packageName;
    private String methodname;
    private ScanResult result;

    protected MyAdapter(int api, MethodVisitor mv, int access, String name, String desc, String className, String packageName, ScanResult result) {
        super(api, mv, access, name, desc);
        this.methodname = name;
        this.className = className;
        this.packageName = packageName;
        this.result = result;
        readRules();
    }

    public void readRules() {
        API = Util.readAPIConfig();
    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        String sig = owner.replace("/", ".") + "." + name;
        if (isPrivacy(sig)) {
            System.out.println("class:" + className);
            PrivacyRule rule = new PrivacyRule();
            rule.setPattern(sig);
            rule.setCategory("api");
            DetectionPoint point = new DetectionPoint();
            InvokeStmt stmt = API.get(sig);
            stmt.setPackageName(packageName);
            stmt.setInvokeClass(className);
            stmt.setInvokeMethod(methodname);
            stmt.setThird(false);
            point.setRule(rule);
            point.setInvokeStmt(stmt);
            result.getPoints().add(point);
            inject();

        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    public void checkThird(ScanResult result, InvokeStmt stmt) {
        Map<String, String> libs = result.getLibs();
        if (libs.containsKey(packageName)){
                stmt.setThird(true);
                stmt.setLibName(packageName);
                stmt.setLibVersion(libs.get(packageName));
        }
    }

    public boolean isPrivacy(String sig) {
        if (API.containsKey(sig)) {
            System.out.println("privacy api found: " + sig);
            return true;
        }
        return false;
    }

    public void inject() {
        String packagePath = this.packageName.replace(".", "/");
        mv.visitMethodInsn(INVOKESTATIC, packagePath + "/DokitApplication", "LogStackTrace", "()V", false);
        mv.visitCode();
    }
}