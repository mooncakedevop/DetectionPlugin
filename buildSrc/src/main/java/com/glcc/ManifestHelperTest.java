package com.glcc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManifestHelperTest {

    @Test
    void getPermissions() {
        ManifestHelper m = new ManifestHelper("/Users/mooncake/IdeaProjects/DetectionPlugin/app/src/main/AndroidManifest.xml");
        m.getPermissions();
    }
}