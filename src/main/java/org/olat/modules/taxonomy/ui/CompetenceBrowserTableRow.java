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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * Initial date: 29.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetenceBrowserTableRow implements FlexiTreeTableNode {

	private CompetenceBrowserTableRow parent;
	private boolean hasChildren;
	
	private String rootCrumb;
	private Taxonomy taxonomy;
	private TaxonomyLevel taxonomyLevel;
	private String displayName;
	private String description;
	
	private FormLink detailsLink;
	
	public CompetenceBrowserTableRow(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}
	
	public CompetenceBrowserTableRow(Taxonomy taxonomy, TaxonomyLevel taxonomyLevel, String displayName, String description) {
		this.taxonomy = taxonomy;
		this.taxonomyLevel = taxonomyLevel;
		this.displayName = displayName;
		this.description = description;
	}
	
	public CompetenceBrowserTableRow(String rootCrumb) {
		this.rootCrumb = rootCrumb;
	}
	
	public String getTaxonomyOrLevel() {
		if (taxonomyLevel != null) {
			return displayName;
		}
		return taxonomy.getDisplayName();
	}
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}
	
	public FormLink getDetailsLink() {
		return detailsLink;
	}
	
	public void setDetailsLink(FormLink detailsLink) {
		this.detailsLink = detailsLink;
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}
	
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	public boolean containsSearch(String search) {
		if (taxonomyLevel != null) {
			return displayName.toLowerCase().contains(search) 
					|| taxonomyLevel.getIdentifier().toLowerCase().contains(search);
		} else if (taxonomy != null) {
			return taxonomy.getDisplayName().toLowerCase().contains(search) 
					|| taxonomy.getIdentifier().toLowerCase().contains(search);
		}
		
		return false;
	}
	
	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}
	
	public void setParent(CompetenceBrowserTableRow parent) {
		this.parent = parent;
		if(parent != null) {
			parent.setHasChildren(true);
		}
	}

	@Override
	public String getCrump() {
		if (taxonomyLevel != null) {
			return taxonomyLevel.getIdentifier();
		}
		if (taxonomy!= null) {
			return taxonomy.getIdentifier();
		}
		if (StringHelper.containsNonWhitespace(rootCrumb)) {
			return rootCrumb;
		}
		return null;
	}
	
	public Long getKey() {
		if (taxonomyLevel != null) {
			return taxonomyLevel.getKey();
		} else if (taxonomy != null) {
			return taxonomy.getKey();
		} else {
			return null;
		}
	}
	
	public String getDescription() {
		if (taxonomyLevel != null) {
			return description;
		} else if (taxonomy != null) {
			return taxonomy.getDescription();
		} else {
			return null;
		}
	}
	
	public String getIdentifier() {
		if (taxonomyLevel != null) {
			return taxonomyLevel.getIdentifier();
		} else if (taxonomy != null) {
			return taxonomy.getIdentifier();
		} else {
			return null;
		}
	}
	
	public String getExternalId() {
		if (taxonomyLevel != null) {
			return taxonomyLevel.getExternalId();
		} else if (taxonomy != null) {
			return taxonomy.getExternalId();
		} else {
			return null;
		}
	}
	
	public String getDisplayName() {
		if (taxonomyLevel != null) {
			return displayName;
		} else if (taxonomy != null) {
			return taxonomy.getDisplayName();
		} else {
			return null;
		}
	}
}
