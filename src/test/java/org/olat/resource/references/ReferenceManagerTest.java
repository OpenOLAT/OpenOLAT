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

package org.olat.resource.references;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 */
public class ReferenceManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private OLATResourceManager resourceManager;
	
	
	// Already tested in BusinessGroupTest :
	//  - getGroupsWithPermissionOnOlatResourceable
	//  - getIdentitiesWithPermissionOnOlatResourceable
	/**
	 * 
	 */
	@Test
	public void testAddReference() {
		OLATResource oressource = JunitTestHelper.createRandomResource();
		OLATResource orestarget = JunitTestHelper.createRandomResource();
		String udata = "üserdätä";
		
		// add a reference
		referenceManager.addReference(oressource, orestarget, udata);
		dbInstance.closeSession();
		
		OLATResourceable targetOres = OresHelper.createOLATResourceableInstance(orestarget.getResourceableTypeName(), orestarget.getResourceableId());
		OLATResource orestarget2 = resourceManager.findOrPersistResourceable(targetOres);
		List<ReferenceImpl> refs = referenceManager.getReferencesTo(orestarget2);
		Assert.assertNotNull(refs);
		Assert.assertEquals("only one reference may exist", 1, refs.size());
		Assert.assertEquals(oressource, refs.get(0).getSource());
	}
	
	@Test
	public void testReferencesToAndFrom() {
		// same resouceable id on purpose
		OLATResource s1 = JunitTestHelper.createRandomResource();
		OLATResource s2 = JunitTestHelper.createRandomResource();
		OLATResource s3 = JunitTestHelper.createRandomResource();
		OLATResource t1 = JunitTestHelper.createRandomResource();
		OLATResource t2 = JunitTestHelper.createRandomResource();
		
		// add references
		referenceManager.addReference(s1,t1,"r11");
		referenceManager.addReference(s2,t1,"r21");
		referenceManager.addReference(s2,t2,"r22");
		referenceManager.addReference(s3,t2,"r32");
		
		dbInstance.closeSession();

		// find the refs again with DB resource
		List<ReferenceImpl> s1R =referenceManager.getReferences(s1);
		Assert.assertEquals("s1 only has one reference", 1, s1R.size());
		ReferenceImpl ref = (ReferenceImpl)s1R.get(0);
		Assert.assertEquals("source and s1 the same", s1, ref.getSource());
		Assert.assertEquals("target and t1 the same", t1, ref.getTarget());
		
		// find the same refs again with  resourceable
		OLATResourceable s1Ores = OresHelper.createOLATResourceableInstance(s1.getResourceableTypeName(), s1.getResourceableId());
		List<ReferenceImpl> s1Rb =referenceManager.getReferences(s1Ores);
		Assert.assertEquals("s1 only has one reference", 1, s1Rb.size());
		ReferenceImpl refb = (ReferenceImpl)s1R.get(0);
		Assert.assertEquals("source and s1 the same", s1, refb.getSource());
		Assert.assertEquals("target and t1 the same", t1, refb.getTarget());
		
		// two refs from s2
		List<ReferenceImpl> s2refs = referenceManager.getReferences(s2);
		Assert.assertEquals("s2 holds two refs (to t1 and t2)", 2, s2refs.size());
		
		// two refs to t2
		List<ReferenceImpl> t2refs = referenceManager.getReferencesTo(t2);
		Assert.assertEquals("t2 holds two source refs (to s2 and s3)", 2, t2refs.size());		
	}
	
	@Test
	public void testAddAndDeleteReference() {
		OLATResource oressource = JunitTestHelper.createRandomResource();
		OLATResource orestarget = JunitTestHelper.createRandomResource();
		String udata = "üserdätä";
		// add a reference
		referenceManager.addReference(oressource, orestarget, udata);
		dbInstance.commitAndCloseSession();
				
		OLATResourceable orestarget2 = OresHelper.createOLATResourceableInstance(orestarget.getResourceableTypeName(), orestarget.getResourceableId());
		List<ReferenceImpl> refs = referenceManager.getReferencesTo(orestarget2);
		Assert.assertEquals("only one reference may exist", 1, refs.size());
		dbInstance.commitAndCloseSession();
		
		for (ReferenceImpl ref : refs) {
			referenceManager.delete(ref);
		}
		dbInstance.commitAndCloseSession();

		// now make sure the reference was deleted
		OLATResourceable orestarget3 = OresHelper.createOLATResourceableInstance(orestarget.getResourceableTypeName(), orestarget.getResourceableId());
		List<ReferenceImpl> norefs = referenceManager.getReferencesTo(orestarget3);
		assertTrue("reference should now be deleted", norefs.isEmpty());
	}
	
	@Test
	public void testAddAndDeleteAllReferences() {
		OLATResource oressource = JunitTestHelper.createRandomResource();
		OLATResource orestarget1 = JunitTestHelper.createRandomResource();
		OLATResource orestarget2 = JunitTestHelper.createRandomResource();
		// add the references
		referenceManager.addReference(oressource, orestarget1, "üserdätä");
		referenceManager.addReference(oressource, orestarget2, "éserdàtà");
		dbInstance.commitAndCloseSession();
				
		List<ReferenceImpl> refs = referenceManager.getReferences(oressource);
		Assert.assertEquals("2 references exist", 2, refs.size());
		dbInstance.commitAndCloseSession();
		
		referenceManager.deleteAllReferencesOf(oressource);
		dbInstance.commitAndCloseSession();

		// now make sure the reference was deleted

		List<ReferenceImpl> norefs = referenceManager.getReferences(oressource);
		assertTrue("reference should now be deleted", norefs.isEmpty());
	}
	
}