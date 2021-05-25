/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.custmgmt.internal;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

public class ContactAvatarServiceTest {

    private static ContactAvatarService avatarSvc;

    @BeforeClass
    public static void setUpClass() {
        avatarSvc = new ContactAvatarService("target");
    }

    @Test
    public void testSuccessfullyGenerateFile() throws Exception {
        ContactAvatarService avatar = new ContactAvatarService("target");
        assertTrue(avatar.create("TS").exists());
        assertTrue(avatar.create("VS").exists());
        assertTrue(avatar.create("JB").exists());
        assertTrue(avatar.create("JK").exists());
        assertTrue(avatar.create("PH").exists());
    }

    @Test
    public void testNullInput() throws Exception {
        File file = avatarSvc.create(null);
        System.out.println("Generated: "+file.getAbsolutePath());
        assertTrue(file.exists());
    }
}
