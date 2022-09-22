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

    public static void createClass(File application, String filePackageName) throws Exception {
        FileOutputStream fos = new FileOutputStream(application);
        fos.write(DokitApplicationDump.dump(filePackageName));
        fos.close();
    }



}