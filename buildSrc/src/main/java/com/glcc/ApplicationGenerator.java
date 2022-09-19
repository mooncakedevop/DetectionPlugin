package com.glcc;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.TransformOutputProvider;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class ApplicationGenerator {

    public static void createClass(File dir,DirectoryInput directoryInput, TransformOutputProvider outputProvider) throws Exception {
        System.out.println("******** start creat class");
//        System.out.println(path);
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY);
        System.out.println("dest: " + dest);
        System.out.println("input: " +directoryInput.getFile());
        System.out.println("dir: "+ dir.getAbsolutePath());
        String o = dir.getAbsolutePath().replace(directoryInput.getFile().getAbsolutePath(), dest.getAbsolutePath());
        File Application = new File(o + "/detectionplugin/DokitApplication.class");
        FileUtils.touch(Application);
        System.out.println("output: " + Application.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(Application);
        fos.write(DokitApplicationDump.dump());
        fos.close();

    }



}