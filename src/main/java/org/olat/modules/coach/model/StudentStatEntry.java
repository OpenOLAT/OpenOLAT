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
package org.olat.modules.coach.model;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;


/**
 * 
 * Dummy bean to transport statistic values about student
 *  
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class StudentStatEntry extends UserPropertiesRow implements CompletionStats {
	
	private int countRepo = 0;
	private int countPassed = 0;
	private int countFailed = 0;
	private int countNotAttempted = 0;
	private int initialLaunch = 0;
	private Double averageCompletion;
	
	private Set<String> repoIds = new HashSet<>();
	private Set<String> launchIds = new HashSet<>();
	
	public StudentStatEntry(Long identityKey, List<UserPropertyHandler> userPropertyHandlers, String[] userProperties, Locale locale) {
		super(identityKey, userPropertyHandlers, userProperties, locale);
	}

	public StudentStatEntry(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
	}
	
	public int getCountRepo() {
		return countRepo;
	}
	
	public void setCountRepo(int countRepo) {
		this.countRepo = countRepo;
	}

	public Set<String> getRepoIds() {
		return repoIds;
	}

	public void setRepoIds(Set<String> repoIds) {
		this.repoIds = repoIds;
	}

	public Set<String> getLaunchIds() {
		return launchIds;
	}

	public void setLaunchIds(Set<String> launchIds) {
		this.launchIds = launchIds;
	}

	public int getCountPassed() {
		return countPassed;
	}

	public void setCountPassed(int countPassed) {
		this.countPassed = countPassed;
	}

	public int getCountFailed() {
		return countFailed;
	}

	public void setCountFailed(int countFailed) {
		this.countFailed = countFailed;
	}

	public int getCountNotAttempted() {
		return countNotAttempted;
	}

	public void setCountNotAttempted(int countNotAttempted) {
		this.countNotAttempted = countNotAttempted;
	}

	public int getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(int initialLaunch) {
		this.initialLaunch = initialLaunch;
	}

	@Override
	public Double getAverageCompletion() {
		return averageCompletion;
	}

	@Override
	public void setAverageCompletion(Double averageCompletion) {
		this.averageCompletion = averageCompletion;
	}
}
