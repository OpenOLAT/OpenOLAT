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

package org.olat.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Mar 11, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class PropertyTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(PropertyTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private PropertyManager pm;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	/**
	 * testGenericInsertFindDelete
	 */
	@Test
	public void testGenericInsertFindDelete() {
		//create resource, identity and group
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("prop-1-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "a buddygroup", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		Property p = pm.createPropertyInstance(identity, group, ores, "catgeneric", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		dbInstance.commitAndCloseSession();
		
		p = pm.findProperty(identity, group, ores, "catgeneric", "TestProperty");
		assertNotNull(p);
		assertEquals(p.getStringValue(), "stringValue");
		assertEquals(p.getFloatValue(), Float.valueOf(1.1f));
		assertEquals(p.getLongValue(), Long.valueOf(123456));
		assertEquals(p.getTextValue(), "textValue");
		
		pm.deleteProperty(p);
		p = pm.findProperty(identity, group, ores, "catgeneric", "TestProperty");
		assertNull(p);
	}
	
	@Test
	public void testFindWithResourceIdList() {
		//create resource, identity
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("prop-2-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		//create the property
		Property p = pm.createPropertyInstance(identity, null, ores, "catidlist", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		dbInstance.commitAndCloseSession();
		
		//check with empty list
		List<Property> emptyProps = pm.findProperties(ores.getResourceableTypeName(), Collections.<Long>emptyList(), "catidlist", "TestProperty");
		assertNotNull(emptyProps);
		Assert.assertTrue(emptyProps.isEmpty());
		
		//check with a real value and a dummy
		List<Long> resIds = new ArrayList<>();
		resIds.add(ores.getResourceableId());
		resIds.add(2456l);//dummy
		List<Property> props = pm.findProperties(ores.getResourceableTypeName(), resIds, "catidlist", "TestProperty");
		assertNotNull(props);
		Assert.assertEquals(1, props.size());
		Assert.assertEquals(p, props.get(0));
	}
	
	@Test
	public void testFindWithIdentityList() {
		//create identities, resource and properties
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("cat-id-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("cat-id-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("cat-id-3-" + UUID.randomUUID().toString());
		OLATResource ores = JunitTestHelper.createRandomResource();
		Property p1 = pm.createPropertyInstance(id1, null, ores, "catidentlist", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p1);
		Property p2 = pm.createPropertyInstance(id2, null, ores, "catidentlist", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p2);
		Property p3 = pm.createPropertyInstance(id3, null, ores, "catidentlist", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p3);
		dbInstance.commitAndCloseSession();
		
		//check with empty list
		List<Property> emptyProps = pm.findProperties(Collections.<Identity>emptyList(), ores, "catidentlist", "TestProperty");
		assertNotNull(emptyProps);
		Assert.assertTrue(emptyProps.isEmpty());
		
		//check with a real value and a dummy
		List<Identity> identities = new ArrayList<>();
		identities.add(id1);
		identities.add(id2);
		
		List<Property> props = pm.findProperties(identities, ores, "catidentlist", "TestProperty");
		assertNotNull(props);
		Assert.assertEquals(2, props.size());
		Assert.assertTrue(props.contains(p1));
		Assert.assertTrue(props.contains(p2));
	}
	
	/**
	 * testGetAllResourceTypeNames
	 */
	@Test
	public void testGetAllResourceTypeNames() {
		//create resource, identity and group
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("prop-3-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "a buddygroup", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		//create a property
		Property p = pm.createPropertyInstance(identity, group, ores, "catall", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		dbInstance.commitAndCloseSession();
			
		Assert.assertNotNull(p.getResourceTypeName());
		List<String> resTypeNames = pm.getAllResourceTypeNames();
		assertTrue(resTypeNames.contains(ores.getResourceableTypeName()));
	}
	
	/**
	 * testListProperties
	 */
	@Test
	public void testListProperties() {
		//create resource, identity and group
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("prop-4-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "a buddygroup", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		Property p = pm.createPropertyInstance(identity, group, ores, "cat", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		
		List<Property> entries = PropertyManager.getInstance().listProperties(identity, group, ores.getResourceableTypeName(), ores.getResourceableId(), "cat", "TestProperty");
		Assert.assertNotNull(entries);
		Assert.assertEquals(1,  entries.size());
		
		Property prop = entries.get(0);
		assertEquals(ores.getResourceableTypeName(), prop.getResourceTypeName());
		assertEquals(ores.getResourceableId(), prop.getResourceTypeId());
		
		int numOfEntries = PropertyManager.getInstance().countProperties(identity, group, ores.getResourceableTypeName(), ores.getResourceableId(), "cat", "TestProperty", null, null);
		Assert.assertEquals(entries.size(), numOfEntries);
	}
	
	/**
	 * testUserInsertFindDelete
	 */
	@Test
	public void testUserInsertFindDelete() {
		//create identity and property
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("user-prop-" + UUID.randomUUID().toString());
		Property p = pm.createUserPropertyInstance(id, "catuser", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		dbInstance.commitAndCloseSession();
		
		p = pm.findUserProperty(id, "catuser", "TestProperty");
		assertNotNull(p);
		assertEquals(p.getStringValue(), "stringValue");
		assertEquals(p.getFloatValue(), Float.valueOf(1.1f));
		assertEquals(p.getTextValue(), "textValue");
		
		pm.deleteProperty(p);
		p = pm.findUserProperty(id, "catuser", "TestProperty");
		assertNull(p);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteUserData() {
		//create some identities and properties
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("del-user-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("del-user-2-" + UUID.randomUUID().toString());
		Property p10 = pm.createPropertyInstance(id1, null, null, "prop-del-1", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p10);
		Property p11 = pm.createPropertyInstance(id1, null, null, "prop-del-2", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p11);
		Property p20 = pm.createPropertyInstance(id2, null, null, "prop-del-3", "TestProperty", Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p20);
		dbInstance.commitAndCloseSession();
		
		//delete user 1 datas
		pm.deleteUserData(id1, "del-" + id1.getName());
		dbInstance.commitAndCloseSession();
		
		//check if really deleted
		Property p10x = pm.findUserProperty(id1, "prop-del-1", "TestProperty");
		assertNull(p10x);
		Property p11x = pm.findUserProperty(id1, "prop-del-2", "TestProperty");
		assertNull(p11x);
		
		//check if id2 has still its properties
		Property p20x = pm.findUserProperty(id2, "prop-del-3", "TestProperty");
		assertNotNull(p20x);
	}
	
	/**
	 * Performance test of 500 propertycreations per type.
	 * Rename to testPerf500Properties to include this test in the test suit.
	 */
	@Test
	public void testPerf500Properties() {
		//create identity, group and resource
		OLATResource res = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("prop-5-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "a buddygroup", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();


		long start, stop;
		long count = 500;
		
		// create generic proerties
		log.info("----------------------------------------------------------------");
		log.info("Performance test startet. Running " + count + " cycles per test.");
		log.info("CREATE generic property test started...");
		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			Property p = pm.createPropertyInstance(identity, group, res, "perf500", "TestProperty" + i, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
			pm.saveProperty(p);
			
			if(i % 50 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		dbInstance.commitAndCloseSession();
		
		stop = System.currentTimeMillis();
		log.info("CREATE generic property test: " + (stop-start) + " ms (" + (count * 1000 / (stop-start)) + "/sec)");
		// some find identitites tests
		List<Identity> ids = pm.findIdentitiesWithProperty(null, null, "perf500", null, false);
		Assert.assertNotNull("Identities cannot be null", ids);
		Assert.assertFalse("Identities cannot be empty", ids.isEmpty());
		Assert.assertTrue("Identities must contains reference identity", ids.contains(identity));
		
		// create course and user properties
		log.info("Preparing user/group properties test. Creating additional properties..");
		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			Property pUser = pm.createUserPropertyInstance(identity, "perf500", "TestProperty" + i, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
			pm.saveProperty(pUser);
			if(i % 50 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		dbInstance.commitAndCloseSession();
		
		stop = System.currentTimeMillis();
		log.info("Ready : " + (stop-start) + " ms (" + (2* count * 1000 / (stop-start)) + "/sec)");
		log.info("Starting find tests. DB holds " + count * 3 + " records.");
		
		// find generic property test
		log.info("FIND generic property test started...");
		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			Property p = pm.findProperty(identity, group, res, "perf500", "TestProperty" + i);
			assertNotNull("Must find the p (count=" + i + ")", p);
			dbInstance.commitAndCloseSession();
		}
		stop = System.currentTimeMillis();
		log.info("FIND generic property test: " + (stop-start) + " ms (" + (count * 1000 / (stop-start)) + "/sec)");
		
		// find user property test
		log.info("FIND user property test started...");
		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			Property p = pm.findUserProperty(identity, "perf500", "TestProperty" + i);
			assertNotNull("Must find the p (count=" + i + ")", p);
			dbInstance.commitAndCloseSession();
		}
		stop = System.currentTimeMillis();
		log.info("FIND user property test: " + (stop-start) + " ms (" + (count * 1000 / (stop-start)) + "/sec)");

		// find & delete
		log.info("FIND and DELETE generic property test started...");
		start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			Property p = pm.findUserProperty(identity, "perf500", "TestProperty" + i);
			pm.deleteProperty(p);
		}
		stop = System.currentTimeMillis();
		log.info("FIND and DELETE generic property test: " + (stop-start) + " ms (" + (count * 1000 / (stop-start)) + "/sec)");

		log.info("----------------------------------------------------------------");
		log.info("Performance test finished.");
	}
	
	/**
	 * testFloatValues
	 * THIS test does only success when you run it against mysql with FLOAT(65,30). FLOAT(65,30) is mysql specific and if you let hibernate generate
	 * the tables automatic it will result in FLOAT only and this test will fail. So this means that you have to populate the tables yourself via the sql file.
	 */
	@Test
	public void testFloatValues() {
		//create identity, group and resource
		OLATResource res = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("prop-6-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "a buddygroup", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		
	  Property original, copy;
	  //gs:changed to FLOAT(65,30) to be compatible with hsqldb and auto generated ddl
	  // Define my own MAX float value because the db precision changed from DECIMAL(78,36) to DECIMAL(65,30)   
	  double floatMaxValue = 1E34;  // 1E35 does failed with mysql 5.0.x
		original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", Float.valueOf(1234534343424213.1324534533456f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(original);
		//DBFactory.getInstance().evict(original);
		dbInstance.commitAndCloseSession();
		copy = pm.findProperty(identity, group, res, "cat", "TestProperty");		
		float f1F = original.getFloatValue().floatValue();
		float copyF = copy.getFloatValue().floatValue();
		assertEquals("values differ:"+f1F+", and "+copyF, f1F, copyF, 0.0000000001f);
	  pm.deleteProperties(identity, group, res, "cat", "TestProperty");
		
	  // note: on mysql 5.0, the standard installation is strict mode, which reports any data truncation error as a real jdbc error.
	  // -1e35 seems out of range for DECIMAL(65,30) so data truncation occurs??? 
	  //                                          +-> 30 digits to the right side of decimal point
	  // From mysql:
	  // The declaration syntax for a DECIMAL column is DECIMAL(M,D). The ranges of values for the arguments in MySQL 5.1 are as follows:
	  // M is the maximum number of digits (the precision). It has a range of 1 to 65. (Older versions of MySQL allowed a range of 1 to 254.)
	  // D is the number of digits to the right of the decimal point (the scale). It has a range of 0 to 30 and must be no larger than M.
		original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", Float.valueOf((float)-floatMaxValue), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(original);
		dbInstance.commitAndCloseSession();
		copy = pm.findProperty(identity, group, res, "cat", "TestProperty");//this one failes at the moment for hsqldb with: incompatible data type in conversion: from SQL type DECIMAL to java.lang.Double, value: -9999999790214767953607394487959552.000000000000000000000000000000
		Assert.assertEquals(original.getFloatValue().floatValue(), copy.getFloatValue().floatValue(), 0.00001);
	    pm.deleteProperties(identity, group, res, "cat", "TestProperty");

		original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", Float.valueOf((float)floatMaxValue), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(original);
		dbInstance.commitAndCloseSession();
		copy = pm.findProperty(identity, group, res, "cat", "TestProperty");		
		Assert.assertEquals(original.getFloatValue().floatValue(), copy.getFloatValue().floatValue(), 0.00001);
	    pm.deleteProperties(identity, group, res, "cat", "TestProperty");

		original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", Float.valueOf(Long.MAX_VALUE), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(original);
		dbInstance.commitAndCloseSession();
		copy = pm.findProperty(identity, group, res, "cat", "TestProperty");		
		Assert.assertEquals(original.getFloatValue().floatValue(), copy.getFloatValue().floatValue(), 0.00001);
	    pm.deleteProperties(identity, group, res, "cat", "TestProperty");

		original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", Float.valueOf(Long.MIN_VALUE), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(original);
		dbInstance.commitAndCloseSession();
		copy = pm.findProperty(identity, group, res, "cat", "TestProperty");		
		Assert.assertEquals(original.getFloatValue().floatValue(), copy.getFloatValue().floatValue(), 0.00001);
		pm.deleteProperties(identity, group, res, "cat", "TestProperty");
	    
	}
	
	@Test
	public void testRealWorldScoreFloatValues() {
		// delete everything from previous tests
		pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue1");
		pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue2");
	  // Test setting and getting of a normal float value
		Property prop1 = pm.createPropertyInstance(null, null, null, "test", "TestPropertyFloatValue1", Float.valueOf(0.9f), null, null, null);
		pm.saveProperty(prop1);
		Property prop2 = pm.createPropertyInstance(null, null, null, "test", "TestPropertyFloatValue2", Float.valueOf(0.1f), null, null, null);
		pm.saveProperty(prop2);
		dbInstance.commitAndCloseSession();
		prop1 = pm.findProperty(null, null, null, "test", "TestPropertyFloatValue1");		
		prop2 = pm.findProperty(null, null, null, "test", "TestPropertyFloatValue2");		
		// In course assessment 0.1 + 0.9 must exactly give 1 as a result
		assertEquals(1, prop1.getFloatValue().floatValue() + prop2.getFloatValue().floatValue(), 0.00001);
		pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue1");
		pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue2");
	}
	
	@Test
	public void testFindIdentities() {
		//create identities, group and resource
		OLATResource res = JunitTestHelper.createRandomResource();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("prop-7-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("prop-10-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("prop-11-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("prop-12-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(id1, "a buddygroup", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();

		String propName = UUID.randomUUID().toString();
		
		Property p = pm.createPropertyInstance(id1, group, res, "cat", propName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		p = pm.createPropertyInstance(id2, group, res, "cat", propName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		p = pm.createPropertyInstance(id3, group, res, "cat", propName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		p = pm.createPropertyInstance(id4, group, res, "cat", propName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		p = pm.createPropertyInstance(id1, group, res, "cat2", propName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		p = pm.createPropertyInstance(id2, group, res, "cat2", propName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", "textValue");
		pm.saveProperty(p);
		
		dbInstance.commitAndCloseSession();
		// now find identities
		List<Identity> ids = pm.findIdentitiesWithProperty(res, "cat", propName, false);
		assertEquals(4, ids.size());
		ids = pm.findIdentitiesWithProperty(res, "cat", propName, true);
		assertEquals(4, ids.size());
		ids = pm.findIdentitiesWithProperty(null, "cat", propName, false);
		assertEquals(4, ids.size());
		ids = pm.findIdentitiesWithProperty(null, "cat", propName, true);
		assertEquals(0, ids.size());
		ids = pm.findIdentitiesWithProperty(null, "cat2", propName, false);
		assertEquals(2, ids.size());
		ids = pm.findIdentitiesWithProperty(null, null, propName, false);
		assertEquals(4, ids.size()); // not 6, must be distinct
		ids = pm.findIdentitiesWithProperty(null, null, propName, true);
		assertEquals(0, ids.size());
	}
	
	@Test
	public void testFindProperties() {
		//create identities, group and resource
		OLATResource res = JunitTestHelper.createRandomResource();
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("prop-8-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("prop-9-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(id1, "a buddygroup", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		String category = "cat3";
		String propertyName = "TestProperty3";
		String textValue = "textValue3";
		Property p = pm.createPropertyInstance(id1, group, res, category, propertyName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", textValue);
		pm.saveProperty(p);
		p = pm.createPropertyInstance(id2, group, res, category, propertyName, Float.valueOf(1.1f), Long.valueOf(123456), "stringValue", textValue);
		pm.saveProperty(p);
		List<Property> propertyList = pm.findProperties(id1, group, res.getResourceableTypeName(), res.getResourceableId(), category, propertyName);
		assertEquals(1, propertyList.size());
		assertEquals(propertyName, propertyList.get(0).getName() );
		assertEquals(textValue, propertyList.get(0).getTextValue() );
		int deletedCount1 = pm.deleteProperties(id1, group, res, category, propertyName);
		Assert.assertEquals(1, deletedCount1);
		int deletedCount2 = pm.deleteProperties(id2, group, res, category, propertyName);
		Assert.assertEquals(1, deletedCount2);
	}
}
