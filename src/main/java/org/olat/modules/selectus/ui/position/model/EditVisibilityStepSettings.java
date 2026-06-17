/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Initial date: 18 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditVisibilityStepSettings {
	
	private final List<String> expertsFields = new ArrayList<>();
	private final List<String> refereesFields = new ArrayList<>();
	private final List<String> comparativeExpertFields = new ArrayList<>();
	private final List<String>[] configurationFields;
	
	private final List<String> expertsDocs = new ArrayList<>();
	private final List<String> refereesDocs = new ArrayList<>();
	private final List<String> comparativeExpertDocs = new ArrayList<>();
	private final List<String>[] configurationDocs;
	
	private final Boolean[] facultyMembersRefereesDocuments;
	private final Boolean[] facultyMembersExpertsDocuments;
	private final Boolean[] facultyMembersExpertsComparativeAssessmentsDocuments;
	
	@SuppressWarnings("unchecked")
	public EditVisibilityStepSettings(int numOfConfigurations) {
		configurationFields = new List[numOfConfigurations];
		for(int i=numOfConfigurations; i-->0; ) {
			configurationFields[i] = new ArrayList<>();
		}
		
		configurationDocs = new List[numOfConfigurations];
		for(int i=numOfConfigurations; i-->0; ) {
			configurationDocs[i] = new ArrayList<>();
		}
		
		facultyMembersRefereesDocuments = new Boolean[numOfConfigurations];
		facultyMembersExpertsDocuments = new Boolean[numOfConfigurations];
		facultyMembersExpertsComparativeAssessmentsDocuments = new Boolean[numOfConfigurations];
	}

	@SuppressWarnings("unchecked")
	public EditVisibilityStepSettings(Position position, List<ApplicationsFeedbackConfiguration> configurations) {
		expertsFields.addAll(position.getExpertRecommendationFields());
		refereesFields.addAll(position.getRefereeRecommendationFields());
		comparativeExpertFields.addAll(position.getComparativeAssessmentExpertFields());
		
		int numOfConfigurations = configurations.size();
		configurationFields = new List[numOfConfigurations];
		for(int i=numOfConfigurations; i-->0; ) {
			configurationFields[i] = new ArrayList<>(configurations.get(i).getFields());
		}
		
		expertsDocs.addAll(position.getExpertRecommendationDocuments());
		refereesDocs.addAll(position.getRefereeRecommendationDocuments());
		comparativeExpertDocs.addAll(position.getComparativeAssessmentExpertDocuments());
		
		configurationDocs = new List[numOfConfigurations];
		for(int i=numOfConfigurations; i-->0; ) {
			configurationDocs[i] = new ArrayList<>(configurations.get(i).getDocuments());
		}
		
		facultyMembersRefereesDocuments = new Boolean[numOfConfigurations];
		facultyMembersExpertsDocuments = new Boolean[numOfConfigurations];
		facultyMembersExpertsComparativeAssessmentsDocuments = new Boolean[numOfConfigurations];
		for(int i=numOfConfigurations; i-->0; ) {
			facultyMembersRefereesDocuments[i] = configurations.get(i).isRefereesDocs();
			facultyMembersExpertsDocuments[i] = configurations.get(i).isExpertsDocs();
			facultyMembersExpertsDocuments[i] = configurations.get(i).isExpertsComparativeAssessmentDocs();
		}
	}
	
	public Boolean getGlobalVisibility(Tab tab) {
		if(isAll(tab)) {
			return Boolean.TRUE;
		}
		if(isNone(tab)) {
			return Boolean.FALSE;
		}
		return null;
	}
	
	public boolean isAll(Tab tab) {
		return hasGlobalmarker(tab, RecruitingModule.ALL);
	}
	
	public boolean isNone(Tab tab) {
		return hasGlobalmarker(tab, RecruitingModule.NONE);
	}
	
	private boolean hasGlobalmarker(Tab tab, String marker) {
		String noneKey = tab.attributesTabKey() + marker;
		boolean hasMark = expertsFields.contains(noneKey)
			|| refereesFields.contains(noneKey)
			|| comparativeExpertFields.contains(noneKey);
		if(!hasMark && configurationFields != null) {
			for(int i=configurationFields.length; i-->0; ) {
				hasMark |= configurationFields[i].contains(noneKey);
			}
		}
		return hasMark;
	}
	
	public List<String> getExpertsFields() {
		return expertsFields;
	}
	
	public List<String> getRefereesFields() {
		return refereesFields;
	}

	public List<String> getComparativeExpertFields() {
		return comparativeExpertFields;
	}

	public List<String> getFacultyMembersFields(int i) {
		if(i >= 0 && i<configurationFields.length) {
			return configurationFields[i];
		}
		return new ArrayList<>();
	}
	
	public List<String> getExpertsDocuments() {
		return expertsDocs;
	}
	
	public List<String> getRefereesDocuments() {
		return refereesDocs;
	}
	
	public List<String> getComparativeExpertDocuments() {
		return comparativeExpertDocs;
	}
	
	public List<String> getFacultyMembersDocuments(int i) {
		if(i >= 0 && i<configurationDocs.length) {
			return configurationDocs[i];
		}
		return new ArrayList<>();
	}
	
	public boolean getFacultyMembersRefereesDocuments(int i) {
		boolean val = false;
		if(i >= 0 && i<facultyMembersRefereesDocuments.length) {
			Boolean value = facultyMembersRefereesDocuments[i];
			val = value != null && value.booleanValue();
		}
		return val;
	}
	
	public void setFacultyMembersRefereesDocuments(int i, boolean val) {
		if(i >= 0 && i<facultyMembersRefereesDocuments.length) {
			facultyMembersRefereesDocuments[i] = Boolean.valueOf(val);
		}
	}
	
	public boolean getFacultyMembersExpertsDocuments(int i) {
		boolean val = false;
		if(i >= 0 && i<facultyMembersExpertsDocuments.length) {
			Boolean value = facultyMembersExpertsDocuments[i];
			val = value != null && value.booleanValue();
		}
		return val;
	}
	
	public void setFacultyMembersExpertsDocuments(int i, boolean val) {
		if(i >= 0 && i<facultyMembersExpertsDocuments.length) {
			facultyMembersExpertsDocuments[i] = Boolean.valueOf(val);
		}
	}
	
	public boolean getFacultyMembersExpertsComparativeAssessmentsDocuments(int i) {
		boolean val = false;
		if(i >= 0 && i<facultyMembersExpertsComparativeAssessmentsDocuments.length) {
			Boolean value = facultyMembersExpertsComparativeAssessmentsDocuments[i];
			val = value != null && value.booleanValue();
		}
		return val;
	}
	
	public void setFacultyMembersExpertsComparativeAssessmentsDocuments(int i, boolean val) {
		if(i >= 0 && i<facultyMembersExpertsComparativeAssessmentsDocuments.length) {
			facultyMembersExpertsComparativeAssessmentsDocuments[i] = Boolean.valueOf(val);
		}
	}
}
