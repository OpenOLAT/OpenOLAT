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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.CodeHelper;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController.AggregatedElement;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController.AggregatedTableComparator;


/**
 * 
 * Initial date: 15 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLecturesOverviewControllerTest {
	
	@Test
	public void orderBy() {
		AggregatedElement element1 = createAggregatedElement(-5, 5, "A", "AAA");
		AggregatedElement element2 = createAggregatedElement(-15, -10, "B", "AAA");
		AggregatedElement element3 = createAggregatedElement(5, 15, "C", "AAA");
		AggregatedElement element4 = createAggregatedElement(null, null, "D", "AAA");
		AggregatedElement element5 = createAggregatedElement(null, null, "D", "BBB");
		AggregatedElement element6 = createAggregatedElement(null, null, "E", "BBB");
		
		// make the list to order and shuffle it
		List<AggregatedElement> elements = new ArrayList<>();
		elements.add(element4);
		elements.add(element3);
		elements.add(element1);
		elements.add(element2);
		elements.add(element6);
		elements.add(element5);
		
		// sort and check
		Collections.sort(elements, new AggregatedTableComparator(Locale.ENGLISH));
		Assert.assertEquals(element1, elements.get(0));
		Assert.assertEquals(element2, elements.get(1));
		Assert.assertEquals(element3, elements.get(2));
		Assert.assertEquals(element4, elements.get(3));
		Assert.assertEquals(element5, elements.get(4));
		Assert.assertEquals(element6, elements.get(5));
	}
	
	private AggregatedElement createAggregatedElement(Integer beginDay, Integer endDay, String displayName, String identifier) {
		CurriculumElementImpl curriculumElement = new CurriculumElementImpl();
		curriculumElement.setKey(CodeHelper.getRAMUniqueID());
		
		if(beginDay != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, beginDay);
			curriculumElement.setBeginDate(cal.getTime());
		}
		if(endDay != null) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, endDay);
				curriculumElement.setEndDate(cal.getTime());
		}
		curriculumElement.setDisplayName(displayName);
		curriculumElement.setIdentifier(identifier);
		CurriculumElementRepositoryEntryViews view = new CurriculumElementRepositoryEntryViews(curriculumElement, Collections.emptyList(), null);
		return new AggregatedElement(view);
	}

}
