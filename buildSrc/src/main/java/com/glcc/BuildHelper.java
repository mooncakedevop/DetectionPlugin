package com.glcc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildHelper {
    public Pattern pattern = Pattern.compile(".*implementation.*'.*'");
    public Pattern dependency = Pattern.compile("'.+'");
    public HashMap<String, String> readGradle(String filePath) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        Path path = Paths.get(filePath);
        List<String>  lines = Files.readAllLines(path, Charset.defaultCharset());
        for (String line: lines){
            if(pattern.matcher(line).matches()){
                Matcher matcher = dependency.matcher(line);
                if(matcher.find()){
                    String[] infos = matcher.group().split(":");
                    String org = infos[0];
                    String product = infos[1];
                    String version = infos[2];
                    map.put(org + ":" + product, version);
                    System.out.println("organization:" + org+ " product:" + product + "  version: " + version);
                }
            }
        }
        return map;
    }
    public static void main(String[] args){
        BuildHelper b = new BuildHelper();
        try {
            b.readGradle("/Users/mooncake/IdeaProjects/car-app-static-analysis/src/main/resources/build.gradle");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


