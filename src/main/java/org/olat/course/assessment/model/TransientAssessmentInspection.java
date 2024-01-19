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
package org.olat.course.assessment.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LockRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionGuardController;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 22 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientAssessmentInspection implements Serializable, LockRequest {
	
	private static final long serialVersionUID = 8485818315734522007L;
	
	private Date from;
	private Date to;
	private int durationInSeconds;
	private String courseDisplayName;
	private String courseExternalRef;
	private String courseNodeName;
	private String courseNodeType;
	
	private Status status;
	private String accessCode;
	private final Long inspectionKey;
	
	private final String elementId;
	private final Long repositoryEntryKey;
	private final OLATResourceable resource;
	
	private String ipList;
	private String safeExamBrowserKey;
	private String safeExamBrowserHint;
	private String safeExamBrowserConfigPList;
	private String safeExamBrowserConfigPListKey;
	private boolean safeExamBrowserConfigDownload;
	private SafeExamBrowserConfiguration safeExamBrowserConfig;
	
	private TransientAssessmentInspection(AssessmentInspection inspection) {
		status = Status.assessment;

		inspectionKey = inspection.getKey();
		elementId = inspection.getSubIdent();
		from = inspection.getFromDate();
		to = inspection.getToDate();
		accessCode = inspection.getAccessCode();
		durationInSeconds = inspection.getConfiguration().getDuration();
		if(inspection.getExtraTime() != null) {
			durationInSeconds += inspection.getExtraTime().intValue();
		}
		
		RepositoryEntry entry = inspection.getConfiguration().getRepositoryEntry();
		entry = CoreSpringFactory.getImpl(RepositoryService.class).loadBy(entry);
		repositoryEntryKey = entry.getKey();
		courseDisplayName = entry.getDisplayname();
		courseExternalRef = entry.getExternalRef();
		
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode element = course.getRunStructure().getNode(elementId);
		if(element != null) {
			courseNodeName = element.getShortTitle();
			courseNodeType = element.getType();
		}
		
		if(inspection.getConfiguration().isSafeExamBrowser()) {
			safeExamBrowserKey = inspection.getConfiguration().getSafeExamBrowserKey();
			safeExamBrowserHint = inspection.getConfiguration().getSafeExamBrowserHint();
			safeExamBrowserConfigPList = inspection.getConfiguration().getSafeExamBrowserConfigPList();
			safeExamBrowserConfigPListKey = inspection.getConfiguration().getSafeExamBrowserConfigPListKey();
			safeExamBrowserConfigDownload = inspection.getConfiguration().isSafeExamBrowserConfigDownload();
			safeExamBrowserConfig = inspection.getConfiguration().getSafeExamBrowserConfiguration();
		}

		resource = OresHelper.createOLATResourceableInstance(AssessmentInspection.class, inspection.getKey());
	}
	
	public static TransientAssessmentInspection valueOf(AssessmentInspection inspection) {
		return new TransientAssessmentInspection(inspection);
	}
	
	public static List<LockRequest> create(List<AssessmentInspection> inspections) {
		List<LockRequest> transientModes = new ArrayList<>(inspections.size());
		for(AssessmentInspection inspection:inspections) {
			transientModes.add(TransientAssessmentInspection.valueOf(inspection));
		}
		return transientModes;
	}
	
	@Override
	public Long getRequestKey() {
		return getInspectionKey();
	}

	@Override
	public OLATResourceable getResource() {
		return resource;
	}

	@Override
	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}
	
	public Long getInspectionKey() {
		return inspectionKey;
	}
	
	public Date getFromDate() {
		return from;
	}
	
	public Date getToDate() {
		return to;
	}
	
	public int getDurationInSeconds() {
		return durationInSeconds;
	}
	
	public String getCourseDisplayName() {
		return courseDisplayName;
	}
	
	public String getCourseExternalRef() {
		return courseExternalRef;
	}
	
	public String getCourseNodeName() {
		return courseNodeName;
	}
	
	public String getCourseNodeType() {
		return courseNodeType;
	}
	
	public String getAccessCode() {
		return accessCode;
	}

	@Override
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public EndStatus getEndStatus() {
		return EndStatus.all;
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

	@Override
	public List<String> getElementList() {
		return List.of(elementId);
	}
	
	public String getIpList() {
		return ipList;
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
	public String getUnlockModalTitle(Locale locale) {
		return Util.createPackageTranslator(AssessmentInspectionGuardController.class, locale)
				.translate("close.inspection.title");
	}

	@Override
	public String getUnlockInfos(Locale locale) {
		AssessmentInspection inspection = CoreSpringFactory.getImpl(AssessmentInspectionService.class).getInspection(inspectionKey);
		if(inspection != null) {
			if(inspection.getInspectionStatus() == AssessmentInspectionStatusEnum.cancelled
					|| inspection.getEndBy() == Role.coach) {
				return Util.createPackageTranslator(AssessmentInspectionGuardController.class, locale)
					.translate("close.inspection.infos.by.coach");
			} else if(inspection.getEndBy() == Role.user) {
				return Util.createPackageTranslator(AssessmentInspectionGuardController.class, locale)
						.translate("close.inspection.infos.by.user");
			}
		}
		
		return Util.createPackageTranslator(AssessmentInspectionGuardController.class, locale)
				.translate("close.inspection.infos");
	}
}
