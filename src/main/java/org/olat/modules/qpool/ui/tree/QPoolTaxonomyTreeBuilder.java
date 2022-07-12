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
package org.olat.modules.qpool.ui.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 12.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
@Scope("prototype")
public class QPoolTaxonomyTreeBuilder {
	
	private static final String INTENDING = "\u00a0"; // &nbsp; non-breaking space

	private Translator translator;
	private boolean addEmptyEntry;
	private List<String> materializedPathKeysWithCompetence;
	private List<TaxonomyLevel> selectableTaxonomyLevels;
	private String[] selectableKeys;
	private String[] selectableValues;
	private String[] taxonomicPaths;
	private String[] taxonomicKeyPaths;
	private List<TaxonomyLevel> treeTaxonomyLevels;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QPoolService qpoolService;

	public QPoolTaxonomyTreeBuilder() {
		reset();
	}

	public void loadTaxonomyLevelsSelection(Translator translator, Identity identity, boolean withEmptyEntry, boolean ignoreCompetences) {
		reset();
		this.translator = translator;
		addEmptyEntry = withEmptyEntry;
		if (ignoreCompetences || qpoolModule.isIgnoreCompetences()) {
			loadAllTaxonomyLevels();
		} else {
			loadTaxonomyLevels(identity, TaxonomyCompetenceTypes.manage, TaxonomyCompetenceTypes.teach);
		}
	}
	
	public void loadTaxonomyLevelsMy(Translator translator,Identity identity) {
		reset();
		this.translator = translator;
		loadTaxonomyLevels(identity, TaxonomyCompetenceTypes.manage, TaxonomyCompetenceTypes.teach);
	}
	
	public void loadTaxonomyLevelsReview(Translator translator,Identity identity) {
		reset();
		this.translator = translator;
		loadTaxonomyLevels(identity, TaxonomyCompetenceTypes.manage, TaxonomyCompetenceTypes.teach);
	}
	
	public void loadTaxonomyLevelsFinal(Translator translator,Identity identity) {
		reset();
		this.translator = translator;
		TaxonomyCompetenceTypes[] types;
		if (qpoolModule.isFinalVisibleTeach()) {
			types = new TaxonomyCompetenceTypes[] {TaxonomyCompetenceTypes.manage, TaxonomyCompetenceTypes.teach};
		} else {
			types = new TaxonomyCompetenceTypes[] {TaxonomyCompetenceTypes.manage};
		}
		loadTaxonomyLevels(identity, types);
	}

	private void loadTaxonomyLevels(Identity identity, TaxonomyCompetenceTypes... type) {
		List<TaxonomyLevel> levels = qpoolService.getTaxonomyLevel(identity, type);
		prefill(levels);
	}
	
	private void loadAllTaxonomyLevels() {
		List<TaxonomyLevel> levels = qpoolService.getTaxonomyLevels();
		prefill(levels);
	}

	private void prefill(List<TaxonomyLevel> levels) {
		prefillMaterializedPathKeysWithCompetence(levels);
		TreeModel tree = buildTreeModel();
		prefillTaxonomyLevels(tree.getRootNode());
		prefillSelectableTaxonomyLevelsArrays();
	}
	
	private void reset() {
		addEmptyEntry = false;
		materializedPathKeysWithCompetence = new ArrayList<>();
		selectableTaxonomyLevels = new ArrayList<>();
		selectableKeys = new String[0];
		selectableValues = new String[0];
		taxonomicPaths = new String[0];
		taxonomicKeyPaths = new String[0];
		treeTaxonomyLevels = new ArrayList<>();
	}
	
	private void prefillMaterializedPathKeysWithCompetence(List<TaxonomyLevel> levels) {
		materializedPathKeysWithCompetence = levels.stream()
				.map(TaxonomyLevel::getMaterializedPathKeys)
				.collect(Collectors.toList());
	}

	public String[] getSelectableKeys() {
		return selectableKeys;
	}

	public String[] getSelectableValues() {
		return selectableValues;
	}
	
	public String[] getTaxonomicPaths() {
		return taxonomicPaths;
	}
	
	public String[] getTaxonomicKeyPaths() {
		return taxonomicKeyPaths;
	}

	public List<TaxonomyLevel> getTreeTaxonomyLevels() {
		return treeTaxonomyLevels;
	}

	private void prefillTaxonomyLevels(INode node) {
		if (node instanceof TaxonomyLevelNode) {
			TaxonomyLevelNode taxonomyLevelNode = (TaxonomyLevelNode) node;
			TaxonomyLevel level = taxonomyLevelNode.getTaxonomyLevel();
			if (level != null) {
			if (acceptSelectableNode(level)) {
				selectableTaxonomyLevels.add(taxonomyLevelNode.getTaxonomyLevel());
			}
			if (acceptTreeNode(level)) {
				treeTaxonomyLevels.add(taxonomyLevelNode.getTaxonomyLevel());
			}
			}
			for (int i = 0; i < node.getChildCount(); i++) {
				prefillTaxonomyLevels(taxonomyLevelNode.getChildAt(i));
			}
		}
	}

	private boolean acceptSelectableNode(TaxonomyLevel taxnomyLevel) {
		boolean accept = true;
		if (taxnomyLevel == null) {
			accept = false;
		} else if (qpoolModule.isReviewProcessEnabled()) {
			accept = hasSelectRight(taxnomyLevel);
		}
		return accept;
	}

	private boolean hasSelectRight(TaxonomyLevel taxnomyLevel) {
		boolean hasSelectRight = false;
		String materalizedPathKeys = taxnomyLevel.getMaterializedPathKeys();
		for (String path: materializedPathKeysWithCompetence) {
			if (hasSelectRight == false) {
				if (materalizedPathKeys.contains(path)) {
					hasSelectRight = true;
				}
			}
		}
		return hasSelectRight;
	}

	private void prefillSelectableTaxonomyLevelsArrays() {
		selectableKeys = new String[selectableTaxonomyLevels.size()];
		selectableValues = new String[selectableTaxonomyLevels.size()];
		taxonomicPaths = new String[selectableTaxonomyLevels.size()];
		taxonomicKeyPaths = new String[selectableTaxonomyLevels.size()];
		for(int i=selectableTaxonomyLevels.size(); i-->0; ) {
			TaxonomyLevel level = selectableTaxonomyLevels.get(i);
			selectableKeys[i] = Long.toString(level.getKey());
			selectableValues[i] = computeIntendention(level, new StringBuilder())
					.append(TaxonomyUIFactory.translateDisplayName(translator, level)).toString();
			taxonomicPaths[i] = level.getMaterializedPathIdentifiers();
			taxonomicKeyPaths[i] = level.getMaterializedPathKeys();
		}
		addEmptyEntry();
	}

	private StringBuilder computeIntendention(TaxonomyLevel level, StringBuilder intendation) {
		TaxonomyLevel parent = level.getParent();
		if (parent != null && selectableTaxonomyLevels.contains(parent)) {
			intendation = intendation.append(INTENDING).append(INTENDING).append(INTENDING).append(INTENDING);
			computeIntendention(parent, intendation);
		}
		return intendation;
	}

	private void addEmptyEntry() {
		if (addEmptyEntry) {
			String[] movedKeys = new String[selectableKeys.length + 1];
			String[] movedValues = new String[selectableValues.length + 1];
			String[] movedTaxonomicPaths = new String[taxonomicPaths.length + 1];
			String[] movedTaxonomicKeyPaths = new String[taxonomicKeyPaths.length + 1];
			movedKeys[0] = "-1";
			movedValues[0] = "-";
			movedTaxonomicPaths[0] = "/";
			movedTaxonomicKeyPaths[0] = "/";
			for (int i=selectableKeys.length; i-->0;) {
				movedKeys[i+1] = selectableKeys[i];
				movedValues[i+1] = selectableValues[i];
				movedTaxonomicPaths[i+1] = taxonomicPaths[i];
				movedTaxonomicKeyPaths[i+1] = taxonomicKeyPaths[i];
			}
			selectableKeys = movedKeys;
			selectableValues = movedValues;
			taxonomicPaths = movedTaxonomicPaths;
			taxonomicKeyPaths = movedTaxonomicKeyPaths;
		}
	}
	
	private boolean acceptTreeNode(TaxonomyLevel taxonomyLevel) {
		return hasTreeRight(taxonomyLevel);
	}
	
	private boolean hasTreeRight(TaxonomyLevel taxonomyLevel) {
		boolean hasTreeRight = false;
		String materalizedPathKeys = taxonomyLevel.getMaterializedPathKeys();
		for (String path: materializedPathKeysWithCompetence) {
			if (!hasTreeRight) {
				if (path.equals(materalizedPathKeys)) {
					hasTreeRight = true;
				}
			}
		}
		return hasTreeRight;
	}

	public TaxonomyLevel getTaxonomyLevel(String key) {
		if (key == null) return null;
		
		if(key.endsWith("/")) {
			key = key.substring(0, key.length() - 1);
		}
		int index = key.lastIndexOf('/');
		if(index >= 0) {
			key = key.substring(index + 1);
		}
		
		Long taxonomyLevelKey = Long.parseLong(key);
		for (TaxonomyLevel taxonomyLevel: selectableTaxonomyLevels) {
			if (taxonomyLevel.getKey().equals(taxonomyLevelKey)) {
				return taxonomyLevel;
			}
		}
		return null;
	}
	
	public TreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		TaxonomyLevelNode root = new TaxonomyLevelNode();
		gtm.setRootNode(root);

		List<TaxonomyLevel> taxonomyLevels = qpoolService.getTaxonomyLevels();
		Map<Long,TaxonomyLevel> keytoLevels = taxonomyLevels.stream()
				.collect(Collectors.toMap(TaxonomyLevel::getKey, l -> l));

		Map<Long,TaxonomyLevelNode> fieldKeyToNode = new HashMap<>();
		for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
			Long key = taxonomyLevel.getKey();
			TaxonomyLevelNode node = fieldKeyToNode.get(key);
			if(node == null) {
				node = new TaxonomyLevelNode(taxonomyLevel);
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
				TaxonomyLevelNode parentNode = fieldKeyToNode.get(parentKey);
				if(parentNode == null) {
					parentLevel = keytoLevels.get(parentKey);//to use the fetched type
					parentNode = new TaxonomyLevelNode(parentLevel);
					TaxonomyLevelType type = parentLevel.getType();
					if(type != null && StringHelper.containsNonWhitespace(type.getCssClass())) {
						parentNode.setIconCssClass(type.getCssClass());
					}
					fieldKeyToNode.put(parentKey, parentNode);
				}
				parentNode.addChild(node);
			}
		}
		
		sort(root);
		
		return gtm;
	}
	
	private void sort(TaxonomyLevelNode parent) {
		parent.sort(new TaxonomyLevelNodeComparator(translator));
		for(int i=parent.getChildCount(); i-->0; ) {
			sort((TaxonomyLevelNode)parent.getChildAt(i));
		}
	}
	
	@SuppressWarnings("serial")
	private static class TaxonomyLevelNode extends GenericTreeNode {
		
		private TaxonomyLevel taxonomyLevel;

		public TaxonomyLevelNode() {
			
		}
		
		public TaxonomyLevelNode(TaxonomyLevel taxonomyLevel) {
			this.taxonomyLevel = taxonomyLevel;
		}

		public TaxonomyLevel getTaxonomyLevel() {
			return taxonomyLevel;
		}
		
    }
	
	private static final class TaxonomyLevelNodeComparator implements Comparator<INode> {
		
		private final Translator translator;
		
		public TaxonomyLevelNodeComparator(Translator translator) {
			this.translator = translator;
		}

		@Override
		public int compare(INode i1, INode i2) {
			if(i1 == null || i2 == null) {
				return compareNullObjects(i1, i2);
			}
			
			Integer s1 = null;
			Integer s2 = null;
			String displayName1 = null;
			String displayName2 = null;

			if(i1 instanceof TaxonomyLevelNode) {
				TaxonomyLevelNode t1 = (TaxonomyLevelNode)i1;
				if(t1.getTaxonomyLevel() != null) {
					displayName1 = TaxonomyUIFactory.translateDisplayName(translator, t1.getTaxonomyLevel());
					s1 = t1.getTaxonomyLevel().getSortOrder();
				}
			}
			
			if(i2 instanceof TaxonomyLevelNode) {
				TaxonomyLevelNode t2 = (TaxonomyLevelNode)i2;
				if(t2.getTaxonomyLevel() != null) {
					displayName2 = TaxonomyUIFactory.translateDisplayName(translator, t2.getTaxonomyLevel());
					s2 = t2.getTaxonomyLevel().getSortOrder();
				}
			}
			
			int c = 0;
			if(s1 == null || s2 == null) {
				c = compareNullObjects(s1, s2);
			} else {
				c = s1.compareTo(s2);
			}
			if(c == 0) {
				if(displayName1 == null || displayName2 == null) {
					c = compareNullObjects(displayName1, displayName2);
				} else {
					c = displayName1.compareTo(displayName2);
				}
			}

			return c;
		}
		
		private final int compareNullObjects(final Object a, final Object b) {
			boolean ba = (a == null);
			boolean bb = (b == null);
			return ba? (bb? 0: -1):(bb? 1: 0);
		}
	}

}
