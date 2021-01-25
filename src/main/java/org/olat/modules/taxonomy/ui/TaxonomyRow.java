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
package org.olat.modules.taxonomy.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyInfos;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyRow implements TaxonomyRef {
	
	private final TaxonomyInfos taxonomy;
	private final boolean documentPoolEnabled;
	private final boolean questionPoolEnabled;
	
	private final FormLink openLink;
	private final FormLink repoLink;
	private final FormLink docPoolLink;
	private final FormLink qPoolLink;
	private final FormLink ePortfolioLink;
	
	public TaxonomyRow(TaxonomyInfos taxonomy, boolean documentPoolEnabled, boolean questionPoolEnabled,
			FormLink openLink, FormLink repoLink, FormLink docPoolLink, FormLink qPoolLink, FormLink ePortfolioLink) {
		this.taxonomy = taxonomy;
		this.openLink = openLink;
		this.repoLink = repoLink;
		this.qPoolLink = qPoolLink;
		this.docPoolLink = docPoolLink;
		this.ePortfolioLink = ePortfolioLink;
		this.documentPoolEnabled = documentPoolEnabled;
		this.questionPoolEnabled = questionPoolEnabled;
	}

	@Override
	public Long getKey() {
		return taxonomy.getKey();
	}
	
	public Date getCreationDate() {
		return taxonomy.getCreationDate();
	}
	
	public String getIdentifier() {
		return taxonomy.getIdentifier();
	}
	
	public String getDisplayName() {
		return taxonomy.getDisplayName();
	}
	
	public String getDescription() {
		return taxonomy.getDescription();
	}
	
	public int getNumberOfLevels() {
		return taxonomy.getNumOfLevels();
	}
	
	public FormLink getOpenLink() {
		return openLink;
	}
	
	public FormLink getRepoLink() {
		return repoLink;
	}

	public FormLink getDocPoolLink() {
		return docPoolLink;
	}

	public FormLink getQPoolLink() {
		return qPoolLink;
	}
	
	public FormLink getEPortfolioLink() {
		return ePortfolioLink;
	}

	public boolean isDocumentPoolEnabled() {
		return documentPoolEnabled;
	}
	
	public boolean isQuestionPoolEnabled() {
		return questionPoolEnabled;
	}
}
