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
package org.olat.course.assessment.restapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.manager.SafeExamBrowserConfigurationSerializer;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.restapi.CurriculumElementVO;
import org.olat.restapi.support.vo.GroupVO;

/**
 * 
 * Initial date: 3 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assessmentModeVO")
public class AssessmentModeVO {
	
	private Long key;
	
	private Date creationDate;
	
	private String externalId;
	private String managedFlagsString;
	
	private String name;
	private String description;

	private String status;
	private String endStatus;

	private Date begin;
	private int leadTime;
	private Date end;
	private int followupTime;
	
	private Boolean manualBeginEnd;
	private String targetAudience;
	
	private Boolean restrictAccessIps;
	private String ipList;
	
	private Boolean restrictAccessElements;
	private String elementList;
	private String startElement;
	
	private Boolean safeExamBrowser;
	private String safeExamBrowserKey;
	private String safeExamBrowserHint;
	private String safeExamBrowserConfigXml;
	private String safeExamBrowserConfigPlist;
	private String safeExamBrowserConfigPlistKey;
	private Boolean safeExamBrowserConfigDownload;
	
	private Boolean applySettingsForCoach;
	
	private Long repositoryEntryKey;
	private Long lectureBlockKey;
	
	private CurriculumElementVO[] curriculumElements;
	private GroupVO[] businessGroups;
	
	public AssessmentModeVO() {
		//
	}
	
	public static AssessmentModeVO valueOf(AssessmentMode mode) {
		AssessmentModeVO vo = new AssessmentModeVO();
		vo.setKey(mode.getKey());
		vo.setCreationDate(mode.getCreationDate());
		vo.setRepositoryEntryKey(mode.getRepositoryEntry().getKey());
		if(mode.getLectureBlock() != null) {
			vo.setLectureBlockKey(mode.getLectureBlock().getKey());
		}
		vo.setName(mode.getName());
		vo.setDescription(mode.getDescription());
		vo.setExternalId(mode.getExternalId());
		vo.setManagedFlagsString(mode.getManagedFlagsString());
		
		Status status = mode.getStatus();
		if(status != null) {
			vo.setStatus(status.name());
		}
		EndStatus endStatus = mode.getEndStatus();
		if(endStatus != null) {
			vo.setEndStatus(endStatus.name());
		}
		
		vo.setBegin(mode.getBegin());
		vo.setLeadTime(mode.getLeadTime());
		vo.setEnd(mode.getEnd());
		vo.setFollowupTime(mode.getFollowupTime());
		
		vo.setManualBeginEnd(mode.isManualBeginEnd());
		Target target = mode.getTargetAudience();
		if(target != null) {
			vo.setTargetAudience(target.name());
		}
		
		vo.setRestrictAccessIps(mode.isRestrictAccessIps());
		vo.setIpList(mode.getIpList());
		
		vo.setRestrictAccessElements(mode.isRestrictAccessElements());
		vo.setElementList(mode.getElementList());
		vo.setStartElement(mode.getStartElement());
		
		vo.setSafeExamBrowser(mode.isSafeExamBrowser());
		if(mode.isSafeExamBrowser()) {
			vo.setSafeExamBrowserKey(mode.getSafeExamBrowserKey());
			vo.setSafeExamBrowserHint(mode.getSafeExamBrowserHint());
			vo.setSafeExamBrowserConfigDownload(mode.isSafeExamBrowserConfigDownload());
			SafeExamBrowserConfiguration configuration = mode.getSafeExamBrowserConfiguration();
			if(configuration != null) {
				vo.setSafeExamBrowserConfigXml(SafeExamBrowserConfigurationSerializer.toXml(configuration));
				vo.setSafeExamBrowserConfigPlist(mode.getSafeExamBrowserConfigPList());
				vo.setSafeExamBrowserConfigPlistKey(mode.getSafeExamBrowserConfigPListKey());
			}
		}
		
		List<CurriculumElementVO> elements = new ArrayList<>();
		for(AssessmentModeToCurriculumElement rel:mode.getCurriculumElements()) {
			CurriculumElement el = rel.getCurriculumElement();
			elements.add(CurriculumElementVO.valueOf(el));
		}
		vo.setCurriculumElements(elements.toArray(new CurriculumElementVO[elements.size()]));
		
		List<GroupVO> groups = new ArrayList<>();
		for(AssessmentModeToGroup rel:mode.getGroups()) {
			BusinessGroup businessGroup = rel.getBusinessGroup();
			groups.add(GroupVO.valueOf(businessGroup));
		}
		vo.setBusinessGroups(groups.toArray(new GroupVO[groups.size()]));
		
		return vo;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(String endStatus) {
		this.endStatus = endStatus;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public int getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public int getFollowupTime() {
		return followupTime;
	}

	public void setFollowupTime(int followupTime) {
		this.followupTime = followupTime;
	}

	public Boolean getManualBeginEnd() {
		return manualBeginEnd;
	}

	public void setManualBeginEnd(Boolean manualBeginEnd) {
		this.manualBeginEnd = manualBeginEnd;
	}

	public String getTargetAudience() {
		return targetAudience;
	}

	public void setTargetAudience(String targetAudience) {
		this.targetAudience = targetAudience;
	}

	public Boolean getRestrictAccessIps() {
		return restrictAccessIps;
	}

	public void setRestrictAccessIps(Boolean restrictAccessIps) {
		this.restrictAccessIps = restrictAccessIps;
	}

	public String getIpList() {
		return ipList;
	}

	public void setIpList(String ipList) {
		this.ipList = ipList;
	}

	public Boolean getRestrictAccessElements() {
		return restrictAccessElements;
	}

	public void setRestrictAccessElements(Boolean restrictAccessElements) {
		this.restrictAccessElements = restrictAccessElements;
	}

	public String getElementList() {
		return elementList;
	}

	public void setElementList(String elementList) {
		this.elementList = elementList;
	}

	public String getStartElement() {
		return startElement;
	}

	public void setStartElement(String startElement) {
		this.startElement = startElement;
	}

	public Boolean getSafeExamBrowser() {
		return safeExamBrowser;
	}

	public void setSafeExamBrowser(Boolean safeExamBrowser) {
		this.safeExamBrowser = safeExamBrowser;
	}

	public String getSafeExamBrowserKey() {
		return safeExamBrowserKey;
	}

	public void setSafeExamBrowserKey(String safeExamBrowserKey) {
		this.safeExamBrowserKey = safeExamBrowserKey;
	}

	public String getSafeExamBrowserHint() {
		return safeExamBrowserHint;
	}

	public void setSafeExamBrowserHint(String safeExamBrowserHint) {
		this.safeExamBrowserHint = safeExamBrowserHint;
	}

	public String getSafeExamBrowserConfigXml() {
		return safeExamBrowserConfigXml;
	}

	public void setSafeExamBrowserConfigXml(String safeExamBrowserConfigXml) {
		this.safeExamBrowserConfigXml = safeExamBrowserConfigXml;
	}

	public String getSafeExamBrowserConfigPlist() {
		return safeExamBrowserConfigPlist;
	}

	public void setSafeExamBrowserConfigPlist(String safeExamBrowserConfigPlist) {
		this.safeExamBrowserConfigPlist = safeExamBrowserConfigPlist;
	}

	public String getSafeExamBrowserConfigPlistKey() {
		return safeExamBrowserConfigPlistKey;
	}

	public void setSafeExamBrowserConfigPlistKey(String safeExamBrowserConfigPlistKey) {
		this.safeExamBrowserConfigPlistKey = safeExamBrowserConfigPlistKey;
	}

	public Boolean getSafeExamBrowserConfigDownload() {
		return safeExamBrowserConfigDownload;
	}

	public void setSafeExamBrowserConfigDownload(Boolean safeExamBrowserConfigDownload) {
		this.safeExamBrowserConfigDownload = safeExamBrowserConfigDownload;
	}

	public Boolean getApplySettingsForCoach() {
		return applySettingsForCoach;
	}

	public void setApplySettingsForCoach(Boolean applySettingsForCoach) {
		this.applySettingsForCoach = applySettingsForCoach;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public void setRepositoryEntryKey(Long repositoryEntryKey) {
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public Long getLectureBlockKey() {
		return lectureBlockKey;
	}

	public void setLectureBlockKey(Long lectureBlockKey) {
		this.lectureBlockKey = lectureBlockKey;
	}
	
	public CurriculumElementVO[] getCurriculumElements() {
		return curriculumElements;
	}

	public void setCurriculumElements(CurriculumElementVO[] curriculumElements) {
		this.curriculumElements = curriculumElements;
	}

	public GroupVO[] getBusinessGroups() {
		return businessGroups;
	}

	public void setBusinessGroups(GroupVO[] businessGroups) {
		this.businessGroups = businessGroups;
	}

	@Override
	public String toString() {
		return "AssessmentModeVO[key=" + key + ":repoEntryKey=" + repositoryEntryKey + "]";
	}
	
	@Override
	public int hashCode() {
		return key == null ? 24348 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AssessmentModeVO) {
			AssessmentModeVO statement = (AssessmentModeVO)obj;
			return key != null && key.equals(statement.getKey());
		}
		return super.equals(obj);
	}
}
