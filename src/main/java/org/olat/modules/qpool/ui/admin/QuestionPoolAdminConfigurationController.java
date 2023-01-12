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
package org.olat.modules.qpool.ui.admin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolAdminConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = { "on" };
	private static final String TAXONOMY_COMPETENCES = "taxonomy.competences";
	private static final String TAXONOMY_ALL = "taxonomy.all";
	private static final String POOL_MANAGER_EDIT_METADATA = "pool.manager.edit.matadata";
	private static final String POOL_MANAGER_EDIT_STATUS = "pool.manager.edit.status";
	private static final String POOL_MANAGER_REVIEW_PROCESS = "pool.manager.review.process";
	private static final String POOL_MANAGER_TAXONOMY = "pool.manager.taxonomy";
	private static final String POOL_MANAGER_POOLS = "pool.manager.pools";
	private static final String POOL_MANAGER_ITEM_TYPES = "pool.manager.item.types";
	private static final String POOL_MANAGER_EDUCATIONAL_CONTEXT = "pool.manager.educational.context";
	private static final String[] POOL_MANAGER_RIGHTS_KEYS = {
			POOL_MANAGER_EDIT_METADATA,
			POOL_MANAGER_EDIT_STATUS,
			POOL_MANAGER_REVIEW_PROCESS,
			POOL_MANAGER_TAXONOMY,
			POOL_MANAGER_POOLS,
			POOL_MANAGER_ITEM_TYPES,
			POOL_MANAGER_EDUCATIONAL_CONTEXT,
	};
	
	private MultipleSelectionElement reviewProcessEnabledEl;
	private MultipleSelectionElement collectionsEnabledEl;
	private MultipleSelectionElement poolsEnabledEl;
	private MultipleSelectionElement sharesEnabledEl;
	private MultipleSelectionElement taxonomyEnabledEl;
	private MultipleSelectionElement educationalContextEnabledEl;
	private MultipleSelectionElement deleteQuestionsWithoutAuthorEl;
	private MultipleSelectionElement poolManagerRightsEl;
	private SingleSelection taxonomyTreeEl;
	private SingleSelection ignoreCompetencesEl;
	private MultipleSelectionElement importCreateTaxonomyLevelEl;
	
	private CloseableModalController closeableModalCtrl;
	private ReviewProcessActivationController reviewProcessActivationCtrl;

	private boolean resetQuestionStates = false;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QPoolService qpoolService;
	
	public QuestionPoolAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_config");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer moduleCont = FormLayoutContainer.createDefaultFormLayout("module", getTranslator());
		moduleCont.setElementCssClass("o_sel_qpool_configuration");
		moduleCont.setFormTitle(translate("admin.configuration.title"));
		moduleCont.setRootForm(mainForm);
		formLayout.add("module", moduleCont);
		
		String[] onValues = new String[] { translate("on") };
		reviewProcessEnabledEl = uifactory.addCheckboxesHorizontal("review.process.enabled", moduleCont, onKeys, onValues);
		reviewProcessEnabledEl.addActionListener(FormEvent.ONCHANGE);
		reviewProcessEnabledEl.setElementCssClass("o_sel_qpool_review_process");
		if (qpoolModule.isReviewProcessEnabled()) {
			reviewProcessEnabledEl.select(onKeys[0], true);
		}
		
		collectionsEnabledEl = uifactory.addCheckboxesHorizontal("collections.enabled", moduleCont, onKeys, onValues);
		if (qpoolModule.isCollectionsEnabled()) {
			collectionsEnabledEl.select(onKeys[0], true);
		}
		
		poolsEnabledEl = uifactory.addCheckboxesHorizontal("pools.enabled", moduleCont, onKeys, onValues);
		if (qpoolModule.isPoolsEnabled()) {
			poolsEnabledEl.select(onKeys[0], true);
		}

		sharesEnabledEl = uifactory.addCheckboxesHorizontal("shares.enabled", moduleCont, onKeys, onValues);
		if (qpoolModule.isSharesEnabled()) {
			sharesEnabledEl.select(onKeys[0], true);
		}
		
		taxonomyEnabledEl = uifactory.addCheckboxesHorizontal("taxonomy.enabled", moduleCont, onKeys, onValues);
		if (qpoolModule.isTaxonomyEnabled()) {
			taxonomyEnabledEl.select(onKeys[0], true);
		}
		taxonomyEnabledEl.addActionListener(FormEvent.ONCHANGE);

		List<Taxonomy> taxonomyList = taxonomyService.getTaxonomyList();
		String[] taxonomyKeys = new String[taxonomyList.size() + 1];
		String[] taxonomyValues = new String[taxonomyList.size() + 1];
		taxonomyKeys[0] = "";
		taxonomyValues[0] = "-";
		for(int i=taxonomyList.size(); i-->0; ) {
			Taxonomy taxonomy = taxonomyList.get(i);
			taxonomyKeys[i + 1] = taxonomy.getKey().toString();
			taxonomyValues[i + 1] = taxonomy.getDisplayName();
		}
		
		String selectedTaxonomyQPoolKey = qpoolModule.getTaxonomyQPoolKey();
		taxonomyTreeEl = uifactory.addDropdownSingleselect("selected.taxonomy.tree", moduleCont, taxonomyKeys, taxonomyValues, null);
		taxonomyTreeEl.setEnabled(false);
		if(StringHelper.containsNonWhitespace(selectedTaxonomyQPoolKey)) {
			for(String taxonomyKey:taxonomyKeys) {
				if(taxonomyKey.equals(selectedTaxonomyQPoolKey)) {
					taxonomyTreeEl.select(taxonomyKey, true);
				}
			}
		}
		
		String[] ignoreCompetencesKeys = new String[] {TAXONOMY_COMPETENCES, TAXONOMY_ALL};
		ignoreCompetencesEl = uifactory.addDropdownSingleselect("taxonomy.selectable", moduleCont,
				ignoreCompetencesKeys, translateKeys(ignoreCompetencesKeys));
		String selectedKey = qpoolModule.isIgnoreCompetences()? TAXONOMY_ALL: TAXONOMY_COMPETENCES;
		ignoreCompetencesEl.select(selectedKey, true);
		
		importCreateTaxonomyLevelEl = uifactory.addCheckboxesHorizontal("import.create.subject", moduleCont, onKeys, onValues);
		if (qpoolModule.isImportCreateTaxonomyLevel()) {
			importCreateTaxonomyLevelEl.select(onKeys[0], true);
		}

		educationalContextEnabledEl = uifactory.addCheckboxesHorizontal("educational.context.enabled", moduleCont, onKeys, onValues);
		if (qpoolModule.isEducationalContextEnabled()) {
			educationalContextEnabledEl.select(onKeys[0], true);
		}
		educationalContextEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		deleteQuestionsWithoutAuthorEl = uifactory.addCheckboxesHorizontal("delete.qustions.without.author", moduleCont, onKeys, onValues);
		deleteQuestionsWithoutAuthorEl.setHelpTextKey("delete.qustions.without.author.info", null);
		if (qpoolModule.isDeleteQuestionsWithoutAuthor()) {
			deleteQuestionsWithoutAuthorEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer poolManagerRightsCont = FormLayoutContainer.createDefaultFormLayout("poolManagerRights", getTranslator());
		poolManagerRightsCont.setFormTitle(translate("admin.pool.manager.title"));
		poolManagerRightsCont.setRootForm(mainForm);
		formLayout.add("poolManagerRights", poolManagerRightsCont);
		
		poolManagerRightsEl = uifactory.addCheckboxesVertical("pool.manager.allowed", poolManagerRightsCont,
				POOL_MANAGER_RIGHTS_KEYS, translateKeys(POOL_MANAGER_RIGHTS_KEYS), 1);
		poolManagerRightsEl.select(POOL_MANAGER_EDIT_METADATA, qpoolModule.isPoolAdminAllowedToEditMetadata());
		poolManagerRightsEl.select(POOL_MANAGER_EDIT_STATUS, qpoolModule.isPoolAdminAllowedToEditStatus());
		poolManagerRightsEl.select(POOL_MANAGER_REVIEW_PROCESS, qpoolModule.isPoolAdminAllowedToConfigReviewProcess());
		poolManagerRightsEl.select(POOL_MANAGER_TAXONOMY, qpoolModule.isPoolAdminAllowedToConfigTaxonomy());
		poolManagerRightsEl.select(POOL_MANAGER_POOLS, qpoolModule.isPoolAdminAllowedToConfigPools());
		poolManagerRightsEl.select(POOL_MANAGER_ITEM_TYPES, qpoolModule.isPoolAdminAllowedToConfigItemTypes());
		poolManagerRightsEl.select(POOL_MANAGER_EDUCATIONAL_CONTEXT, qpoolModule.isPoolAdminAllowedToConfigEducationalContext());
		
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("global", getTranslator());
		buttonsWrapperCont.setElementCssClass("o_sel_qpool_buttons");
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add("buttonsWrapper", buttonsWrapperCont);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsWrapperCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	private String[] translateKeys(String[] keys) {
		return Stream.of(keys)
				.map(key -> getTranslator().translate(key))
				.toArray(String[]::new);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (reviewProcessEnabledEl == source) {
			boolean enable = reviewProcessEnabledEl.isAtLeastSelected(1);
			doConfirmEnabled(ureq, enable);
		} else if (taxonomyEnabledEl == source) {
			doEnableTaxonomyLevel();
		} else if (educationalContextEnabledEl == source) {
			showInfo("admin.start.indexer");
		}
		super.formInnerEvent(ureq, source, event);
	}


	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (reviewProcessActivationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				resetQuestionStates = reviewProcessActivationCtrl.isResetStatesSelected();
				taxonomyEnabledEl.select(onKeys[0], true);
			} else {
				reviewProcessEnabledEl.select(onKeys[0], false);
			}
			closeableModalCtrl.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(reviewProcessActivationCtrl);
		removeAsListenerAndDispose(closeableModalCtrl);
		reviewProcessActivationCtrl = null;
		closeableModalCtrl = null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		taxonomyTreeEl.clearError();
		if(!taxonomyTreeEl.isOneSelected()) {
			taxonomyTreeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean reviewProcessEnabled = reviewProcessEnabledEl.isAtLeastSelected(1);
		qpoolModule.setReviewProcessEnabled(reviewProcessEnabled);
		if (reviewProcessEnabled) {
			doResetQuestionStates();
		}
		
		boolean collectionsEnabled = collectionsEnabledEl.isAtLeastSelected(1);
		qpoolModule.setCollectionsEnabled(collectionsEnabled);
		
		boolean poolsEnabled = poolsEnabledEl.isAtLeastSelected(1);
		qpoolModule.setPoolsEnabled(poolsEnabled);

		boolean sharesEnabled = sharesEnabledEl.isAtLeastSelected(1);
		qpoolModule.setSharesEnabled(sharesEnabled);
		
		boolean taxonomyEnabled = taxonomyEnabledEl.isAtLeastSelected(1);
		qpoolModule.setTaxonomyEnabled(taxonomyEnabled);
		
		String selectedTaxonomyQPoolKey = taxonomyTreeEl.getSelectedKey();
		qpoolModule.setTaxonomyQPoolKey(selectedTaxonomyQPoolKey);
		
		String selectedIgnoreCompetences = ignoreCompetencesEl.getSelectedKey();
		if (TAXONOMY_ALL.equals(selectedIgnoreCompetences)) {
			qpoolModule.setIgnoreCompetences(true);
		} else {
			qpoolModule.setIgnoreCompetences(false);
		}
		
		boolean importCreateTaxonomyLevel = importCreateTaxonomyLevelEl.isAtLeastSelected(1);
		qpoolModule.setImportCreateTaxonomyLevel(importCreateTaxonomyLevel);
		
		boolean educationalContextEnabled = educationalContextEnabledEl.isAtLeastSelected(1);
		qpoolModule.setEducationalContextEnabled(educationalContextEnabled);
		
		boolean deleteQuestionsWithoutAuthor = deleteQuestionsWithoutAuthorEl.isAtLeastSelected(1);
		qpoolModule.setDeleteQuestionsWithoutAuthor(deleteQuestionsWithoutAuthor);
		
		Collection<String> selectedPoolManagerRights = poolManagerRightsEl.getSelectedKeys();
		boolean poolAdminAllowedToEditMetadata = selectedPoolManagerRights.contains(POOL_MANAGER_EDIT_METADATA);
		qpoolModule.setPoolAdminAllowedToEditMetadata(poolAdminAllowedToEditMetadata);
		boolean poolAdminAllowedToEditStatus = selectedPoolManagerRights.contains(POOL_MANAGER_EDIT_STATUS);
		qpoolModule.setPoolAdminAllowedToEditStatus(poolAdminAllowedToEditStatus);
		boolean poolAdminAllowedToConfigReviewProcess = selectedPoolManagerRights.contains(POOL_MANAGER_REVIEW_PROCESS);
		qpoolModule.setPoolAdminAllowedToConfigReviewProcess(poolAdminAllowedToConfigReviewProcess );
		boolean poolAdminAllowedToConfigTaxonomy = selectedPoolManagerRights.contains(POOL_MANAGER_TAXONOMY);
		qpoolModule.setPoolAdminAllowedToConfigTaxonomy(poolAdminAllowedToConfigTaxonomy);
		boolean poolAdminAllowedToConfigPools = selectedPoolManagerRights.contains(POOL_MANAGER_POOLS);
		qpoolModule.setPoolAdminAllowedToConfigPools(poolAdminAllowedToConfigPools);
		boolean poolAdminAllowedToConfigItemTypes = selectedPoolManagerRights.contains(POOL_MANAGER_ITEM_TYPES);
		qpoolModule.setPoolAdminAllowedToConfigItemTypes(poolAdminAllowedToConfigItemTypes);
		boolean poolAdminAllowedToConfigEducationalContext = selectedPoolManagerRights.contains(POOL_MANAGER_EDUCATIONAL_CONTEXT);
		qpoolModule.setPoolAdminAllowedToConfigEducationalContext(poolAdminAllowedToConfigEducationalContext);
	}
	
	private void doConfirmEnabled(UserRequest ureq, boolean enable) {
		if (!enable) return;
		
		reviewProcessActivationCtrl = new ReviewProcessActivationController(ureq, getWindowControl());
		listenTo(reviewProcessActivationCtrl);
		closeableModalCtrl = new CloseableModalController(getWindowControl(), null,
				reviewProcessActivationCtrl.getInitialComponent(), true,
				translate("review.process.confirm.enable.title"), false);
		listenTo(closeableModalCtrl);
		closeableModalCtrl.activate();
	}

	private void doResetQuestionStates() {
		if (resetQuestionStates) {
			qpoolService.resetAllStatesToDraft(getIdentity());
		}
	}
	
	private void doEnableTaxonomyLevel() {
		boolean taxonomyEnabledSelected = taxonomyEnabledEl.isAtLeastSelected(1);
		boolean reviewProcessEnabledSelected = reviewProcessEnabledEl.isAtLeastSelected(1);
		if (reviewProcessEnabledSelected && !taxonomyEnabledSelected) {
			showWarning("taxonomy.can.not.be.deactivated");
			taxonomyEnabledEl.select(onKeys[0], true);
		} else {
			showInfo("admin.start.indexer");
		}
	}

}