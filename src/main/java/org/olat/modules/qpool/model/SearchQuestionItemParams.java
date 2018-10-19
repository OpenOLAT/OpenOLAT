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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 28.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchQuestionItemParams {
	
	private Long poolKey;
	private Collection<Long> itemKeys;
	private String format;
	private String searchString;
	private List<String> condQueries;
	
	private boolean favoritOnly;
	private Identity author;
	
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

	public boolean isFulltextSearch() {
		return StringHelper.containsNonWhitespace(searchString) ||
				condQueries != null && !condQueries.isEmpty();
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public List<String> getCondQueries() {
		if(condQueries == null) {
			return new ArrayList<>(1);
		}
		return new ArrayList<>(condQueries);
	}

	public void setCondQueries(List<String> condQueries) {
		this.condQueries = condQueries;
	}

	public Identity getIdentity() {
		return identity;
	}
	
	public Roles getRoles() {
		return roles;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	@Override
	public SearchQuestionItemParams clone() {
		SearchQuestionItemParams clone = new SearchQuestionItemParams(identity, roles, locale);
		clone.poolKey = poolKey;
		clone.format = format;
		clone.searchString = searchString;
		clone.condQueries = getCondQueries();
		clone.favoritOnly = favoritOnly;
		clone.author = author;
		clone.likeTaxonomyLevel = likeTaxonomyLevel;
		clone.questionStatus = questionStatus;
		clone.onlyAuthor = onlyAuthor;
		clone.excludeAuthor = excludeAuthor;
		clone.excludeRater = excludeRater;
		clone.withoutTaxonomyLevelOnly = withoutTaxonomyLevelOnly;
		clone.withoutAuthorOnly = withoutAuthorOnly;
		return clone;
	}

}
