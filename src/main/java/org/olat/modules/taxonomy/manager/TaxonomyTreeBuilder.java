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
package org.olat.modules.taxonomy.manager;

import static org.olat.core.util.StringHelper.EMPTY;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.model.TaxonomyTreeNodeType;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * Build the tree of taxonomy
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeBuilder {
	
	public static final String LEVEL_PREFIX = "level-";
	
	private Locale locale;
	private Taxonomy taxonomy;
	private final String rootTitle;
	private final Identity identity;
	private final String templateDirectory;
	private final boolean isTaxonomyAdmin;
	private final boolean enableTemplates;
	
	private final TaxonomyService taxonomyService;
	private final List<String> lostAndFoundIdentifiers;
	
	public TaxonomyTreeBuilder(Taxonomy taxonomy, Identity identity, String rootTitle,
			boolean isTaxonomyAdmin, boolean enableTemplates, String templateDirectory, Locale locale) {
		taxonomyService = CoreSpringFactory.getImpl(TaxonomyService.class);
		lostAndFoundIdentifiers = CoreSpringFactory.getImpl(TaxonomyModule.class).getLostAndFoundsIdentifiers();
		this.locale = locale;
		this.taxonomy = taxonomy;
		this.identity = identity;
		this.rootTitle = rootTitle;
		this.enableTemplates = enableTemplates;
		this.templateDirectory = templateDirectory;
		this.isTaxonomyAdmin = isTaxonomyAdmin;
	}
	
	public TreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		TaxonomyTreeNode root = new TaxonomyTreeNode(taxonomy);
		root.setTitle("ROOT competence");
		gtm.setRootNode(root);

		if(taxonomy != null) {
			taxonomy = taxonomyService.getTaxonomy(taxonomy);
			if(StringHelper.containsNonWhitespace(rootTitle)) {
				root.setTitle(rootTitle);
			} else {
				root.setTitle(taxonomy.getDisplayName());
			}
			root.setUserObject(taxonomy);

			if(locale == null) {
				locale = CoreSpringFactory.getImpl(I18nManager.class).getCurrentThreadLocale();
			}
			
			//taxonomy directory
			if(enableTemplates) {
				VFSContainer taxonomyDirectory = taxonomyService.getDocumentsLibrary(taxonomy);
				TaxonomyTreeNode taxonomyDirectorNode = new TaxonomyTreeNode(taxonomy, taxonomyDirectory, TaxonomyTreeNodeType.templates);
				taxonomyDirectorNode.setTitle(templateDirectory);
				taxonomyDirectorNode.setUserObject(taxonomyDirectory);
				root.addChild(taxonomyDirectorNode);
			}
		
			//taxonomy levels
			List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevels(taxonomy);
			Map<Long,TaxonomyLevel> keytoLevels = levels.stream()
					.collect(Collectors.toMap(TaxonomyLevel::getKey, l -> l));
			
			Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
			Map<Long,TaxonomyTreeNode> fieldKeyToNode = new HashMap<>();
			for(TaxonomyLevel taxonomyLevel:levels) {
				Long key = taxonomyLevel.getKey();
				TaxonomyTreeNode node = fieldKeyToNode.get(key);
				if(node == null) {
					String displayName = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, taxonomyLevel, EMPTY);
					node = new TaxonomyTreeNode(taxonomy, taxonomyLevel, displayName, getType(taxonomyLevel));
					TaxonomyLevelType type = taxonomyLevel.getType();
					if(type != null && StringHelper.containsNonWhitespace(type.getCssClass())) {
						node.setIconCssClass(type.getCssClass());
					}
					fieldKeyToNode.put(key, node);
				}

				TaxonomyLevel parentLevel = taxonomyLevel.getParent();
				if(parentLevel == null) {
					//this is a root
					root.addChild(node);
				} else {
					Long parentKey = parentLevel.getKey();
					TaxonomyTreeNode parentNode = fieldKeyToNode.get(parentKey);
					if(parentNode == null) {
						parentLevel = keytoLevels.get(parentKey);//to use the fetched type
						String displayName = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, taxonomyLevel);
						parentNode = new TaxonomyTreeNode(taxonomy, parentLevel, displayName, getType(parentLevel));
						TaxonomyLevelType type = parentLevel.getType();
						if(type != null && StringHelper.containsNonWhitespace(type.getCssClass())) {
							parentNode.setIconCssClass(type.getCssClass());
						}
						fieldKeyToNode.put(parentKey, parentNode);
					}
					parentNode.addChild(node);
				}
			}
			
			computePermissions(root);
			trimVisiblity(root);
			sort(root);
		}
		return gtm;
	}
	
	private TaxonomyTreeNodeType getType(TaxonomyLevel taxonomyLevel) {
		TaxonomyTreeNodeType type;
		String identifier = taxonomyLevel.getIdentifier();
		if(StringHelper.containsNonWhitespace(identifier)) {
			if(lostAndFoundIdentifiers.contains(identifier)) {
				type = TaxonomyTreeNodeType.lostAndFound;
			} else {
				type = TaxonomyTreeNodeType.taxonomyLevel;
			}
		} else {
			type = TaxonomyTreeNodeType.taxonomyLevel;
		}
		return type;		
	}
	
	private void sort(TaxonomyTreeNode parent) {
		parent.sort(new TaxonomyTreeNodeComparator());
		for(int i=parent.getChildCount(); i-->0; ) {
			sort((TaxonomyTreeNode)parent.getChildAt(i));
		}
	}
	
	private void trimVisiblity(TaxonomyTreeNode parent) {
		boolean someInvisible;
		do {
			someInvisible = false;
			List<TaxonomyTreeNode> children = listChildren(parent);
			for(TaxonomyTreeNode child:children) {
				//remove invisible nodes and lost+found
				if(!child.isVisible() || child.getType() == TaxonomyTreeNodeType.lostAndFound)  {
					List<TaxonomyTreeNode> childrenOfChild = listChildren(child);
					parent.remove(child);
					for(TaxonomyTreeNode childOfChild : childrenOfChild) {
						parent.addChild(childOfChild);
					}
					someInvisible = true;
				}
			}
		} while(someInvisible);
		
		for(int i=0; i<parent.getChildCount(); i++) {
			trimVisiblity((TaxonomyTreeNode)parent.getChildAt(i));
		}
	}
	
	private List<TaxonomyTreeNode> listChildren(TaxonomyTreeNode parent) {
		List<TaxonomyTreeNode> children = new ArrayList<>(parent.getChildCount());
		for(int i=0; i<parent.getChildCount(); i++) {
			children.add((TaxonomyTreeNode)parent.getChildAt(i));
		}
		return children;
	}
	
	private void computePermissions(TaxonomyTreeNode root) {
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyCompetences(taxonomy, identity, new Date());
		Map<TaxonomyLevel, List<TaxonomyCompetenceTypes>> levelToCompetences = new HashMap<>();
		for(TaxonomyCompetence competence:competences) {
			TaxonomyLevel level = competence.getTaxonomyLevel();
			if(levelToCompetences.containsKey(level)) {
				levelToCompetences.get(level).add(competence.getCompetenceType());
			} else {
				List<TaxonomyCompetenceTypes> types = new ArrayList<>(4);
				types.add(competence.getCompetenceType());
				levelToCompetences.put(level, types);
			}	
		}
		computePermissionsRecursive(root, levelToCompetences);
		trimRecursive(root);
	}

	private void computePermissionsRecursive(TaxonomyTreeNode node, Map<TaxonomyLevel, List<TaxonomyCompetenceTypes>> levelToCompetences) {
		boolean hasRead = node.isCanRead();
		boolean hasWrite = node.isCanWrite();
		
		if(node.getType() == TaxonomyTreeNodeType.lostAndFound) {
			hasRead = isTaxonomyAdmin;
			hasWrite = isTaxonomyAdmin;
			node.setCanRead(hasRead);
			node.setCanWrite(hasWrite);
		} else if(node.getTaxonomyLevel() != null) {
			TaxonomyLevel level = node.getTaxonomyLevel();
			TaxonomyLevelType type = level.getType();
			if(type != null) {
				List<TaxonomyCompetenceTypes> competences = levelToCompetences.get(level);
				if(competences != null && competences.size() > 0) {
					for(TaxonomyCompetenceTypes competence:competences) {
						hasRead |= hasReadAccess(type, competence);
						hasWrite |= hasWriteAccess(type, competence);
						
						if(competence == TaxonomyCompetenceTypes.teach && type.getDocumentsLibraryTeachCompetenceReadParentLevels() > 0) {
							int parentLevels = type.getDocumentsLibraryTeachCompetenceReadParentLevels();
							
							TaxonomyTreeNode parent = (TaxonomyTreeNode)node.getParent();
							for(int i=parentLevels; i-->0 && parent != null; ) {
								parent.setCanRead(true);
								parent = (TaxonomyTreeNode)parent.getParent();
							}
						}
					}
				} else if(isTaxonomyAdmin) {
					hasRead |= hasReadAccess(type, null);
					hasWrite |= hasWriteAccess(type, null);
				}
			}
			node.setCanRead(hasRead);
			node.setCanWrite(hasWrite);
		} else if(node.getType() == TaxonomyTreeNodeType.templates) {
			hasRead = true;
			hasWrite = isTaxonomyAdmin;
			node.setCanRead(hasRead);
			node.setCanWrite(hasWrite);
		}
		
		for(int i=node.getChildCount(); i-->0; ) {
			TaxonomyTreeNode child = (TaxonomyTreeNode)node.getChildAt(i);
			child.setCanRead(hasRead);
			child.setCanWrite(hasWrite);
			computePermissionsRecursive(child, levelToCompetences);
		}
	}
	
	/**
	 * Propagate read and write permissions to the children or remove nodes without
	 * permissions.
	 * 
	 * @param node The start of a sub tree
	 * @return True if some node as some permissions
	 */
	private boolean trimRecursive(TaxonomyTreeNode node) {
		boolean canRead = node.isCanRead();
		boolean canWrite = node.isCanWrite();
		boolean someAllowed = false;
		for(int i=node.getChildCount(); i-->0; ) {
			TaxonomyTreeNode child = (TaxonomyTreeNode)node.getChildAt(i);
			child.setCanRead(canRead || child.isCanRead());
			child.setCanWrite(canWrite || child.isCanWrite());
			boolean subChildAllowed = trimRecursive(child);
			if(!subChildAllowed && !child.isCanRead() && !child.isCanWrite()) {
				node.remove(child);
			} else {
				someAllowed |= true;
			}
		}
		
		return someAllowed || canRead || canWrite;
	}
	
	private boolean hasReadAccess(TaxonomyLevelType type, TaxonomyCompetenceTypes competence) {
		if(isTaxonomyAdmin) {
			return type.isDocumentsLibraryManageCompetenceEnabled()
					|| type.isDocumentsLibraryTeachCompetenceReadEnabled()
					|| type.isDocumentsLibraryHaveCompetenceReadEnabled()
					|| type.isDocumentsLibraryTargetCompetenceReadEnabled();
		}
		if(competence == TaxonomyCompetenceTypes.manage) {
			return type.isDocumentsLibraryManageCompetenceEnabled();
		}
		if(competence == TaxonomyCompetenceTypes.teach) {
			return type.isDocumentsLibraryTeachCompetenceReadEnabled();
		}
		if(competence == TaxonomyCompetenceTypes.have) {
			return type.isDocumentsLibraryHaveCompetenceReadEnabled();
		}
		if(competence == TaxonomyCompetenceTypes.target) {
			return type.isDocumentsLibraryTargetCompetenceReadEnabled();
		}
		return false;
	}
	
	private boolean hasWriteAccess(TaxonomyLevelType type, TaxonomyCompetenceTypes competence) {
		if(isTaxonomyAdmin) {
			return type.isDocumentsLibraryManageCompetenceEnabled()
					|| type.isDocumentsLibraryTeachCompetenceWriteEnabled();
		}
		if(competence == TaxonomyCompetenceTypes.manage) {
			return type.isDocumentsLibraryManageCompetenceEnabled();
		}
		if(competence == TaxonomyCompetenceTypes.teach) {
			return type.isDocumentsLibraryTeachCompetenceWriteEnabled();
		}
		return false;
	}
}