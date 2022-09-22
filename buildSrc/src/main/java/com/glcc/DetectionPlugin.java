package com.glcc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.android.build.gradle.AppExtension;

import java.util.Collections;

public class DetectionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");
        appExtension.registerTransform(new DetectionTransform(project), Collections.EMPTY_LIST);
    }
}