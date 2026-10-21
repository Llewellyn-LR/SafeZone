package com.safezone.app.res;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VectorDrawableResourcesTest {
    private static final Pattern PATH = Pattern.compile("<path\\b[^>]*>");
    private static final Pattern PATH_DATA = Pattern.compile("android:pathData=\"([^\"]+)\"");

    @Test
    public void everyVectorDrawableHasAtLeastOnePathWithData() throws Exception {
        File dir = new File("src/main/res/drawable");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        assertTrue("drawable folder not found at " + dir.getAbsolutePath(), files != null && files.length > 0);

        for (File f : files) {
            String xml = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            if (!xml.contains("<vector")) continue;
            Matcher paths = PATH.matcher(xml);
            int count = 0;
            while (paths.find()) {
                count++;
                Matcher data = PATH_DATA.matcher(paths.group());
                assertTrue(f.getName() + ": a <path> has no pathData", data.find());
                assertFalse(f.getName() + ": empty pathData", data.group(1).trim().isEmpty());
            }
            assertTrue(f.getName() + ": vector has no <path> at all", count > 0);
        }
    }
}
