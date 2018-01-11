/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.resource.internal.gdrive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class GDriveRepositoryTest {

    private static final String UPLOADED_RESOURCE_MIME = "image/png";
    private static final String UPLOADED_RESOURCE_TITLE = "test.png";
    private static final File DOWNLOADS_DIR = new File(".");
    private static final boolean USE_DIRECT_DOWNLOAD = true;
    private static GDriveRepository repo;

    @Rule
    public MethodRule stopSomeMethods = new MethodRule() {
        @Override
        public Statement apply(Statement base, FrameworkMethod method,
                Object target) {
            if (repo != null) {
                System.out.println("Have GDrive connection, continuing...");
                return base;
            } else {
                System.out
                        .println("No GDrive connection, probably due to not having any credentials.");
                return new Statement() {
                    @Override
                    public void evaluate() { /* do nothing */
                    }
                };
            }
        }

    };

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            repo = new GDriveRepository();
        } catch (Exception e) {
            ;
        }
    }

    @Test
    public void testPushSmallPngToRepo() {
        System.out.println("testPushSmallPngToRepo");
        pushImageToRepo(UPLOADED_RESOURCE_TITLE, UPLOADED_RESOURCE_MIME);
    }

    @Test
    public void testPushLargeJpgToRepo() {
        pushImageToRepo("tux.jpg", "image/jpeg");
    }

    private void pushImageToRepo(String resourceTitle, String resourceMime) {
        InputStream is = null;
        try {
            is = getClass()
                    .getResourceAsStream("/static/images/" + resourceTitle);
            assertTrue("Could not find test resource to store.", is != null);
            repo.write(resourceTitle, resourceMime, null, is);
        } catch (IOException e) {
            fail("Failed to store: " + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // the google api says it will take care of close but just in
                // case...
            }
        }

        try {
            com.google.api.services.drive.model.File uploaded = repo
                    .getMetadataOfLastUpload();
            if (uploaded.getDownloadUrl() != null) {
                // if test skipped the upload due to it already existing then
                // this will fail. If this happened for some other reason search
                // test should catch that.
                // TODO maybe it would be better to do the search test here
                repo.downloadFile(DOWNLOADS_DIR, USE_DIRECT_DOWNLOAD, uploaded);
                assertEquals(resourceMime, uploaded.getMimeType());
            }
            assertEquals(resourceTitle, uploaded.getTitle());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to retrieve resource just stored.");
        }
    }

    @Test
    public void testSearchForSpecificFile() {
        try {
            List<com.google.api.services.drive.model.File> search = repo
                    .search("title = '" + UPLOADED_RESOURCE_TITLE + "'");
            System.out.println("  found: " + search.size() + " entries.");
            assertEquals(1, search.size());
            for (com.google.api.services.drive.model.File file : search) {
                System.out.println("  " + file.getId() + ":" + file.getTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    // @Test
    // public void testSearchForFolders() {
    // try {
    // List<com.google.api.services.drive.model.File> search = repo
    // .search("title = '" + UPLOADED_RESOURCE_TITLE + "'");
    // System.out.println("  found: " + search.size() + " entries.");
    // assertEquals(1, search.size());
    // for (com.google.api.services.drive.model.File file : search) {
    // System.out.println("  " + file.getId() + ":" + file.getTitle());
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // fail();
    // }
    // }
}
