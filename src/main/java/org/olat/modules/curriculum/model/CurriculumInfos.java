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

import org.olat.modules.curriculum.Curriculum;

/**
 * 
 * Initial date: 22 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumInfos {
	
	private final Curriculum curriculum;
	private final long numOfElements;
	
	public CurriculumInfos(Curriculum curriculum, Long numOfElements) {
		this.curriculum = curriculum;
		this.numOfElements = numOfElements == null ? 0l : numOfElements.longValue();
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}

	public long getNumOfElements() {
		return numOfElements;
	}
	

	@Override
	public int hashCode() {
		return curriculum.getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumInfos infos) {
			return curriculum.equals(infos.curriculum);
		}
		return false;
	}
}
