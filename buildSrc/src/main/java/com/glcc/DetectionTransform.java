package com.glcc;

import com.alibaba.fastjson2.JSON;
import com.android.build.api.transform.*;
import com.glcc.bean.*;
import com.quinn.hunter.transform.HunterTransform;
import com.quinn.hunter.transform.RunVariant;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;

class DetectionTransform extends HunterTransform {
    private Project project;
    private String packageName;
    private boolean first = true;

    private ScanResult result = new ScanResult();
    AppInfo appInfo = new AppInfo();

    Map<String, String> libs;


    public DetectionTransform(Project project) {
        super(project);
        this.project = project;
    }

    @Override
    protected RunVariant getRunVariant() {
        return super.getRunVariant();
    }

    public ManifestHelper scanPermission() {
        System.out.println("*******ScanPermission start*******");
        ManifestHelper m = new ManifestHelper(project.getProjectDir() + "/src/main/AndroidManifest.xml");


        System.out.println("packageName: " + m.getPackageName());
        appInfo.setPackageName(m.getPackageName());
        appInfo.setPluginVersion("0.1.0");
        System.out.println("permissions : ");
        for (Permission p : m.getPermissions()) {
            System.out.println(p.getName());
        }
        result.setPermissions(m.getPermissions());
        return m;
    }

    public void scanLib() {
        System.out.println("*******ScanLib start*******");
        BuildHelper b = new BuildHelper();
        try {
        libs =  b.readGradle(project.getProjectDir() + "/build.gradle");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        long start = System.currentTimeMillis();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        ManifestHelper m = scanPermission();
        this.packageName = m.getPackageName();
        scanLib();
        if (libs != null) System.out.println("*******ScanLib finish*******\n");
        result.setLibs(libs);
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        //copy jar
        inputs.forEach(transformInput -> {
            transformInput.getJarInputs().forEach(jarInput -> {
                File destJar = outputProvider.getContentLocation(jarInput.getName(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(), Format.JAR);
                try {
                    transformJarItrnput(jarInput, destJar);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        });
        inputs.forEach(transformInput -> transformInput.getDirectoryInputs().forEach(directoryInput -> {
            File dstFile = outputProvider.getContentLocation(
                    directoryInput.getName(),
                    directoryInput.getContentTypes(),
                    directoryInput.getScopes(),
                    Format.DIRECTORY);
            // ????????????????????????
            transformDir(directoryInput.getFile(), dstFile);

        }));
        result.setTimeUsed((int) (System.currentTimeMillis() - start));
        String str = JSON.toJSONString(result);

        System.out.println("json output: " + str);


    }

    private void transformDir(File inputDir, File dstDir) {
        try {
            if (dstDir.exists()) {
                FileUtils.forceDelete(dstDir);
            }
            FileUtils.forceMkdir(dstDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String inputDirPath = inputDir.getAbsolutePath();
        String dstDirPath = dstDir.getAbsolutePath();
        File[] files = inputDir.listFiles();
        for (File file : files) {
            System.out.println("transformDir-->" + file.getAbsolutePath());
            String dstFilePath = file.getAbsolutePath();
            dstFilePath = dstFilePath.replace(inputDirPath, dstDirPath);
            File dstFile = new File(dstFilePath);
            if (file.isDirectory()) {
                System.out.println("isDirectory-->" + file.getAbsolutePath());
                // ??????
                transformDir(file, dstFile);
            } else if (file.isFile()) {
                // ????????????class??????
                transformSingleFile(file, dstFile);
            }
        }
    }

    /**
     * ??????class??????
     * ?????????
     * ????????????InjectTest.class?????????????????????????????????class????????????????????????????????????????????????????????????class
     *
     * @param inputFile
     * @param dstFile
     */
    private void transformSingleFile(File inputFile, File dstFile) {
        System.out.println("transformSingleFile-->" + inputFile.getAbsolutePath());
        String path = inputFile.getAbsolutePath();
        String[] arr = path.split(File.separator);
        // convert packageName to file path
        String packagePath = packageName.replace(".", File.separator);
        if (inputFile.getAbsolutePath().contains(packagePath)) {
            System.out.println("inject class");
            if (first) {
                InjectApplication(packagePath, dstFile.getParent());
                first = false;
            }
            doInject(inputFile);
        }
        try {
            FileUtils.copyFile(inputFile, dstFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????jar
     * ???jar????????????????????????????????????
     *
     * @param inputJarFile
     * @param dstFile
     */
    private void transformJar(File inputJarFile, File dstFile) {
        try {
            FileUtils.copyFile(inputJarFile, dstFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transformJarItrnput(JarInput jarInput, File destFile) throws IOException {
        if (jarInput.getFile().getAbsolutePath().endsWith(".jar")) {
            //?????????????????????,??????????????????,?????????
            String jarName = jarInput.getName();
            System.out.println(jarName);
            String[] nameArr = jarName.split(":");
            if (nameArr.length < 2) {
                FileUtils.copyFile(jarInput.getFile(), destFile);
                return;
            }
            String key = "";
           for (int i = 0; i < nameArr.length-1; i++){
               key += nameArr[i];
           }
            if (!libs.containsKey(key)){
                System.out.println("lib key: " + key);
                FileUtils.copyFile(jarInput.getFile(), destFile);
                return;
            }

            JarFile jarFile = new JarFile(jarInput.getFile().getAbsolutePath());
            Enumeration enumeration = jarFile.entries();
            File tmpFile = new File(jarInput.getFile().getParent() + File.separator + "classes_temp.jar");
            //????????????????????????????????????
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
            //????????????
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) continue;
                //????????????class ????????????????????????-------------
                if (!entryName.contains("android") && (entryName.endsWith(".class"))) {
                    jarOutputStream.putNextEntry(zipEntry);
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream));
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                    //??????????????????   ?????????????????????
                    ClassVisitor cv = new LibVisitor(classWriter, entryName, key, result);
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES);
                    byte[] code = classWriter.toByteArray();
                    jarOutputStream.write(code);
                } else {
                    jarOutputStream.putNextEntry(zipEntry);
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();
            }
            //??????
            jarOutputStream.close();
            jarFile.close();
            //??????output??????
            FileUtils.copyFile(tmpFile, destFile);
            tmpFile.delete();
        }
    }

    private void InjectApplication(String filePackageName, String dstPath) {
        try {
            File Application = new File(dstPath + File.separator + "DokitApplication.class");
            FileUtils.touch(Application);
            System.out.println();
            ApplicationGenerator.createClass(Application, filePackageName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doInject(File inputFile) {
        try {
            InputStream inputStream = new FileInputStream(inputFile);
            ClassReader reader = new ClassReader(inputStream);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            PrivacyVisitor visitor = new PrivacyVisitor(writer, inputFile.getName(), packageName, result);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            byte[] code = writer.toByteArray();
            FileOutputStream fos = new FileOutputStream(inputFile);
            fos.write(code);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
