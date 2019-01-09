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
package org.olat.modules.ims.cp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ContentPackage;
import org.olat.ims.cp.objects.CPItem;
import org.olat.ims.cp.objects.CPOrganization;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The test class for the CPManager and its implementation.
 * 
 * <P>
 * Initial Date: Jun 11, 2009 <br>
 * 
 * @author gwassmann
 */
public class CPManagerTest extends OlatTestCase {

	private static final String ITEM_ID = "this_is_a_great_inital_item_identifier";
	private static final String PAGE_TITLE = "fancy page";
	private static final OLog log = Tracing.createLoggerFor(CPManagerTest.class);
	
	@Autowired
	private CPManager cpManager;
	
	private ContentPackage cp;

	@Before
	public void setUp() {
		// create some users with user manager
		try {
			log.info("setUp start ------------------------");
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(
					this.getClass(), Long.valueOf((long) (Math.random()*100000)));
			cp = cpManager.createNewCP(ores, PAGE_TITLE);
			assertNotNull("crated cp is null, check filesystem where the temp cp's are created.", cp);

		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}

	@After
	public void tearDown() {
		cp.getRootDir().delete();
	}
	
	@Test
	public void testLoad() {
		ContentPackage relodedCP = cpManager.load(cp.getRootDir(), cp.getResourcable());
		assertNotNull(relodedCP);
		CPOrganization orga = relodedCP.getFirstOrganizationInManifest();
		assertNotNull(orga);
		CPItem item = orga.getFirstItem();
		assertEquals(PAGE_TITLE, item.getTitle());
	}

	@Test
	public void testAddBlankPage() {
		final String pageTitle = "the blank page";
		String ident = cpManager.addBlankPage(cp, pageTitle);
		assertNotNull(ident);	
	}

	public void testAddElementAfter() {
		CPItem newItem = new CPItem();
		cpManager.addElementAfter(cp, newItem, ITEM_ID);
		assertTrue("The new item wasn't inserted at the second position.", newItem.getPosition() == 1);
	}

	@Test
	public void testGetItemTitle() {
		String title = cpManager.getItemTitle(cp, ITEM_ID);
		assertNotNull(title);
		assertEquals(PAGE_TITLE, title);
	}

	@Test
	public void testGetPageByItemId() {
		String href = cpManager.getPageByItemId(cp, ITEM_ID);
		VFSItem file = cp.getRootDir().resolve(href);
		assertNotNull("The file path doesn't lead to a file.", file);
	}
	
	@Test
	public void testWriteToFile() {
		cpManager.writeToFile(cp); // Throws exception on failure
	}
	
	@Test
	public void testWriteToZip() {
		VFSLeaf zip = cpManager.writeToZip(cp);
		Assert.assertNotNull("The zip file wasn't created properly", zip);
		Assert.assertTrue("The zip file cannot be empty", zip.getSize() > 0);
	}
}
