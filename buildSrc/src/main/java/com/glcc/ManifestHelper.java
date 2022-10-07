package com.glcc;

import com.glcc.bean.Permission;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManifestHelper {
    private Document doc;
    private String pkgName;
    private List<Permission> permissions;
    private List<String> activities;

    public ManifestHelper(String xmlPath) {

        try {
            File f = new File(xmlPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//步骤1
            DocumentBuilder builder = factory.newDocumentBuilder();//步骤2
            this.doc = builder.parse(f);//步骤3

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPackageName() {
        if (this.pkgName != null) return this.pkgName;
        Node node = doc.getFirstChild();
        NamedNodeMap attrs = node.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.item(i).getNodeName() == "package") {
                this.pkgName = attrs.item(i).getNodeValue();
            }
        }
        return this.pkgName;
    }

    public List<Permission> getPermissions() {
        if (this.permissions != null) return permissions;
        //get permissions
        NodeList permissionList = this.doc.getElementsByTagName("uses-permission");
        this.permissions = new ArrayList<>();
        Map<String, Permission> map = Util.readPermissionConfig();

        for (int i = 0; i < permissionList.getLength(); i++) {
            Node permission = permissionList.item(i);
            String key = permission.getAttributes().item(0).getNodeValue();
            Permission p;
            if(map.containsKey(key)){
                p = map.get(key);
                System.out.println(key);
            } else {
                p = new Permission();
                p.setName("未知权限");
                p.setPattern(key);
            }
            this.permissions.add(p);
        }

        return this.permissions;
    }

    public List<String> getActivities() {
        if (this.activities != null) return this.activities;
        // get activities
        NodeList activityList = doc.getElementsByTagName("activity");
        this.activities = new ArrayList<>();

        for (int i = 0; i < activityList.getLength(); i++) {
            Node activity = activityList.item(i);
            NamedNodeMap attrs = activity.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                if (attrs.item(j).getNodeName() == "android:name") {
                    String sTem = attrs.item(j).getNodeValue();
                    if (sTem.startsWith(".")) {
                        sTem = this.pkgName + sTem;
                    }
                    this.activities.add(sTem);
                }
            }
        }
        return this.activities;
    }
}
