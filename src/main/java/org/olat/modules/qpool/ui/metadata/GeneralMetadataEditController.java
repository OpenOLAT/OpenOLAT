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
package org.olat.modules.qpool.ui.metadata;

import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateElementLogic;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeHelper;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.admin.TaxonomyTreeModel;
import org.olat.modules.qpool.ui.events.QItemEdited;
/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GeneralMetadataEditController extends FormBasicController {

	private FormSubmit okButton;
	private FormLink selectContext;
	private FormLayoutContainer selectContextCont;
	private TextElement titleEl, keywordsEl, coverageEl, addInfosEl, languageEl;
	
	private SelectionTree selectPathCmp;
	private CloseableModalController cmc;
	
	private String taxonomicPath;
	private TaxonomyLevel selectedTaxonomicPath;
	private QuestionItem item;
	private final QPoolService qpoolService;
	
	public GeneralMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		taxonomicPath = item.getTaxonomicPath();
		
		initForm(ureq);
	}
	
	public GeneralMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		taxonomicPath = item.getTaxonomicPath();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("general");

		uifactory.addStaticTextElement("general.key", item.getKey().toString(), formLayout);
		uifactory.addStaticTextElement("general.identifier", item.getIdentifier(), formLayout);
		uifactory.addStaticTextElement("general.master.identifier", item.getMasterIdentifier(), formLayout);
		
		String title = item.getTitle();
		titleEl = uifactory.addTextElement("general.title", "general.title", 1000, title, formLayout);
		String keywords = item.getKeywords();
		keywordsEl = uifactory.addTextElement("general.keywords", "general.keywords", 1000, keywords, formLayout);
		String coverage = item.getCoverage();
		coverageEl = uifactory.addTextElement("general.coverage", "general.coverage", 1000, coverage, formLayout);
		String addInfos = item.getAdditionalInformations();
		addInfosEl = uifactory.addTextElement("general.additional.informations", "general.additional.informations",
				256, addInfos, formLayout);
		String language = item.getLanguage();
		languageEl = uifactory.addTextElement("general.language", "general.language", 10, language, formLayout);
		
		//classification
		String txPath = taxonomicPath == null ? "" : taxonomicPath;
		String selectContextPage = velocity_root + "/edit_edu_context.html";
		selectContextCont = FormLayoutContainer.createCustomFormLayout("owners", getTranslator(), selectContextPage);
		selectContextCont.setLabel("classification.taxonomic.path", null);
		selectContextCont.contextPut("path", txPath);
		formLayout.add(selectContextCont);
		selectContextCont.setRootForm(mainForm);
		selectContext = uifactory.addFormLink("select", selectContextCont, Link.BUTTON_SMALL);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		okButton =uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == selectPathCmp) {
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
				GenericTreeNode node = (GenericTreeNode)selectPathCmp.getSelectedNode();
				selectedTaxonomicPath = (TaxonomyLevel)node.getUserObject();
				selectContextCont.contextPut("path", selectedTaxonomicPath.getMaterializedPathNames());
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateElementLogic(titleEl, titleEl.getMaxLength(), true, true);
		allOk &= validateElementLogic(keywordsEl, keywordsEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(coverageEl, coverageEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(addInfosEl, addInfosEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(languageEl, languageEl.getMaxLength(), true, true);
		return allOk && super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == selectContext) {
			okButton.getComponent().setDirty(false);
			doOpenSelection(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenSelection(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			
			selectPathCmp = new SelectionTree("taxPathSelection", getTranslator());
			selectPathCmp.addListener(this);
			selectPathCmp.setMultiselect(false);
			selectPathCmp.setFormButtonKey("select");
			selectPathCmp.setShowCancelButton(true);
			TaxonomyTreeModel treeModel = new TaxonomyTreeModel();
			selectPathCmp.setTreeModel(treeModel);
			
			if(itemImpl.getTaxonomyLevel() != null || selectedTaxonomicPath != null) {
				TaxonomyLevel txPath = selectedTaxonomicPath == null ? itemImpl.getTaxonomyLevel() : selectedTaxonomicPath;
				TreeNode selectedNode = TreeHelper.findNodeByUserObject(txPath, treeModel.getRootNode());
				selectPathCmp.setSelectedNodeId(selectedNode.getIdent());
			}

			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					selectPathCmp, true, translate("classification.taxonomic.path"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			itemImpl.setTitle(titleEl.getValue());
			itemImpl.setKeywords(keywordsEl.getValue());
			itemImpl.setCoverage(coverageEl.getValue());
			itemImpl.setAdditionalInformations(addInfosEl.getValue());
			itemImpl.setLanguage(languageEl.getValue());
			if(selectedTaxonomicPath != null) {
				itemImpl.setTaxonomyLevel(selectedTaxonomicPath);
			}
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}