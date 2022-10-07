package com.glcc;
import com.glcc.bean.DetectionPoint;
import com.glcc.bean.InvokeStmt;
import com.glcc.bean.PrivacyRule;
import com.glcc.bean.ScanResult;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ScanVisitor extends ClassVisitor {
    private String className;
    private String packageName;
    ScanResult result = new ScanResult();

    public ScanVisitor(ClassVisitor classVisitor, String className, String packageName, ScanResult result) {
        super(Opcodes.ASM5, classVisitor);
        this.className = className;
        this.packageName = packageName;
        this.result = result;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new ScanAdapter(Opcodes.ASM5, methodVisitor, access, name, desc, className, packageName, result);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }
}

class ScanAdapter extends AdviceAdapter {
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
    private List<String> res = new LinkedList<>();
    private String className;
    private String packageName;
    private String methodname;
    private ScanResult result;

    protected ScanAdapter(int api, MethodVisitor mv, int access, String name, String desc, String className, String packageName, ScanResult result) {
        super(api, mv, access, name, desc);
        this.methodname = name;
        this.className = className;
        this.packageName = packageName;
        this.result = result;
        readRules();
    }

    public void readRules() {
        String filePath = "/Users/mooncake/AndroidStudioProjects/DetectionPlugin/buildSrc/src/main/resources/privacy.txt";

        try {
            FileInputStream fin = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(fin);
            BufferedReader buffReader = new BufferedReader(reader);
            String str = "";
            while ((str = buffReader.readLine()) != null) {
                res.add(str);
            }
            buffReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

        String sig = owner.replace("/", ".") + "." + name;
        System.out.println("sig: " + sig);
        if (isPrivacy(sig)) {
            System.out.println("class:" + className);
            PrivacyRule rule = new PrivacyRule();
            rule.setPattern(sig);
            rule.setCategory("api");
            DetectionPoint point = new DetectionPoint();
            InvokeStmt stmt = new InvokeStmt();
            stmt.setPackageName(packageName);
            stmt.setInvokeClass(className);
            stmt.setInvokeMethod(methodname);
            checkThird(result, stmt);
            point.setRule(rule);
            point.setInvokeStmt(stmt);
            result.getPoints().add(point);

        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    public void checkThird(ScanResult result, InvokeStmt stmt) {
        Map<String, String> libs = result.getLibs();
        for (Map.Entry<String, String> lib : libs.entrySet()) {
            if (lib.getKey().equals(packageName)) {
                stmt.setThird(true);
                stmt.setLibName(lib.getKey());
                stmt.setLibVersion(lib.getValue());
            }
        }
    }

    public boolean isPrivacy(String sig) {
        for (String str : res) {
            if (str.contains(sig)) {
                System.out.println("privacy api found: " + sig);
                return true;
            }
        }
        return false;
    }


}