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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import jakarta.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.imscp.xml.manifest.ManifestMetadataType;
import org.olat.imscp.xml.manifest.MetadataType;
import org.olat.imsmd.xml.manifest.ClassificationType;
import org.olat.imsmd.xml.manifest.ContextType;
import org.olat.imsmd.xml.manifest.CopyrightandotherrestrictionsType;
import org.olat.imsmd.xml.manifest.CoverageType;
import org.olat.imsmd.xml.manifest.DescriptionType;
import org.olat.imsmd.xml.manifest.EducationalType;
import org.olat.imsmd.xml.manifest.EntryType;
import org.olat.imsmd.xml.manifest.GeneralType;
import org.olat.imsmd.xml.manifest.KeywordType;
import org.olat.imsmd.xml.manifest.LangstringType;
import org.olat.imsmd.xml.manifest.LifecycleType;
import org.olat.imsmd.xml.manifest.LomType;
import org.olat.imsmd.xml.manifest.PurposeType;
import org.olat.imsmd.xml.manifest.RightsType;
import org.olat.imsmd.xml.manifest.SourceType;
import org.olat.imsmd.xml.manifest.StatusType;
import org.olat.imsmd.xml.manifest.StringType;
import org.olat.imsmd.xml.manifest.TaxonType;
import org.olat.imsmd.xml.manifest.TaxonpathType;
import org.olat.imsmd.xml.manifest.TechnicalType;
import org.olat.imsmd.xml.manifest.TitleType;
import org.olat.imsmd.xml.manifest.TypicallearningtimeType;
import org.olat.imsmd.xml.manifest.ValueType;
import org.olat.imsmd.xml.manifest.VersionType;
import org.olat.imsqti.xml.manifest.QTIMetadataType;
import org.olat.modules.qpool.QuestionItem;
import org.olat.oo.xml.manifest.OpenOLATMetadataType;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 23.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManifestMetadataBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(ManifestMetadataBuilder.class);
	
	protected static final org.olat.oo.xml.manifest.ObjectFactory ooObjectFactory = new org.olat.oo.xml.manifest.ObjectFactory();
	protected static final org.olat.imscp.xml.manifest.ObjectFactory cpObjectFactory = new org.olat.imscp.xml.manifest.ObjectFactory();
	protected static final org.olat.imsmd.xml.manifest.ObjectFactory mdObjectFactory = new org.olat.imsmd.xml.manifest.ObjectFactory();
	protected static final org.olat.imsqti.xml.manifest.ObjectFactory qtiObjectFactory = new org.olat.imsqti.xml.manifest.ObjectFactory();
	
	public static final String ASSESSMENTTEST_MIMETYPE = "text/x-imsqti-test-xml";
	public static final String ASSESSMENTITEM_MIMETYPE = "text/x-imsqti-item-xml";
	
	private MetadataType metadata;
	private ManifestMetadataType manifestMetadata;
	
	
	
	public ManifestMetadataBuilder() {
		metadata = cpObjectFactory.createMetadataType();
	}
	
	public ManifestMetadataBuilder(MetadataType metadata) {
		this.metadata = metadata;
	}
	
	public MetadataType getMetadata() {
		return metadata;
	}
	
	public void setMetadata(MetadataType metadata) {
		this.metadata = metadata;
	}
	
	public String getIdentifier() {
		GeneralType general = getGeneral(false);
		if(general != null) {
			for(Object any:general.getContent()) {
				if(any instanceof JAXBElement<?> && ((JAXBElement<?>)any).getName().getLocalPart().equals("identifier")) {
					return (String)((JAXBElement<?>)any).getValue();
				}
			}
		}
		return null;
	}
	
	public String getTitle() {
		GeneralType general = getGeneral(false);
		if(general != null) {
			TitleType type = getFromAny(TitleType.class, general.getContent());
			return type == null ? null : getFirstString(type.getLangstring());
		}
		return null;
	}
	
	public void setTitle(String title, String lang) {
		GeneralType general = getGeneral(true);
		if(general != null) {
			TitleType type = getFromAny(TitleType.class, general.getContent());
			if(type == null) {
				type = mdObjectFactory.createTitleType();
				general.getContent().add(mdObjectFactory.createTitle(type));
			}
			createOrUpdateFirstLangstring(type.getLangstring(), title, lang);
		}
	}
	
	public String getDescription() {
		GeneralType general = getGeneral(false);
		if(general != null) {
			DescriptionType type = getFromAny(DescriptionType.class, general.getContent());
			return type == null ? null : getFirstString(type.getLangstring());
		}
		return null;
	}
	
	public void setDescription(String description, String lang) {
		GeneralType general = getGeneral(true);
		if(general != null) {
			DescriptionType type = getFromAny(DescriptionType.class, general.getContent());
			if(type == null) {
				type = mdObjectFactory.createDescriptionType();
				general.getContent().add(mdObjectFactory.createDescription(type));
			}
			createOrUpdateFirstLangstring(type.getLangstring(), description, lang);
		}
	}
	
	public String getGeneralKeywords() {
		GeneralType general = getGeneral(false);
		if(general != null) {
			StringBuilder keywords = new StringBuilder();
			for(Object any:general.getContent()) {
				if(any instanceof JAXBElement<?>
					&& ((JAXBElement<?>)any).getValue().getClass().equals(KeywordType.class)) {
					KeywordType keywordType = (KeywordType)((JAXBElement<?>)any).getValue();
					List<LangstringType> langStrings = keywordType.getLangstring();
					for(LangstringType langString:langStrings) {
						String keyword = langString.getValue();
						if(keywords.length() > 0) keywords.append(" ");
						keywords.append(keyword);
					}
				}
			}
			return keywords.toString();
		}
		return null;
	}
	
	public void setGeneralKeywords(String keywords, String lang) {
		GeneralType general = getGeneral(true);
		if(general != null) {
			clearFromAny(KeywordType.class, general.getContent());
			for(StringTokenizer tokenizer = new StringTokenizer(keywords, " "); tokenizer.hasMoreTokens(); ) {
				String keyword = tokenizer.nextToken();
				KeywordType type = mdObjectFactory.createKeywordType();
				general.getContent().add(mdObjectFactory.createKeyword(type));
				createOrUpdateFirstLangstring(type.getLangstring(), keyword, lang);
			}
		}
	}
	
	public void setLanguage(String language, String lang) {
		GeneralType general = getGeneral(true);
		if(general != null) {
			StringType type = getFromAny(StringType.class, general.getContent());
			if(type == null) {
				type = mdObjectFactory.createStringType();
				QName languageQNAME = new QName("http://www.imsglobal.org/xsd/imsmd_v1p2", "context");
				JAXBElement<StringType> typeEl = new JAXBElement<>(languageQNAME, StringType.class, null, type);
				general.getContent().add(typeEl);
			}
			type.setLang(lang);
			type.setValue(language);
		}
	}
	
	public String getCoverage() {
		GeneralType general = getGeneral(false);
		if(general != null) {
			CoverageType type = getFromAny(CoverageType.class, general.getContent());
			if(type != null) {
				return getFirstString(type.getLangstring());
			}
		}
		return null;
	}
	
	public void setCoverage(String coverage, String lang) {
		GeneralType general = getGeneral(true);
		if(general != null) {
			CoverageType type = getFromAny(CoverageType.class, general.getContent());
			if(type == null) {
				type = mdObjectFactory.createCoverageType();
				general.getContent().add(mdObjectFactory.createCoverage(type));
			}
			createOrUpdateFirstLangstring(type.getLangstring(), coverage, lang);
		}
	}
	
	public void setTechnicalFormat(String... formats) {
		if(formats != null && formats.length > 0 && formats[0] != null) {
			TechnicalType technical = getTechnical(true);
			clearFromAny("format", technical.getContent());
			for(int i=0; i<formats.length; i++) {
				technical.getContent().add(mdObjectFactory.createFormat(formats[i]));
			}
		}
	}
	
	public String getEducationContext() {
		EducationalType educational = getEducational(true);
		StringBuilder sb = new StringBuilder();
		if(educational != null) {
			ContextType type = getFromAny(ContextType.class, educational.getContent());
			if(type != null && type.getSource() != null && type.getSource().getLangstring() != null
					&& type.getValue() != null && type.getValue().getLangstring() != null) {
				String source = type.getSource().getLangstring().getValue();
				String value = type.getValue().getLangstring().getValue();
				if(StringHelper.containsNonWhitespace(source) && StringHelper.containsNonWhitespace(value)) {
					sb.append(value);
				}
			}
		}
		return sb.length() == 0 ? null: sb.toString();
	}
	
	public void setEducationalContext(String context, String lang) {
		if(StringHelper.containsNonWhitespace(context)) {
			EducationalType educational = getEducational(true);
			if(educational != null) {
				ContextType type = getFromAny(ContextType.class, educational.getContent());
				if(type == null) {
					type = mdObjectFactory.createContextType();
					educational.getContent().add(mdObjectFactory.createContext(type));
				}
				SourceType sourceType = mdObjectFactory.createSourceType();
				sourceType.setLangstring(createString("https://www.openolat.org", lang));
				ValueType valueType = mdObjectFactory.createValueType();
				valueType.setLangstring(createString(context, lang));
				type.setSource(sourceType);
				type.setValue(valueType);
			}
		} else {
			EducationalType educational = getEducational(false);
			if(educational != null) {
				clearFromAny(ContextType.class, educational.getContent());
			}
		}
	}
	
	public String getEducationalLearningTime() {
		EducationalType educational = getEducational(true);
		if(educational != null) {
			TypicallearningtimeType type = getFromAny(TypicallearningtimeType.class, educational.getContent());
			if(type != null) {
				return type.getDatetime();
			}
		}
		return null;
	}
	
	public void setEducationalLearningTime(String datetime) {
		EducationalType educational = getEducational(true);
		if(educational != null) {
			TypicallearningtimeType type = getFromAny(TypicallearningtimeType.class, educational.getContent());
			if(type == null) {
				type = mdObjectFactory.createTypicallearningtimeType();
				educational.getContent().add(mdObjectFactory.createTypicallearningtime(type));
			}
			type.setDatetime(datetime);
		}
	}
	
	public String getLicense() {
		RightsType rights = getRights(true);
		if(rights != null) {
			CopyrightandotherrestrictionsType type = getFromAny(CopyrightandotherrestrictionsType.class, rights.getContent());
			if(type != null && type.getValue() != null && type.getValue().getLangstring() != null) {
				return type.getValue().getLangstring().getValue();
			}	
		}
		return null;
	}
	
	public void setLicense(String license) {
		RightsType rights = getRights(true);
		CopyrightandotherrestrictionsType type = getFromAny(CopyrightandotherrestrictionsType.class, rights.getContent());
		if(type == null) {
			type = mdObjectFactory.createCopyrightandotherrestrictionsType();
			rights.getContent().add(mdObjectFactory.createCopyrightandotherrestrictions(type));
		}
		SourceType sourceType = mdObjectFactory.createSourceType();
		sourceType.setLangstring(createString("https://www.openolat.org", "en"));
		ValueType valueType = mdObjectFactory.createValueType();
		valueType.setLangstring(createString(license, "en"));
		type.setSource(sourceType);
		type.setValue(valueType);
	}
	
	public String getClassificationTaxonomy() {
		StringBuilder sb = new StringBuilder();
		ClassificationType classification = getClassification("discipline", null, false);
		if(classification != null) {
			TaxonpathType taxonpath = getFromAny(TaxonpathType.class, classification.getContent());
			if(taxonpath != null) {
				List<TaxonType> taxons = taxonpath.getTaxon();
				if(taxons != null) {
					for(TaxonType taxon:taxons) {
						if(taxon.getEntry() != null && !taxon.getEntry().getLangstring().isEmpty()) {
							LangstringType value = taxon.getEntry().getLangstring().get(0);
							if(value != null && value.getValue() != null) {
								sb.append("/").append(value.getValue());
							}
						}
					}
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	/**
	 * Set a taxonomy path of purpose "discipline"
	 * @param taxonomyPath
	 * @param lang
	 */
	public void setClassificationTaxonomy(String taxonomyPath, String lang) {
		if(StringHelper.containsNonWhitespace(taxonomyPath)) {
			ClassificationType classification = getClassification("discipline", lang, true);
			if(classification != null) {
				TaxonpathType taxonpathType = mdObjectFactory.createTaxonpathType();
				clearFromAny(TaxonpathType.class, classification.getContent());
				classification.getContent().add(mdObjectFactory.createTaxonpath(taxonpathType));
				taxonpathType.getTaxon().clear();
				
				SourceType sourceType = mdObjectFactory.createSourceType();
				sourceType.setLangstring(createString("Unkown", "en"));
				taxonpathType.setSource(sourceType);
				
				for(StringTokenizer tokenizer = new StringTokenizer(taxonomyPath, "/"); tokenizer.hasMoreTokens(); ) {
					String level = tokenizer.nextToken();
					
					TaxonType taxonType = mdObjectFactory.createTaxonType();
					EntryType entryType = mdObjectFactory.createEntryType();
					createOrUpdateFirstLangstring(entryType.getLangstring(), level, lang);
					taxonType.setEntry(entryType);
					taxonpathType.getTaxon().add(taxonType);
				}
			}
		} else {
			ClassificationType classification = getClassification("discipline", lang, false);
			if(classification != null) {
				clearFromAny(TaxonpathType.class, classification.getContent());
			}
		}
	}

	public void createOrUpdateFirstLangstring(List<LangstringType> langStrings, String value, String lang) {
		if(langStrings.isEmpty()) {
			langStrings.add(createString(value, lang));
		} else {
			langStrings.get(0).setValue(value);
			langStrings.get(0).setLang(lang);
		}
	}
	
	public LangstringType createString(String value, String lang) {
		LangstringType string = mdObjectFactory.createLangstringType();
		string.setLang(lang);
		string.setValue(value);
		return string;
	}
	
	public SourceType createSource(String value, String lang) {
		SourceType sourceType = mdObjectFactory.createSourceType();
		sourceType.setLangstring(createString(value, lang));
		return sourceType;
	}
	
	public ValueType createValue(String value, String lang) {
		ValueType valueType = mdObjectFactory.createValueType();
		valueType.setLangstring(createString(value, lang));
		return valueType;
	}
	
	public String getFirstString(List<LangstringType> langStrings) {
		String firstString = null;
		if(langStrings != null && !langStrings.isEmpty()) {
			firstString = langStrings.get(0).getValue();
		}
		return firstString;
	}
	
	public RightsType getRights(boolean create) {
		LomType lom = getLom(create);
		if(lom == null) return null;
		
		RightsType rights = lom.getRights();
		if(rights == null && create) {
			rights = mdObjectFactory.createRightsType();
			lom.setRights(rights);
		}
		return rights;
	}
	
	public ClassificationType getClassification(String purpose, String lang, boolean create) {
		LomType lom = getLom(create);
		if(lom == null) return null;

		ClassificationType classification = null;
		List<ClassificationType> classifications = lom.getClassification();
		for(ClassificationType cl:classifications) {
			PurposeType purposeType = getFromAny(PurposeType.class, cl.getContent());
			if(purposeType != null && purposeType.getValue() != null && purposeType.getValue().getLangstring() != null) {
				String value = purposeType.getValue().getLangstring().getValue();
				if(value != null && value.equals(purpose)) {
					classification = cl;
					break;
				}
			}	
		}
		
		if(classification == null && create) {
			classification = mdObjectFactory.createClassificationType();
			PurposeType purposeType = mdObjectFactory.createPurposeType();
			purposeType.setSource(createSource("LOMv1.0", lang));
			purposeType.setValue(createValue(purpose, lang));
			classification.getContent().add(mdObjectFactory.createPurpose(purposeType));
			lom.getClassification().add(classification);
		}
		return classification;
	}
	
	public TechnicalType getTechnical(boolean create) {
		LomType lom = getLom(create);
		if(lom == null) return null;
		
		TechnicalType technical = lom.getTechnical();
		if(technical == null && create) {
			technical = mdObjectFactory.createTechnicalType();
			lom.setTechnical(technical);
		}
		return technical;
	}

	public GeneralType getGeneral(boolean create) {
		LomType lom = getLom(create);
		if(lom == null) return null;
		
		GeneralType general = lom.getGeneral();
		if(general == null && create) {
			general = mdObjectFactory.createGeneralType();
			lom.setGeneral(general);
		}
		return general;
	}
	
	public EducationalType getEducational(boolean create) {
		LomType lom = getLom(create);
		if(lom == null) return null;
		
		EducationalType educational = lom.getEducational();
		if(educational == null && create) {
			educational = mdObjectFactory.createEducationalType();
			lom.setEducational(educational);
		}
		return educational;
	}
	
	public String getLifecycleVersion() {
		LifecycleType lifecycle = getLifecycle(false);
		if(lifecycle != null) {
			VersionType type = getFromAny(VersionType.class, lifecycle.getContent());
			if(type != null) {
				return getFirstString(type.getLangstring());
			}
		}
		return null;
	}
	
	public void setLifecycleVersion(String version) {
		LifecycleType lifecycle = getLifecycle(true);
		VersionType type = getFromAny(VersionType.class, lifecycle.getContent());
		if(type == null) {
			type = mdObjectFactory.createVersionType();
			lifecycle.getContent().add(mdObjectFactory.createVersion(type));
		}
		createOrUpdateFirstLangstring(type.getLangstring(), version, "en");
	}
	
	public String getLifecycleStatus() {
		LifecycleType lifecycle = getLifecycle(false);
		if(lifecycle != null) {
			StatusType status = getFromAny(StatusType.class, lifecycle.getContent());
			if(status != null && status.getValue() != null && status.getValue().getLangstring() != null) {
				return status.getValue().getLangstring().getValue();
			}
		}
		return null;
	}
	
	public LifecycleType getLifecycle(boolean create) {
		LomType lom = getLom(create);
		if(lom == null) return null;
		
		LifecycleType lifecycle = lom.getLifecycle();
		if(lifecycle == null && create) {
			lifecycle = mdObjectFactory.createLifecycleType();
			lom.setLifecycle(lifecycle);
		}
		return lifecycle;
	}
	
	public LomType getLom(boolean create) {
		LomType lom = getFromAny(LomType.class, getMetadataList());
		if(lom == null && create) {
			lom = mdObjectFactory.createLomType();
			getMetadataList().add(mdObjectFactory.createLom(lom));
		}
        return lom;
	}
	
	/**
	 * Return the openolat metadata if it exists or, if specified, create
	 * one and append it to the metadata of the resource.
	 * 
	 * @param resource The resource with the metadata
	 * @param create True create the qtiMetadata
	 * @return
	 */
	public OpenOLATMetadataType getOpenOLATMetadata(boolean create) {
		List<Object> anyMetadataList = getMetadataList();
		OpenOLATMetadataType ooMetadata = null;
		for(Object anyMetadata:anyMetadataList) {
			if(anyMetadata instanceof JAXBElement<?>
				&& ((JAXBElement<?>)anyMetadata).getValue() instanceof OpenOLATMetadataType) {
				ooMetadata = (OpenOLATMetadataType)((JAXBElement<?>)anyMetadata).getValue();
			}
		}
		
		if(ooMetadata == null && create) {
			ooMetadata = ooObjectFactory.createOpenOLATMetadataType();
			getMetadataList().add(ooObjectFactory.createOoMetadata(ooMetadata));
		}
		return ooMetadata;
	}
	
	public String getOpenOLATMetadataQuestionType() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getQuestionType();
	}
	
	public void setOpenOLATMetadataQuestionType(String questionType) {
		getOpenOLATMetadata(true).setQuestionType(questionType);
	}
	
	public String getOpenOLATMetadataIdentifier() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getQpoolIdentifier();
	}
	
	public void setOpenOLATMetadataIdentifier(String identifier) {
		getOpenOLATMetadata(true).setQpoolIdentifier(identifier);
	}
	
	public String getOpenOLATMetadataMasterIdentifier() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getMasterIdentifier();
	}
	
	public void setOpenOLATMetadataMasterIdentifier(String masterIdentifier) {
		getOpenOLATMetadata(true).setMasterIdentifier(masterIdentifier);
	}
	
	public Double getOpenOLATMetadataDifficulty() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getDifficulty();
	}
	
	public void setOpenOLATMetadataDifficulty(Double difficulty) {
		getOpenOLATMetadata(true).setDifficulty(difficulty);
	}
	
	public void setOpenOLATMetadataDifficulty(BigDecimal difficulty) {
		if(difficulty == null) {
			setOpenOLATMetadataDifficulty((Double)null);
		} else {
			setOpenOLATMetadataDifficulty(Double.valueOf(difficulty.doubleValue()));
		}
	}
	
	public Double getOpenOLATMetadataDiscriminationIndex() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getDiscriminationIndex();
	}
	
	public void setOpenOLATMetadataDiscriminationIndex(Double discriminationIndex) {
		getOpenOLATMetadata(true).setDiscriminationIndex(discriminationIndex);
	}
	
	public void setOpenOLATMetadataDiscriminationIndex(BigDecimal discriminationIndex) {
		if(discriminationIndex == null) {
			setOpenOLATMetadataDiscriminationIndex((Double)null);
		} else {
			setOpenOLATMetadataDiscriminationIndex(Double.valueOf(discriminationIndex.doubleValue()));
		}
	}
	
	public Integer getOpenOLATMetadataDistractors() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getDistractors();
	}
	
	public void setOpenOLATMetadataDistractors(Integer distractors) {
		getOpenOLATMetadata(true).setDistractors(distractors);
	}
	
	public Double getOpenOLATMetadataStandardDeviation() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getStandardDeviation();
	}
	
	public void setOpenOLATMetadataStandardDeviation(Double standardDeviation) {
		getOpenOLATMetadata(true).setStandardDeviation(standardDeviation);
	}
	
	public void setOpenOLATMetadataStandardDeviation(BigDecimal standardDeviation) {
		if(standardDeviation == null) {
			setOpenOLATMetadataStandardDeviation((Double)null);
		} else {
			setOpenOLATMetadataStandardDeviation(Double.valueOf(standardDeviation.doubleValue()));
		}
	}
	
	public Integer getOpenOLATMetadataUsage() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getUsage();
	}
	
	public void setOpenOLATMetadataUsage(Integer usage) {
		getOpenOLATMetadata(true).setUsage(usage);
	}
	
	public String getOpenOLATMetadataAssessmentType() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getAssessmentType();
	}
	
	public void setOpenOLATMetadataAssessmentType(String type) {
		getOpenOLATMetadata(true).setAssessmentType(type);
	}
	
	public Date getOpenOLATMetadataCopiedAt() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		if(ooMetadata != null && ooMetadata.getCopiedAt() != null) {
			return ooMetadata.getCopiedAt().toGregorianCalendar().getTime();
		}
		return null;
	}
	
	public void setOpenOLATMetadataCopiedAt(Date date) {
		try {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			getOpenOLATMetadata(true).setCopiedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
		} catch (DatatypeConfigurationException e) {
			log.error("", e);
		}
	}
	
	public String getOpenOLATMetadataCreator() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getCreator();
	}
	
	public void setOpenOLATMetadataCreator(String creator) {
		getOpenOLATMetadata(true).setCreator(creator);
	}
	
	public String getOpenOLATMetadataTopic() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getTopic();
	}
	
	public void setOpenOLATMetadataTopic(String topic) {
		getOpenOLATMetadata(true).setTopic(topic);
	}
	
	public String getOpenOLATMetadataAdditionalInformations() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getAdditionalInformations();
	}
	
	public void setOpenOLATMetadataAdditionalInformations(String informations) {
		getOpenOLATMetadata(true).setAdditionalInformations(informations);
	}
	
	public Integer getOpenOLATMetadataCorrectionTime() {
		OpenOLATMetadataType ooMetadata = getOpenOLATMetadata(false);
		return ooMetadata == null ? null : ooMetadata.getCorrectionTime();
	}
	
	public void setOpenOLATMetadataCorrectionTime(Integer timeInMinute) {
		getOpenOLATMetadata(true).setCorrectionTime(timeInMinute);
	}
	
	/**
	 * Return the qti metadata if it exists or if specified, create
	 * one and append it to the metadata of the resource.
	 * 
	 * @param resource The resource with the metadata
	 * @param create True create the qtiMetadata
	 * @return
	 */
	public QTIMetadataType getQtiMetadata(boolean create) {
		List<Object> anyMetadataList = getMetadataList();
		QTIMetadataType qtiMetadata = null;
		for(Object anyMetadata:anyMetadataList) {
			if(anyMetadata instanceof JAXBElement<?>
				&& ((JAXBElement<?>)anyMetadata).getValue() instanceof QTIMetadataType) {
				qtiMetadata = (QTIMetadataType)((JAXBElement<?>)anyMetadata).getValue();
			}
		}
		
		if(qtiMetadata == null && create) {
			qtiMetadata = qtiObjectFactory.createQTIMetadataType();
			getMetadataList().add(qtiObjectFactory.createQtiMetadata(qtiMetadata));
		}
		return qtiMetadata;
	}
	
	public String getQtiMetadataToolName() {
		QTIMetadataType qtiMetadata = getQtiMetadata(false);
		return qtiMetadata == null ? null : qtiMetadata.getToolName();
	}
	
	public String getQtiMetadaToolVendor() {
		QTIMetadataType qtiMetadata = getQtiMetadata(false);
		return qtiMetadata == null ? null : qtiMetadata.getToolVendor();
	}
	
	public String getQtiMetadataToolVersion() {
		QTIMetadataType qtiMetadata = getQtiMetadata(false);
		return qtiMetadata == null ? null : qtiMetadata.getToolVersion();
	}
	
	public void setQtiMetadataTool(String toolName, String toolVendor, String toolVersion) {
		QTIMetadataType qtiMetadata = getQtiMetadata(true);
		qtiMetadata.setToolName(toolName);
		qtiMetadata.setToolVendor(toolVendor);
		qtiMetadata.setToolVersion(toolVersion);
	}
	
	public void setQtiMetadataInteractionTypes(List<String> interactions) {
		QTIMetadataType qtiMetadata = getQtiMetadata(true);
		
		qtiMetadata.getInteractionType().clear();
		for(String interaction:interactions) {
			qtiMetadata.getInteractionType().add(interaction);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <U> U getFromAny(Class<U> type, List<Object> anyList) {
		U object = null;
		for(Object any:anyList) {
			if(any instanceof JAXBElement<?>
				&& ((JAXBElement<?>)any).getValue().getClass().equals(type)) {
				object = (U)((JAXBElement<?>)any).getValue();
			}
		}
		return object;
	}

	private void clearFromAny(String type, List<Object> anyList) {
		for(Iterator<Object> anyIterator=anyList.iterator(); anyIterator.hasNext(); ) {
			Object any = anyIterator.next();
			if(any instanceof JAXBElement<?>
				&& ((JAXBElement<?>)any).getName().getLocalPart().equals(type)) {
				anyIterator.remove();
			}
		}
	}
	
	private void clearFromAny(Class<?> type, List<Object> anyList) {
		for(Iterator<Object> anyIterator=anyList.iterator(); anyIterator.hasNext(); ) {
			Object any = anyIterator.next();
			if(any instanceof JAXBElement<?>
				&& ((JAXBElement<?>)any).getValue().getClass().equals(type)) {
				anyIterator.remove();
			}
		}
	}
	
	public List<Object> getMetadataList() {
		return metadata == null ? manifestMetadata.getAny() : metadata.getAny();
	}
	
	public void appendMetadataFrom(QuestionItem item, ResolvedAssessmentItem resolvedAssessmentItem, Locale locale) {
		AssessmentItem assessmentItem = null;
		if(resolvedAssessmentItem != null) {
			assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		}
		appendMetadataFrom(item, assessmentItem, locale);
	}
	
	/**
	 * This method will add new metadata to the current ones.
	 * 
	 * @param item
	 * @param locale
	 */
	public void appendMetadataFrom(QuestionItem item, AssessmentItem assessmentItem, Locale locale) {
		String lang = item.getLanguage();
		if(!StringHelper.containsNonWhitespace(lang)) {
			lang = locale.getLanguage();
		}

		//LOM : General
		if(StringHelper.containsNonWhitespace(item.getTitle())) {
			setTitle(item.getTitle(), lang);
		}
		if(StringHelper.containsNonWhitespace(item.getCoverage())) {
			setCoverage(item.getCoverage(), lang);
		}
		if(StringHelper.containsNonWhitespace(item.getKeywords())) {
			setGeneralKeywords(item.getKeywords(), lang);
		}
		if(StringHelper.containsNonWhitespace(item.getDescription())) {
			setDescription(item.getDescription(), lang);
		}
		//LOM : Technical
		setTechnicalFormat(ManifestBuilder.ASSESSMENTITEM_MIMETYPE);
		//LOM : Educational
		if(StringHelper.containsNonWhitespace(item.getEducationalContextLevel())) {
			setEducationalContext(item.getEducationalContextLevel(), lang);
		}
		if(StringHelper.containsNonWhitespace(item.getEducationalLearningTime())) {
			setEducationalLearningTime(item.getEducationalLearningTime());
		}
		if(item.getLanguage() != null) {
			setLanguage(item.getLanguage(), lang);
		}
		//LOM : Lifecycle
		if(StringHelper.containsNonWhitespace(item.getItemVersion())) {
			setLifecycleVersion(item.getItemVersion());
		}
		//LOM : Rights
		LicenseService lService = CoreSpringFactory.getImpl(LicenseService.class);
		ResourceLicense license = lService.loadLicense(item);
		if(license != null) {
			String licenseText = null;
			LicenseType licenseType = license.getLicenseType();
			if (lService.isFreetext(licenseType)) {
				licenseText = license.getFreetext();
			} else if (!lService.isNoLicense(licenseType)) {
				licenseText = license.getLicenseType().getName();
			}
			if (StringHelper.containsNonWhitespace(licenseText)) {
				setLicense(licenseText);
			}
			setOpenOLATMetadataCreator(license.getLicensor());
		}
		//LOM : classification
		if(StringHelper.containsNonWhitespace(item.getTaxonomicPath())) {
			setClassificationTaxonomy(item.getTaxonomicPath(), lang);
		}
		
		// QTI 2.1
		setQtiMetadataTool(item.getEditor(), null, item.getEditorVersion());
		
		if(assessmentItem != null) {
			List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
			List<String> interactionNames = new ArrayList<>(interactions.size());
			for(Interaction interaction:interactions) {
				interactionNames.add(interaction.getQtiClassName());
			}
			setQtiMetadataInteractionTypes(interactionNames);
		}
		
		// OpenOLAT
		if(item.getType() != null) {
			setOpenOLATMetadataQuestionType(item.getType().getType());
		} else {
			setOpenOLATMetadataQuestionType(null);
		}
		setOpenOLATMetadataIdentifier(item.getIdentifier());
		setOpenOLATMetadataDifficulty(item.getDifficulty());
		setOpenOLATMetadataDiscriminationIndex(item.getDifferentiation());
		setOpenOLATMetadataDistractors(item.getNumOfAnswerAlternatives());
		setOpenOLATMetadataStandardDeviation(item.getStdevDifficulty());
		setOpenOLATMetadataUsage(item.getUsage());
		setOpenOLATMetadataAssessmentType(item.getAssessmentType());
		setOpenOLATMetadataTopic(item.getTopic());
		setOpenOLATMetadataAdditionalInformations(item.getAdditionalInformations());
		setOpenOLATMetadataCorrectionTime(item.getCorrectionTime());
	}
}
