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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

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
	public void testPathResource() {
		String businessPath = "[path=/Pflanzenschutz/Gesetzliches:0]";
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		assertNotNull(entries);
		assertEquals(1, entries.size());
		assertNotNull(entries.get(0).getOLATResourceable());
		
		OLATResourceable ores = entries.get(0).getOLATResourceable();
		assertEquals(new Long(0), ores.getResourceableId());
		assertEquals("path=/Pflanzenschutz/Gesetzliches", ores.getResourceableTypeName());
	}
	
	@Test
	public void testPathResourceWrongButAutomaticallyHealed() {
		String businessPath = "[path=/Pflanzenschutz/Gesetzliches]";
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		assertNotNull(entries);
		assertEquals(1, entries.size());
		assertNotNull(entries.get(0).getOLATResourceable());
		
		OLATResourceable ores = entries.get(0).getOLATResourceable();
		assertEquals(new Long(0), ores.getResourceableId());
		assertEquals("path=/Pflanzenschutz/Gesetzliches", ores.getResourceableTypeName());
	}

}
