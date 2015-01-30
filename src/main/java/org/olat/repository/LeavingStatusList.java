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
package org.olat.repository;

/**
 * 
 * Initial date: 28.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LeavingStatusList {
	
	private boolean warningManagedCourse;
	private boolean warningManagedGroup;
	private boolean warningGroupWithMultipleResources;
	
	public boolean isWarningManagedCourse() {
		return warningManagedCourse;
	}
	
	public void setWarningManagedCourse(boolean warningManagedCourse) {
		this.warningManagedCourse = warningManagedCourse;
	}
	
	public boolean isWarningManagedGroup() {
		return warningManagedGroup;
	}
	
	public void setWarningManagedGroup(boolean warningManagedGroup) {
		this.warningManagedGroup = warningManagedGroup;
	}
	
	public boolean isWarningGroupWithMultipleResources() {
		return warningGroupWithMultipleResources;
	}
	
	public void setWarningGroupWithMultipleResources(
			boolean warningGroupWithMultipleResources) {
		this.warningGroupWithMultipleResources = warningGroupWithMultipleResources;
	}
}
