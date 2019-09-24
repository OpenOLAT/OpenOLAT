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
*/
package org.olat.group.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;
import org.olat.group.manager.GroupXStream;

/**
 * 
 * Description:<br>
 * Check import/export from group with XStream (was made with edenlib)
 * 
 * <P>
 * Initial Date:  5 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupImportExportXStreamTest {
	
	@Test
	public void importLearningGroupTest() {
		InputStream input = BusinessGroupImportExportXStreamTest.class.getResourceAsStream("learninggroupexport.xml");
		GroupXStream xstream = new GroupXStream();
		OLATGroupExport export = xstream.fromXML(input);
		assertNotNull(export);
		assertNotNull(export.getAreas());
		assertNotNull(export.getAreas().getGroups());
		assertEquals(1, export.getAreas().getGroups().size());
		assertNotNull(export.getGroups());
		assertNotNull(export.getGroups().getGroups());
		assertEquals(2, export.getGroups().getGroups().size());
		
		assertEquals("Form Group 2", export.getGroups().getGroups().get(1).getName());
		
		String output = xstream.toXML(export);
		assertNotNull(output);
	}
	
	
	@Test
	public void importRightGroupTest() {
		InputStream input = BusinessGroupImportExportXStreamTest.class.getResourceAsStream("rightgroupexport.xml");
		GroupXStream xstream = new GroupXStream();
		OLATGroupExport export = xstream.fromXML(input);
		assertNotNull(export);
		assertNotNull(export.getAreas());
		assertNotNull(export.getGroups());
		assertNotNull(export.getGroups().getGroups());
		assertEquals(2, export.getGroups().getGroups().size());
		
		assertEquals("Test Right 2", export.getGroups().getGroups().get(1).getName());
		
		String output = xstream.toXML(export);
		assertNotNull(output);
	}
}
