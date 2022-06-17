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
package org.olat.modules.fo.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Pseudonym;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.model.PseudonymStatistics;
import org.olat.modules.fo.ui.ForumPseudonymsDataModel.PseudoCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumPseudonymsAdminController extends FormBasicController {
	
	private FormLink newPseudonymButton;
	private FlexiTableElement tableEl;
	private ForumPseudonymsDataModel model;

	private CloseableModalController cmc;
	private NewPseudonymController newPseudoCtrl;
	private DialogBoxController confirmDeleteBox;
	
	@Autowired
	private ForumManager forumManager;
	
	public ForumPseudonymsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_pseudonyms");
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		loadModel(null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.pseudonyms.title");
		
		newPseudonymButton = uifactory.addFormLink("new.pseudonym", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PseudoCols.pseudonym));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PseudoCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PseudoCols.numOfMessages));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		model = new ForumPseudonymsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "pseudonyms", model, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_forum");
		tableEl.setEmptyTableSettings("forum.empty", null, "o_forum_status_thread_icon");
		tableEl.setSearchEnabled(true);
	}
	
	private void loadModel(String searchString) {
		List<PseudonymStatistics> stats = forumManager.getPseudonymStatistics(searchString);
		model.setObjects(stats);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newPseudonymButton == source) {
			doNewPseudonym(ureq);
		} else if(tableEl == source) {
			String cmd = event.getCommand();
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if(se.getIndex() >= 0 && se.getIndex() < model.getRowCount()) {
					PseudonymStatistics row = model.getObject(se.getIndex());
					if("delete".equals(cmd)) {
						doConfirmDelete(ureq, row);
					}
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				doSearch((FlexiTableSearchEvent)event);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doDelete((PseudonymStatistics)confirmDeleteBox.getUserObject());
				loadModel(tableEl.getQuickSearchString());
			}
		} else if(newPseudoCtrl == source) {
			
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newPseudoCtrl);
		removeAsListenerAndDispose(cmc);
		newPseudoCtrl = null;
		cmc = null;
	}
	
	protected void doSearch(FlexiTableSearchEvent event) {
		loadModel(event.getSearch());
	}

	private void doNewPseudonym(UserRequest ureq) {
		if(newPseudoCtrl != null) return;
		
		newPseudoCtrl = new NewPseudonymController(ureq, getWindowControl());
		listenTo(newPseudoCtrl);
		
		String title = translate("new.pseudonym");
		cmc = new CloseableModalController(getWindowControl(), "close", newPseudoCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, PseudonymStatistics row) {
		String[] args = new String[] { row.getPseudonym(), row.getNumOfMessages().toString() };
		String title = translate("confirm.delete.pseudonym.title", args);
		String msg = translate("confirm.detele.pseudonym.msg", args);
		confirmDeleteBox = activateYesNoDialog(ureq, title, msg, confirmDeleteBox);
		confirmDeleteBox.setUserObject(row);
	}
	
	private void doDelete(PseudonymStatistics row) {
		Pseudonym pseudonym = forumManager.getPseudonymByKey(row.getKey());
		forumManager.deletePseudonym(pseudonym);
	}
}
