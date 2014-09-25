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
package org.olat.ims.qti.questionimport;

import org.olat.ims.qti.editor.beecom.objects.Item;

/**
 * 
 * Initial date: 25.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemAndMetadata {
	
	private final Item item;
	
	private String language;
	private String taxonomyPath;
	private String keywords;
	private String coverage;

	private boolean hasError;
	
	public ItemAndMetadata(Item item) {
		this.item = item;
	}

	public Item getItem() {
		return item;
	}
	
	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getTaxonomyPath() {
		return taxonomyPath;
	}

	public void setTaxonomyPath(String taxonomyPath) {
		this.taxonomyPath = taxonomyPath;
	}

	/**
	 * 
	 * @return The type defined in org.olat.ims.qti.editor.beecom.objects.Question.TYPE_*
	 */
	public int getQuestionType() {
		return item.getQuestion().getType();
	}

	public void setTitle(String title) {
		item.setTitle(title);
	}
	
	public void setDescription(String description) {
		item.setObjectives(description);
	}

	public boolean isHasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}
}
