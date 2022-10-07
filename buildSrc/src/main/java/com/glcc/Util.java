package com.glcc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.glcc.bean.Permission;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Util {
    private static String DefaultPath = "/Users/mooncake/IdeaProjects/DetectionPlugin/buildSrc/src/main/resources/permission.json";

    public static Map<String, Permission> readPermissionConfig() {
        String jsonStr = null;
        try {
            jsonStr = FileUtils.readFileToString(new File(DefaultPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject obj = JSON.parseObject(jsonStr);
        JSONArray objArr = (JSONArray) obj.get("config");
        HashMap<String, Permission> map = new HashMap<>();

        for (int i = 0; i < objArr.size(); i++) {
            JSONObject o = (JSONObject) objArr.get(i);
            Permission permission = new Permission();
            permission.setName((String) o.get("name"));
            permission.setDesc((String) o.get("desc"));
            permission.setPattern((String) o.get("pattern"));
            permission.setSensitive((Boolean) o.get("sensitive"));
            map.put(permission.getPattern(), permission);
        }
        return map;
    }
}
