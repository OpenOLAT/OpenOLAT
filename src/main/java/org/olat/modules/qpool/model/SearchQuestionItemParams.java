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
package org.olat.modules.qpool.model;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 28.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchQuestionItemParams implements Cloneable {
	
	private Collection<Long> itemKeys;
	
	
	private Long poolKey;
	private String format;
	private Identity author;
	private String title;
	private String topic;
	private String owner;
	private String keywords;
	private String coverage;
	private String informations;
	private String language;
	private String assessmentType;
	private String searchString;
	private boolean favoritOnly;
	private OLATResource resource;
	private LicenseType licenseType;
	private QEducationalContext level;
	private QuestionItemCollection collection;
	private List<QItemType> excludedItemTypes;
	
	private QItemType itemType;
	private TaxonomyLevel taxonomyLevel;
	private TaxonomyLevel likeTaxonomyLevel;
	private QuestionStatus questionStatus;
	private Identity onlyAuthor;
	private Identity excludeAuthor;
	private Identity excludeRater;
	
	private boolean withoutTaxonomyLevelOnly;
	private boolean withoutAuthorOnly;
	
	private final Identity identity;
	private final Roles roles;
	private final Locale locale;
	
	public SearchQuestionItemParams() {
		this(null, null, null);
	}
	
	public SearchQuestionItemParams(Identity identity, Roles roles, Locale locale) {
		this.identity = identity;
		this.roles = roles;
		this.locale = locale;
	}

	public Long getPoolKey() {
		return poolKey;
	}

	public void setPoolKey(Long poolKey) {
		this.poolKey = poolKey;
	}

	public Collection<Long> getItemKeys() {
		return itemKeys;
	}

	public void setItemKeys(Collection<Long> itemKeys) {
		this.itemKeys = itemKeys;
	}

	public QuestionItemCollection getCollection() {
		return collection;
	}

	public void setCollection(QuestionItemCollection collection) {
		this.collection = collection;
	}

	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	public QItemType getItemType() {
		return itemType;
	}

	public void setItemType(QItemType itemType) {
		this.itemType = itemType;
	}

	public List<QItemType> getExcludedItemTypes() {
		return excludedItemTypes;
	}

	public void setExcludedItemTypes(List<QItemType> excludedItemTypes) {
		this.excludedItemTypes = excludedItemTypes;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
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

	public String getInformations() {
		return informations;
	}

	public void setInformations(String informations) {
		this.informations = informations;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isFavoritOnly() {
		return favoritOnly;
	}

	public void setFavoritOnly(boolean favoritOnly) {
		this.favoritOnly = favoritOnly;
	}

	public Identity getAuthor() {
		return author;
	}

	public void setAuthor(Identity author) {
		this.author = author;
	}
	
	public TaxonomyLevel getLikeTaxonomyLevel() {
		return likeTaxonomyLevel;
	}

	public void setLikeTaxonomyLevel(TaxonomyLevel likeTaxonomyLevel) {
		this.likeTaxonomyLevel = likeTaxonomyLevel;
	}

	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
	}

	public String getAssessmentType() {
		return assessmentType;
	}

	public void setAssessmentType(String assessmentType) {
		this.assessmentType = assessmentType;
	}

	public QuestionStatus getQuestionStatus() {
		return questionStatus;
	}

	public void setQuestionStatus(QuestionStatus questionStatus) {
		this.questionStatus = questionStatus;
	}

	public Identity getOnlyAuthor() {
		return onlyAuthor;
	}

	public void setOnlyAuthor(Identity onlyAuthor) {
		this.onlyAuthor = onlyAuthor;
	}

	public Identity getExcludeAuthor() {
		return excludeAuthor;
	}

	public void setExcludeAuthor(Identity excludeAuthor) {
		this.excludeAuthor = excludeAuthor;
	}

	public Identity getExcludeRater() {
		return excludeRater;
	}

	public void setExcludeRated(Identity excludeRater) {
		this.excludeRater = excludeRater;
	}

	public boolean isWithoutTaxonomyLevelOnly() {
		return withoutTaxonomyLevelOnly;
	}

	public void setWithoutTaxonomyLevelOnly(boolean withoutTaxonomyLevelOnly) {
		this.withoutTaxonomyLevelOnly = withoutTaxonomyLevelOnly;
	}

	public boolean isWithoutAuthorOnly() {
		return withoutAuthorOnly;
	}

	public void setWithoutAuthorOnly(boolean withoutAuthorOnly) {
		this.withoutAuthorOnly = withoutAuthorOnly;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	

	public LicenseType getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	public QEducationalContext getLevel() {
		return level;
	}

	public void setLevel(QEducationalContext level) {
		this.level = level;
	}

	public Identity getIdentity() {
		return identity;
	}
	
	public Roles getRoles() {
		return roles;
	}
	
	public SearchQuestionItemParams copy() {
		SearchQuestionItemParams clone = new SearchQuestionItemParams(identity, roles, locale);
		return enrich(clone);
	}
	
	public SearchQuestionItemParams enrich(SearchQuestionItemParams clone) {
		if(poolKey != null) {
			clone.poolKey = poolKey;
		}
		if(StringHelper.containsNonWhitespace(format)) {
			clone.format = format;
		}
		if(StringHelper.containsNonWhitespace(title)) {
			clone.title = title;
		}
		if(StringHelper.containsNonWhitespace(coverage)) {
			clone.coverage = coverage;
		}
		if(StringHelper.containsNonWhitespace(keywords)) {
			clone.keywords = keywords;
		}
		if(StringHelper.containsNonWhitespace(language)) {
			clone.language = language;
		}
		if(StringHelper.containsNonWhitespace(topic)) {
			clone.topic = topic;
		}
		if(StringHelper.containsNonWhitespace(informations)) {
			clone.informations = informations;
		}
		if(StringHelper.containsNonWhitespace(searchString)) {
			clone.searchString = searchString;
		}
		if(StringHelper.containsNonWhitespace(owner)) {
			clone.owner = owner;
		}
		if(favoritOnly) {
			clone.favoritOnly = favoritOnly;
		}
		if(author != null) {
			clone.author = author;
		}
		if(itemType != null) {
			clone.itemType = itemType;
		}
		if(excludedItemTypes != null) {
			clone.excludedItemTypes = excludedItemTypes;
		}
		if(taxonomyLevel != null) {
			clone.taxonomyLevel = taxonomyLevel;
		}
		if(likeTaxonomyLevel != null) {
			clone.likeTaxonomyLevel = likeTaxonomyLevel;
		}
		if(questionStatus != null) {
			clone.questionStatus = questionStatus;
		}
		if(onlyAuthor != null) {
			clone.onlyAuthor = onlyAuthor;
		}
		if(excludeAuthor != null) {
			clone.excludeAuthor = excludeAuthor;
		}
		if(excludeRater != null) {
			clone.excludeRater = excludeRater;
		}
		if(withoutTaxonomyLevelOnly) {
			clone.withoutTaxonomyLevelOnly = withoutTaxonomyLevelOnly;
		}
		if(withoutAuthorOnly) {
			clone.withoutAuthorOnly = withoutAuthorOnly;
		}
		if(licenseType != null) {
			clone.licenseType = licenseType;
		}
		if(level != null) {
			clone.level = level;
		}
		if(assessmentType != null) {
			clone.assessmentType = assessmentType;
		}
		return clone;
	}

}
