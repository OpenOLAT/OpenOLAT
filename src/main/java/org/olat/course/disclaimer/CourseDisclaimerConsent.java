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
package org.olat.course.disclaimer;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntry;

/* 
 * Date: 19 Mar 2020<br>
 * @author Alexander Boeckle
 */
public interface CourseDisclaimerConsent extends CreateInfo, ModifiedInfo, Persistable {
	/**
	 * Returns whether the disclaimer 1 is accepted or not
	 * 
	 * @return boolean
	 */
	public boolean isDisc1Accepted();
	
	/**
	 * Set the consent for disclaimer 1
	 * 
	 * @param accepted
	 */
	public void setDisc1(boolean accepted);
	
	/**
	 * Returns whether the disclaimer 1 is accepted or not
	 * 
	 * @return boolean
	 */
	public boolean isDisc2Accepted();
	
	/**
	 * Set the consent for disclaimer 1
	 * 
	 * @param accepted
	 */
	public void setDisc2(boolean accepted);
	
	/**
	 * Returns the repository entry for this consent
	 * 
	 * @return RepositoryEntry
	 */
	public RepositoryEntry getRepositoryEntry();
	
	/**
	 * Set the repository entry for this consent
	 * 
	 * @param repositoryEntry
	 */
	public void setRepositoryEntry(RepositoryEntry repositoryEntry);
	
	/**
	 * Returns the identity related to this consent
	 * 
	 * @return Identity
	 */
	public Identity getIdentity();
	
	/*
	 * Set the identity related to this consent
	 */
	public void setIdentity(Identity identity);
	
	/**
	 * Returns the date when the disclaimer was accepted
	 * 
	 * @return Date
	 */
	public Date getConsentDate();
}
