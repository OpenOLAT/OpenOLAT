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
package org.olat.commons.memberlist.model;

/**
 * 
 * Initial date: 28 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementInfos {
	
	private final String curriculumDisplayName;
	private final String curriculumIdentifier;
	
	private final String rootElementDisplayName;
	private final String rootElementIdentifier;
	
	public CurriculumElementInfos(String curriculumDisplayName, String curriculumIdentifier,
			String rootElementDisplayName, String rootElementIdentifier) {
		this.curriculumDisplayName = curriculumDisplayName;
		this.curriculumIdentifier = curriculumIdentifier;
		this.rootElementDisplayName = rootElementDisplayName;
		this.rootElementIdentifier = rootElementIdentifier;
	}

	public String getCurriculumDisplayName() {
		return curriculumDisplayName;
	}

	public String getCurriculumIdentifier() {
		return curriculumIdentifier;
	}

	public String getRootElementDisplayName() {
		return rootElementDisplayName;
	}

	public String getRootElementIdentifier() {
		return rootElementIdentifier;
	}
	
	

}
