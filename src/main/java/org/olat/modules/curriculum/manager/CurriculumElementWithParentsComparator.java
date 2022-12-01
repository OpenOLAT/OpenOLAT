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
package org.olat.modules.curriculum.manager;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementWithParents;

/**
 * 
 * Initial date: 1 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithParentsComparator implements Comparator<CurriculumElementWithParents> {
	
	private final Collator collator;
	
	public CurriculumElementWithParentsComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(CurriculumElementWithParents c1, CurriculumElementWithParents c2) {
		Curriculum cur1 = c1.getCurriculum();
		Curriculum cur2 = c2.getCurriculum();
		
		int c = collator.compare(cur1.getDisplayName(), cur2.getDisplayName());
		if(c == 0) {
			List<CurriculumElement> el1 = c1.getParents();
			List<CurriculumElement> el2 = c2.getParents();
			
			int numOfElements = Math.min(el1.size(), el2.size());
			
			for(int i=0; i<numOfElements; i++) {
				if(el1.size() > i && el2.size() > i) {
					CurriculumElement subEl1 = el1.get(i);
					CurriculumElement subEl2 = el2.get(i);
					c = collator.compare(subEl1.getDisplayName(), subEl2.getDisplayName());
					if(c != 0) {
						break;
					}
				} else if(el1.size() == el2.size()) {
					c = 0;
					break;
				} else if(el1.size() > el2.size()) {
					c = -1;
					break;
				} else {
					c = 1;
					break;
				}
			}
		}
		
		if(c == 0) {
			c = collator.compare(c1.getDisplayName(), c2.getDisplayName());
		}
		if(c == 0) {
			c = c1.getKey().compareTo(c2.getKey());
		}
		return c;
	}
}
