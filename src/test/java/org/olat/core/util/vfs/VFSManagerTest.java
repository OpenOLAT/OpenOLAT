/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.util.vfs;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 
 * <P>
 * Initial Date:  16.06.2009 <br>
 * @author patrickb
 */
public class VFSManagerTest {

	/**
	 * Test method for {@link org.olat.core.util.vfs.VFSManager#sanitizePath(java.lang.String)}.
	 */
	@Test 
	public void testSanitizePath() {
		/*
		 * Make sure we always have a path that starts with a "/".
		 */
		String path1 = null;
		String path1_expected = "/";
		assertTrue(VFSManager.sanitizePath(path1).equals(path1_expected));
		
		String path2 = "";
		String path2_expected = "/";
		assertTrue(VFSManager.sanitizePath(path2).equals(path2_expected));
		
		String path3 = "/";
		String path3_expected = "/";
		assertTrue(VFSManager.sanitizePath(path3).equals(path3_expected));
		
		String path4 = ".";
		String path4_expected = "/.";
		assertTrue(VFSManager.sanitizePath(path4).equals(path4_expected));
		
		String path5 = "./";
		String path5_expected = "/."; // (cut trailing slash)
		assertTrue(VFSManager.sanitizePath(path5).equals(path5_expected));
		
		String path6 = "cutTrailingSlash/";
		String path6_expected = "/cutTrailingSlash";
		assertTrue(VFSManager.sanitizePath(path6).equals(path6_expected));

		String path7 = "../";
		try {
			VFSManager.sanitizePath(path7);
			fail();
		} catch (IllegalArgumentException e) {
		}

		String path8 = "/..";
		try {
			VFSManager.sanitizePath(path8);
			fail();
		} catch (IllegalArgumentException e) {
		}

		String path9 = "/../";
		try {
			VFSManager.sanitizePath(path9);
			fail();
		} catch (IllegalArgumentException e) {
		}

		String path10 = "/.../";
		try {
			VFSManager.sanitizePath(path10);
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
}
