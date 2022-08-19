package com.glcc;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.quinn.hunter.transform.HunterTransform;
import com.quinn.hunter.transform.RunVariant;

import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;

class DetectionTransform extends HunterTransform {
    private Project project;
    public DetectionTransform(Project project){
        super(project);
        this.project = project;
    }

    @Override
    protected RunVariant getRunVariant() {
        return super.getRunVariant();
    }
    public void scanPermission(){
        System.out.println("*******ScanPermission start*******");
        ManifestHelper m = new ManifestHelper(project.getProjectDir()+"/src/main/AndroidManifest.xml");
        m.getPermissions().forEach(permission -> System.out.println(permission));
        System.out.println("*******ScanPermission finish*******\n");
    }

    public void scanLib(){
        System.out.println("*******ScanLib start*******");
        BuildHelper b = new BuildHelper();
        try {
            b.readGradle(project.getProjectDir()+"/build.gradle");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("*******ScanLib finish*******\n");

    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        scanPermission();
        scanLib();
        transformInvocation.getInputs().forEach(input -> {
                    input.getDirectoryInputs().forEach(directoryInput -> {
                        String path = directoryInput.getFile().getAbsolutePath();
                        System.out.println("Privacy detection plugin is running\n");
                        inject(path, project);
                        System.out.println("\nPrivacy detection plugin scan end\n");
//                        check(directoryInput.getFile());
                    });
                }
                );
    }
    public void inject(String path, Project project){
        try {
            File dirs = new File(path);
            for (File dir: Objects.requireNonNull(dirs.listFiles())){
                if (dir.getName().contains("com")){
                    Arrays.stream(checkFiles(dir)).forEach(file -> {
                        if (file.getName().endsWith(".class") && !isExclude(file.getName())) {
                            try {
                                doInject(project, file, path);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private  void doInject(Project project, File clsFile, String originPath) throws NotFoundException, CannotCompileException {
        try {
            InputStream inputStream = new FileInputStream(clsFile);
            ClassReader reader = new ClassReader(inputStream);
            ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
            PrivacyVisitor visitor = new PrivacyVisitor(writer, clsFile.getName());
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public File[] checkFiles(File file){
        File[] files = file.listFiles();
        if (!files[0].isDirectory()){
            return files;
        }else{
            return checkFiles(files[0]);
        }

    }
    public boolean isExclude(String name){
        return name.equals("R.class");
    }
    public void check(File file){
        if(file.isDirectory()){
            File[] files = file.listFiles();
            Arrays.stream(files).forEach(
                    this::check
            );
            return;
        }
        String fileName = file.getName();
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
//            CtMethod ctMethod = new CtMethod();
//            ctMethod.instrument(new ExprEditor(){
//                @Override
//                public void edit(MethodCall m) throws CannotCompileException {
//                    if(m.getMethodName().contains("android/telephony/TelephonyManage)"){
//                        System.out.println(m.getMethodName());
//                    }
//                }
//            });
            ClassFile classFile = new ClassFile(dis);
            classFile.getMethods().forEach(methodInfo -> {
                CodeAttribute code = methodInfo.getCodeAttribute();
                CodeIterator itor = code.iterator();

                while (itor.hasNext()){
                    int index = 0;
                    try {
                        index = itor.next();
                    } catch (BadBytecode badBytecode) {
                        badBytecode.printStackTrace();
                    }
                    int op = itor.byteAt(index);
                }

//                if (code.contains("android/telephony/TelephonyManage")) {
//                    System.out.println(classFile.getName());
//                }
                ;

            });
            //                        ClassPool pool = ClassPool.getDefault();
            //                        CtClass ctClass = pool.getCtClass("");
            //                        ctClass.getMethod().insertBefore();

        } catch (Exception e) {
            System.out.println("transform fail");
            e.printStackTrace();
        }
    }
}
