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
package org.olat.course.assessment.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientAssessmentMode implements Serializable {

	private static final long serialVersionUID = -5738682288044689497L;
	private String displayName;
	private Long repositoryEntryKey;
	private OLATResourceable resource;
	
	private Long modeKey;
	private String name;
	private String description;
	private Date begin;
	private Date beginWithLeadTime;
	private Date end;
	private Date endWithFollowupTime;
	private int leadTime;
	private int followupTime;
	private String startElementKey;
	private List<String> elementList;
	
	private final boolean manual;
	
	private final Status status;
	private final EndStatus endStatus;
	
	private String ipList;
	private String safeExamBrowserKey;
	private String safeExamBrowserHint;
	
	public TransientAssessmentMode(AssessmentMode mode) {
		displayName = mode.getRepositoryEntry().getDisplayname();
		resource = OresHelper.clone(mode.getRepositoryEntry().getOlatResource());
		repositoryEntryKey = mode.getRepositoryEntry().getKey();
		
		modeKey = mode.getKey();
		name = mode.getName();
		description = mode.getDescription();
		begin = mode.getBegin();
		beginWithLeadTime = mode.getBeginWithLeadTime();
		end = mode.getEnd();
		endWithFollowupTime = mode.getEndWithFollowupTime();
		
		leadTime = mode.getLeadTime();
		followupTime = mode.getFollowupTime();
		startElementKey = mode.getStartElement();
		elementList = mode.getElementAsList();
		
		manual = mode.isManualBeginEnd();
		
		status = mode.getStatus();
		endStatus = mode.getEndStatus();

		if(mode.isRestrictAccessIps()) {
			ipList = mode.getIpList();
		}
		
		if(mode.isSafeExamBrowser()) {
			safeExamBrowserKey = mode.getSafeExamBrowserKey();
			safeExamBrowserHint = mode.getSafeExamBrowserHint();
		}
	}
	
	public static List<TransientAssessmentMode> create(List<AssessmentMode> modes) {
		List<TransientAssessmentMode> transientModes = new ArrayList<>(modes.size());
		for(AssessmentMode mode:modes) {
			transientModes.add(new TransientAssessmentMode(mode));
		}
		return transientModes;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public OLATResourceable getResource() {
		return resource;
	}

	public void setResource(OLATResourceable resource) {
		this.resource = resource;
	}

	public Long getModeKey() {
		return modeKey;
	}

	public void setModeKey(Long modeKey) {
		this.modeKey = modeKey;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Status getStatus() {
		return status;
	}
	
	public EndStatus getEndStatus() {
		return endStatus;
	}

	public Date getBegin() {
		return begin;
	}

	public Date getBeginWithLeadTime() {
		return beginWithLeadTime;
	}

	public Date getEnd() {
		return end;
	}

	public Date getEndWithFollowupTime() {
		return endWithFollowupTime;
	}

	public boolean isManual() {
		return manual;
	}

	public int getLeadTime() {
		return leadTime;
	}
	
	public int getFollowupTime() {
		return followupTime;
	}

	public String getSafeExamBrowserKey() {
		return safeExamBrowserKey;
	}

	public String getSafeExamBrowserHint() {
		return safeExamBrowserHint;
	}

	public String getStartElementKey() {
		return startElementKey;
	}
	
	public List<String> getElementList() {
		return elementList;
	}
	
	public String getIpList() {
		return ipList;
	}
	
}