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

import org.apache.logging.log4j.Level;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.ui.QuestionsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Manage the list of levels
 * 
 * Initial date: 18.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QEducationalContextsAdminController extends FormBasicController {
	
	private FormLink createType;
	
	private LevelDataModel model;
	private FlexiTableElement tableEl;

	private CloseableModalController cmc;
	private QEducationalContextEditController editCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private SingleKeyTranslatorController singleKeyTrnsCtrl;
	
	@Autowired
	private QPoolService qpoolService;
	
	public QEducationalContextsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, "levels_admin", Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		initForm(ureq);
		reloadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.id.i18nKey(), Cols.id.ordinal(), true, "key"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.level.i18nKey(), Cols.level.ordinal(), true, "level"));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("translate", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.levelI18n.i18nKey(), Cols.levelI18n.ordinal(), "translate", renderer));
		FlexiCellRenderer delRenderer = new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("delete"), "delete-level"), null);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", Cols.deletable.ordinal(), "delete-level", delRenderer));

		model = new LevelDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "levels", model, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(false);
		
		createType = uifactory.addFormLink("create.level", formLayout, Link.BUTTON);
		createType.setElementCssClass("o_sel_add_level");
	}
	
	private void reloadModel() {
		List<QEducationalContext> rows = qpoolService.getAllEducationlContexts();
		model.setObjects(rows);
		tableEl.reset();	
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createType) {
			doEdit(ureq, null);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete-level".equals(se.getCommand())) {
					QEducationalContext row = model.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				} else if("translate".equals(se.getCommand())) {
					QEducationalContext row = model.getObject(se.getIndex());
					doOpenTranslationTool(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editCtrl) {
			if(event == Event.DONE_EVENT) {
				reloadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteCtrl) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				QEducationalContext level = (QEducationalContext)confirmDeleteCtrl.getUserObject();
				doDelete(level);
			}
		} else if(source == singleKeyTrnsCtrl) {
			reloadModel();
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTranslationTool(UserRequest ureq, QEducationalContext row) {
		String key2Translate = "item.level." + row.getLevel().toLowerCase();
		singleKeyTrnsCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), key2Translate,
				QuestionsController.class);
		listenTo(singleKeyTrnsCtrl);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", singleKeyTrnsCtrl.getInitialComponent(), true,
				translate("translation"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, QEducationalContext level) {
		String title = translate("delete.level");
		String text = translate("delete.level.confirm", new String[]{ level.getLevel() });
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(level);
	}
	
	private void doDelete(QEducationalContext level) {
		if(qpoolService.deleteEducationalContext(level)) {
			reloadModel();
			showInfo("educational.context.deleted");
		} else {
			showError("educational.context.notdeleted");
		}
	}
	
	private void doEdit(UserRequest ureq, QEducationalContext level) {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new QEducationalContextEditController(ureq, getWindowControl(), level);
		listenTo(editCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCtrl.getInitialComponent(), true, translate("create.level"));
		cmc.activate();
		listenTo(cmc);	
	}
	
	private enum Cols {
		id("level.key"),
		level("level.level"),
		levelI18n("level.translation"),
		deletable("level.deletable");
		
		private final String i18nKey;
	
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	private class LevelDataModel extends DefaultFlexiTableDataModel<QEducationalContext> {
		
		public LevelDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			QEducationalContext level = getObject(row);
			switch(Cols.values()[col]) {
				case id: return level.getKey();
				case level: return level.getLevel();
				case levelI18n: {
					String i18nKey = "item.level." + level.getLevel().toLowerCase();
					String translation = getTranslator().translate(i18nKey, null, Level.OFF);
					if(i18nKey.equals(translation) || translation.length() > 256) {
						return level.getLevel();
					}
					return translation;
				}
				case deletable: return level.isDeletable();
				default: return "";
			}
		}
	}
}
