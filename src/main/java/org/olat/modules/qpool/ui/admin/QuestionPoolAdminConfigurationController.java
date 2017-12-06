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

import java.util.List;

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
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement reviewProcessEnabledEl;
	private MultipleSelectionElement collectionsEnabledEl;
	private MultipleSelectionElement poolsEnabledEl;
	private MultipleSelectionElement sharesEnabledEl;
	private SingleSelection taxonomyTreeEl;
	
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
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.configuration.title");
		
		String[] onValues = new String[] { translate("on") };
		reviewProcessEnabledEl = uifactory.addCheckboxesHorizontal("review.process.enabled", formLayout, onKeys, onValues);
		reviewProcessEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (qpoolModule.isReviewProcessEnabled()) {
			reviewProcessEnabledEl.select(onKeys[0], true);
		}
		
		collectionsEnabledEl = uifactory.addCheckboxesHorizontal("collections.enabled", formLayout, onKeys, onValues);
		collectionsEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (qpoolModule.isCollectionsEnabled()) {
			collectionsEnabledEl.select(onKeys[0], true);
		}
		
		poolsEnabledEl = uifactory.addCheckboxesHorizontal("pools.enabled", formLayout, onKeys, onValues);
		poolsEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (qpoolModule.isPoolsEnabled()) {
			poolsEnabledEl.select(onKeys[0], true);
		}

		sharesEnabledEl = uifactory.addCheckboxesHorizontal("shares.enabled", formLayout, onKeys, onValues);
		sharesEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (qpoolModule.isSharesEnabled()) {
			sharesEnabledEl.select(onKeys[0], true);
		}

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
		taxonomyTreeEl = uifactory.addDropdownSingleselect("selected.taxonomy.tree", formLayout, taxonomyKeys, taxonomyValues, null);
		taxonomyTreeEl.setEnabled(false);
		if(StringHelper.containsNonWhitespace(selectedTaxonomyQPoolKey)) {
			for(String taxonomyKey:taxonomyKeys) {
				if(taxonomyKey.equals(selectedTaxonomyQPoolKey)) {
					taxonomyTreeEl.select(taxonomyKey, true);
				}
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (reviewProcessEnabledEl == source) {
			boolean enable = reviewProcessEnabledEl.isAtLeastSelected(1);
			doConfirmEnabled(ureq, enable);
		}
		super.formInnerEvent(ureq, source, event);
	}


	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (reviewProcessActivationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				resetQuestionStates = reviewProcessActivationCtrl.isResetStatesSelected();
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
		boolean allOk = true;
		
		taxonomyTreeEl.clearError();
		if(!taxonomyTreeEl.isOneSelected()) {
			taxonomyTreeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
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
		
		String selectedTaxonomyQPoolKey = taxonomyTreeEl.getSelectedKey();
		qpoolModule.setTaxonomyQPoolKey(selectedTaxonomyQPoolKey);
	}
	
	@Override
	protected void doDispose() {
		//
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
	
}