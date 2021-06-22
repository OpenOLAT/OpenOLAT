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
package de.bps.course.nodes;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.bps.olat.modules.cl.Checklist;
import de.bps.olat.modules.cl.Checkpoint;
import de.bps.olat.modules.cl.CheckpointResult;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChecklistCourseNodeTest {
	
	@Test
	public void readExportedChecklist() throws URISyntaxException {
		URL listUrl = ChecklistCourseNodeTest.class.getResource("checklist_103952250813148.xml");
		File listFile = new File(listUrl.toURI());
		Checklist checklist = (Checklist)ChecklistCourseNode.getXStream().fromXML(listFile);
		Assert.assertNotNull(checklist);
	}
	
	@Test
	public void writeReadChecklist() throws URISyntaxException {
		Checklist list = new Checklist();
		list.setCreationDate(new Date());
		list.setDescription("A checklist");
		list.setTitle("Checks");
		
		Checkpoint point = new Checkpoint();
		point.setChecklist(list);
		point.setCreationDate(new Date());
		point.setLastModified(new Date());
		point.setMode("Mode");
		point.setDescription("A point");
		point.setTitle("Title");
		
		list.addCheckpoint(0, point);
		
		CheckpointResult result = new CheckpointResult();
		result.setCheckpoint(point);
		result.setCreationDate(new Date());
		result.setLastModified(new Date());
		result.setIdentityId(Long.valueOf(2176238l));
		result.setResult(true);
		
		List<CheckpointResult> results = new ArrayList<>();
		results.add(result);
		point.setResults(results);
		
		String xml = ChecklistCourseNode.getXStream().toXML(list);
		Assert.assertNotNull(xml);
		Checklist checklist = (Checklist)ChecklistCourseNode.getXStream().fromXML(xml);
		Assert.assertNotNull(checklist);
	}

}
