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
package org.olat.course;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 24.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseXStreamAliasesTest {
	
	@Test
	public void readRunStructureBpsChecklist() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("runstructure_checklist.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}
	
	@Test
	public void readRunStructureAllInOne() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("runstructure_allinone.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}
	
	@Test
	public void readEditorTreeModelWithShibbolethCondition() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("editortreemodel_shibboleth_specific.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}
	
	@Test
	public void readEditorTreeModelWithOldAdobeConnectConfiguration() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("editortreemodel_old_classroom.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}
	
	@Test
	public void readEditorTreeModelWithOlWimbaConfiguration() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("editortreemodel_wimba.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}
	
	/**
	 * Check the link list.
	 * 
	 * @throws IOException
	 */
	@Test
	public void readEditorTreeModelWithLinklist() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("editortreemodel_ll.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}
	
	/**
	 * Check some optional course element: edubase, edusharing and card2brain
	 * 
	 * @throws IOException
	 */
	@Test
	public void readEditorTreeModelWithEdu() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("editortreemodel_edu.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}
	
	/**
	 * Check some optional course element: Opencast, Livestream
	 * 
	 * @throws IOException
	 */
	@Test
	public void readEditorTreeModelWithOpencastLiveStream() throws IOException {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("editortreemodel_opencast_livestream.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
		in.close();
	}

}
