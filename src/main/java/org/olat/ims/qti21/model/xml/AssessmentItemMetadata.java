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
package org.olat.ims.qti21.model.xml;

import java.math.BigDecimal;
import java.util.Locale;

import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.imsmd.xml.manifest.EducationalType;
import org.olat.imsqti.xml.manifest.QTIMetadataType;
import org.olat.oo.xml.manifest.OpenOLATMetadataType;

/**
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemMetadata {
	
	private String language;
	private String taxonomyPath;
	private String keywords;
	private String coverage;
	private String level;
	private String typicalLearningTime;
	private String license;
	private String editor;
	private String editorVersion;
	private String topic;
	private String creator;
	private String assessmentType;
	private String additionalInformations;
	private int numOfAnswerAlternatives;
	private BigDecimal difficulty;
	private BigDecimal differentiation;
	private BigDecimal stdevDifficulty;
	private Integer correctionTime;

	private boolean hasError;
	
	private QTI21QuestionType questionType;
	private String interactionType;
	
	public AssessmentItemMetadata() {
		//
	}
	
	public AssessmentItemMetadata(ManifestMetadataBuilder metadaBuilder) {
		fromBuilder(metadaBuilder);
	}
	
	public QTI21QuestionType getQuestionType() {
		return questionType;
	}
	
	public void setQuestionType(QTI21QuestionType questionType) {
		this.questionType = questionType;
	}
	
	public String getInteractionType() {
		return interactionType;
	}
	
	public void setInteractionType(String interactionType) {
		this.interactionType = interactionType;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTaxonomyPath() {
		return taxonomyPath;
	}

	public void setTaxonomyPath(String taxonomyPath) {
		this.taxonomyPath = taxonomyPath;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getTypicalLearningTime() {
		return typicalLearningTime;
	}

	public void setTypicalLearningTime(String typicalLearningTime) {
		this.typicalLearningTime = typicalLearningTime;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public String getEditorVersion() {
		return editorVersion;
	}

	public void setEditorVersion(String editorVersion) {
		this.editorVersion = editorVersion;
	}

	public int getNumOfAnswerAlternatives() {
		return numOfAnswerAlternatives;
	}

	public void setNumOfAnswerAlternatives(int numOfAnswerAlternatives) {
		this.numOfAnswerAlternatives = numOfAnswerAlternatives;
	}

	public BigDecimal getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(BigDecimal difficulty) {
		this.difficulty = difficulty;
	}

	public BigDecimal getDifferentiation() {
		return differentiation;
	}

	public void setDifferentiation(BigDecimal differentiation) {
		this.differentiation = differentiation;
	}

	public BigDecimal getStdevDifficulty() {
		return stdevDifficulty;
	}

	public void setStdevDifficulty(BigDecimal stdevDifficulty) {
		this.stdevDifficulty = stdevDifficulty;
	}

	public boolean isHasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getAssessmentType() {
		return assessmentType;
	}

	public void setAssessmentType(String assessmentType) {
		this.assessmentType = assessmentType;
	}

	public String getAdditionalInformations() {
		return additionalInformations;
	}

	public void setAdditionalInformations(String additionalInformations) {
		this.additionalInformations = additionalInformations;
	}

	public Integer getCorrectionTime() {
		return correctionTime;
	}

	public void setCorrectionTime(Integer correctionTime) {
		this.correctionTime = correctionTime;
	}

	public void toBuilder(ManifestMetadataBuilder metadata, Locale locale) {
		if(getQuestionType() != null) {
			metadata.setOpenOLATMetadataQuestionType(getQuestionType().getPrefix());
		}
		metadata.setTechnicalFormat(ManifestBuilder.ASSESSMENTITEM_MIMETYPE);
		
		String lang = locale.getLanguage();
		
		// general
		if(StringHelper.containsNonWhitespace(keywords)) {
			metadata.setGeneralKeywords(keywords, lang);
		}
		if(StringHelper.containsNonWhitespace(coverage)) {
			metadata.setCoverage(lang, lang);
		}

		//educational
		if(StringHelper.containsNonWhitespace(level)) {
			metadata.setEducationalContext(level, lang);
		}
		if(StringHelper.containsNonWhitespace(typicalLearningTime)) {
			metadata.setEducationalLearningTime(typicalLearningTime);
		}

		//classification
		if(StringHelper.containsNonWhitespace(taxonomyPath)) {
			metadata.setClassificationTaxonomy(taxonomyPath, lang);
		}

		// rights
		if(StringHelper.containsNonWhitespace(license)) {
			metadata.setLicense(license);
		}

		//qti metadata
		if(StringHelper.containsNonWhitespace(editor) || StringHelper.containsNonWhitespace(editorVersion)) {
			metadata.setQtiMetadataTool(editor, null, editorVersion);
		}

		//openolat metadata
		if(differentiation != null) {
			metadata.setOpenOLATMetadataDiscriminationIndex(differentiation.doubleValue());
		}
		if(difficulty != null) {
			metadata.setOpenOLATMetadataDifficulty(difficulty.doubleValue());
		}
		if(stdevDifficulty != null) {
			metadata.setOpenOLATMetadataStandardDeviation(stdevDifficulty.doubleValue());
		}
		if(numOfAnswerAlternatives >= 0) {
			metadata.setOpenOLATMetadataDistractors(numOfAnswerAlternatives);
		}
		metadata.setOpenOLATMetadataCreator(creator);
		metadata.setOpenOLATMetadataTopic(topic);
		metadata.setOpenOLATMetadataAssessmentType(assessmentType);
		metadata.setOpenOLATMetadataAdditionalInformations(additionalInformations);
		metadata.setOpenOLATMetadataCorrectionTime(correctionTime);
	}
	
	public void fromBuilder(ManifestMetadataBuilder metadata) {
		// general
		keywords = metadata.getGeneralKeywords();
		coverage = metadata.getCoverage();
		
		//educational
		EducationalType educational = metadata.getEducational(false);
		if(educational != null) {
			level = metadata.getEducationContext();
		}
		typicalLearningTime = metadata.getEducationalLearningTime();
		
		//taxonomy
		taxonomyPath = metadata.getClassificationTaxonomy();

		//rights
		license = metadata.getLicense();
		
		//qti metadata
		QTIMetadataType qtiMetadata = metadata.getQtiMetadata(true);
		if(qtiMetadata != null) {
			if(qtiMetadata.getToolName() != null) {
				editor = qtiMetadata.getToolName();
			}
			if(qtiMetadata.getToolVersion() != null) {
				editorVersion = qtiMetadata.getToolVersion();
			}
		}
		
		//openolat metadata
		OpenOLATMetadataType openolatMetadata = metadata.getOpenOLATMetadata(false);
		if(openolatMetadata != null) {
			if(openolatMetadata.getDiscriminationIndex() != null) {
				differentiation = new BigDecimal(openolatMetadata.getDiscriminationIndex());
			}
			if(openolatMetadata.getDifficulty() != null) {
				difficulty = new BigDecimal(openolatMetadata.getDifficulty());
			}
			if(openolatMetadata.getStandardDeviation() != null) {
				stdevDifficulty = new BigDecimal(openolatMetadata.getStandardDeviation());
			}
			if(openolatMetadata.getDistractors() != null) {
				numOfAnswerAlternatives = openolatMetadata.getDistractors().intValue();
			}
			if(openolatMetadata.getTopic() != null) {
				topic = openolatMetadata.getTopic();
			}
			if(openolatMetadata.getAssessmentType() != null) {
				assessmentType = openolatMetadata.getAssessmentType();
			}
			if(openolatMetadata.getCreator() != null) {
				creator = openolatMetadata.getCreator();
			}
			if(openolatMetadata.getAdditionalInformations() != null) {
				additionalInformations = openolatMetadata.getAdditionalInformations();
			}
			if(openolatMetadata.getCorrectionTime() != null) {
				correctionTime = openolatMetadata.getCorrectionTime();
			}
		}
	}
}