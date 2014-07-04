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
	public void testRunStructure_bpsChecklist() {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("runstructure_checklist.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
	}
	
	@Test
	public void testmapping_allInOne() {
		XStream xStream = CourseXStreamAliases.getReadCourseXStream();
		InputStream in = CourseXStreamAliasesTest.class.getResourceAsStream("runstructure_allinone.xml");
		Object runStructure = xStream.fromXML(in);
		Assert.assertNotNull(runStructure);
	}

}
