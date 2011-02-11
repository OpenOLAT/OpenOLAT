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
* <p>
*/ 

package org.olat.resource.references;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.OlatTestCase;


/**
 * 
 */
public class ReferenceManagerTest extends OlatTestCase {

	private static Logger log = Logger.getLogger(ReferenceManagerTest.class.getName());
	private static Long RESOURCABLE_ID_1 = new Long(123);
	private static Long RESOURCABLE_ID_2 = new Long(456);
	private static Long RESOURCABLE_ID_3 = new Long(457);


	// Already tested in BusinessGroupTest :
	//  - getGroupsWithPermissionOnOlatResourceable
	//  - getIdentitiesWithPermissionOnOlatResourceable
	/**
	 * 
	 */
	@Test public void testAddReference() {
		OLATResource oressource = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type1", RESOURCABLE_ID_1));
		OLATResource orestarget = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_2));
		String udata = "üserdätä";
		
		// add a reference
		ReferenceManager.getInstance().addReference(oressource, orestarget, udata);
		DBFactory.getInstance().closeSession();
		
		OLATResource orestarget2 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_2));
		List refs = ReferenceManager.getInstance().getReferencesTo(orestarget2);
		for (Iterator it_refs = refs.iterator(); it_refs.hasNext();) {
			ReferenceImpl ref = (ReferenceImpl) it_refs.next();
			System.out.println("ref:"+ref);
		}
		assertTrue("only one reference may exist", refs.size() == 1);
	}
	
	@Test public void testReferencesToAndFrom() {
		// same resouceable id on purpose
		OLATResource s1 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("s1rrtype1",  RESOURCABLE_ID_1));
		OLATResource s2 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("s2rrtype1",  RESOURCABLE_ID_1));
		OLATResource s3 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("s31rrtype1", RESOURCABLE_ID_1));
		OLATResource t1 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("t1rrtype1",  RESOURCABLE_ID_1));
		OLATResource t2 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("t2rrtype1",  RESOURCABLE_ID_1));
		
		// add references
		ReferenceManager.getInstance().addReference(s1,t1,"r11");
		ReferenceManager.getInstance().addReference(s2,t1,"r21");
		ReferenceManager.getInstance().addReference(s2,t2,"r22");
		ReferenceManager.getInstance().addReference(s3,t2,"r32");
		
		DBFactory.getInstance().closeSession();

		// find the refs again
		
		List s1R = ReferenceManager.getInstance().getReferences(s1);
		assertTrue("s1 only has one reference", s1R.size() == 1);
		ReferenceImpl ref = (ReferenceImpl)s1R.get(0);
		assertTrue("source and s1 the same", OresHelper.equals(ref.getSource(), s1));
		assertTrue("target and t1 the same", OresHelper.equals(ref.getTarget(), t1));
		
		// two refs from s2
		List s2refs = ReferenceManager.getInstance().getReferences(s2);
		assertTrue ("s2 holds two refs (to t1 and t2)", s2refs.size() == 2);
		
		// two refs to t2
		List t2refs = ReferenceManager.getInstance().getReferencesTo(t2);
		assertTrue ("t2 holds two source refs (to s2 and s3)", t2refs.size() == 2);		
		
	}
	
	@Test public void testAddAndDeleteReference() {
		OLATResource oressource = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type1", RESOURCABLE_ID_1));
		OLATResource orestarget = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_3));
		String udata = "üserdätä";
		
		// add a reference
		ReferenceManager.getInstance().addReference(oressource, orestarget, udata);
		DBFactory.getInstance().closeSession();
				
		OLATResource orestarget2 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_3));
		List refs = ReferenceManager.getInstance().getReferencesTo(orestarget2);
		assertTrue("only one reference may exist", refs.size() == 1);
		for (Iterator it_refs = refs.iterator(); it_refs.hasNext();) {
			ReferenceImpl ref = (ReferenceImpl) it_refs.next();
			ReferenceManager.getInstance().delete(ref);
		}

		DBFactory.getInstance().closeSession();

		// now make sure the reference was deleted
		OLATResource orestarget3 = OLATResourceManager.getInstance().findOrPersistResourceable(OresHelper.createOLATResourceableInstance("type2targ", RESOURCABLE_ID_3));
		List norefs = ReferenceManager.getInstance().getReferencesTo(orestarget3);
		assertTrue("reference should now be deleted", norefs.size() == 0);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After public void tearDown() throws Exception {
		try {
			//DB.getInstance().delete("select * from o_bookmark");
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}
}