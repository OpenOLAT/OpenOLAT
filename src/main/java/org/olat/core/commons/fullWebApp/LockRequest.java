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
package org.olat.core.commons.fullWebApp;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.OLATResourceable;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;

/**
 * 
 * Initial date: 22 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LockRequest {
	
	Long getRequestKey();
	
	OLATResourceable getResource();
	
	Long getRepositoryEntryKey();
	
	Status getStatus();
	
	EndStatus getEndStatus();
	
	boolean hasLinkToQuitSEB();
	
	String getLinkToQuitSEB();
	
	List<String> getElementList();
	
	
	String getUnlockModalTitle(Locale locale);
	
	String getUnlockInfos(Locale locale);

}
