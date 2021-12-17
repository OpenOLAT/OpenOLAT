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
package org.olat.modules.coach;

/* 
 * Initial date: 17 Jun 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public interface RoleSecurityCallback {
	
	/**
	 * Is this role allowed to reset passwords?
	 * 
	 * @return boolean
	 */
	public boolean canResetPassword(); 
	
	/**
	 * Is this role allowed to view efficiency statements?
	 * 
	 * @return
	 */
	public boolean canViewEfficiencyStatements();
	
	/**
	 * Is this role allowed to show the user's calendar?
	 * 
	 * @return
	 */
	public boolean canViewCalendar();
	
	/**
	 * Is this role allowed to receive certificates per mail?
	 * 
	 * @return
	 */
	public boolean canReceiveCertificatesMail();
	
	/**
	 * Is this role allowed to contact the user?
	 * 
	 * @return
	 */
	public boolean canContact();
	
	/**
	 * Is this role allowed to view the progress and status of a course?
	 * 
	 * @return
	 */
	public boolean canViewCourseProgressAndStatus();
	
	/**
	 * Is this role allowed to see a list of courses and curriculums?
	 * 
	 * @return
	 */
	public boolean canViewCoursesAndCurriculum();
	
	/**
	 * Is this role allowed to see a user's lectures and absences?
	 * 
	 * @return
	 */
	public boolean canViewLecturesAndAbsences();
	
	/**
	 * Is this role allowed to check the quality report?
	 * 
	 * @return
	 */
	public boolean canViewQualityReport();
	
	/**
	 * Is this role allowed to view resources and bookings?
	 * 
	 * @return
	 */
	public boolean canViewResourcesAndBookings();
	
	/**
	 * Is this role allowed to view and edit a profile?
	 * 
	 * @return
	 */
	public boolean canViewAndEditProfile();
	
	/**
	 * Is this role allowed to list a user's group memberships?
	 * 
	 * @return
	 */
	public boolean canViewGroupMemberships();
	
	/**
	 * Is this user allowed to see administrative properties?
	 * 
	 * @return
	 */
	public boolean isAdministrativeUser();
	
	/**	
	 * Is this user allowed to upload external certificates in the efficiency statement?
	 * 
	 * @return
	 */
	public boolean canUploadExternalCertificate();
	
}
