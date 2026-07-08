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
package org.olat.modules.selectus.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingDuplicateApplicationOption;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Position extends PositionRef, OLATResourceable, PositionCommonFields {
	
	public Date getCreationDate();

	public SecurityGroup getCommitteeGroup();
	
	public SecurityGroup getCommitteeHeadGroup();
	
	public void setCommitteeHeadGroup(SecurityGroup headGroup);
	
	public SecurityGroup getSecretaryGroup();
	
	public void setSecretaryGroup(SecurityGroup secretaryGroup);

	public SecurityGroup getExOfficioGroup();
	
	public void setExOfficioGroup(SecurityGroup exOfficioGroup);

	public String getPlaningsNumber();

	public void setPlaningsNumber(String identifier);

	public void setAvailableLanguages(String availableLanguages);

	public void setPositionTitle(String title);

	public void setPositionTitleDe(String title);
	
	public void setPositionTitleFr(String title);
	
	public void setPositionTitle(String title, Locale locale);
	
	public String getMLTitle(Locale locale);

	public String getShortTitle();

	public void setShortTitle(String shortTitle);
	
	public String getShortTitleDe();

	public void setShortTitleDe(String shortTitle);
	
	public String getShortTitleFr();

	public void setShortTitleFr(String shortTitle);
	
	public String getShortTitle(Locale locale);

	public void setShortTitle(String shortTitle, Locale locale);
	
	public String getMLShortTitle(Locale locale);

	public String getDescription();

	public void setDescription(String description);

	public String getDescriptionDe();

	public void setDescriptionDe(String description);
	
	public String getDescriptionFr();

	public void setDescriptionFr(String description);
	
	public String getDescription(Locale locale);

	public void setDescription(String description, Locale locale);
	
	public String getMLDescription(Locale locale);
	
	public String getMessageToCommitte();
	
	public void setMessageToCommitte(String messageToCommitte);

	public void setDepartment(String department);

	public void setDepartmentDe(String department);
	
	public void setDepartmentFr(String department);
	
	public String getMLDepartement(Locale locale);

	public void setDepartment(String description, Locale locale);

	public String getHomepage();

	public void setHomepage(String homepage);
	
	public Date getRatingDeadline();
	
	public void setRatingDeadline(Date deadline);

	public Date getApplicationDeadline();

	public void setApplicationDeadline(Date applicationDeadline);
	
	public Date getCommitteeReminderDate();

	public void setCommitteeReminderDate(Date date);
	
	public Date getCommitteeReminderSentDate();

	public void setCommitteeReminderSentDate(Date committeeReminderSentDate);
	
	public String getCommitteeReminderMailSubject();

	public void setCommitteeReminderMailSubject(String subject);
	
	public String getCommitteeReminderMailTemplate();

	public void setCommitteeReminderMailTemplate(String template);

	public String getCommitteeReminderMailLetter();

	public void setCommitteeReminderMailLetter(String configuration);
	
	
	public String getApplicationConfirmationMailTemplate();
	
	public String getApplicationConfirmationMailTemplate(Locale locale);
	
	public void setApplicationConfirmationMailTemplate(String template);
	
	public void setApplicationConfirmationMailTemplate(String template, Locale locale);

	public String getApplicationConfirmationMailTemplateDe();
	
	public void setApplicationConfirmationMailTemplateDe(String template);

	public String getApplicationConfirmationMailTemplateFr();
	
	public void setApplicationConfirmationMailTemplateFr(String template);
	
	public String getApplicationConfirmationMailLetter();
	
	public void setApplicationConfirmationMailLetter(String letterConfiguration);
	
	
	public String getApplicationConfirmationWithRefereeManagementMailTemplate();
	
	public String getApplicationConfirmationWithRefereeManagementMailTemplate(Locale locale);
	
	public void setApplicationConfirmationWithRefereeManagementMailTemplate(String template);
	
	public void setApplicationConfirmationWithRefereeManagementMailTemplate(String template, Locale locale);

	public String getApplicationConfirmationWithRefereeManagementMailTemplateDe();
	
	public void setApplicationConfirmationWithRefereeManagementMailTemplateDe(String template);
	
	public String getApplicationConfirmationWithRefereeManagementMailTemplateFr();
	
	public void setApplicationConfirmationWithRefereeManagementMailTemplateFr(String template);
	
	public String getApplicationConfirmationWithRefereeManagementMailLetter();
	
	public void setApplicationConfirmationWithRefereeManagementMailLetter(String letterConfiguration);
	
	
	public String getApplicationConfirmationDuplicateMailTemplate(Locale locale);
	
	public void setApplicationConfirmationDuplicateMailTemplate(String template, Locale locale);
	
	public String getApplicationConfirmationDuplicateMailTemplate();

	public void setApplicationConfirmationDuplicateMailTemplate(String template);
	
	public String getApplicationConfirmationDuplicateMailTemplateDe();
	
	public void setApplicationConfirmationDuplicateMailTemplateDe(String template);

	public String getApplicationConfirmationDuplicateMailTemplateFr();
	
	public void setApplicationConfirmationDuplicateMailTemplateFr(String template);
	
	public String getApplicationConfirmationDuplicateMailLetter();

	public void setApplicationConfirmationDuplicateMailLetter(String letterConfiguration);
	
	
	public String getProfessorship();

	public void setProfessorship(String professorship);

	public String getStatus();

	public void setStatus(String status);
	
	public String getJobAds();
	
	public void setJobAds(String ads);
	
	
	public boolean isAdvertised();
	
	public void setAdvertised(boolean val);
	
	public String getTabsConfiguration();

	public void setTabsConfiguration(String tabsConfiguration);
	
	public TabConfiguration getTabConfiguration(Tab tab);

	public void setTabConfiguration(Tab tab, TabConfiguration tabConfiguration);
	
	public List<Tab> getCustomTabsList();
	
	public void setCustomTabsList(List<Tab> tabs);
	
	public List<Tab> getCustomEnabledTabsList();
	
	
	public List<String> getExcludedAttributesList();
	
	public void setExcludedAttributesList(List<String> fields);
	
	/**
	 * @return The setting, can be null if not configured
	 */
	public MailSettingEnum getMailSetting();
	
	public void setMailSetting(MailSettingEnum setting);
	
	public String getSenderMail();
	
	public void setSenderMail(String mail);
	
	public String getBccMail();
	
	public void setBccMail(String mail);
	
	public boolean isDecisionTool();
	
	public void setDecisionTool(boolean enable);
	
	public boolean isApplicationProject();
	
	public void setApplicationProject(boolean enable);
	
	public boolean isApplicationAcademicalBackground();
	
	public void setApplicationAcademicalBackground(boolean enable);
	
	public Set<String> getAvailableDocuments();

	public void setAvailableDocuments(Collection<String> docs);
	
	public Map<DocumentEnum, List<DocumentType>> getDocumentTypes();
	
	public Set<DocumentEnum> getPdfDocuments();
	
	public void setPdfDocuments(Collection<DocumentEnum> pdfs);
	
	public Set<DocumentEnum> getXlsxDocuments();
	
	public void setXlsxDocuments(Collection<DocumentEnum> xlsx);
	
	public Set<DocumentEnum> getDocxDocuments();
	
	public void setDocxDocuments(Collection<DocumentEnum> docs);
	
	public Set<DocumentEnum> getJpgDocuments();
	
	public void setJpgDocuments(Collection<DocumentEnum> jpg);

	public Set<String> getMandatoryDocuments();
	
	public void setMandatoryDocuments(Collection<String> docs);
	
	public Set<String> getStaffDocuments();
	
	public void setStaffDocuments(Collection<String> docs);
	
	public Set<DocumentEnum> getDocumentsInCombinedFile();
	
	public void setDocumentsInCombinedFile(Collection<DocumentEnum> docs);
	
	public Map<DocumentEnum,String> getDocumentNames();
	
	public void setDocumentNames(Map<DocumentEnum,String> names);
	
	public Map<DocumentEnum,String> getDocumentNamesDe();

	public void setDocumentNamesDe(Map<DocumentEnum, String> names);
	
	public Map<DocumentEnum,String> getDocumentNamesFr();

	public void setDocumentNamesFr(Map<DocumentEnum, String> names);
	
	public Map<DocumentEnum,String> getDocumentExplain();

	public void setDocumentExplain(Map<DocumentEnum, String> names);
	
	public Map<DocumentEnum,String> getDocumentExplainDe();

	public void setDocumentExplainDe(Map<DocumentEnum, String> names);
	
	public Map<DocumentEnum,String> getDocumentExplainFr();

	public void setDocumentExplainFr(Map<DocumentEnum, String> names);
	
	public String getDocumentName(DocumentEnum doc, Locale locale);
	
	public void setDocumentName(DocumentEnum doc, Locale locale, String name);
	
	public String getDocumentExplain(DocumentEnum doc, Locale locale);
	
	public void setDocumentExplain(DocumentEnum doc, Locale locale, String explain);
	
	public Map<DocumentEnum,Integer> getDocumentSizes();
	
	public void setDocumentSizes(Map<DocumentEnum,Integer> sizes);
	
	public Attachment getDocument1();
	
	public void setDocument1(Attachment attachment);
	
	public Attachment getDocument2();

	public void setDocument2(Attachment attachment);
	
	public Attachment getDocument3();

	public void setDocument3(Attachment attachment);
	
	public PolicyLink getPolicyLink1();

	public void setPolicyLink1(PolicyLink policyLink1);

	public PolicyLink getPolicyLink2();

	public void setPolicyLink2(PolicyLink policyLink2);

	public PolicyLink getPolicyLink3();

	public void setPolicyLink3(PolicyLink policyLink3);

	public PolicyLink getPolicyLink4();

	public void setPolicyLink4(PolicyLink policyLink4);
	
	public boolean isExpertRecommendationEnabled();

	public void setExpertRecommendationEnabled(boolean expertRecommendationEnabled);

	public Date getExpertRecommandationDeadline();

	public void setExpertRecommandationDeadline(Date expertRecommandationDeadline);
	
	public String getExpertRecommandationMailSubject();

	public void setExpertRecommandationMailSubject(String subject);

	public String getExpertRecommandationMailTemplate();

	public void setExpertRecommandationMailTemplate(String template);
	
	public String getExpertRecommandationMailLetter();

	public void setExpertRecommandationMailLetter(String configuration);
	
	Set<String> getExpertRecommendationDocuments();

	void setExpertRecommendationDocuments(Collection<String> documents);
	
	Set<String> getExpertRecommendationFields();

	void setExpertRecommendationFields(Collection<String> fields);
	
	public String getExpertConfirmationSubmissionMailSubject();

	public void setExpertConfirmationSubmissionMailSubject(String subject);

	public String getExpertConfirmationSubmissionMailTemplate();

	public void setExpertConfirmationSubmissionMailTemplate(String template);

	public boolean isRefereeRecommendationEnabled();

	public void setRefereeRecommendationEnabled(boolean refereeRecommendationEnabled);

	public Date getRefereeRecommandationDeadline();

	public void setRefereeRecommandationDeadline(Date refereeRecommandationDeadline);
	
	public boolean isApplicantRefereeManagementEnabled();

	public void setApplicantRefereeManagementEnabled(boolean applicantRefereeManagementEnabled);

	public Date getApplicantRefereeManagementDeadline();

	public void setApplicantRefereeManagementDeadline(Date applicantRefereeManagementDeadline);
	
	Set<String> getRefereeRecommendationDocuments();

	void setRefereeRecommendationDocuments(Collection<String> documents);
	
	Set<String> getRefereeRecommendationFields();

	void setRefereeRecommendationFields(Collection<String> fields);
	
	
	
	public boolean isComparativeAssessmentExpertEnabled();

	public void setComparativeAssessmentExpertEnabled(boolean enabled);

	public Date getComparativeAssessmentExpertDeadline();

	public void setComparativeAssessmentExpertDeadline(Date deadline);
	
	public String getComparativeAssessmentExpertMailSubject();

	public void setComparativeAssessmentExpertMailSubject(String subject);

	public String getComparativeAssessmentExpertMailTemplate();

	public void setComparativeAssessmentExpertMailTemplate(String template);
	
	public String getComparativeAssessmentExpertMailLetter();

	public void setComparativeAssessmentExpertMailLetter(String configuration);
	
	Set<String> getComparativeAssessmentExpertDocuments();

	void setComparativeAssessmentExpertDocuments(Collection<String> documents);
	
	Set<String> getComparativeAssessmentExpertFields();

	void setComparativeAssessmentExpertFields(Collection<String> fields);
	
	String getComparativeAssessmentExpertConfirmationSubmissionMailSubject();

	void setComparativeAssessmentExpertConfirmationSubmissionMailSubject(String subject);

	String getComparativeAssessmentExpertConfirmationSubmissionMailTemplate();

	void setComparativeAssessmentExpertConfirmationSubmissionMailTemplate(String template);
	

	public Long getMinReferees();
	
	public long getMinRefereesAsLong();

	public void setMinReferees(Long minReferees);

	public Long getMaxReferees();
	
	public long getMaxRefereesAsLong();

	public void setMaxReferees(Long maxReferees);
	
	public String getRefereeRecommandationMailSubject();

	public void setRefereeRecommandationMailSubject(String subject);

	public String getRefereeRecommandationMailTemplate();

	public void setRefereeRecommandationMailTemplate(String template);

	public ReferenceSendMailType getRefereeRecommandationSendMailType();

	public void setRefereeRecommandationSendMailType(ReferenceSendMailType type);

	public String getRefereeRecommandationMailLetter();

	public void setRefereeRecommandationMailLetter(String configuration);
	
	public String getRefereeConfirmationSubmissionMailSubject();

	public void setRefereeConfirmationSubmissionMailSubject(String subject);

	public String getRefereeConfirmationSubmissionMailTemplate();

	public void setRefereeConfirmationSubmissionMailTemplate(String template);
	
	
	public boolean isPublicFeedbackEnabled();

	public void setPublicFeedbackEnabled(boolean enable);

	public Date getPublicFeedbackDeadline();

	public void setPublicFeedbackDeadline(Date deadline);
	
	
	public Organisation getOrganisation();
	
	public void setOrganisation(Organisation organisation);
	
	public boolean isCommitteeCommentEnabled();
	
	public void setCommitteeCommentEnabled(boolean enabled);
	
	public PositionRole[] getCommitteeCommentVisiblity();
	
	public void setCommitteeCommentVisiblity(PositionRole[] roles);

	public boolean isReviewEnabled();

	public void setReviewEnabled(boolean reviewEnabled);

	public PositionReviewDefinition getReviewDefinition();

	public void setReviewDefinition(PositionReviewDefinition reviewDefinition);
	
	public List<PositionAttributeDefinition> getAttributesDefinitions();
	
	public List<PositionAttributeDefinition> getAttributesDefinitions(PositionApplicationAttributeTabEnum tab);
	
	public void setAttributesDefinitions(List<PositionAttributeDefinition> definitions);
	
	public boolean isSystemTagsEnabled();
	
	public void setSystemTagsEnabled(boolean enabled);
	
	public boolean isPositionTagsEnabled();
	
	public void setPositionTagsEnabled(boolean enabled);
	
	public RecruitingDuplicateApplicationOption getDuplicateApplicationAllowedEnum();

	public void setDuplicateApplicationAllowedEnum(RecruitingDuplicateApplicationOption allowed);

	public boolean isValid();
	
	public String toStringFull();
	
	public Set<ApplicationAttribute> getAttributes();

	public void setAttributes(Set<ApplicationAttribute> attributes);
	
}
