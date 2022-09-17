package com.glcc;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class ApplicationGenerator {

    public static void createClass(String path) throws Exception {
        System.out.println("******** start creat class");
        System.out.println(path);

        //拷贝到javac
            FileUtils.writeByteArrayToFile(
                    new File(path + "/com/detectionplugin/DokitApplication.class"),
                    DokitApplicationDump.dump());
//        FileUtils.writeByteArrayToFile(
//                new File(path + "/intermediates/transforms/auto-log/debug/0/com/example/autolog/FakeClass.class"),
//                cw.toByteArray());
    }



}