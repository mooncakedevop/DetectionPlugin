package com.glcc;

import com.android.build.api.transform.*;
import com.quinn.hunter.transform.HunterTransform;
import com.quinn.hunter.transform.RunVariant;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;


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
        Collection<TransformInput> inputs =  transformInvocation.getInputs();
        //copy jar
        transformInvocation.getInputs().forEach( transformInput -> {
            transformInput.getJarInputs().forEach(jarInput -> {
                File jarFile = jarInput.getFile();
                File destJar = outputProvider.getContentLocation(jarInput.getName(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(), Format.JAR);
               transformJar(jarFile,destJar);
            });
        });
        inputs.forEach(transformInput -> transformInput.getDirectoryInputs().forEach( directoryInput -> {
            File dstFile = outputProvider.getContentLocation(
                    directoryInput.getName(),
                    directoryInput.getContentTypes(),
                    directoryInput.getScopes(),
                    Format.DIRECTORY);
            // 执行转化整个目录
            transformDir(directoryInput.getFile(), dstFile);

        }));

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
                // 递归
                transformDir(file, dstFile);
            } else if (file.isFile()) {
                // 转化单个class文件
                transformSingleFile(file, dstFile);
            }
        }
    }

    /**
     * 转化class文件
     * 注意：
     *      这里只对InjectTest.class进行插桩，但是对于其他class要原封不动的拷贝过去，不然结果中就会缺少class
     * @param inputFile
     * @param dstFile
     */
    private void transformSingleFile(File inputFile, File dstFile) {
        System.out.println("transformSingleFile-->" + inputFile.getAbsolutePath());
        String path =  inputFile.getAbsolutePath();
        String[] arr = path.split(File.separator);
        // convert packageName to file path
        String packagePath = packageName.replace(".", File.separator);
        System.out.println("package path"+ packagePath);
        System.out.println("input path" + inputFile.getAbsolutePath());
        if (inputFile.getAbsolutePath().contains(packagePath)) {
            System.out.println("");
            if(first){
                InjectApplication(packagePath,dstFile.getParent());
                first = false;
            }
                doInject(inputFile);
        }
        try {
            FileUtils.copyFile(inputFile,dstFile,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转化jar
     * 对jar暂不做处理，所以直接拷贝
     * @param inputJarFile
     * @param dstFile
     */
    private void transformJar(File inputJarFile, File dstFile) {
        try {
            FileUtils.copyFile(inputJarFile,dstFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void InjectApplication(String filePackageName,String dstPath) {
        try {
            System.out.println("dst: " + dstPath);
            System.out.println("pkg: " + filePackageName);
            File Application = new File(dstPath  + File.separator +"DokitApplication.class");
            FileUtils.touch(Application);
            System.out.println("add class: "  + Application.getAbsolutePath());
            System.out.println();
            ApplicationGenerator.createClass(Application, filePackageName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private  void doInject(File inputFile) {
        try {
            InputStream inputStream = new FileInputStream(inputFile);
            ClassReader reader = new ClassReader(inputStream);
            ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
            PrivacyVisitor visitor = new PrivacyVisitor(writer, inputFile.getName());
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
