package com.glcc;

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


public class PrivacyVisitor extends ClassVisitor {
    private String className;
    private String packageName;

    public PrivacyVisitor(ClassVisitor classVisitor, String className, String packageName) {
        super(Opcodes.ASM5, classVisitor);
        this.className = className;
        this.packageName = packageName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new MyAdapter(Opcodes.ASM5, methodVisitor, access, name, desc, className, packageName);
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
    private List<String> res = new LinkedList<>();
    private String className;
    private String packageName;

    protected MyAdapter(int api, MethodVisitor mv, int access, String name, String desc, String className, String packageName) {
        super(api, mv, access, name, desc);
        this.className = className;
        this.packageName = packageName;
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
        if (isPrivacy(sig)) {
//            new InjectVisitor(Opcodes.ASM5,mv);
            System.out.println("class:" + className);
            inject();

        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
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

    public void inject() {
        String packagePath = this.packageName.replace(".","/");
        mv.visitMethodInsn(INVOKESTATIC, "com/detectionplugin/DokitApplication", "LogStackTrace", "()V", false);
        mv.visitCode();
    }
}