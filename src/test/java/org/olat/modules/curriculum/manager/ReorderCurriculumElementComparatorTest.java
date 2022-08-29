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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;


/**
 * 
 * Initial date: 29 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReorderCurriculumElementComparatorTest {
	
	@Test
	public void order() {
		CurriculumElementImpl c1 = new CurriculumElementImpl();
		c1.setKey(1l);
		CurriculumElementImpl c2 = new CurriculumElementImpl();
		c2.setKey(3l);
		
		List<CurriculumElement> elements = new ArrayList<>();
		elements.add(c1);
		elements.add(c2);
		
		List<Long> orderedList = List.of(3l, 1l);
		ReorderCurriculumElementComparator comparator = new ReorderCurriculumElementComparator(orderedList);
		Collections.sort(elements, comparator);
		
		Assert.assertEquals(1, elements.indexOf(c1));
		Assert.assertEquals(0, elements.indexOf(c2));
	}
	
	@Test
	public void orderWithNull() {
		CurriculumElementImpl c1 = new CurriculumElementImpl();
		c1.setKey(1l);
		CurriculumElementImpl c2 = new CurriculumElementImpl();
		c2.setKey(3l);
		
		List<CurriculumElement> elements = new ArrayList<>();
		elements.add(c1);
		elements.add(null);
		elements.add(c2);
		
		List<Long> orderedList = List.of(3l, 1l);
		ReorderCurriculumElementComparator comparator = new ReorderCurriculumElementComparator(orderedList);
		Collections.sort(elements, comparator);
		
		Assert.assertEquals(1, elements.indexOf(c1));
		Assert.assertEquals(0, elements.indexOf(c2));
		Assert.assertNull(elements.get(2));
	}

}
