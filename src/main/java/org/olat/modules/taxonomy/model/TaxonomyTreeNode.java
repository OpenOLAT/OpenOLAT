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
package org.olat.modules.taxonomy.model;

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeNode extends GenericTreeNode {

	private static final long serialVersionUID = 1621713861366690591L;
	
	private boolean canRead;
	private boolean canWrite;
	
	private VFSContainer directory;
	private final Taxonomy taxonomy;
	private TaxonomyLevel taxonomyLevel;
	private final TaxonomyTreeNodeType nodeType;
	
	public TaxonomyTreeNode(Taxonomy taxonomy) {
		super();
		this.taxonomy = taxonomy;
		nodeType = TaxonomyTreeNodeType.taxonomy;
	}
	
	public TaxonomyTreeNode(Taxonomy taxonomy, VFSContainer directory, TaxonomyTreeNodeType type) {
		super();
		this.directory = directory;
		this.taxonomy = taxonomy;
		nodeType = type;
	}
	
	public TaxonomyTreeNode(Taxonomy taxonomy, TaxonomyLevel taxonomyLevel, String levelDisplyName, TaxonomyTreeNodeType type) {
		super(taxonomyLevel.getKey().toString());
		setTitle(levelDisplyName);
		this.taxonomy = taxonomy;
		this.taxonomyLevel = taxonomyLevel;
		setUserObject(taxonomyLevel);
		nodeType = type;
	}
	
	public TaxonomyTreeNodeType getType() {
		return nodeType;
	}
	
	public boolean isVisible() {
		return nodeType == TaxonomyTreeNodeType.taxonomy 
				|| nodeType == TaxonomyTreeNodeType.templates
				|| (taxonomyLevel != null && (taxonomyLevel.getType() == null ? true : taxonomyLevel.getType().isVisible()));
	}
	
	public boolean isDocumentsLibraryEnabled() {
		return taxonomyLevel != null && taxonomyLevel.getType() != null
				&& taxonomyLevel.getType().isDocumentsLibraryEnabled()
				&& (taxonomyLevel.getType().isDocumentsLibraryManageCompetenceEnabled()
						|| taxonomyLevel.getType().isDocumentsLibraryTeachCompetenceReadEnabled()
						|| taxonomyLevel.getType().isDocumentsLibraryHaveCompetenceReadEnabled()
						|| taxonomyLevel.getType().isDocumentsLibraryTargetCompetenceReadEnabled());
	}
	
	public boolean isCanRead() {
		return canRead || nodeType == TaxonomyTreeNodeType.templates;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	/**
	 * @return The container of a node of type container.
	 */
	public VFSContainer getDirectory() {
		return directory;
	}

	/**
	 * @return The taxonomy level if the node is of the type taxonomyLevel
	 */
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	public String getIconCssClass() {
		switch(nodeType) {
			case taxonomy: return "o_icon_taxonomy";
			case templates: return "o_icon_taxonomy_templates";
			case lostAndFound: return "o_icon_taxonomy_templates";
			case taxonomyLevel: {
				TaxonomyLevelType type = taxonomyLevel.getType();
				if(type != null && StringHelper.containsNonWhitespace(type.getCssClass())) {
					return type.getCssClass();
				}
				if(getChildCount() > 0) {
					return "o_icon_taxonomy_level";
				}
				return "o_icon_taxonomy_level_leaf";	
			}
			default: return null;
		}
	}
}