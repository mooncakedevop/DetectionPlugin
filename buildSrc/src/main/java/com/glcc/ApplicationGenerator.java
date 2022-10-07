package com.glcc;

import java.io.File;
import java.io.FileOutputStream;

public class ApplicationGenerator {
    public static void createClass(File application, String filePackageName) throws Exception {
        FileOutputStream fos = new FileOutputStream(application);
        fos.write(DokitApplicationDump.dump(filePackageName));
        fos.close();
    }
}