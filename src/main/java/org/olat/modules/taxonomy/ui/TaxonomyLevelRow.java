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
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyLevelType;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelRow implements TaxonomyLevelRef, FlexiTreeTableNode {

	private final TaxonomyLevelType type;
	private final TaxonomyLevel taxonomyLevel;

	private final String language;
	private final String displayName;
	private final String description;
	private final Long parentLevelKey;
	
	// Only used in import wizard
	private final boolean isUpdated;
	private final boolean hasMultipleLangs;
	private boolean hasBackgroundImage;
	private boolean hasTeaserImage;
	
	private int numOfChildren = 0;
	private FormLink toolsLink;
	private TaxonomyLevelRow parent;
	
	public TaxonomyLevelRow(TaxonomyLevel taxonomyLevel, String language, String displayName, String description, FormLink toolsLink) {
		this(taxonomyLevel, language, displayName, description, toolsLink, false, false);
	}
	
	public TaxonomyLevelRow(TaxonomyLevel taxonomyLevel, String language, String displayName, String description,
							boolean isUpdated, boolean hasMultipleLangs) {
		this(taxonomyLevel, language, displayName, description, null, isUpdated, hasMultipleLangs);
	}
	
	public TaxonomyLevelRow(TaxonomyLevel taxonomyLevel, String language, String displayName, String description, FormLink toolsLink,
							boolean isUpdated, boolean hasMultipleLangs) {
		this.taxonomyLevel = taxonomyLevel;
		this.language = language;
		this.displayName = displayName;
		this.description = description;
		this.toolsLink = toolsLink;
		this.isUpdated = isUpdated;
		this.hasMultipleLangs = hasMultipleLangs;
		parentLevelKey = taxonomyLevel.getParent() == null ? null : taxonomyLevel.getParent().getKey();
		type = taxonomyLevel.getType();
	}
	
	@Override
	public Long getKey() {
		return taxonomyLevel.getKey();
	}

	@Override
	public TaxonomyLevelRow getParent() {
		return parent;
	}
	
	@Override
	public String getCrump() {
		return getDisplayName();
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}
	
	public TaxonomyLevelType getTaxonomyLevelType() {
		return type;
	}
	
	public TaxonomyLevelManagedFlag[] getManagedFlags() {
		return taxonomyLevel.getManagedFlags();
	}

	public Long getParentLevelKey() {
		return parentLevelKey;
	}
	
	public void setParent(TaxonomyLevelRow parent) {
		this.parent = parent;
	}

	public String getLanguage() {
		return language;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public Integer getSortOrder() {
		return taxonomyLevel.getSortOrder();
	}
	
	public String getIdentifier() {
		return taxonomyLevel.getIdentifier();
	}
	
	public String getExternalId() {
		return taxonomyLevel.getExternalId();
	}
	
	public Long getTypeKey() {
		return type == null ? null : type.getKey();
	}
	
	public String getTypeIdentifier() {
		return type == null ? "" : type.getIdentifier();
	}
	
	public int getNumberOfChildren() {
		return numOfChildren;
	}
	
	public Integer getOrder() {
		return taxonomyLevel.getSortOrder();
	}
	
	public String getDescription() {
		return description;
	}
	
	public void incrementNumberOfChildren() {
		numOfChildren++;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public boolean hasMultipleLangs() {
		return hasMultipleLangs;
	}

	public void setHasBackgroundImage(boolean hasBackgroundImage) {
		this.hasBackgroundImage = hasBackgroundImage;
	}

	public void setHasTeaserImage(boolean hasTeaserImage) {
		this.hasTeaserImage = hasTeaserImage;
	}

	public boolean hasBackgroundImage() {
		return hasBackgroundImage;
	}

	public boolean hasTeaserImage() {
		return hasTeaserImage;
	}
	
	public boolean isUpdated() {
		return isUpdated;
	}
	
	@Override
	public int hashCode() {
		return taxonomyLevel.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TaxonomyLevelRow) {
			TaxonomyLevelRow row = (TaxonomyLevelRow)obj;
			return taxonomyLevel != null && taxonomyLevel.equals(row.taxonomyLevel);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("row[identifier=").append(taxonomyLevel.getIdentifier() == null ? "" : taxonomyLevel.getIdentifier())
		  .append("]").append(super.toString());
		return sb.toString();
	}
}
