/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.id.context;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Description:<br>
 * Test some methods of the BusinessControlFactory
 * 
 * <P>
 * Initial Date:  24 janv. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessControlFactoryTest {
	
	@Test
	public void businessPath() {
		String businessPath = "[QPool:0][QItemCollection:340819968][QuestionItem:294649898]";
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		Assert.assertNotNull(entries);
		Assert.assertEquals(3, entries.size());
		Assert.assertNotNull(entries.get(0).getOLATResourceable());
		Assert.assertNotNull(entries.get(1).getOLATResourceable());
		Assert.assertNotNull(entries.get(2).getOLATResourceable());
		
		Assert.assertEquals(new Long(0l), entries.get(0).getOLATResourceable().getResourceableId());
		Assert.assertEquals("QPool", entries.get(0).getOLATResourceable().getResourceableTypeName());
		Assert.assertEquals(new Long(340819968l), entries.get(1).getOLATResourceable().getResourceableId());
		Assert.assertEquals("QItemCollection", entries.get(1).getOLATResourceable().getResourceableTypeName());
		Assert.assertEquals(new Long(294649898l), entries.get(2).getOLATResourceable().getResourceableId());
		Assert.assertEquals("QuestionItem", entries.get(2).getOLATResourceable().getResourceableTypeName());
	}
	
	@Test
	public void pathResource() {
		String businessPath = "[path=/Pflanzenschutz/Gesetzliches:0]";
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertNotNull(entries.get(0).getOLATResourceable());
		
		OLATResourceable ores = entries.get(0).getOLATResourceable();
		Assert.assertEquals(new Long(0), ores.getResourceableId());
		Assert.assertEquals("path=/Pflanzenschutz/Gesetzliches", ores.getResourceableTypeName());
	}
	
	@Test
	public void pathResourceWrongButAutomaticallyHealed() {
		String businessPath = "[path=/Pflanzenschutz/Gesetzliches]";
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertNotNull(entries.get(0).getOLATResourceable());
		
		OLATResourceable ores = entries.get(0).getOLATResourceable();
		Assert.assertEquals(new Long(0), ores.getResourceableId());
		Assert.assertEquals("path=/Pflanzenschutz/Gesetzliches", ores.getResourceableTypeName());
	}
	
	@Test
	public void pathWithDoublePoints() {
		String businessPath = "[RepositoryEntry:408649729][CourseNode:93480746431333][path=/Dru34567/Test: double point:0]";
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		Assert.assertNotNull(entries);
		Assert.assertEquals(3, entries.size());
		Assert.assertNotNull(entries.get(0).getOLATResourceable());
		Assert.assertNotNull(entries.get(1).getOLATResourceable());
		Assert.assertNotNull(entries.get(2).getOLATResourceable());
		
		//check every value
		Assert.assertEquals("RepositoryEntry", entries.get(0).getOLATResourceable().getResourceableTypeName());
		Assert.assertEquals(new Long(408649729l), entries.get(0).getOLATResourceable().getResourceableId());
		Assert.assertEquals("CourseNode", entries.get(1).getOLATResourceable().getResourceableTypeName());
		Assert.assertEquals(new Long(93480746431333l), entries.get(1).getOLATResourceable().getResourceableId());
		Assert.assertEquals("path=/Dru34567/Test: double point", entries.get(2).getOLATResourceable().getResourceableTypeName());
		Assert.assertEquals(new Long(0l), entries.get(2).getOLATResourceable().getResourceableId());
	}

}
