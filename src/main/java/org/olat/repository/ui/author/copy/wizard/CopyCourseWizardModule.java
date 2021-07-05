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
package org.olat.repository.ui.author.copy.wizard;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 05.07.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class CopyCourseWizardModule extends AbstractSpringModule {

	private static final String WIZARD_MODE = "course.copy.wizard.mode";
	
	private static final String META_DATA_COPY_TYPE = "course.copy.wizard.metadata";
	private static final String GROUPS_COPY_TYPE = "course.copy.wizard.groups";
	private static final String OWNERS_COPY_TYPE = "course.copy.wizard.owners";
	private static final String COACHES_COPY_TYPE = "course.copy.wizard.coaches";
	private static final String CATALOG_COPY_TYPE = "course.copy.wizard.catalog";
	private static final String DISCLAIMER_COPY_TYPE = "course.copy.wizard.disclaimer";
	
	private static final String BLOG_COPY_TYPE = "course.copy.wizard.blog";
	private static final String FOLDER_COPY_TYPE = "course.copy.wizard.folder";
	private static final String WIKI_COPY_TYPE = "course.copy.wizard.wiki";
	
	private static final String REMINDER_COPY_TYPE = "course.copy.wizard.reminder";
	private static final String ASSESSMENT_MODE_COPY_TYPE = "course.copy.wizard.assessment";
	private static final String LECTURE_BLOCK_COPY_TYPE = "course.copy.wizard.lecture.block";
	
	@Value("${course.copy.wizard.mode}")
	private CopyType wizardMode;
	
	@Value("${course.copy.wizard.metadata}")
	private CopyType metaDataCopyType;
	@Value("${course.copy.wizard.groups}")
	private CopyType groupsCopyType;
	@Value("${course.copy.wizard.owners}")
	private CopyType ownersCopyType;
	@Value("${course.copy.wizard.coaches}")
	private CopyType coachesCopyType;
	@Value("${course.copy.wizard.catalog}")
	private CopyType catalogCopyType;
	@Value("${course.copy.wizard.disclaimer}")
	private CopyType disclaimerCopyType;
	
	@Value("${course.copy.wizard.blog}")
	private CopyType blogCopyType;
	@Value("${course.copy.wizard.folder}")
	private CopyType folderCopyType;
	@Value("${course.copy.wizard.wiki}")
	private CopyType wikiCopyType;
	
	@Value("${course.copy.wizard.reminder}")
	private CopyType reminderCopyType;
	@Value("${course.copy.wizard.assessment}")
	private CopyType assessmentCopyType;
	@Value("${course.copy.wizard.lecture.block}")
	private CopyType lectureBlockCopyType;
	
	
	public CopyCourseWizardModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		initFromChangedProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		wizardMode = getCopyType(WIZARD_MODE, wizardMode);
		
		metaDataCopyType = getCopyType(META_DATA_COPY_TYPE, metaDataCopyType);
		groupsCopyType = getCopyType(GROUPS_COPY_TYPE, groupsCopyType);
		ownersCopyType = getCopyType(OWNERS_COPY_TYPE, ownersCopyType);
		coachesCopyType = getCopyType(COACHES_COPY_TYPE, coachesCopyType);
		catalogCopyType = getCopyType(CATALOG_COPY_TYPE, catalogCopyType);
		disclaimerCopyType = getCopyType(DISCLAIMER_COPY_TYPE, disclaimerCopyType);
		
		blogCopyType = getCopyType(BLOG_COPY_TYPE, blogCopyType);
		folderCopyType = getCopyType(FOLDER_COPY_TYPE, folderCopyType);
		wikiCopyType = getCopyType(WIKI_COPY_TYPE, wikiCopyType);
		
		reminderCopyType = getCopyType(REMINDER_COPY_TYPE, reminderCopyType);
		assessmentCopyType = getCopyType(ASSESSMENT_MODE_COPY_TYPE, assessmentCopyType);
		lectureBlockCopyType = getCopyType(LECTURE_BLOCK_COPY_TYPE, lectureBlockCopyType);
	}
	
	private CopyType getCopyType(String property, CopyType fallBack) {
        String propertyObj = getStringPropertyValue(property, false);
        if (propertyObj != null) {
            try {
                return CopyType.valueOf(propertyObj);
            } catch (Exception exception) {
            	// Nothing to do here
            }
        }
        
        return fallBack;
    }
	
	public CopyType getWizardMode() {
		return wizardMode;
	}
	
	public void setWizardMode(CopyType wizardMode) {
		this.wizardMode = wizardMode;
		setStringProperty(WIZARD_MODE, wizardMode.name(), true);
	}
	
	public CopyType getMetaDataCopyType() {
		return metaDataCopyType;
	}

	public void setMetaDataCopyType(CopyType metaDataCopyType) {
		this.metaDataCopyType = metaDataCopyType;
		setStringProperty(META_DATA_COPY_TYPE, metaDataCopyType.name(), true);
	}

	public CopyType getGroupsCopyType() {
		return groupsCopyType;
	}

	public void setGroupsCopyType(CopyType groupsCopyType) {
		this.groupsCopyType = groupsCopyType;
		setStringProperty(GROUPS_COPY_TYPE, groupsCopyType.name(), true);
	}

	public CopyType getOwnersCopyType() {
		return ownersCopyType;
	}

	public void setOwnersCopyType(CopyType ownersCopyType) {
		this.ownersCopyType = ownersCopyType;
		setStringProperty(OWNERS_COPY_TYPE, ownersCopyType.name(), true);
	}

	public CopyType getCoachesCopyType() {
		return coachesCopyType;
	}

	public void setCoachesCopyType(CopyType coachesCopyType) {
		this.coachesCopyType = coachesCopyType;
		setStringProperty(COACHES_COPY_TYPE, coachesCopyType.name(), true);
	}

	public CopyType getCatalogCopyType() {
		return catalogCopyType;
	}

	public void setCatalogCopyType(CopyType catalogCopyType) {
		this.catalogCopyType = catalogCopyType;
		setStringProperty(CATALOG_COPY_TYPE, catalogCopyType.name(), true);
	}

	public CopyType getDisclaimerCopyType() {
		return disclaimerCopyType;
	}

	public void setDisclaimerCopyType(CopyType disclaimerCopyType) {
		this.disclaimerCopyType = disclaimerCopyType;
		setStringProperty(DISCLAIMER_COPY_TYPE, disclaimerCopyType.name(), true);
	}

	public CopyType getBlogCopyType() {
		return blogCopyType;
	}

	public void setBlogCopyType(CopyType blogCopyType) {
		this.blogCopyType = blogCopyType;
		setStringProperty(BLOG_COPY_TYPE, blogCopyType.name(), true);
	}

	public CopyType getFolderCopyType() {
		return folderCopyType;
	}

	public void setFolderCopyType(CopyType folderCopyType) {
		this.folderCopyType = folderCopyType;
		setStringProperty(FOLDER_COPY_TYPE, folderCopyType.name(), true);
	}

	public CopyType getWikiCopyType() {
		return wikiCopyType;
	}

	public void setWikiCopyType(CopyType wikiCopyType) {
		this.wikiCopyType = wikiCopyType;
		setStringProperty(WIKI_COPY_TYPE, wikiCopyType.name(), true);
	}

	public CopyType getReminderCopyType() {
		return reminderCopyType;
	}

	public void setReminderCopyType(CopyType reminderCopyType) {
		this.reminderCopyType = reminderCopyType;
		setStringProperty(REMINDER_COPY_TYPE, reminderCopyType.name(), true);
	}

	public CopyType getAssessmentCopyType() {
		return assessmentCopyType;
	}

	public void setAssessmentCopyType(CopyType assessmentCopyType) {
		this.assessmentCopyType = assessmentCopyType;
		setStringProperty(ASSESSMENT_MODE_COPY_TYPE, assessmentCopyType.name(), true);
	}

	public CopyType getLectureBlockCopyType() {
		return lectureBlockCopyType;
	}

	public void setLectureBlockCopyType(CopyType lectureBlockCopyType) {
		this.lectureBlockCopyType = lectureBlockCopyType;
		setStringProperty(LECTURE_BLOCK_COPY_TYPE, lectureBlockCopyType.name(), true);
	}
}
