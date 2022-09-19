package com.glcc;

import com.android.build.api.transform.*;
import com.quinn.hunter.transform.HunterTransform;
import com.quinn.hunter.transform.RunVariant;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;

class DetectionTransform extends HunterTransform {
    private Project project;
    private String packageName;
    private boolean first = true;
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
        System.out.println("packageName: " + m.getPackageName());
        packageName = m.getPackageName();
        m.getPermissions().forEach(permission -> System.out.println(permission));
        System.out.println("*******ScanPermission finish*******\n");
    }

    public HashMap<String, String> scanLib(){
        System.out.println("*******ScanLib start*******");
        BuildHelper b = new BuildHelper();
        try {
            return b.readGradle(project.getProjectDir()+"/build.gradle");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        scanPermission();
        HashMap<String, String> map = scanLib();
        if (map !=null) System.out.println("*******ScanLib finish*******\n");
        //copy jar
        transformInvocation.getInputs().forEach( transformInput -> {
            transformInput.getJarInputs().forEach(jarInput -> {
                File jarFile = jarInput.getFile();
                File destJar = outputProvider.getContentLocation(jarInput.getName(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(), Format.JAR);
                try {
                    FileUtils.copyFile(jarFile, destJar);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        transformInvocation.getInputs().forEach(input -> {

                    input.getDirectoryInputs().forEach(directoryInput -> {
                        String path = directoryInput.getFile().getAbsolutePath();
                        System.out.println("Privacy detection plugin is running\n");
//
                        inject(path, project,directoryInput ,outputProvider);
                        File destDir = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                        try {
                            FileUtils.copyDirectory(directoryInput.getFile(), destDir);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("\nPrivacy detection plugin scan end\n");
//                        check(directoryInput.getFile());
                    });
                }
                );
    }
    public void inject(String path, Project project, DirectoryInput directoryInput, TransformOutputProvider outputProvider){
        try {

            File dirs = new File(path);
            for (File dir: Objects.requireNonNull(dirs.listFiles())){

                if (dir.getName().contains("com")){

                    if(first){
                        InjectApplication(dir,directoryInput, outputProvider);
                        first = false;
                    }
                    Arrays.stream(checkFiles(dir)).forEach(file -> {
                        if (file.getName().endsWith(".class") && !isExclude(file.getName())) {
                            try {
                                doInject(project, file, path,directoryInput ,outputProvider);
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

    private void InjectApplication(File dir,DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        try {
            ApplicationGenerator.createClass(dir, directoryInput, outputProvider);
            System.out.println("111");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private  void doInject(Project project, File clsFile, String originPath, DirectoryInput directoryInput, TransformOutputProvider outputProvider) throws NotFoundException, CannotCompileException {
        try {
            InputStream inputStream = new FileInputStream(clsFile);
            ClassReader reader = new ClassReader(inputStream);
            ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
            PrivacyVisitor visitor = new PrivacyVisitor(writer, clsFile.getName());
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            byte[] code = writer.toByteArray();
            FileOutputStream fos = new FileOutputStream(clsFile);
            fos.write(code);
            fos.close();
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

            });

        } catch (Exception e) {
            System.out.println("transform fail");
            e.printStackTrace();
        }
    }
}
