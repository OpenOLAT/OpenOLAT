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
package org.olat.course.nodes.practice.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.util.DateUtils;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.model.PracticeAssessmentItemGlobalRefImpl;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 16 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeServiceFreeShuffleTest extends OlatTestCase {
	
	private static int counter = 0;
	
	@Autowired
	private PracticeServiceImpl practiceService;
	
	/**
	 * The first session returns only level 0 (question with level 0 or new questions).
	 */
	@Test
	public void session1() {
		Map<String,PracticeAssessmentItemGlobalRef> refs = generateRefs(200, 100, 10);	
		List<PracticeItem> newQuestions = generateItems(100);
		List<PracticeItem> leveledItems = generateItems(refs);
		
		List<PracticeItem> questions = new ArrayList<>();
		questions.addAll(newQuestions);
		questions.addAll(leveledItems);
		
		List<PracticeAssessmentItemGlobalRef> refList = new ArrayList<>(refs.values());
		List<PracticeItem> shuffledItems = practiceService.filterFreeShufflePlayMode(1, questions, 20, true, refList);
		Assert.assertEquals(20, shuffledItems.size());

		LevelCounter levelCounter = new LevelCounter();
		for(PracticeItem shuffledItem:shuffledItems) {
			levelCounter.count(refs.get(shuffledItem.getIdentifier()));
		}

		Assert.assertEquals(20, levelCounter.numOf(0));
		Assert.assertEquals(0, levelCounter.numOf(1));
		Assert.assertEquals(0, levelCounter.numOf(2));
	}
	
	/**
	 * The first session returns 50% level 1 and 50% level 0 or new questions.
	 */
	@Test
	public void session2() {
		Map<String,PracticeAssessmentItemGlobalRef> refs = generateRefs(200, 100, 10);	
		List<PracticeItem> newQuestions = generateItems(100);
		List<PracticeItem> leveledItems = generateItems(refs);
		
		List<PracticeItem> questions = new ArrayList<>();
		questions.addAll(newQuestions);
		questions.addAll(leveledItems);
		
		List<PracticeAssessmentItemGlobalRef> refList = new ArrayList<>(refs.values());
		List<PracticeItem> shuffledItems = practiceService.filterFreeShufflePlayMode(2, questions, 20, true, refList);
		Assert.assertEquals(20, shuffledItems.size());
		
		LevelCounter levelCounter = new LevelCounter();
		for(PracticeItem shuffledItem:shuffledItems) {
			levelCounter.count(refs.get(shuffledItem.getIdentifier()));
		}
		
		Assert.assertEquals(10, levelCounter.numOf(0));
		Assert.assertEquals(10, levelCounter.numOf(1));
		Assert.assertEquals(0, levelCounter.numOf(2));
		Assert.assertEquals(0, levelCounter.numOf(3));
	}
	
	/**
	 * The first session returns 50% level 0 (or new questions), 30% level 1, 20% level 2.
	 */
	@Test
	public void session3() {
		Map<String,PracticeAssessmentItemGlobalRef> refs = generateRefs(500, 300, 50);	
		List<PracticeItem> newQuestions = generateItems(100);
		List<PracticeItem> leveledItems = generateItems(refs);
		
		List<PracticeItem> questions = new ArrayList<>();
		questions.addAll(newQuestions);
		questions.addAll(leveledItems);
		
		List<PracticeAssessmentItemGlobalRef> refList = new ArrayList<>(refs.values());
		List<PracticeItem> shuffledItems = practiceService.filterFreeShufflePlayMode(3, questions, 100, true, refList);
		Assert.assertEquals(100, shuffledItems.size());
		
		LevelCounter levelCounter = new LevelCounter();
		for(PracticeItem shuffledItem:shuffledItems) {
			levelCounter.count(refs.get(shuffledItem.getIdentifier()));
		}
		
		Assert.assertEquals(50, levelCounter.numOf(0));
		Assert.assertEquals(30, levelCounter.numOf(1));
		Assert.assertEquals(20, levelCounter.numOf(2));
		Assert.assertEquals(0, levelCounter.numOf(3));
	}
	
	/**
	 * If there are not enough questions of a certain level, compensate with the next smaller
	 * level.
	 */
	@Test
	public void session3WithMissingQuestions() {
		Map<String,PracticeAssessmentItemGlobalRef> refs = generateRefs(500, 15, 10);
		List<PracticeItem> newQuestions = generateItems(100);
		List<PracticeItem> leveledItems = generateItems(refs);
		
		List<PracticeItem> questions = new ArrayList<>();
		questions.addAll(newQuestions);
		questions.addAll(leveledItems);
		
		List<PracticeAssessmentItemGlobalRef> refList = new ArrayList<>(refs.values());
		List<PracticeItem> shuffledItems = practiceService.filterFreeShufflePlayMode(3, questions, 100, true, refList);
		Assert.assertEquals(100, shuffledItems.size());
		
		LevelCounter levelCounter = new LevelCounter();
		for(PracticeItem shuffledItem:shuffledItems) {
			levelCounter.count(refs.get(shuffledItem.getIdentifier()));
		}
		
		Assert.assertEquals(75, levelCounter.numOf(0));
		Assert.assertEquals(15, levelCounter.numOf(1));
		Assert.assertEquals(10, levelCounter.numOf(2));
		Assert.assertEquals(0, levelCounter.numOf(3));
	}
	
	/**
	 * If there are not enough questions of a certain level, compensate with the next smaller
	 * level.
	 */
	@Test
	public void session3WithDate() {
		Map<String,PracticeAssessmentItemGlobalRef> refs = generateRefs(500, 200, 100);
		List<PracticeItem> questions = generateItems(refs);
		
		int countOldLevel2 = 15;
		Date today = new Date();
		for(PracticeItem question:questions) {
			PracticeAssessmentItemGlobalRef ref = refs.get(question.getIdentifier());
			if(ref.getLevel() == 2) {
				if(countOldLevel2 > 0) {
					ref.setLastAttempts(DateUtils.addDays(today, 6));
					countOldLevel2--;
				} else {
					ref.setLastAttempts(DateUtils.addDays(today, 3));
				}
			}
		}

		List<PracticeAssessmentItemGlobalRef> refList = new ArrayList<>(refs.values());
		List<PracticeItem> shuffledItems = practiceService.filterFreeShufflePlayMode(3, questions, 100, true, refList);
		Assert.assertEquals(100, shuffledItems.size());
		
		int level2OlderThan4Days = 0;
		LevelCounter levelCounter = new LevelCounter();
		for(PracticeItem shuffledItem:shuffledItems) {
			PracticeAssessmentItemGlobalRef ref = refs.get(shuffledItem.getIdentifier());
			int level = levelCounter.count(ref);
			if(level == 2 && ref.getLastAttempts() != null && CalendarUtils.numOfDays(today, ref.getLastAttempts()) > 4) {
				level2OlderThan4Days++;
			}
		}
		
		Assert.assertEquals(50, levelCounter.numOf(0));
		Assert.assertEquals(30, levelCounter.numOf(1));
		Assert.assertEquals(20, levelCounter.numOf(2));
		Assert.assertEquals(0, levelCounter.numOf(3));
		
		Assert.assertEquals(15, level2OlderThan4Days);
	}
	

	private Map<String,PracticeAssessmentItemGlobalRef> generateRefs(int... levels) {
		Map<String,PracticeAssessmentItemGlobalRef> refs = new HashMap<>();
		
		for(int i=0; i<levels.length; i++) {
			for(int j=0; j<levels[i]; j++) {
				PracticeAssessmentItemGlobalRefImpl ref = new PracticeAssessmentItemGlobalRefImpl();
				ref.setIdentifier("ref-" + (++counter));
				ref.setLevel(i);
				refs.put(ref.getIdentifier(), ref);
			}
		}
		
		return refs;
	}
	
	private List<PracticeItem> generateItems(Map<String, PracticeAssessmentItemGlobalRef> refs) {
		return refs.values().stream().map(ref -> {
			String identifier = ref.getIdentifier();
			return new PracticeItem(identifier, identifier, null, null, null, null);
			
		}).collect(Collectors.toList());
	}
	
	private List<PracticeItem> generateItems(int numOfQuestions) {
		List<PracticeItem> items = new ArrayList<>();
		for(int i=0; i<numOfQuestions; i++) {
			String identifier = "item-" + (++counter);
			items.add(new PracticeItem(identifier, identifier, null, null, null, null));
		}
		return items;
	}
	
	private static class LevelCounter {
		
		private int[] levels = { 0, 0, 0, 0, 0, 0 };
		
		public int numOf(int level) {
			return levels[level];
		}

		public int count(PracticeAssessmentItemGlobalRef ref) {
			int level = ref == null ? 0 : ref.getLevel();
			if(level >= 0 && level < levels.length) {
				levels[level]++;
			} else {
				Assert.fail();
			}
			return level;
		}
	}
}
