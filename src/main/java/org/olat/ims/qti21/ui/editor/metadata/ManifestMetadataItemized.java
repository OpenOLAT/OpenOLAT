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
package org.olat.ims.qti21.ui.editor.metadata;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.pool.QTI21MetadataConverter;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemEditable;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Manifest based implementation of QuestionItem, editable.
 * 
 * Initial date: 8 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManifestMetadataItemized implements QuestionItem, QuestionItemEditable {
	
	private final ManifestMetadataBuilder metadataBuilder;
	
	private final QTI21MetadataConverter metadataConverter;
	
	private String lang;
	private String directory;
	
	private QItemType itemType;
	private QEducationalContext context;
	private TaxonomyLevel taxonomyLevel;
	
	@Autowired
	private QItemTypeDAO itemTypeDao;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QEducationalContextDAO educationalContextDao;

	
	public ManifestMetadataItemized(ManifestMetadataBuilder metadataBuilder, String lang) {
		CoreSpringFactory.autowireObject(this);
		
		this.metadataBuilder = metadataBuilder;
		this.lang = lang;
		metadataConverter = new QTI21MetadataConverter(itemTypeDao, educationalContextDao, qpoolService);
		if(StringHelper.containsNonWhitespace(metadataBuilder.getClassificationTaxonomy())) {
			taxonomyLevel = metadataConverter.toTaxonomy(metadataBuilder.getClassificationTaxonomy());
		}
		if(StringHelper.containsNonWhitespace(metadataBuilder.getEducationContext())) {
			context = metadataConverter.toEducationalContext(metadataBuilder.getEducationContext());
		}
		if(StringHelper.containsNonWhitespace(metadataBuilder.getOpenOLATMetadataQuestionType())) {
			itemType = metadataConverter.toType(metadataBuilder.getOpenOLATMetadataQuestionType());
		}
	}
	
	public ManifestMetadataItemized(ManifestMetadataBuilder metadataBuilder, String lang, QTI21MetadataConverter metadataConverter) {
		CoreSpringFactory.autowireObject(this);
		
		this.metadataConverter = metadataConverter;
		this.metadataBuilder = metadataBuilder;
		this.lang = lang;
		if(StringHelper.containsNonWhitespace(metadataBuilder.getClassificationTaxonomy())) {
			taxonomyLevel = metadataConverter.toTaxonomy(metadataBuilder.getClassificationTaxonomy());
		}
		if(StringHelper.containsNonWhitespace(metadataBuilder.getEducationContext())) {
			context = metadataConverter.toEducationalContext(metadataBuilder.getEducationContext());
		}
		if(StringHelper.containsNonWhitespace(metadataBuilder.getOpenOLATMetadataQuestionType())) {
			itemType = metadataConverter.toType(metadataBuilder.getOpenOLATMetadataQuestionType());
		}
	}

	@Override
	public Long getKey() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return metadataBuilder.getOpenOLATMetadataIdentifier();
	}

	@Override
	public String getMasterIdentifier() {
		return metadataBuilder.getOpenOLATMetadataMasterIdentifier();
	}

	@Override
	public String getTitle() {
		return metadataBuilder.getTitle();
	}

	@Override
	public String getLanguage() {
		return lang;
	}

	@Override
	public String getKeywords() {
		return metadataBuilder.getGeneralKeywords();
	}

	@Override
	public void setKeywords(String keywords) {
		metadataBuilder.setGeneralKeywords(keywords, lang);
	}

	@Override
	public void setLanguage(String language) {
		this.lang = language;
		metadataBuilder.setLanguage(language, language);
	}

	@Override
	public String getTaxonomyLevelName() {
		if(taxonomyLevel != null) {
			return taxonomyLevel.getDisplayName();
		}
		return metadataBuilder.getClassificationTaxonomy();
	}

	@Override
	public String getTaxonomicPath() {
		if(taxonomyLevel != null) {
			String path = taxonomyLevel.getMaterializedPathIdentifiers();
			if(path != null) {
				path += "/" + taxonomyLevel.getIdentifier();
			}
			return path;
		}
		return metadataBuilder.getClassificationTaxonomy();
	}

	@Override
	public String getTopic() {
		return metadataBuilder.getOpenOLATMetadataTopic();
	}

	@Override
	public void setTopic(String topic) {
		metadataBuilder.setOpenOLATMetadataTopic(topic);
	}

	@Override
	public String getEducationalContextLevel() {
		if(context != null) {
			return context.getLevel();
		}
		return metadataBuilder.getEducationContext();
	}

	@Override
	public String getEducationalLearningTime() {
		return metadataBuilder.getEducationalLearningTime();
	}	

	@Override
	public void setEducationalLearningTime(String time) {
		metadataBuilder.setEducationalLearningTime(time);
	}

	@Override
	public String getItemType() {
		return metadataBuilder.getOpenOLATMetadataQuestionType();
	}

	@Override
	public BigDecimal getDifficulty() {
		Double val = metadataBuilder.getOpenOLATMetadataDifficulty();
		return val == null ? null : BigDecimal.valueOf(val.doubleValue());
	}
	
	@Override
	public void setDifficulty(BigDecimal difficulty) {
		metadataBuilder.setOpenOLATMetadataDifficulty(difficulty);
	}

	@Override
	public BigDecimal getStdevDifficulty() {
		Double val = metadataBuilder.getOpenOLATMetadataStandardDeviation();
		return val == null ? null : BigDecimal.valueOf(val.doubleValue());
	}

	@Override
	public void setStdevDifficulty(BigDecimal stdevDifficulty) {
		metadataBuilder.setOpenOLATMetadataStandardDeviation(stdevDifficulty);
	}

	@Override
	public BigDecimal getDifferentiation() {
		Double val =  metadataBuilder.getOpenOLATMetadataDiscriminationIndex();
		return val == null ? null : BigDecimal.valueOf(val.doubleValue());
	}
	
	@Override
	public void setDifferentiation(BigDecimal differentiation) {
		metadataBuilder.setOpenOLATMetadataDiscriminationIndex(differentiation);
	}

	@Override
	public int getNumOfAnswerAlternatives() {
		Integer val = metadataBuilder.getOpenOLATMetadataDistractors();
		return val == null ? 0 : val.intValue();
	}

	@Override
	public void setNumOfAnswerAlternatives(int numOfAnswerAlternatives) {
		metadataBuilder.setOpenOLATMetadataDistractors(Integer.valueOf(numOfAnswerAlternatives));
	}

	@Override
	public int getUsage() {
		Integer usage = metadataBuilder.getOpenOLATMetadataUsage();
		return usage == null ? 0 : usage.intValue();
	}
	
	@Override
	public void setUsage(int numOfUsage) {
		metadataBuilder.setOpenOLATMetadataUsage(Integer.valueOf(numOfUsage));
	}

	@Override
	public QuestionStatus getQuestionStatus() {
		return null;
	}

	@Override
	public Date getQuestionStatusLastModified() {
		return null;
	}

	@Override
	public String getFormat() {
		return QTI21Constants.QTI_21_FORMAT;
	}

	@Override
	public Integer getCorrectionTime() {
		return metadataBuilder.getOpenOLATMetadataCorrectionTime();
	}
	
	@Override
	public void setCorrectionTime(Integer timeInMinute) {
		metadataBuilder.setOpenOLATMetadataCorrectionTime(timeInMinute);
	}

	@Override
	public Date getLastModified() {
		return null;
	}

	@Override
	public void setLastModified(Date date) {
		//
	}

	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public String getResourceableTypeName() {
		return null;
	}

	@Override
	public Long getResourceableId() {
		return null;
	}

	@Override
	public String getDescription() {
		return metadataBuilder.getDescription();
	}

	@Override
	public String getCoverage() {
		return metadataBuilder.getCoverage();
	}

	@Override
	public void setCoverage(String coverage) {
		metadataBuilder.setCoverage(coverage, lang);
	}

	@Override
	public String getAdditionalInformations() {
		return metadataBuilder.getOpenOLATMetadataAdditionalInformations();
	}

	@Override
	public void setAdditionalInformations(String informations) {
		metadataBuilder.setOpenOLATMetadataAdditionalInformations(informations);
	}

	@Override
	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
		if(taxonomyLevel == null) {
			metadataBuilder.setClassificationTaxonomy(null, lang);
		} else {
			metadataBuilder.setClassificationTaxonomy(getTaxonomicPath(), lang);
		}
	}

	@Override
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	public QEducationalContext getEducationalContext() {
		return context;
	}

	@Override
	public void setEducationalContext(QEducationalContext context) {
		this.context = context;
		if(context == null) {
			metadataBuilder.setEducationalContext(null, lang);
		} else {
			metadataBuilder.setEducationalContext(context.getLevel(), lang);
		}
	}

	@Override
	public QItemType getType() {
		return itemType;
	}

	@Override
	public String getAssessmentType() {
		return metadataBuilder.getOpenOLATMetadataAssessmentType();
	}

	@Override
	public void setAssessmentType(String type) {
		metadataBuilder.setOpenOLATMetadataAssessmentType(type);
	}

	@Override
	public String getItemVersion() {
		return metadataBuilder.getLifecycleVersion();
	}

	@Override
	public QLicense getLicense() {
		return null;
	}

	@Override
	public String getCreator() {
		return metadataBuilder.getOpenOLATMetadataCreator();
	}

	@Override
	public String getEditor() {
		return metadataBuilder.getQtiMetadaToolVendor();
	}

	@Override
	public String getEditorVersion() {
		return metadataBuilder.getQtiMetadataToolVersion();
	}

	@Override
	public String getDirectory() {
		return directory;
	}
}
