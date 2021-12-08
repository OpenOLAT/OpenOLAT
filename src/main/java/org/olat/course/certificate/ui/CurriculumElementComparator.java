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
package org.olat.course.certificate.ui;

import java.util.Comparator;

import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;

/**
 * Initial date: 08.12.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CurriculumElementComparator implements Comparator<CurriculumElement > {

	@Override
	public int compare(CurriculumElement element1, CurriculumElement element2) {
		int l1 = getLevels(element1);
		int l2 = getLevels(element2);
		int c = Integer.compare(l1, l2);
		if(c == 0) {
			c = element1.getKey().compareTo(element2.getKey());
		}
		return c;
	}
	
	private final int getLevels(CurriculumElement node) {
		String materializedPath = node.getMaterializedPathKeys();
		return StringHelper.count(materializedPath, '/');
	}
}
