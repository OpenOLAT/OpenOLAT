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
import java.util.stream.Collectors;

import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 1 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithParents implements CurriculumElementRef, Comparable<CurriculumElementWithParents> {
	
	private final int order;
	private final CurriculumElement curriculumElement;
	private final List<CurriculumElement> parents;
	private final String parentLine;
	private final Curriculum curriculum;
	
	public CurriculumElementWithParents(CurriculumElement curriculumElement, List<CurriculumElement> parents, int order) {
		this.curriculumElement = curriculumElement;
		this.curriculum = curriculumElement.getCurriculum();
		this.parents = parents == null ? List.of() : List.copyOf(parents);
		this.order = order;
		
		parentLine = this.parents.stream()
				.map(CurriculumElement::getDisplayName)
				.collect(Collectors.joining(" / "));
	}
	
	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}
	
	public String getDisplayName() {
		return curriculumElement.getDisplayName();
	}
	
	public String getParentLine() {
		return parentLine;
	}
	
	public String getCurriculumDisplayName() {
		return curriculum.getDisplayName();
	}

	public int getOrder() {
		return order;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public List<CurriculumElement> getParents() {
		return parents;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	@Override
	public int compareTo(CurriculumElementWithParents o) {
		return Integer.compare(order, o.order);
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
		if(obj instanceof CurriculumElementWithParents) {
			CurriculumElementWithParents el = (CurriculumElementWithParents)obj;
			return curriculumElement.equals(el.getCurriculumElement());
		}
		return false;
	}
}
