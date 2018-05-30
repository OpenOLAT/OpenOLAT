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
package org.olat.modules.curriculum.model;

import java.util.List;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntryMyView;

/**
 * A wrapper class
 * 
 * Initial date: 30 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRepositoryEntryViews {
	
	private final CurriculumElement curriculumElement;
	private final List<RepositoryEntryMyView> entries;
	
	public CurriculumElementRepositoryEntryViews(CurriculumElement curriculumElement, List<RepositoryEntryMyView> entries) {
		this.curriculumElement = curriculumElement;
		this.entries = entries;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public List<RepositoryEntryMyView> getEntries() {
		return entries;
	}

	@Override
	public int hashCode() {
		return curriculumElement.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementRepositoryEntryViews) {
			CurriculumElementRepositoryEntryViews el = (CurriculumElementRepositoryEntryViews)obj;
			return curriculumElement.equals(el.curriculumElement);
		}
		return super.equals(obj);
	}
}
