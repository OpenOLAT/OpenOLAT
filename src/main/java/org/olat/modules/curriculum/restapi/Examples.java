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
package org.olat.modules.curriculum.restapi;

import java.util.Date;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Examples {
	
	public static final CurriculumVO SAMPLE_CURRICULUMVO = new CurriculumVO();
	
	public static final CurriculumElementVO SAMPLE_CURRICULUMELEMENTVO = new CurriculumElementVO();
	
	static {
		SAMPLE_CURRICULUMVO.setKey(2l);
		SAMPLE_CURRICULUMVO.setDisplayName("Dipl. engineer");
		SAMPLE_CURRICULUMVO.setIdentifier("DIP-ENG-CH");
		SAMPLE_CURRICULUMVO.setDescription("A diploma as engineer");
		SAMPLE_CURRICULUMVO.setDegree("License");
		SAMPLE_CURRICULUMVO.setExternalId("DIP-12387");
		SAMPLE_CURRICULUMVO.setManagedFlagsString("delete");
		SAMPLE_CURRICULUMVO.setOrganisationKey(1l);
		
		SAMPLE_CURRICULUMELEMENTVO.setKey(3l);
		SAMPLE_CURRICULUMELEMENTVO.setIdentifier("CURR-EL-1");
		SAMPLE_CURRICULUMELEMENTVO.setDisplayName("A curriculum element");
		SAMPLE_CURRICULUMELEMENTVO.setDescription("This is a description");
		SAMPLE_CURRICULUMELEMENTVO.setCurriculumKey(2l);
		SAMPLE_CURRICULUMELEMENTVO.setExternalId("EXT-19");
		SAMPLE_CURRICULUMELEMENTVO.setBeginDate(new Date());
		SAMPLE_CURRICULUMELEMENTVO.setEndDate(new Date());
		SAMPLE_CURRICULUMELEMENTVO.setCurriculumElementTypeKey(25l);
		SAMPLE_CURRICULUMELEMENTVO.setParentElementKey(1l);
		SAMPLE_CURRICULUMELEMENTVO.setManagedFlagsString("delete");
	}
}