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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CurriculumSecurityCallback {
	
	public boolean canNewCurriculum();
	
	public boolean canEditCurriculum();
	
	public boolean canManagerCurriculumUsers();
	
	public boolean canNewCurriculumElement();
	
	public boolean canEditCurriculumElements();
	
	/**
	 * @param element The curriculum element
	 * @return true if the user can edit the specified curriculum element
	 */
	public boolean canEditCurriculumElement(CurriculumElement element);
	
	public boolean canEditCurriculumTree();
	
	public boolean canManagerCurriculumElementsUsers();
	
	public boolean canManagerCurriculumElementUsers(CurriculumElement element);
	
	public boolean canManagerCurriculumElementResources(CurriculumElement element);
	
	public boolean canViewAllCalendars();
	
	public boolean canViewAllLectures();
	
	public boolean canViewAllLearningProgress();

}
