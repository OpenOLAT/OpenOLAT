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
import java.util.Locale;

import org.olat.core.commons.fullWebApp.LockRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ActionVerb;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ILoggingResourceable;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentLoggingAction;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.ui.mode.AssessmentModeGuardController;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientAssessmentMode implements Serializable, LockRequest {

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
	private String safeExamBrowserConfigPList;
	private String safeExamBrowserConfigPListKey;
	private boolean safeExamBrowserConfigDownload;
	private SafeExamBrowserConfiguration safeExamBrowserConfig;
	
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
			safeExamBrowserConfigPList = mode.getSafeExamBrowserConfigPList();
			safeExamBrowserConfigPListKey = mode.getSafeExamBrowserConfigPListKey();
			safeExamBrowserConfigDownload = mode.isSafeExamBrowserConfigDownload();
			safeExamBrowserConfig = mode.getSafeExamBrowserConfiguration();
		}
	}
	
	public static List<LockRequest> create(List<AssessmentMode> modes) {
		List<LockRequest> transientModes = new ArrayList<>(modes.size());
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

	@Override
	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	@Override
	public OLATResourceable getResource() {
		return resource;
	}

	public void setResource(OLATResourceable resource) {
		this.resource = resource;
	}
	
	@Override
	public Long getRequestKey() {
		return getModeKey();
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

	@Override
	public Status getStatus() {
		return status;
	}
	
	@Override
	public EndStatus getEndStatus() {
		return endStatus;
	}

	public Date getBegin() {
		return begin;
	}

	public Date getBeginWithLeadTime() {
		return beginWithLeadTime;
	}

	@Override
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

	public String getSafeExamBrowserConfigPList() {
		return safeExamBrowserConfigPList;
	}

	public String getSafeExamBrowserConfigPListKey() {
		return safeExamBrowserConfigPListKey;
	}
	
	public boolean isSafeExamBrowserConfigDownload() {
		return safeExamBrowserConfigDownload;
	}
	
	public SafeExamBrowserConfiguration getSafeExamBrowserConfig() {
		return safeExamBrowserConfig;
	}
	
	@Override
	public boolean hasLinkToQuitSEB() {
		return StringHelper.containsNonWhitespace(safeExamBrowserConfigPList)
				&& safeExamBrowserConfig != null
				&& StringHelper.containsNonWhitespace(safeExamBrowserConfig.getLinkToQuit());
	}

	@Override
	public String getLinkToQuitSEB() {
		if(StringHelper.containsNonWhitespace(safeExamBrowserConfigPList) && safeExamBrowserConfig != null
				&& StringHelper.containsNonWhitespace(safeExamBrowserConfig.getLinkToQuit())) {
			return safeExamBrowserConfig.getLinkToQuit();
		}
		return null;
	}

	public String getStartElementKey() {
		return startElementKey;
	}

	@Override
	public List<String> getElementList() {
		return elementList;
	}
	
	public String getIpList() {
		return ipList;
	}

	@Override
	public String getUnlockInfos(Locale locale) {
		return Util.createPackageTranslator(AssessmentModeGuardController.class, locale)
				.translate("current.mode.end.info");
	}

	@Override
	public String getUnlockModalTitle(Locale locale) {
		return Util.createPackageTranslator(AssessmentModeGuardController.class, locale)
				.translate("current.mode");
	}
	
	@Override
	public ILoggingAction getLoggingAction(ActionVerb verb) {
		if(verb == ActionVerb.lock) {
			return AssessmentLoggingAction.ASSESSMENT_MODE_LOCK;
		} else if(verb == ActionVerb.unlock) {
			return AssessmentLoggingAction.ASSESSMENT_MODE_UNLOCK;
		} else if(verb == ActionVerb.start) {
			return AssessmentLoggingAction.ASSESSMENT_MODE_START;
		} else if(verb == ActionVerb.end) {
			return AssessmentLoggingAction.ASSESSMENT_MODE_END;
		} else if(verb == ActionVerb.guard) {
			return AssessmentLoggingAction.ASSESSMENT_MODE_GUARD;
		}
		return null;
	}

	@Override
	public List<ILoggingResourceable> getLoggingResources() {
		List<ILoggingResourceable> loggingResourceableList = new ArrayList<>();
		loggingResourceableList.add(CoreLoggingResourceable.wrap(getResource(), OlatResourceableType.course, displayName));
		loggingResourceableList.add(CoreLoggingResourceable.wrap(OresHelper
				.createOLATResourceableInstance(AssessmentMode.class, getRequestKey()), OlatResourceableType.assessmentMode, name));
		return loggingResourceableList;
	}
}