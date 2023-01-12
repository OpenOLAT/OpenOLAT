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
package org.olat.modules.video.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.test.OlatTestCase;

import org.junit.Assert;
import org.junit.Test;


public class TimelineModelTest extends OlatTestCase {

	@Test
	public void testGetNumberOfLanes() {
		List<TimelineRow> events = new ArrayList<>();
		events.add(new TimelineRow(UUID.randomUUID().toString(), 0, 1000, TimelineEventType.ANNOTATION, "", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 500, 1000, TimelineEventType.ANNOTATION, "", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 1000, 1000, TimelineEventType.ANNOTATION, "", ""));
		int nbLanes = TimelineModel.getNumberOfLanes(events);
		Assert.assertEquals(2, nbLanes);
	}

	@Test
	public void testDistributeToLanes() {
		List<TimelineRow> events = new ArrayList<>();
		events.add(new TimelineRow(UUID.randomUUID().toString(), 0, 1000, TimelineEventType.ANNOTATION, "a1", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 500, 1000, TimelineEventType.ANNOTATION, "b1", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 999, 1001, TimelineEventType.ANNOTATION, "c1", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 1000, 1000, TimelineEventType.ANNOTATION, "a2", ""));
		List<List<TimelineRow>> lanes = TimelineModel.distributeToLanes(events);
		Assert.assertEquals(3, lanes.size());
		Assert.assertEquals(2, lanes.get(0).size());
		Assert.assertEquals(1, lanes.get(1).size());
		Assert.assertEquals(1, lanes.get(2).size());
		Assert.assertEquals("a1", lanes.get(0).get(0).getText());
		Assert.assertEquals("a2", lanes.get(0).get(1).getText());
		Assert.assertEquals("b1", lanes.get(1).get(0).getText());
		Assert.assertEquals("c1", lanes.get(2).get(0).getText());
	}

	@Test
	public void testSimpleLaneDistribution() {
		List<TimelineRow> events = new ArrayList<>();
		events.add(new TimelineRow(UUID.randomUUID().toString(), 16000, 1, TimelineEventType.QUIZ, "q1", ""));
		List<List<TimelineRow>> lanes = TimelineModel.distributeToLanes(events);
		Assert.assertEquals(1, lanes.size());
		Assert.assertEquals(1, lanes.get(0).size());
	}

	@Test
	public void testComplexLaneDistribution() {
		List<TimelineRow> events = new ArrayList<>();

		events.add(new TimelineRow(UUID.randomUUID().toString(), 1000, 2000, TimelineEventType.ANNOTATION, "a1", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 4000, 4000, TimelineEventType.ANNOTATION, "b2", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 10000, 5000, TimelineEventType.ANNOTATION, "c3", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 21000, 3000, TimelineEventType.ANNOTATION, "d4", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 23000, 2000, TimelineEventType.ANNOTATION, "e5", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 24000, 2000, TimelineEventType.ANNOTATION, "f6", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 17000, 3000, TimelineEventType.ANNOTATION, "g7", ""));
		events.add(new TimelineRow(UUID.randomUUID().toString(), 4000, 5000, TimelineEventType.ANNOTATION, "h8", ""));

		int nbLanes = TimelineModel.getNumberOfLanes(events);
		Assert.assertEquals(2, nbLanes);

		List<List<TimelineRow>> lanes = TimelineModel.distributeToLanes(events);
		Assert.assertEquals(2, lanes.size());
		Assert.assertEquals(6, lanes.get(0).size());
		Assert.assertEquals(2, lanes.get(1).size());
		Assert.assertEquals(Arrays.asList("a1", "b2", "c3", "g7", "d4", "f6"), lanes.get(0).stream().map(TimelineRow::getText).collect(Collectors.toList()));
		Assert.assertEquals(Arrays.asList("h8", "e5"), lanes.get(1).stream().map(TimelineRow::getText).collect(Collectors.toList()));
	}
}