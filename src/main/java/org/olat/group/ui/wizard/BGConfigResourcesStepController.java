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
package org.olat.group.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.group.ui.edit.BusinessGroupEditResourceController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.ui.RepositoryTableModel;
import org.olat.repository.ui.author.RepositoryEntrySmallDetailsController;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGConfigResourcesStepController extends StepFormBasicController {
	
	private FormLink addResource;
	private TableController resourcesCtr;
	private RepositoryTableModel repoTableModel;
	
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtrl;
	private RepositoryEntrySmallDetailsController infosCtrl;
	private ReferencableEntriesSearchController repoSearchCtr;
	
	public BGConfigResourcesStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "resources");
		setTranslator(Util.createPackageTranslator(BusinessGroupEditResourceController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addResource = uifactory.addFormLink("cmd.addresource", formLayout, Link.BUTTON);

		Translator resourceTrans = Util.createPackageTranslator(RepositoryTableModel.class, getLocale(), getTranslator());
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("config.resources.noresources"));
		resourcesCtr = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans);
		listenTo(resourcesCtr);

		repoTableModel = new RepositoryTableModel(getLocale());
		repoTableModel.addColumnDescriptors(resourcesCtr, false, false, true, true);
		resourcesCtr.setTableDataModel(repoTableModel);
		
		((FormLayoutContainer)formLayout).put("resources", resourcesCtr.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == repoSearchCtr) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done
				RepositoryEntry re = repoSearchCtr.getSelectedEntry();
				removeAsListenerAndDispose(repoSearchCtr);
				cmc.deactivate();
				if (re != null && !repoTableModel.getObjects().contains(re)) {
					// check if already in model
					repoTableModel.addObject(re);
					resourcesCtr.modelChanged();
					flc.setDirty(true);
				}
			}else if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				// repository search controller done
				List<RepositoryEntry> res = repoSearchCtr.getSelectedEntries();
				removeAsListenerAndDispose(repoSearchCtr);
				cmc.deactivate();
				if (res != null && !res.isEmpty()) {
					// check if already in model
					List<RepositoryEntry> entries = new ArrayList<>(res.size());
					for(RepositoryEntry re:res) {
						if(!repoTableModel.getObjects().contains(re)) {
							entries.add(re);
						}
					}
					repoTableModel.addObjects(entries);
					resourcesCtr.modelChanged();
					flc.setDirty(true);
				}
			}
		} else if (source == resourcesCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				RepositoryEntry re = repoTableModel.getObject(te.getRowId());
				if (actionid.equals(RepositoryTableModel.TABLE_ACTION_REMOVE_LINK)) {
					//present dialog box if resource should be removed
					if(repoTableModel.getObjects().remove(re)) {
						resourcesCtr.modelChanged();
					}
				} else if(RepositoryTableModel.TABLE_ACTION_INFOS.equals(actionid)) {
					int row = resourcesCtr.getIndexOfSortedObject(re);
					doOpenInfos(ureq, re, row);
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addResource) {
			removeAsListenerAndDispose(repoSearchCtr);
			removeAsListenerAndDispose(cmc);
			
			repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[]{CourseModule.getCourseTypeName()},
					translate("resources.add"), true, true, true, false, true, false);
			listenTo(repoSearchCtr);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), this.repoSearchCtr.getInitialComponent(), true, translate("resources.add.title"));
			listenTo(cmc);
			cmc.activate();
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	@Override
	protected void formOK(UserRequest ureq) {
		BGConfigBusinessGroup configuration = (BGConfigBusinessGroup)getFromRunContext("configuration");
		if(configuration == null) {
			configuration = new BGConfigBusinessGroup();
			addToRunContext("configuration", configuration);
		}
		List<RepositoryEntry> entries = repoTableModel.getObjects();
		configuration.setResources(entries);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doOpenInfos(UserRequest ureq, RepositoryEntry repositoryEntry, int rowId) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(infosCtrl);
		
		infosCtrl = new RepositoryEntrySmallDetailsController(ureq, getWindowControl(), repositoryEntry);
		listenTo(infosCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), infosCtrl.getInitialComponent(),
				"ore" + rowId + "ref", null, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
}