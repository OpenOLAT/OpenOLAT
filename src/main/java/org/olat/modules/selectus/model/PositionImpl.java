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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingDuplicateApplicationOption;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.model.position.TabsConfigurationXStream;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionReviewDefinitionImpl;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="rposition")
@Table(name="o_selectus_position")
@NamedQuery(name="positionOrderedByCreationDate", query="select position from rposition position order by position.creationDate desc")
public class PositionImpl implements Position, CreateInfo, Persistable {

	private static final long serialVersionUID = 7846992495959711760L;
	private static final Logger log = Tracing.createLoggerFor(PositionImpl.class);
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="pos_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;
	
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	
	@Column(name="positiontitle", nullable=true, unique=false, insertable=true, updatable=true)
	private String positionTitle;
	@Column(name="positiontitlede", nullable=true, unique=false, insertable=true, updatable=true)
	private String positionTitleDe;
	@Column(name="positiontitlefr", nullable=true, unique=false, insertable=true, updatable=true)
	private String positionTitleFr;
	
	@Column(name="shorttitle", nullable=true, unique=false, insertable=true, updatable=true)
	private String shortTitle;
	@Column(name="shorttitlede", nullable=true, unique=false, insertable=true, updatable=true)
	private String shortTitleDe;
	@Column(name="shorttitlefr", nullable=true, unique=false, insertable=true, updatable=true)
	private String shortTitleFr;
	
	@Column(name="descr", nullable=true, unique=false, insertable=true, updatable=true)
	private String description;
	@Column(name="descrde", nullable=true, unique=false, insertable=true, updatable=true)
	private String descriptionDe;
	@Column(name="descrfr", nullable=true, unique=false, insertable=true, updatable=true)
	private String descriptionFr;
	
	@Column(name="availablelanguages", nullable=true, unique=false, insertable=true, updatable=true)
	private String availableLanguages;

	@Column(name="planingsnumber", nullable=true, unique=false, insertable=true, updatable=true)
	private String planingsNumber;
	@Column(name="professorship", nullable=true, unique=false, insertable=true, updatable=true)
	private String professorship;
	@Column(name="department", nullable=true, unique=false, insertable=true, updatable=true)
	private String department;
	@Column(name="departmentde", nullable=true, unique=false, insertable=true, updatable=true)
	private String departmentDe;
	@Column(name="departmentfr", nullable=true, unique=false, insertable=true, updatable=true)
	private String departmentFr;
	
	@Column(name="decisiontool", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean decisionTool;
	
	@Column(name="applicationproject", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean applicationProject;
	
	@Column(name="academicalbackground", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean applicationAcademicalBackground;
	
	@Column(name="messagecommittee", nullable=true, unique=false, insertable=true, updatable=true)
	private String messageToCommitte;
	
	@Column(name="advertised", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean advertised;
	
	@Column(name="tabsconfiguration", nullable=true, unique=false, insertable=true, updatable=true)
	private String tabsConfiguration;
	@Column(name="customtabs", nullable=true, unique=false, insertable=true, updatable=true)
	private String customTabs;
	
	@Column(name="homepage", nullable=true, unique=false, insertable=true, updatable=true)
	private String homepage;
	@Column(name="jobads", nullable=true, unique=false, insertable=true, updatable=true)
	private String jobAds;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="ratingdeadline", nullable=true, insertable=true, updatable=true)
	private Date ratingDeadline;
	@Temporal(TemporalType.DATE)
	@Column(name="applicationdeadline", nullable=true, insertable=true, updatable=true)
	private Date applicationDeadline;
	@Temporal(TemporalType.DATE)
	@Column(name="committeereminder", nullable=true, insertable=true, updatable=true)
	private Date committeeReminderDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="committeeremindersent", nullable=true, insertable=true, updatable=true)
	private Date committeeReminderSentDate;
	@Column(name="committee_mail_subject", nullable=true, insertable=true, updatable=true)
	private String committeeReminderMailSubject;
	@Column(name="committee_mail_template", nullable=true, insertable=true, updatable=true)
	private String committeeReminderMailTemplate;
	@Column(name="committee_mail_letter", nullable=true, insertable=true, updatable=true)
	private String committeeReminderMailLetter;

	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_committeegroup", nullable=false, insertable=true, updatable=true)
	private SecurityGroup committeeGroup;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_committeeheadgroup", nullable=true, insertable=true, updatable=true)
	private SecurityGroup committeeHeadGroup;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_secretarygroup", nullable=true, insertable=true, updatable=true)
	private SecurityGroup secretaryGroup;
	@ManyToOne(targetEntity=SecurityGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_exofficiogroup", nullable=true, insertable=true, updatable=true)
	private SecurityGroup exOfficioGroup;

	@Column(name="status", nullable=true, insertable=true, updatable=true)
	private String status;
	@Column(name="is_valid", nullable=true, insertable=true, updatable=true)
	private boolean valid = true;

	@Column(name="availabledocs", nullable=true, insertable=true, updatable=true)
	private String availableDocs;
	@Column(name="mandatorydocs", nullable=true, insertable=true, updatable=true)
	private String mandatoryDocs;
	@Column(name="staffdocs", nullable=true, insertable=true, updatable=true)
	private String staffDocs;
	@Column(name="combineddocs", nullable=true, insertable=true, updatable=true)
	private String combinedDocs;
	@Column(name="docsizes", nullable=true, insertable=true, updatable=true)
	private String docSizes;
	@Column(name="docsnames", nullable=true, insertable=true, updatable=true)
	private String docNames;
	@Column(name="docsnamesde", nullable=true, insertable=true, updatable=true)
	private String docNamesDe;
	@Column(name="docsnamesfr", nullable=true, insertable=true, updatable=true)
	private String docNamesFr;
	@Column(name="docsexplain", nullable=true, insertable=true, updatable=true)
	private String docExplain;
	@Column(name="docsexplainde", nullable=true, insertable=true, updatable=true)
	private String docExplainDe;
	@Column(name="docsexplainfr", nullable=true, insertable=true, updatable=true)
	private String docExplainFr;
	
	@Column(name="docspdfs", nullable=true, insertable=true, updatable=true)
	private String docPdfs;
	@Column(name="docsdocx", nullable=true, insertable=true, updatable=true)
	private String docDocx;
	@Column(name="docsxlsx", nullable=true, insertable=true, updatable=true)
	private String docXlsx;
	@Column(name="docsjpg", nullable=true, insertable=true, updatable=true)
	private String docJpg;
	
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_doc_1_id", nullable=true, insertable=true, updatable=true)
	private Attachment document1;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_doc_2_id", nullable=true, insertable=true, updatable=true)
	private Attachment document2;
	@ManyToOne(targetEntity=AttachmentImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_doc_3_id", nullable=true, insertable=true, updatable=true)
	private Attachment document3;
	
	@Column(name="mailsetting", nullable=true, insertable=true, updatable=true)
	private String mailSettingString;
	@Column(name="mailsender", nullable=true, insertable=true, updatable=true)
	private String senderMail;
	@Column(name="mailbcc", nullable=true, insertable=true, updatable=true)
	private String bccMail;
	
	@Column(name="rec_excluded_attributes", nullable=true, insertable=true, updatable=true)
	private String excludedAttributesString;
	
	@Column(name="rec_expert_enable", nullable=true, insertable=true, updatable=true)
	private boolean expertRecommendationEnabled = false;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="rec_expert_deadline", nullable=true, insertable=true, updatable=true)
	private Date expertRecommandationDeadline;
	@Column(name="rec_expert_mail_subject", nullable=true, insertable=true, updatable=true)
	private String expertRecommandationMailSubject;
	@Column(name="rec_expert_mail_template", nullable=true, insertable=true, updatable=true)
	private String expertRecommandationMailTemplate;
	@Column(name="rec_expert_mail_letter", nullable=true, insertable=true, updatable=true)
	private String expertRecommandationMailLetter;
	@Column(name="rec_expert_docs", nullable=true, insertable=true, updatable=true)
	private String expertRecommendationDocs;
	@Column(name="rec_expert_fields", nullable=true, insertable=true, updatable=true)
	private String expertRecommendationFieldsString;
	
	@Column(name="confsub_expert_mail_subject", nullable=true, insertable=true, updatable=true)
	private String expertConfirmationSubmissionMailSubject;
	@Column(name="confsub_expert_mail_template", nullable=true, insertable=true, updatable=true)
	private String expertConfirmationSubmissionMailTemplate;

	@Column(name="rec_referee_enable", nullable=true, insertable=true, updatable=true)
	private boolean refereeRecommendationEnabled = false;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="rec_referee_deadline", nullable=true, insertable=true, updatable=true)
	protected Date refereeRecommandationDeadline;
	@Column(name="rec_referee_docs", nullable=true, insertable=true, updatable=true)
	private String refereeRecommendationDocs;
	@Column(name="rec_referee_fields", nullable=true, insertable=true, updatable=true)
	private String refereeRecommendationFieldsString;
	
	@Column(name="rec_app_referee_mgt_enable", nullable=true, insertable=true, updatable=true)
	private boolean applicantRefereeManagementEnabled = false;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="rec_app_referee_mgt_deadline", nullable=true, insertable=true, updatable=true)
	private Date applicantRefereeManagementDeadline;
	
	@Column(name="rec_referee_min", nullable=true, insertable=true, updatable=true)
	private Long minReferees;
	@Column(name="rec_referee_max", nullable=true, insertable=true, updatable=true)
	private Long maxReferees;
	@Column(name="rec_referee_mail_subject", nullable=true, insertable=true, updatable=true)
	private String refereeRecommandationMailSubject;
	@Column(name="rec_referee_mail_template", nullable=true, insertable=true, updatable=true)
	private String refereeRecommandationMailTemplate;
	@Column(name="rec_referee_mail_letter", nullable=true, insertable=true, updatable=true)
	private String refereeRecommandationMailLetter;
	@Column(name="rec_referee_mail_type", nullable=true, insertable=true, updatable=true)
	private String recommandationSendMailType;
	
	@Column(name="confsub_referee_mail_subject", nullable=true, insertable=true, updatable=true)
	private String refereeConfirmationSubmissionMailSubject;
	@Column(name="confsub_referee_mail_template", nullable=true, insertable=true, updatable=true)
	private String refereeConfirmationSubmissionMailTemplate;
	
	@Column(name="rec_comp_expert_enable", nullable=true, insertable=true, updatable=true)
	private boolean comparativeAssessmentExpertEnabled = false;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="rec_comp_expert_deadline", nullable=true, insertable=true, updatable=true)
	private Date comparativeAssessmentExpertDeadline;
	@Column(name="rec_comp_expert_mail_subject", nullable=true, insertable=true, updatable=true)
	private String comparativeAssessmentExpertMailSubject;
	@Column(name="rec_comp_expert_mail_template", nullable=true, insertable=true, updatable=true)
	private String comparativeAssessmentExpertMailTemplate;
	@Column(name="rec_comp_expert_docs", nullable=true, insertable=true, updatable=true)
	private String comparativeAssessmentExpertDocs;
	@Column(name="rec_comp_expert_mail_letter", nullable=true, insertable=true, updatable=true)
	private String comparativeAssessmentExpertMailLetter;
	@Column(name="rec_comp_expert_fields", nullable=true, insertable=true, updatable=true)
	private String comparativeAssessmentExpertFieldsString;
	
	@Column(name="confs_comp_expert_mail_subject", nullable=true, insertable=true, updatable=true)
	private String comparativeAssessmentExpertConfirmationSubmissionMailSubject;
	@Column(name="confs_comp_expert_mail_template", nullable=true, insertable=true, updatable=true)
	private String comparativeAssessmentExpertConfirmationSubmissionMailTemplate;
	
	@Column(name="rec_public_feedback_enable", nullable=true, insertable=true, updatable=true)
	private boolean publicFeedbackEnabled = false;
	@Column(name="rec_public_feedback_deadline", nullable=true, insertable=true, updatable=true)
	private Date publicFeedbackDeadline;
	
	@Column(name="conf_mail_subject", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationMailSubject;
	@Column(name="conf_mail_template", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationMailTemplate;
	@Column(name="conf_mail_subject_de", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationMailSubjectDe;
	@Column(name="conf_mail_template_de", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationMailTemplateDe;
	@Column(name="conf_mail_subject_fr", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationMailSubjectFr;
	@Column(name="conf_mail_template_fr", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationMailTemplateFr;
	
	@Column(name="conf_mail_letter", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationMailLetter;
	
	@Column(name="conf_mail_subject_mgmt", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationWithRefereeManagementMailSubject;
	@Column(name="conf_mail_template_mgmt", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationWithRefereeManagementMailTemplate;
	@Column(name="conf_mail_subject_mgmt_de", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationWithRefereeManagementMailSubjectDe;
	@Column(name="conf_mail_template_mgmt_de", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationWithRefereeManagementMailTemplateDe;
	@Column(name="conf_mail_subject_mgmt_fr", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationWithRefereeManagementMailSubjectFr;
	@Column(name="conf_mail_template_mgmt_fr", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationWithRefereeManagementMailTemplateFr;
	
	@Column(name="conf_mail_letter_mgmt", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationWithRefereeManagementMailLetter;
	
	@Column(name="confdup_mail_subject", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationDuplicateMailSubject;
	@Column(name="confdup_mail_template", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationDuplicateMailTemplate;
	@Column(name="confdup_mail_subject_de", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationDuplicateMailSubjectDe;
	@Column(name="confdup_mail_template_de", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationDuplicateMailTemplateDe;
	@Column(name="confdup_mail_subject_fr", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationDuplicateMailSubjectFr;
	@Column(name="confdup_mail_template_fr", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationDuplicateMailTemplateFr;
	@Column(name="confdup_mail_letter", nullable=true, insertable=true, updatable=true)
	private String applicationConfirmationDuplicateMailLetter;
	
	@Column(name="committeecomment_enable", nullable=true, insertable=true, updatable=true)
	private boolean committeeCommentEnabled = false;
	@Column(name="committeecomment_visibility", nullable=true, insertable=true, updatable=true)
	private String committeeCommentVisiblityString;
	
	@Column(name="review_enable", nullable=true, insertable=true, updatable=true)
	private boolean reviewEnabled = false;
	@Column(name="system_tags_enable", nullable=true, insertable=true, updatable=true)
	private boolean systemTagsEnabled = false;
	@Column(name="position_tags_enable", nullable=true, insertable=true, updatable=true)
	private boolean positionTagsEnabled = false;
	
	@Column(name="position_duplicate_app_allowed", nullable=true, insertable=true, updatable=true)
	private String duplicateApplicationAllowed;
	
	@ManyToOne(targetEntity=PositionReviewDefinitionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_review_definition_id", nullable=true, insertable=true, updatable=true)
	private PositionReviewDefinition reviewDefinition;
	
	@ManyToOne(targetEntity=OrganisationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_organisation_id", nullable=true, insertable=true, updatable=true)
	private Organisation organisation;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "label", column = @Column(name = "rating_policy_link_label_1")),
		@AttributeOverride(name = "url", column = @Column(name = "rating_policy_link_url_1"))
	})
	private PolicyLink policyLink1;
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "label", column = @Column(name = "rating_policy_link_label_2")),
		@AttributeOverride(name = "url", column = @Column(name = "rating_policy_link_url_2"))
	})
	private PolicyLink policyLink2;
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "label", column = @Column(name = "rating_policy_link_label_3")),
		@AttributeOverride(name = "url", column = @Column(name = "rating_policy_link_url_3"))
	})
	private PolicyLink policyLink3;
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "label", column = @Column(name = "rating_policy_link_label_4")),
		@AttributeOverride(name = "url", column = @Column(name = "rating_policy_link_url_4"))
	})
	private PolicyLink policyLink4;
	
	@OneToMany(targetEntity=PositionAttributeDefinitionImpl.class, fetch=FetchType.EAGER, mappedBy="position",
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<PositionAttributeDefinition> attributesDefinitions;
	
	@OneToMany(targetEntity=ApplicationAttributeImpl.class, mappedBy="position",
			cascade= { CascadeType.ALL })
	private Set<ApplicationAttribute> attributes;	

	public PositionImpl() {
		//
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String getResourceableTypeName() {
		return "RecruitingPositionImpl";
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public SecurityGroup getCommitteeGroup() {
		return committeeGroup;
	}

	public void setCommitteeGroup(SecurityGroup committeeGroup) {
		this.committeeGroup = committeeGroup;
	}

	@Override
	public SecurityGroup getCommitteeHeadGroup() {
		return committeeHeadGroup;
	}

	@Override
	public void setCommitteeHeadGroup(SecurityGroup committeeHeadGroup) {
		this.committeeHeadGroup = committeeHeadGroup;
	}

	@Override
	public SecurityGroup getSecretaryGroup() {
		return secretaryGroup;
	}

	@Override
	public void setSecretaryGroup(SecurityGroup secretaryGroup) {
		this.secretaryGroup = secretaryGroup;
	}

	@Override
	public SecurityGroup getExOfficioGroup() {
		return exOfficioGroup;
	}

	@Override
	public void setExOfficioGroup(SecurityGroup exOfficioGroup) {
		this.exOfficioGroup = exOfficioGroup;
	}

	@Override
	public String getPlaningsNumber() {
		return planingsNumber;
	}

	@Override
	public void setPlaningsNumber(String identifier) {
		this.planingsNumber = identifier;
	}

	@Override
	public String getAvailableLanguages() {
		return availableLanguages;
	}

	@Override
	public void setAvailableLanguages(String availableLanguages) {
		this.availableLanguages = availableLanguages;
	}

	@Override
	public String getPositionTitle() {
		return positionTitle;
	}

	@Override
	public void setPositionTitle(String positionTitle) {
		this.positionTitle = positionTitle;
	}

	@Override
	public String getPositionTitleDe() {
		return positionTitleDe;
	}

	@Override
	public void setPositionTitleDe(String positionTitleDe) {
		this.positionTitleDe = positionTitleDe;
	}

	@Override
	public String getPositionTitleFr() {
		return positionTitleFr;
	}

	@Override
	public void setPositionTitleFr(String positionTitleFr) {
		this.positionTitleFr = positionTitleFr;
	}

	@Override
	public String getPositionTitle(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getPositionTitleDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getPositionTitleFr();
		}
		return getPositionTitle();
	}

	@Override
	public void setPositionTitle(String title, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setPositionTitleDe(title);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setPositionTitleFr(title);
		}  else {
			setPositionTitle(title);
		}
	}
	
	@Override
	public String getMLTitle(Locale locale) {
		return PositionMLHelper.getPositionMLTitle(this, locale);
	}

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	@Override
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@Override
	public String getShortTitleDe() {
		return shortTitleDe;
	}

	@Override
	public void setShortTitleDe(String shortTitleDe) {
		this.shortTitleDe = shortTitleDe;
	}
	
	@Override
	public String getShortTitleFr() {
		return shortTitleFr;
	}

	@Override
	public void setShortTitleFr(String title) {
		this.shortTitleFr = title;
	}

	@Override
	public String getShortTitle(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getShortTitleDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getShortTitleFr();
		}
		return getShortTitle();
	}

	@Override
	public void setShortTitle(String shortTitle, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setShortTitleDe(shortTitle);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setShortTitleFr(shortTitle);
		} else {
			setShortTitle(shortTitle);
		}
	}

	@Override
	public String getMLShortTitle(Locale locale) {
		return PositionMLHelper.getShortMLTitle(this, locale);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescriptionDe() {
		return descriptionDe;
	}

	@Override
	public void setDescriptionDe(String descriptionDe) {
		this.descriptionDe = descriptionDe;
	}

	@Override
	public String getDescriptionFr() {
		return descriptionFr;
	}

	@Override
	public void setDescriptionFr(String descriptionFr) {
		this.descriptionFr = descriptionFr;
	}

	@Override
	public String getDescription(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getDescriptionDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getDescriptionFr();
		}
		return getDescription();
	}

	@Override
	public void setDescription(String description, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setDescriptionDe(description);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setDescriptionFr(description);
		} else {
			setDescription(description);
		}
	}
	
	@Override @Transient
	public String getMLDescription(Locale locale) {
		return PositionMLHelper.getPositionMLDescription(this, locale);
	}

	@Override
	public String getMessageToCommitte() {
		return messageToCommitte;
	}

	@Override
	public void setMessageToCommitte(String messageToCommitte) {
		this.messageToCommitte = messageToCommitte;
	}
	
	@Override
	public boolean isAdvertised() {
		return advertised;
	}

	@Override
	public void setAdvertised(boolean val) {
		this.advertised = val;
	}

	@Override
	public String getTabsConfiguration() {
		return tabsConfiguration;
	}

	@Override
	public void setTabsConfiguration(String tabsConfiguration) {
		this.tabsConfiguration = tabsConfiguration;
	}

	@Override
	public TabConfiguration getTabConfiguration(Tab tab) {
		TabsConfiguration config = null;
		if(StringHelper.containsNonWhitespace(tabsConfiguration)) {
			 config = TabsConfigurationXStream.fromXml(tabsConfiguration);
		}
		if(config == null) {
			config = new TabsConfiguration();
			setTabsConfiguration(TabsConfigurationXStream.toXml(config));
		}
		TabConfiguration tabConfig = config.getConfiguration(tab);
		if(tabConfig == null) {
			tabConfig = new TabConfiguration(tab);
			config.setConfiguration(tab, tabConfig);
			setTabsConfiguration(TabsConfigurationXStream.toXml(config));
		}
		return tabConfig;
	}

	@Override
	public void setTabConfiguration(Tab tab, TabConfiguration tabConfiguration) {
		TabsConfiguration config;
		if(StringHelper.containsNonWhitespace(tabsConfiguration)) {
			config = TabsConfigurationXStream.fromXml(tabsConfiguration);
		} else {
			config = new TabsConfiguration();
		}
		config.setConfiguration(tab, tabConfiguration);
		setTabsConfiguration(TabsConfigurationXStream.toXml(config));
	}
	
	public String getCustomTabs() {
		return customTabs;
	}

	public void setCustomTabs(String customTabs) {
		this.customTabs = customTabs;
	}

	@Override
	@Transient
	public List<Tab> getCustomTabsList() {
		List<Tab> tabsList = new ArrayList<>(5);
		if(StringHelper.containsNonWhitespace(customTabs)) {
			String[] tabs = customTabs.split(",");
			for(String tab:tabs) {
				if(StringHelper.containsNonWhitespace(tab)) {
					tabsList.add(Tab.valueOf(tab));
				}
			}
		}
		return tabsList;
	}

	@Override
	@Transient
	public void setCustomTabsList(List<Tab> tabs) {
		if(tabs == null || tabs.isEmpty()) {
			customTabs = null;
		} else {
			customTabs = tabs.stream()
					.map(Tab::name)
					.collect(Collectors.joining(","));
		}
	}

	@Override
	public List<Tab> getCustomEnabledTabsList() {
		return getCustomTabsList().stream().filter(tab -> {
			return !getTabConfiguration(tab).isDisabled();
		}).collect(Collectors.toList());
	}

	@Override
	public String getDepartment() {
		return department;
	}

	@Override
	public void setDepartment(String department) {
		this.department = department;
	}

	@Override
	public String getDepartmentDe() {
		return departmentDe;
	}

	@Override
	public void setDepartmentDe(String departmentDe) {
		this.departmentDe = departmentDe;
	}

	@Override
	public String getDepartmentFr() {
		return departmentFr;
	}

	@Override
	public void setDepartmentFr(String departmentFr) {
		this.departmentFr = departmentFr;
	}

	@Override @Transient
	public String getMLDepartement(Locale locale) {
		return PositionMLHelper.getPositionMLDepartment(this, locale);
	}
	
	@Override
	public String getDepartment(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getDepartmentDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getDepartmentFr();
		}
		return getDepartment();
	}

	@Override
	public void setDepartment(String department, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setDepartmentDe(department);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setDepartmentFr(department);
		} else {
			setDepartment(department);
		}
	}

	@Override
	public String getHomepage() {
		return homepage;
	}

	@Override
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	@Override
	public Date getRatingDeadline() {
		return ratingDeadline;
	}

	@Override
	public void setRatingDeadline(Date ratingDeadline) {
		this.ratingDeadline = ratingDeadline;
	}

	@Override
	public Date getApplicationDeadline() {
		return applicationDeadline;
	}

	@Override
	public void setApplicationDeadline(Date applicationDeadline) {
		this.applicationDeadline = applicationDeadline;
	}
	
	@Override
	public String getApplicationConfirmationMailSubject(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getApplicationConfirmationMailSubjectDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getApplicationConfirmationMailSubjectFr();
		}
		return getApplicationConfirmationMailSubject();
	}

	@Override
	public void setApplicationConfirmationMailSubject(String template, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setApplicationConfirmationMailSubjectDe(template);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setApplicationConfirmationMailSubjectFr(template);
		} else {
			setApplicationConfirmationMailSubject(template);
		}
	}
	
	@Override
	public String getApplicationConfirmationMailTemplate(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getApplicationConfirmationMailTemplateDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getApplicationConfirmationMailTemplateFr();
		}
		return getApplicationConfirmationMailTemplate();
	}

	@Override
	public void setApplicationConfirmationMailTemplate(String template, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setApplicationConfirmationMailTemplateDe(template);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setApplicationConfirmationMailTemplateFr(template);
		} else {
			setApplicationConfirmationMailTemplate(template);
		}
	}

	@Override
	public String getApplicationConfirmationMailSubject() {
		return applicationConfirmationMailSubject;
	}

	@Override
	public void setApplicationConfirmationMailSubject(String subject) {
		this.applicationConfirmationMailSubject = subject;
	}

	@Override
	public String getApplicationConfirmationMailTemplate() {
		return applicationConfirmationMailTemplate;
	}

	@Override
	public void setApplicationConfirmationMailTemplate(String template) {
		this.applicationConfirmationMailTemplate = template;
	}

	@Override
	public String getApplicationConfirmationMailSubjectDe() {
		return applicationConfirmationMailSubjectDe;
	}

	@Override
	public void setApplicationConfirmationMailSubjectDe(String subject) {
		this.applicationConfirmationMailSubjectDe = subject;
	}

	@Override
	public String getApplicationConfirmationMailTemplateDe() {
		return applicationConfirmationMailTemplateDe;
	}

	@Override
	public void setApplicationConfirmationMailTemplateDe(String template) {
		this.applicationConfirmationMailTemplateDe = template;
	}

	@Override
	public String getApplicationConfirmationMailSubjectFr() {
		return applicationConfirmationMailSubjectFr;
	}

	@Override
	public void setApplicationConfirmationMailSubjectFr(String subject) {
		this.applicationConfirmationMailSubjectFr = subject;
	}

	@Override
	public String getApplicationConfirmationMailTemplateFr() {
		return applicationConfirmationMailTemplateFr;
	}

	@Override
	public void setApplicationConfirmationMailTemplateFr(String template) {
		this.applicationConfirmationMailTemplateFr = template;
	}

	@Override
	public String getApplicationConfirmationMailLetter() {
		return applicationConfirmationMailLetter;
	}

	@Override
	public void setApplicationConfirmationMailLetter(String confirmation) {
		this.applicationConfirmationMailLetter = confirmation;
	}

	@Override
	public String getApplicationConfirmationWithRefereeManagementMailSubject() {
		return applicationConfirmationWithRefereeManagementMailSubject;
	}

	@Override
	public void setApplicationConfirmationWithRefereeManagementMailSubject(String subject) {
		this.applicationConfirmationWithRefereeManagementMailSubject = subject;
	}

	@Override
	public String getApplicationConfirmationWithRefereeManagementMailTemplate() {
		return applicationConfirmationWithRefereeManagementMailTemplate;
	}
	
	@Override
	public void setApplicationConfirmationWithRefereeManagementMailTemplate(String template) {
		applicationConfirmationWithRefereeManagementMailTemplate = template;
	}
	
	@Override
	public String getApplicationConfirmationWithRefereeManagementMailSubjectDe() {
		return applicationConfirmationWithRefereeManagementMailSubjectDe;
	}

	@Override
	public void setApplicationConfirmationWithRefereeManagementMailSubjectDe(String subject) {
		this.applicationConfirmationWithRefereeManagementMailSubjectDe = subject;
	}

	@Override
	public String getApplicationConfirmationWithRefereeManagementMailTemplateDe() {
		return applicationConfirmationWithRefereeManagementMailTemplateDe;
	}

	@Override
	public void setApplicationConfirmationWithRefereeManagementMailTemplateDe(String template) {
		applicationConfirmationWithRefereeManagementMailTemplateDe = template;
	}
	
	@Override
	public String getApplicationConfirmationWithRefereeManagementMailSubjectFr() {
		return applicationConfirmationWithRefereeManagementMailSubjectFr;
	}

	@Override
	public void setApplicationConfirmationWithRefereeManagementMailSubjectFr(String subject) {
		this.applicationConfirmationWithRefereeManagementMailSubjectFr = subject;
	}

	@Override
	public String getApplicationConfirmationWithRefereeManagementMailTemplateFr() {
		return applicationConfirmationWithRefereeManagementMailTemplateFr;
	}

	@Override
	public void setApplicationConfirmationWithRefereeManagementMailTemplateFr(String template) {
		this.applicationConfirmationWithRefereeManagementMailTemplateFr = template;
	}
	
	@Override
	public String getApplicationConfirmationWithRefereeManagementMailSubject(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getApplicationConfirmationWithRefereeManagementMailSubjectDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getApplicationConfirmationWithRefereeManagementMailSubjectFr();
		}
		return getApplicationConfirmationWithRefereeManagementMailSubject();
	}
	
	@Override
	public void setApplicationConfirmationWithRefereeManagementMailSubject(String template, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setApplicationConfirmationWithRefereeManagementMailSubjectDe(template);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setApplicationConfirmationWithRefereeManagementMailSubjectFr(template);
		} else {
			setApplicationConfirmationWithRefereeManagementMailSubject(template);
		}
	}

	@Override
	public String getApplicationConfirmationWithRefereeManagementMailTemplate(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getApplicationConfirmationWithRefereeManagementMailTemplateDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getApplicationConfirmationWithRefereeManagementMailTemplateFr();
		}
		return getApplicationConfirmationWithRefereeManagementMailTemplate();
	}
	
	@Override
	public void setApplicationConfirmationWithRefereeManagementMailTemplate(String template, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setApplicationConfirmationWithRefereeManagementMailTemplateDe(template);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setApplicationConfirmationWithRefereeManagementMailTemplateFr(template);
		} else {
			setApplicationConfirmationWithRefereeManagementMailTemplate(template);
		}
	}

	@Override
	public String getApplicationConfirmationWithRefereeManagementMailLetter() {
		return applicationConfirmationWithRefereeManagementMailLetter;
	}

	@Override
	public void setApplicationConfirmationWithRefereeManagementMailLetter(String letter) {
		applicationConfirmationWithRefereeManagementMailLetter = letter;
	}
	
	@Override
	public String getApplicationConfirmationDuplicateMailSubject(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getApplicationConfirmationDuplicateMailSubjectDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getApplicationConfirmationDuplicateMailSubjectFr();
		}
		return getApplicationConfirmationDuplicateMailSubject();
	}

	@Override
	public void setApplicationConfirmationDuplicateMailSubject(String subject, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setApplicationConfirmationDuplicateMailSubjectDe(subject);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setApplicationConfirmationDuplicateMailSubjectFr(subject);
		} else {
			setApplicationConfirmationDuplicateMailSubject(subject);
		}
	}

	@Override
	public String getApplicationConfirmationDuplicateMailTemplate(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getApplicationConfirmationDuplicateMailTemplateDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getApplicationConfirmationDuplicateMailTemplateFr();
		}
		return getApplicationConfirmationDuplicateMailTemplate();
	}

	@Override
	public void setApplicationConfirmationDuplicateMailTemplate(String template, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setApplicationConfirmationDuplicateMailTemplateDe(template);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setApplicationConfirmationDuplicateMailTemplateFr(template);
		} else {
			setApplicationConfirmationDuplicateMailTemplate(template);
		}
	}

	@Override
	public String getApplicationConfirmationDuplicateMailSubject() {
		return applicationConfirmationDuplicateMailSubject;
	}

	@Override
	public void setApplicationConfirmationDuplicateMailSubject(String subject) {
		this.applicationConfirmationDuplicateMailSubject = subject;
	}

	@Override
	public String getApplicationConfirmationDuplicateMailTemplate() {
		return applicationConfirmationDuplicateMailTemplate;
	}

	@Override
	public void setApplicationConfirmationDuplicateMailTemplate(String template) {
		this.applicationConfirmationDuplicateMailTemplate = template;
	}

	@Override
	public String getApplicationConfirmationDuplicateMailSubjectDe() {
		return applicationConfirmationDuplicateMailSubjectDe;
	}

	@Override
	public void setApplicationConfirmationDuplicateMailSubjectDe(String subject) {
		this.applicationConfirmationDuplicateMailSubjectDe = subject;
	}

	@Override
	public String getApplicationConfirmationDuplicateMailTemplateDe() {
		return applicationConfirmationDuplicateMailTemplateDe;
	}

	@Override
	public void setApplicationConfirmationDuplicateMailTemplateDe(String template) {
		this.applicationConfirmationDuplicateMailTemplateDe = template;
	}
	
	@Override
	public String getApplicationConfirmationDuplicateMailSubjectFr() {
		return applicationConfirmationDuplicateMailSubjectFr;
	}

	@Override
	public void setApplicationConfirmationDuplicateMailSubjectFr(String subject) {
		this.applicationConfirmationDuplicateMailSubjectFr = subject;
	}

	@Override
	public String getApplicationConfirmationDuplicateMailTemplateFr() {
		return applicationConfirmationDuplicateMailTemplateFr;
	}

	@Override
	public void setApplicationConfirmationDuplicateMailTemplateFr(String template) {
		this.applicationConfirmationDuplicateMailTemplateFr = template;
	}

	@Override
	public String getApplicationConfirmationDuplicateMailLetter() {
		return applicationConfirmationDuplicateMailLetter;
	}

	@Override
	public void setApplicationConfirmationDuplicateMailLetter(String configuration) {
		this.applicationConfirmationDuplicateMailLetter = configuration;
	}

	@Override
	public Date getCommitteeReminderDate() {
		return committeeReminderDate;
	}

	@Override
	public void setCommitteeReminderDate(Date date) {
		committeeReminderDate = date;
	}

	@Override
	public Date getCommitteeReminderSentDate() {
		return committeeReminderSentDate;
	}

	@Override
	public void setCommitteeReminderSentDate(Date committeeReminderSentDate) {
		this.committeeReminderSentDate = committeeReminderSentDate;
	}

	@Override
	public String getCommitteeReminderMailSubject() {
		return committeeReminderMailSubject;
	}

	@Override
	public void setCommitteeReminderMailSubject(String committeeReminderMailSubject) {
		this.committeeReminderMailSubject = committeeReminderMailSubject;
	}

	@Override
	public String getCommitteeReminderMailTemplate() {
		return committeeReminderMailTemplate;
	}

	@Override
	public void setCommitteeReminderMailTemplate(String committeeReminderMailTemplate) {
		this.committeeReminderMailTemplate = committeeReminderMailTemplate;
	}
	
	@Override
	public String getCommitteeReminderMailLetter() {
		return committeeReminderMailLetter;
	}

	@Override
	public void setCommitteeReminderMailLetter(String configuration) {
		this.committeeReminderMailLetter = configuration;
	}

	@Override
	public String getProfessorship() {
		return professorship;
	}

	@Override
	public void setProfessorship(String professorship) {
		this.professorship = professorship;
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getJobAds() {
		return jobAds;
	}

	@Override
	public void setJobAds(String jobAds) {
		this.jobAds = jobAds;
	}

	@Override
	public boolean isDecisionTool() {
		return decisionTool;
	}

	@Override
	public void setDecisionTool(boolean decisionTool) {
		this.decisionTool = decisionTool;
	}

	@Override
	public boolean isApplicationProject() {
		return applicationProject;
	}

	@Override
	public void setApplicationProject(boolean applicationProject) {
		this.applicationProject = applicationProject;
	}

	@Override
	public boolean isApplicationAcademicalBackground() {
		return applicationAcademicalBackground;
	}

	@Override
	public void setApplicationAcademicalBackground(boolean enable) {
		this.applicationAcademicalBackground = enable;
	}

	@Override
	public MailSettingEnum getMailSetting() {
		MailSettingEnum setting = null;
		if(StringHelper.containsNonWhitespace(mailSettingString)) {
			setting = MailSettingEnum.valueOf(mailSettingString);
		}
		return setting;
	}
	
	@Override
	public void setMailSetting(MailSettingEnum setting) {
		if(setting == null) {
			mailSettingString = null;
		} else {
			mailSettingString = setting.name();
		}
	}

	public String getMailSettingString() {
		return mailSettingString;
	}

	public void setMailSettingString(String mailSettingString) {
		this.mailSettingString = mailSettingString;
	}

	@Override
	public String getSenderMail() {
		return senderMail;
	}

	@Override
	public void setSenderMail(String senderMail) {
		this.senderMail = senderMail;
	}

	@Override
	public String getBccMail() {
		return bccMail;
	}

	@Override
	public void setBccMail(String bccMail) {
		this.bccMail = bccMail;
	}
	
	public String getExcludedAttributesString() {
		return excludedAttributesString;
	}

	public void setExcludedAttributesString(String excludedAttributesString) {
		this.excludedAttributesString = excludedAttributesString;
	}

	@Override
	public List<String> getExcludedAttributesList() {
		if(StringHelper.containsNonWhitespace(excludedAttributesString)) {
			String[] fieldsArr = excludedAttributesString.split(",");
			return List.of(fieldsArr);
		}
		return List.of();
	}
	
	@Override
	public void setExcludedAttributesList(List<String> fields) {
		if(fields == null || fields.isEmpty()) {
			setExcludedAttributesString(null);
		} else {
			String val = String.join(",", fields);
			setExcludedAttributesString(val);
		}	
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getAvailableDocs() {
		return availableDocs;
	}

	public void setAvailableDocs(String availableDocs) {
		this.availableDocs = availableDocs;
	}

	public String getMandatoryDocs() {
		return mandatoryDocs;
	}

	public void setMandatoryDocs(String mandatoryDocs) {
		this.mandatoryDocs = mandatoryDocs;
	}

	@Override
	public Set<String> getAvailableDocuments() {
		return DocumentEnum.documentStringToSet(availableDocs);
	}

	@Override
	public void setAvailableDocuments(Collection<String> docs) {
		setAvailableDocs(documentToString(docs));
	}

	@Override
	public Set<String> getMandatoryDocuments() {
		return DocumentEnum.documentStringToSet(mandatoryDocs);
	}
	
	@Override
	public void setMandatoryDocuments(Collection<String> docs) {
		setMandatoryDocs(documentToString(docs));
	}
	
	@Override
	@Transient
	public Map<DocumentEnum, List<DocumentType>> getDocumentTypes() {
		Map<DocumentEnum, List<DocumentType>> types = new EnumMap<>(DocumentEnum.class);
		appendDocumentType(getPdfDocuments(), DocumentType.pdf, types);
		appendDocumentType(getXlsxDocuments(), DocumentType.xlsx, types);
		appendDocumentType(getDocxDocuments(), DocumentType.docx, types);
		appendDocumentType(getJpgDocuments(), DocumentType.jpg, types);
		return types;
	}
	
	private void appendDocumentType(Set<DocumentEnum> documents, DocumentType type, Map<DocumentEnum, List<DocumentType>> map) {
		for(DocumentEnum document:documents) {
			map.computeIfAbsent(document, d -> new ArrayList<>(3))
				.add(type);
		}
	}

	@Override
	public Set<DocumentEnum> getPdfDocuments() {
		if("all".equals(docPdfs)) {
			return DocumentEnum.documentStringToEnumSet(availableDocs);
		}
		return DocumentEnum.documentStringToEnumSet(docPdfs);
	}

	@Override
	public void setPdfDocuments(Collection<DocumentEnum> pdfs) {
		docPdfs = documentEnumToString(pdfs);
	}

	@Override
	public Set<DocumentEnum> getXlsxDocuments() {
		return DocumentEnum.documentStringToEnumSet(docXlsx);
	}

	@Override
	public void setXlsxDocuments(Collection<DocumentEnum> xlsx) {
		docXlsx = documentEnumToString(xlsx);
	}

	@Override
	public Set<DocumentEnum> getDocxDocuments() {
		return DocumentEnum.documentStringToEnumSet(docDocx);
	}

	@Override
	public void setDocxDocuments(Collection<DocumentEnum> docx) {
		docDocx = documentEnumToString(docx);
	}

	@Override
	public Set<DocumentEnum> getJpgDocuments() {
		return DocumentEnum.documentStringToEnumSet(docJpg);
	}

	@Override
	public void setJpgDocuments(Collection<DocumentEnum> jpg) {
		docJpg = documentEnumToString(jpg);
	}

	public String getDocPdfs() {
		return docPdfs;
	}

	public void setDocPdfs(String docPdfs) {
		this.docPdfs = docPdfs;
	}

	public String getDocDocx() {
		return docDocx;
	}

	public void setDocDocx(String docDocx) {
		this.docDocx = docDocx;
	}

	public String getDocXlsx() {
		return docXlsx;
	}

	public void setDocXlsx(String docXlsx) {
		this.docXlsx = docXlsx;
	}

	public String getStaffDocs() {
		return staffDocs;
	}

	public void setStaffDocs(String staffDocs) {
		this.staffDocs = staffDocs;
	}

	@Override
	public Set<String> getStaffDocuments() {
		return DocumentEnum.documentStringToSet(staffDocs);
	}

	@Override
	public void setStaffDocuments(Collection<String> docs) {
		setStaffDocs(documentToString(docs));
	}

	public String getCombinedDocs() {
		return combinedDocs;
	}

	public void setCombinedDocs(String combinedDocs) {
		this.combinedDocs = combinedDocs;
	}

	@Override
	public Set<DocumentEnum> getDocumentsInCombinedFile() {
		return DocumentEnum.documentStringToEnumSet(combinedDocs);
	}

	@Override
	public void setDocumentsInCombinedFile(Collection<DocumentEnum> docs) {
		setCombinedDocs(documentEnumToString(docs));
	}
	
	public String getDocNames() {
		return docNames;
	}

	public void setDocNames(String docNames) {
		this.docNames = docNames;
	}

	public String getDocNamesDe() {
		return docNamesDe;
	}

	public void setDocNamesDe(String docNamesDe) {
		this.docNamesDe = docNamesDe;
	}
	
	public String getDocNamesFr() {
		return docNamesFr;
	}

	public void setDocNamesFr(String names) {
		this.docNamesFr = names;
	}

	public String getDocExplain() {
		return docExplain;
	}

	public void setDocExplain(String docExplain) {
		this.docExplain = docExplain;
	}

	public String getDocExplainDe() {
		return docExplainDe;
	}

	public void setDocExplainDe(String docExplainDe) {
		this.docExplainDe = docExplainDe;
	}
	
	public String getDocExplainFr() {
		return docExplainFr;
	}

	public void setDocExplainFr(String explain) {
		this.docExplainFr = explain;
	}

	@Override
	@Transient
	public String getDocumentName(DocumentEnum doc, Locale locale) {
		Map<DocumentEnum,String> names;
		
		String[] lang = getAvailableLanguagesArray();
		if((lang.length == 1 && "de".equals(lang[0]))
				|| (locale != null && locale.getLanguage().equals("de"))) {
			names = getDocumentNamesDe();
		} else if((lang.length == 1 && "fr".equals(lang[0]))
				|| (locale != null && locale.getLanguage().equals("fr"))) {
			names = getDocumentNamesFr();
		} else {
			names = getDocumentNames();
		}
		return names != null ? names.get(doc) : null;
	}

	@Override
	public void setDocumentName(DocumentEnum doc, Locale locale, String name) {
		if(locale != null && locale.getLanguage().equals("de")) {
			Map<DocumentEnum,String> names = getDocumentNamesDe();
			names.put(doc, name);
			setDocumentNamesDe(names);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			Map<DocumentEnum,String> names = getDocumentNamesFr();
			names.put(doc, name);
			setDocumentNamesFr(names);
		} else {
			Map<DocumentEnum,String> names = getDocumentNames();
			names.put(doc, name);
			setDocumentNames(names);
		}
	}

	@Override
	@Transient
	public String getDocumentExplain(DocumentEnum doc, Locale locale) {
		Map<DocumentEnum,String> explain;
		if(locale != null && locale.getLanguage().equals("de")) {
			explain = getDocumentExplainDe();	
		} else {
			explain = getDocumentExplain();
		}
		return explain != null ? explain.get(doc) : null;
	}

	@Override
	public void setDocumentExplain(DocumentEnum doc, Locale locale, String explain) {
		if(locale != null && locale.getLanguage().equals("de")) {
			Map<DocumentEnum,String> explainMap = getDocumentExplainDe();
			explainMap.put(doc, explain);
			setDocumentExplainDe(explainMap);	
		} else {
			Map<DocumentEnum,String> explainMap = getDocumentExplain();
			explainMap.put(doc, explain);
			setDocumentExplain(explainMap);
		}
	}

	@Override
	@Transient
	public Map<DocumentEnum,String> getDocumentNames() {
		return toStringMap(getDocNames());
	}

	@Override
	public void setDocumentNames(Map<DocumentEnum, String> names) {
		setDocNames(toStringJSON(names));
	}
	
	@Override
	@Transient
	public Map<DocumentEnum,String> getDocumentNamesDe() {
		return toStringMap(getDocNamesDe());
	}

	@Override
	public void setDocumentNamesDe(Map<DocumentEnum, String> names) {
		setDocNamesDe(toStringJSON(names));
	}
	
	@Override
	@Transient
	public Map<DocumentEnum,String> getDocumentNamesFr() {
		return toStringMap(getDocNamesFr());
	}
	
	@Override
	public void setDocumentNamesFr(Map<DocumentEnum, String> names) {
		setDocNamesFr(toStringJSON(names));
	}
	
	@Override
	@Transient
	public Map<DocumentEnum,String> getDocumentExplain() {
		return toStringMap(getDocExplain());
	}

	@Override
	public void setDocumentExplain(Map<DocumentEnum, String> names) {
		setDocExplain(toStringJSON(names));
	}
	
	@Override
	@Transient
	public Map<DocumentEnum,String> getDocumentExplainDe() {
		return toStringMap(getDocExplainDe());
	}

	@Override
	public void setDocumentExplainDe(Map<DocumentEnum, String> names) {
		setDocExplainDe(toStringJSON(names));
	}
	
	@Override
	@Transient
	public Map<DocumentEnum,String> getDocumentExplainFr() {
		return toStringMap(getDocExplainFr());
	}
	
	@Override
	public void setDocumentExplainFr(Map<DocumentEnum, String> names) {
		setDocExplainFr(toStringJSON(names));
	}
	
	private  Map<DocumentEnum,String> toStringMap(String val) {
		Map<DocumentEnum,String> values = new EnumMap<>(DocumentEnum.class);
		try {
			if(val != null && val.length() > 5) {
				JSONObject object = new JSONObject(val);
				for(Iterator<String> it=object.keys(); it.hasNext(); ) {
					String name = it.next();
					values.put(DocumentEnum.valueOf(name), object.getString(name));
				}
			}
		} catch (JSONException e) {
			log.error("", e);
		}
		return values;
	}
	
	private String toStringJSON(Map<DocumentEnum, String> values) {
		String string = null;
		try {
			if(values != null && !values.isEmpty()) {
				JSONObject object = new JSONObject();
				for(Map.Entry<DocumentEnum, String> size:values.entrySet()) {
					object.put(size.getKey().name(), size.getValue());
				}
				string = object.toString();
			}
		} catch (JSONException e) {
			log.error("", e);
		}
		return string;
	}
	
	public String getDocSizes() {
		return docSizes;
	}

	public void setDocSizes(String docSizes) {
		this.docSizes = docSizes;
	}

	@Override
	@Transient
	public Map<DocumentEnum,Integer> getDocumentSizes() {
		Map<DocumentEnum,Integer> sizeSet = new EnumMap<>(DocumentEnum.class);
		try {
			String sizes = getDocSizes();
			if(sizes != null && sizes.length() > 5) {
				JSONObject object = new JSONObject(sizes);
				for(Iterator<String> it=object.keys(); it.hasNext(); ) {
					String name = it.next();
					sizeSet.put(DocumentEnum.valueOf(name), Integer.valueOf(object.getInt(name)));
				}
			}
		} catch (JSONException e) {
			log.error("", e);
		}
		return sizeSet;
	}

	@Override
	public void setDocumentSizes(Map<DocumentEnum,Integer> sizes) {
		try {
			if(sizes == null || sizes.isEmpty()) {
				setDocSizes(null);
			} else {
				JSONObject object = new JSONObject();
				for(Map.Entry<DocumentEnum, Integer> size:sizes.entrySet()) {
					object.put(size.getKey().name(), size.getValue().intValue());
				}
				setDocSizes(object.toString());
			}
		} catch (JSONException e) {
			log.error("", e);
		}
	}

	@Override
	public Attachment getDocument1() {
		return document1;
	}
	
	@Override
	public void setDocument1(Attachment document1) {
		this.document1 = document1;
	}

	@Override
	public Attachment getDocument2() {
		return document2;
	}

	@Override
	public void setDocument2(Attachment document2) {
		this.document2 = document2;
	}

	@Override
	public Attachment getDocument3() {
		return document3;
	}

	@Override
	public void setDocument3(Attachment document3) {
		this.document3 = document3;
	}

	@Override
	public PolicyLink getPolicyLink1() {
		return policyLink1;
	}

	@Override
	public void setPolicyLink1(PolicyLink policyLink1) {
		this.policyLink1 = policyLink1;
	}

	@Override
	public PolicyLink getPolicyLink2() {
		return policyLink2;
	}

	@Override
	public void setPolicyLink2(PolicyLink policyLink2) {
		this.policyLink2 = policyLink2;
	}

	@Override
	public PolicyLink getPolicyLink3() {
		return policyLink3;
	}

	@Override
	public void setPolicyLink3(PolicyLink policyLink3) {
		this.policyLink3 = policyLink3;
	}

	@Override
	public PolicyLink getPolicyLink4() {
		return policyLink4;
	}

	@Override
	public void setPolicyLink4(PolicyLink policyLink4) {
		this.policyLink4 = policyLink4;
	}


	
	private String documentToString(Collection<String> docs) {
		StringBuilder sb = new StringBuilder();
		for(String doc:docs) {
			if(sb.length() > 0) sb.append(",");
			sb.append(doc);
		}
		return sb.toString();
	}
	
	private String documentEnumToString(Collection<DocumentEnum> docs) {
		StringBuilder sb = new StringBuilder();
		for(DocumentEnum doc:docs) {
			if(sb.length() > 0) sb.append(",");
			sb.append(doc.name());
		}
		return sb.toString();
	}

	@Override
	public boolean isExpertRecommendationEnabled() {
		return expertRecommendationEnabled;
	}

	@Override
	public void setExpertRecommendationEnabled(boolean expertRecommendationEnabled) {
		this.expertRecommendationEnabled = expertRecommendationEnabled;
	}

	@Override
	public Date getExpertRecommandationDeadline() {
		return expertRecommandationDeadline;
	}

	@Override
	public void setExpertRecommandationDeadline(Date expertRecommandationDeadline) {
		this.expertRecommandationDeadline = expertRecommandationDeadline;
	}

	@Override
	public String getExpertRecommandationMailSubject() {
		return expertRecommandationMailSubject;
	}

	@Override
	public void setExpertRecommandationMailSubject(String expertRecommandationMailSubject) {
		this.expertRecommandationMailSubject = expertRecommandationMailSubject;
	}

	@Override
	public String getExpertRecommandationMailTemplate() {
		return expertRecommandationMailTemplate;
	}

	@Override
	public void setExpertRecommandationMailTemplate(String expertRecommandationMailTemplate) {
		this.expertRecommandationMailTemplate = expertRecommandationMailTemplate;
	}
	
	@Override
	public String getExpertRecommandationMailLetter() {
		return expertRecommandationMailLetter;
	}

	@Override
	public void setExpertRecommandationMailLetter(String configuration) {
		this.expertRecommandationMailLetter = configuration;
	}

	public String getExpertRecommendationDocs() {
		return expertRecommendationDocs;
	}

	public void setExpertRecommendationDocs(String expertRecommendationDocs) {
		this.expertRecommendationDocs = expertRecommendationDocs;
	}
	
	@Override
	public Set<String> getExpertRecommendationDocuments() {
		return DocumentEnum.documentStringToSet(expertRecommendationDocs);
	}

	@Override
	public void setExpertRecommendationDocuments(Collection<String> docs) {
		setExpertRecommendationDocs(documentToString(docs));
	}

	public String getExpertRecommendationFieldsString() {
		return expertRecommendationFieldsString;
	}

	public void setExpertRecommendationFieldsString(String expertRecommendationFieldsString) {
		this.expertRecommendationFieldsString = expertRecommendationFieldsString;
	}
	
	@Override
	public Set<String> getExpertRecommendationFields() {
		return getFields(getExpertRecommendationFieldsString());
	}

	@Override
	public void setExpertRecommendationFields(Collection<String> fields) {
		setExpertRecommendationFieldsString(toFieldsString(fields));
	}

	@Override
	public String getExpertConfirmationSubmissionMailSubject() {
		return expertConfirmationSubmissionMailSubject;
	}

	@Override
	public void setExpertConfirmationSubmissionMailSubject(String expertConfirmationSubmissionMailSubject) {
		this.expertConfirmationSubmissionMailSubject = expertConfirmationSubmissionMailSubject;
	}

	@Override
	public String getExpertConfirmationSubmissionMailTemplate() {
		return expertConfirmationSubmissionMailTemplate;
	}

	@Override
	public void setExpertConfirmationSubmissionMailTemplate(String expertConfirmationSubmissionMailTemplate) {
		this.expertConfirmationSubmissionMailTemplate = expertConfirmationSubmissionMailTemplate;
	}

	@Override
	public boolean isRefereeRecommendationEnabled() {
		return refereeRecommendationEnabled;
	}

	@Override
	public void setRefereeRecommendationEnabled(boolean refereeRecommendationEnabled) {
		this.refereeRecommendationEnabled = refereeRecommendationEnabled;
	}

	@Override
	public Date getRefereeRecommandationDeadline() {
		return refereeRecommandationDeadline;
	}

	@Override
	public void setRefereeRecommandationDeadline(Date refereeRecommandationDeadline) {
		this.refereeRecommandationDeadline = refereeRecommandationDeadline;
	}

	public String getRefereeRecommendationDocs() {
		return refereeRecommendationDocs;
	}

	public void setRefereeRecommendationDocs(String refereeRecommendationDocs) {
		this.refereeRecommendationDocs = refereeRecommendationDocs;
	}
	
	@Override
	public Set<String> getRefereeRecommendationDocuments() {
		return DocumentEnum.documentStringToSet(refereeRecommendationDocs);
	}

	@Override
	public void setRefereeRecommendationDocuments(Collection<String> docs) {
		setRefereeRecommendationDocs(documentToString(docs));
	}

	public String getRefereeRecommendationFieldsString() {
		return refereeRecommendationFieldsString;
	}

	public void setRefereeRecommendationFieldsString(String refereeRecommendationFieldsString) {
		this.refereeRecommendationFieldsString = refereeRecommendationFieldsString;
	}

	@Override
	public Set<String> getRefereeRecommendationFields() {
		return getFields(getRefereeRecommendationFieldsString());
	}

	@Override
	public void setRefereeRecommendationFields(Collection<String> fields) {
		setRefereeRecommendationFieldsString(toFieldsString(fields));
	}

	@Override
	public boolean isApplicantRefereeManagementEnabled() {
		return applicantRefereeManagementEnabled;
	}

	@Override
	public void setApplicantRefereeManagementEnabled(boolean applicantRefereeManagementEnabled) {
		this.applicantRefereeManagementEnabled = applicantRefereeManagementEnabled;
	}

	@Override
	public Date getApplicantRefereeManagementDeadline() {
		return applicantRefereeManagementDeadline;
	}

	@Override
	public void setApplicantRefereeManagementDeadline(Date applicantRefereeManagementDeadline) {
		this.applicantRefereeManagementDeadline = applicantRefereeManagementDeadline;
	}

	@Override
	public Long getMinReferees() {
		return minReferees;
	}

	@Override
	@Transient
	public long getMinRefereesAsLong() {
		return getMinReferees() == null ? 0l : getMinReferees().longValue();
	}

	@Override
	public void setMinReferees(Long minReferees) {
		this.minReferees = minReferees;
	}

	@Override
	public Long getMaxReferees() {
		return maxReferees;
	}
	
	@Override
	@Transient
	public long getMaxRefereesAsLong() {
		long min = getMinRefereesAsLong();
		return getMaxReferees() == null ? (min == 0 ? 4 : min) : getMaxReferees().longValue();
	}

	@Override
	public void setMaxReferees(Long maxReferees) {
		this.maxReferees = maxReferees;
	}

	@Override
	public String getRefereeRecommandationMailSubject() {
		return refereeRecommandationMailSubject;
	}

	@Override
	public void setRefereeRecommandationMailSubject(String refereeRecommandationMailSubject) {
		this.refereeRecommandationMailSubject = refereeRecommandationMailSubject;
	}

	@Override
	public String getRefereeRecommandationMailTemplate() {
		return refereeRecommandationMailTemplate;
	}

	@Override
	public void setRefereeRecommandationMailTemplate(String refereeRecommandationMailTemplate) {
		this.refereeRecommandationMailTemplate = refereeRecommandationMailTemplate;
	}
	
	@Override
	public String getRefereeRecommandationMailLetter() {
		return refereeRecommandationMailLetter;
	}

	@Override
	public void setRefereeRecommandationMailLetter(String configuration) {
		this.refereeRecommandationMailLetter = configuration;
	}

	@Override
	public String getRefereeConfirmationSubmissionMailSubject() {
		return refereeConfirmationSubmissionMailSubject;
	}

	@Override
	public void setRefereeConfirmationSubmissionMailSubject(String refereeConfirmationSubmissionMailSubject) {
		this.refereeConfirmationSubmissionMailSubject = refereeConfirmationSubmissionMailSubject;
	}

	@Override
	public String getRefereeConfirmationSubmissionMailTemplate() {
		return refereeConfirmationSubmissionMailTemplate;
	}

	@Override
	public void setRefereeConfirmationSubmissionMailTemplate(String refereeConfirmationSubmissionMailTemplate) {
		this.refereeConfirmationSubmissionMailTemplate = refereeConfirmationSubmissionMailTemplate;
	}

	public String getRecommandationSendMailType() {
		return recommandationSendMailType;
	}

	public void setRecommandationSendMailType(String recommandationSendMailType) {
		this.recommandationSendMailType = recommandationSendMailType;
	}

	@Override
	@Transient
	public ReferenceSendMailType getRefereeRecommandationSendMailType() {
		return StringHelper.containsNonWhitespace(recommandationSendMailType) ?
			ReferenceSendMailType.valueOf(recommandationSendMailType) : null;
	}

	@Override
	public void setRefereeRecommandationSendMailType(ReferenceSendMailType type) {
		if(type == null) {
			recommandationSendMailType = null;
		} else {
			recommandationSendMailType = type.name();
		}
	}

	@Override
	public boolean isComparativeAssessmentExpertEnabled() {
		return comparativeAssessmentExpertEnabled;
	}

	@Override
	public void setComparativeAssessmentExpertEnabled(boolean enabled) {
		comparativeAssessmentExpertEnabled = enabled;
	}

	@Override
	public Date getComparativeAssessmentExpertDeadline() {
		return comparativeAssessmentExpertDeadline;
	}

	@Override
	public void setComparativeAssessmentExpertDeadline(Date deadline) {
		comparativeAssessmentExpertDeadline = deadline;
	}

	@Override
	public String getComparativeAssessmentExpertMailSubject() {
		return comparativeAssessmentExpertMailSubject;
	}

	@Override
	public void setComparativeAssessmentExpertMailSubject(String comparativeAssessmentExpertMailSubject) {
		this.comparativeAssessmentExpertMailSubject = comparativeAssessmentExpertMailSubject;
	}

	@Override
	public String getComparativeAssessmentExpertMailTemplate() {
		return comparativeAssessmentExpertMailTemplate;
	}

	@Override
	public void setComparativeAssessmentExpertMailTemplate(String template) {
		comparativeAssessmentExpertMailTemplate = template;
	}

	@Override
	public String getComparativeAssessmentExpertMailLetter() {
		return comparativeAssessmentExpertMailLetter;
	}

	@Override
	public void setComparativeAssessmentExpertMailLetter(String comparativeAssessmentExpertMailLetter) {
		this.comparativeAssessmentExpertMailLetter = comparativeAssessmentExpertMailLetter;
	}

	public String getComparativeAssessmentExpertDocs() {
		return comparativeAssessmentExpertDocs;
	}

	public void setComparativeAssessmentExpertDocs(String documents) {
		this.comparativeAssessmentExpertDocs = documents;
	}

	@Override
	public Set<String> getComparativeAssessmentExpertDocuments() {
		return DocumentEnum.documentStringToSet(comparativeAssessmentExpertDocs);
	}

	@Override
	public void setComparativeAssessmentExpertDocuments(Collection<String> docs) {
		setComparativeAssessmentExpertDocs(documentToString(docs));
	}

	public String getComparativeAssessmentExpertFieldsString() {
		return comparativeAssessmentExpertFieldsString;
	}

	public void setComparativeAssessmentExpertFieldsString(String comparativeAssessmentExpertFieldsString) {
		this.comparativeAssessmentExpertFieldsString = comparativeAssessmentExpertFieldsString;
	}

	@Override
	public Set<String> getComparativeAssessmentExpertFields() {
		return getFields(getComparativeAssessmentExpertFieldsString());
	}

	@Override
	public void setComparativeAssessmentExpertFields(Collection<String> fields) {
		setComparativeAssessmentExpertFieldsString(toFieldsString(fields));
	}
	
	private static final Set<String> getFields(String fieldsString) {
		Set<String> fields = new HashSet<>();
		if(StringHelper.containsNonWhitespace(fieldsString)) {
			String[] fieldsArr = fieldsString.split(",");
			for(String field:fieldsArr) {
				if(StringHelper.containsNonWhitespace(field)) {
					fields.add(field);
				}
			}
		}
		return fields;
	}
	
	private static final String toFieldsString(Collection<String> fields) {
		if(fields == null || fields.isEmpty()) {
			return null;
		} 
		return String.join(",", fields);
	}

	@Override
	public String getComparativeAssessmentExpertConfirmationSubmissionMailSubject() {
		return comparativeAssessmentExpertConfirmationSubmissionMailSubject;
	}

	@Override
	public void setComparativeAssessmentExpertConfirmationSubmissionMailSubject(
			String comparativeAssessmentExpertConfirmationSubmissionMailSubject) {
		this.comparativeAssessmentExpertConfirmationSubmissionMailSubject = comparativeAssessmentExpertConfirmationSubmissionMailSubject;
	}

	@Override
	public String getComparativeAssessmentExpertConfirmationSubmissionMailTemplate() {
		return comparativeAssessmentExpertConfirmationSubmissionMailTemplate;
	}

	@Override
	public void setComparativeAssessmentExpertConfirmationSubmissionMailTemplate(
			String comparativeAssessmentExpertConfirmationSubmissionMailTemplate) {
		this.comparativeAssessmentExpertConfirmationSubmissionMailTemplate = comparativeAssessmentExpertConfirmationSubmissionMailTemplate;
	}

	@Override
	public boolean isPublicFeedbackEnabled() {
		return publicFeedbackEnabled;
	}

	@Override
	public void setPublicFeedbackEnabled(boolean enable) {
		publicFeedbackEnabled = enable;
	}

	@Override
	public Date getPublicFeedbackDeadline() {
		return publicFeedbackDeadline;
	}

	@Override
	public void setPublicFeedbackDeadline(Date deadline) {
		publicFeedbackDeadline = deadline;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	@Override
	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public boolean isCommitteeCommentEnabled() {
		return committeeCommentEnabled;
	}

	@Override
	public void setCommitteeCommentEnabled(boolean enabled) {
		this.committeeCommentEnabled = enabled;
	}

	@Override
	@Transient
	public PositionRole[] getCommitteeCommentVisiblity() {
		PositionRole[] roles;
		if(StringHelper.containsNonWhitespace(committeeCommentVisiblityString)) {
			roles = PositionRole.valueOfArray(committeeCommentVisiblityString);
		} else {
			roles = PositionRole.EMPTY;
		}
		return roles;
	}

	@Override
	public void setCommitteeCommentVisiblity(PositionRole[] roles) {
		if(roles == null || roles.length == 0) {
			committeeCommentVisiblityString = null;
		} else {
			committeeCommentVisiblityString = PositionRole.toString(roles);
		}
	}

	public String getCommitteeCommentVisiblityString() {
		return committeeCommentVisiblityString;
	}

	public void setCommitteeCommentVisiblityString(String committeeCommentVisiblityString) {
		this.committeeCommentVisiblityString = committeeCommentVisiblityString;
	}

	@Override
	public boolean isReviewEnabled() {
		return reviewEnabled;
	}

	@Override
	public void setReviewEnabled(boolean reviewEnabled) {
		this.reviewEnabled = reviewEnabled;
	}

	@Override
	public PositionReviewDefinition getReviewDefinition() {
		return reviewDefinition;
	}

	@Override
	public void setReviewDefinition(PositionReviewDefinition reviewDefinition) {
		this.reviewDefinition = reviewDefinition;
	}

	@Override
	public List<PositionAttributeDefinition> getAttributesDefinitions() {
		if(attributesDefinitions == null) {
			attributesDefinitions = new ArrayList<>();
		}
		return attributesDefinitions;
	}

	@Override
	public void setAttributesDefinitions(List<PositionAttributeDefinition> attributeDefinitions) {
		this.attributesDefinitions = attributeDefinitions;
	}

	@Override
	public List<PositionAttributeDefinition> getAttributesDefinitions(PositionApplicationAttributeTabEnum tab) {
		List<PositionAttributeDefinition> defs = getAttributesDefinitions();
		if(tab == null || defs == null || defs.isEmpty()) {
			return defs;
		}
		return defs.stream()
				.filter(def -> tab.equals(def.getTabEnum()))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isSystemTagsEnabled() {
		return systemTagsEnabled;
	}

	@Override
	public void setSystemTagsEnabled(boolean systemTagsEnabled) {
		this.systemTagsEnabled = systemTagsEnabled;
	}

	@Override
	public boolean isPositionTagsEnabled() {
		return positionTagsEnabled;
	}

	@Override
	public void setPositionTagsEnabled(boolean positionTagsEnabled) {
		this.positionTagsEnabled = positionTagsEnabled;
	}

	@Override
	public RecruitingDuplicateApplicationOption getDuplicateApplicationAllowedEnum() {
		String val = getDuplicateApplicationAllowed();
		return StringHelper.containsNonWhitespace(val)
				? RecruitingDuplicateApplicationOption.valueOf(val)
				: null;
	}

	@Override
	public void setDuplicateApplicationAllowedEnum(RecruitingDuplicateApplicationOption allowed) {
		if(allowed == null) {
			setDuplicateApplicationAllowed(null);
		} else {
			setDuplicateApplicationAllowed(allowed.name());
		}
	}

	public String getDuplicateApplicationAllowed() {
		return duplicateApplicationAllowed;
	}

	public void setDuplicateApplicationAllowed(String allowed) {
		// String because they want perhaps the algorithm configured at position level
		this.duplicateApplicationAllowed = allowed;
	}

	@Override
	public Set<ApplicationAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(Set<ApplicationAttribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public int hashCode() {
		return key == null ? 39845893 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PositionImpl) {
			PositionImpl pos = (PositionImpl)obj;
			return getKey() != null && getKey().equals(pos.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("position[key:").append(getKey());
		if(StringHelper.containsNonWhitespace(shortTitle)) {
			sb.append(";shortTitle:").append(shortTitle);
		} else if(StringHelper.containsNonWhitespace(shortTitleDe)) {
			sb.append(";shortTitle:").append(shortTitleDe);
		}
		if(StringHelper.containsNonWhitespace(positionTitle)) {
			sb.append(";positionTitle:").append(positionTitle);
		} else if(StringHelper.containsNonWhitespace(positionTitleDe)) {
			sb.append(";positionTitle:").append(positionTitleDe);
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public String toStringFull() {
		StringBuilder sb = new StringBuilder();
		sb.append("position[key:").append(getKey()).append(";")
			.append("planingsNumber=").append(planingsNumber == null ? "" : planingsNumber).append(";")
			.append("positionTitle=").append(positionTitle == null ? "" : positionTitle).append(";")
			.append("shortTitle=").append(shortTitle == null ? "" : shortTitle).append(";")
			.append("description=").append(description == null ? "" : description).append(";")
			.append("department=").append(department == null ? "" : department).append(";")
			.append("homepage=").append(homepage == null ? "" : homepage).append(";")
			.append("applicationDeadline=").append(applicationDeadline == null ? "" : applicationDeadline).append(";")
			.append("status=").append(status == null ? "" : status).append(";")
			.append("valid=").append(valid ? "true" : "false").append(";");
		return sb.append("]").toString();
	}
}
