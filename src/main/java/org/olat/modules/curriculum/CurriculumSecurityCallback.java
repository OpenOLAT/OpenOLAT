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
package org.olat.modules.curriculum;

/**
 * 
 * Initial date: 15 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface CurriculumSecurityCallback {

	boolean canViewCurriculums();
	
	boolean canImportCurriculums();
	
	boolean canViewImplementations();
	
	boolean canNewCurriculum();
	
	boolean canEditCurriculum(Curriculum curriculum);
	
	boolean canExportCurriculum(Curriculum curriculum);
	
	
	boolean canDeleteCurriculum();
	
	boolean canExportCurriculums();
	
	boolean canManagerCurriculumUsers();
	
	boolean canNewCurriculumElement(Curriculum curriculum);
	
	boolean canEditCurriculumElements(Curriculum curriculum);

	boolean canViewCurriculumElement(CurriculumElement element);
	
	/**
	 * @param element The curriculum element
	 * @return true if the user can edit the specified curriculum element
	 */
	boolean canEditCurriculumElement(CurriculumElement element);
	
	boolean canExportCurriculumElement(CurriculumElement element);
	
	boolean canMoveCurriculumElement(CurriculumElement element);
	
	boolean canDeleteCurriculumElement(CurriculumElement element);
	
	boolean canEditCurriculumElementSettings(CurriculumElement element);
	
	boolean canEditCurriculumTree();
	
	boolean canManageCurriculumElementsUsers(Curriculum curriculum);
	
	boolean canManageCurriculumElementUsers(CurriculumElement element);

	boolean canViewCurriculumElementResources(CurriculumElement element);
	
	boolean canManageCurriculumElementResources(CurriculumElement element);
	
	boolean canViewCatalogSettings(CurriculumElement element);
	
	boolean canViewAllCalendars();
	
	boolean canNewLectureBlock();
	
	boolean canViewAllLectures(Curriculum curriculum);
	
	boolean canViewAllLearningProgress();
	
	boolean canCurriculumReports(Curriculum curriculum);
	
	boolean canCurriculumsReports();

}
