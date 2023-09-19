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
package org.olat.ims.qti21.manager;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.ims.qti21.model.LogViewerEntry;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;

/**
 * 
 * Initial date: 25 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogViewerDeserializerTest {
	
	@Test
	public void readEntriesSimpleTest() throws Exception {
		URL logUrl = LogViewerDeserializerTest.class.getResource("auditlog_Course_SimpleTest.log");
		File logFile = new File(logUrl.toURI());
		
		LogViewerDeserializer deserializer = new LogViewerDeserializer(logFile, null, null);
		List<LogViewerEntry> entries = deserializer.readEntries();
		Assert.assertNotNull(entries);
		Assert.assertEquals(6, entries.size());

		// Item Event
		LogViewerEntry itemEntry = entries.get(2);
		Assert.assertEquals(CandidateTestEventType.ITEM_EVENT, itemEntry.getTestEventType());
		Assert.assertEquals(CandidateItemEventType.ATTEMPT_VALID, itemEntry.getItemEventType());
		Assert.assertEquals("ai0f577986b94f1c821d6cc65d943809", itemEntry.getAssessmentItemId());
		
		// Outcomes
		LogViewerEntry outcomeEntry = entries.get(4);
		Assert.assertEquals(0.0d, outcomeEntry.getMinScore(), 0.0001);
		Assert.assertEquals(1.0d, outcomeEntry.getMaxScore(), 0.0001);
		Assert.assertEquals(1.0d, outcomeEntry.getScore(), 0.0001);
		Assert.assertEquals(Boolean.TRUE, outcomeEntry.getPassed());
	}
}
