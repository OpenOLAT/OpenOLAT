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
package org.olat.modules.lecture.model;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableTimeLineRow;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.ui.LectureListDetailsController;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallBasicStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRow implements LectureBlockRef, FlexiTableTimeLineRow {
	
	private final String teachers;
	private final boolean iamTeacher;
	private final String entryExternalRef;
	private final String entryDisplayname;
	private LectureBlock lectureBlock;
	private boolean assessmentMode;
	private long numOfParticipants;
	private long leadTime;
	private long followupTime;
	private final Reference curriculumElement;
	private final Reference entry;
	private final boolean rollCallEnabled;
	private boolean nextScheduled;
	
	private FormLink toolsLink;
	private LectureListDetailsController detailsCtrl;
	
	private DateChooser dateChooser;
	private FormLink teacherChooserLink;
	private TextElement locationElement;
	private List<Identity> teachersList;

	private FormLink rollCallLink;
	private FormItem openOnlineMeetingLink;
	
	private final String entryUrl;
	private final ZonedDateTime date;
	private final Translator translator;
	private List<TaxonomyLevel> subjects;

	public LectureBlockRow(LectureBlock lectureBlock, ZonedDateTime date,
			String entryDisplayname, String externalRef,
			String teachers, boolean iamTeacher, Reference curriculumElement, Reference entry,
			long numOfParticipants, long leadTime, long followupTime,
			boolean assessmentMode, boolean rollcallEnabled, Translator translator) {
		this.translator = translator;
		this.lectureBlock = lectureBlock;
		this.date = date;
		this.teachers = teachers;
		this.entry = entry;
		this.entryUrl = (entry != null && entry.key() != null)
				? BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + entry.key() + "]")
				: null;
		this.leadTime = leadTime;
		this.followupTime = followupTime;
		this.numOfParticipants = numOfParticipants;
		this.curriculumElement = curriculumElement;

		this.iamTeacher = iamTeacher;
		this.entryExternalRef = externalRef;
		this.entryDisplayname = entryDisplayname;
		this.assessmentMode = assessmentMode;
		this.rollCallEnabled = rollcallEnabled;
	}
	
	@Override
	public Long getKey() {
		return lectureBlock.getKey();
	}
	
	public String getTitle() {
		return lectureBlock.getTitle();
	}
	
	public String getExternalRef() {
		return lectureBlock.getExternalRef();
	}
	
	public String getLocation() {
		return lectureBlock.getLocation();
	}
	
	@Override
	public ZonedDateTime getDate() {
		return date;
	}
	
	public Date getStartDate() {
		return lectureBlock.getStartDate();
	}
	
	public Date getEndDate() {
		return lectureBlock.getEndDate();
	}

	public boolean isIamTeacher() {
		return iamTeacher;
	}
	
	public String getEntryExternalRef() {
		return entryExternalRef;
	}
	
	public String getEntryDisplayname() {
		return entryDisplayname;
	}
	
	public String getEntryUrl() {
		return entryUrl;
	}

	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
	
	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}
	
	public String getLectureBlockStatusBadge() {
		return LectureBlockStatusCellRenderer.getStatusLabel(lectureBlock, isNextScheduled(), translator);
	}
	
	public String getRollCallStatusBadge() {
		return rollCallEnabled
				? LectureBlockRollCallBasicStatusCellRenderer.getStatusLabel(lectureBlock, translator)
				: "";
	}
	
	public long getNumOfParticipants() {
		return numOfParticipants;
	}
	
	public void setNumOfParticipants(long participants) {
		this.numOfParticipants = participants;
	}
	
	public long getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(long leadTime) {
		this.leadTime = leadTime;
	}

	public long getFollowupTime() {
		return followupTime;
	}

	public void setFollowupTime(long followupTime) {
		this.followupTime = followupTime;
	}

	public String getTeachers() {
		return teachers;
	}
	
	public Reference getCurriculumElement() {
		return curriculumElement;
	}

	public Reference getEntry() {
		return entry;
	}

	public boolean isAssessmentMode() {
		return assessmentMode;
	}
	
	public void setAssessmentMode(boolean assessmentMode) {
		this.assessmentMode = assessmentMode;
	}
	
	public boolean isRollCallEnabled() {
		return rollCallEnabled;
	}
	
	public boolean isNextScheduled() {
		return nextScheduled;
	}

	public void setNextScheduled(boolean nextScheduled) {
		this.nextScheduled = nextScheduled;
	}

	public DateChooser getDateChooser() {
		return dateChooser;
	}
	
	public void setDateChooser(DateChooser dateChooser) {
		this.dateChooser = dateChooser;
	}
	
	public FormLink getTeacherChooserLink() {
		return teacherChooserLink;
	}
	
	public void setTeacherChooserLink(FormLink teacherChooserLink) {
		this.teacherChooserLink = teacherChooserLink;
	}
	
	public TextElement getLocationElement() {
		return locationElement;
	}
	
	public void setLocationElement(TextElement locationElement) {
		this.locationElement = locationElement;
	}
	
	public List<Identity> getTeachersList() {
		return teachersList;
	}
	
	public void setTeachersList(List<Identity> teachersList) {
		this.teachersList = teachersList;
	}
	
	public FormLink getToolsLink() {
		if(toolsLink != null) {
			toolsLink.setCustomEnabledLinkCSS("");
			toolsLink.setDomReplacementWrapperRequired(false);
			toolsLink.setFormLayout("nolayout");
		}
		return toolsLink;
	}
	
	public FormLink getToolsButton() {
		if(toolsLink != null) {
			toolsLink.setCustomEnabledLinkCSS("btn btn-default");
			toolsLink.setDomReplacementWrapperRequired(false);
			toolsLink.setFormLayout("nolayout");
		}
		return toolsLink;
	}
	
	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public boolean hasOnlineMeeting() {
		return openOnlineMeetingLink != null;
	}
	
	public String getMeetingTitle() {
		return lectureBlock.getMeetingTitle();
	}
	
	public FormItem getOpenOnlineMeetingSmallButton() {
		if(openOnlineMeetingLink instanceof FormLink link) {
			link.setCustomEnabledLinkCSS("btn btn-xs btn-default");
		} else if(openOnlineMeetingLink instanceof ExternalLinkItem link) {
			link.setCssClass("btn btn-xs btn-default");
		}
		return openOnlineMeetingLink;
	}
	
	public FormItem getOpenOnlineMeetingButton() {
		if(openOnlineMeetingLink instanceof FormLink link) {
			link.setCustomEnabledLinkCSS("btn btn-default");
		} else if(openOnlineMeetingLink instanceof ExternalLinkItem link) {
			link.setCssClass("btn btn-default");
		}
		return openOnlineMeetingLink;
	}

	public void setOpenOnlineMeetingLink(FormItem openOnlineMeetingLink) {
		this.openOnlineMeetingLink = openOnlineMeetingLink;
	}
	
	public FormLink getRollCallLink() {
		if(rollCallLink != null) {
			rollCallLink.setCustomEnabledLinkCSS("");
			rollCallLink.setI18nKey(null);
			rollCallLink.setFormLayout("nolayout");
		}
		return rollCallLink;
	}
	
	public FormLink getRollCallButton() {
		if(rollCallLink != null) {
			rollCallLink.setCustomEnabledLinkCSS("btn btn-default");
			rollCallLink.setI18nKey(translator.translate("edit.type.absence"));
			rollCallLink.setFormLayout("nolayout");
		}
		return rollCallLink;
	}
	
	public void setRollCallButton(FormLink rollCallLink) {
		this.rollCallLink = rollCallLink;
	}

	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public LectureListDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(LectureListDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}

	public List<TaxonomyLevel> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<TaxonomyLevel> subjects) {
		this.subjects = subjects;
	}
}
