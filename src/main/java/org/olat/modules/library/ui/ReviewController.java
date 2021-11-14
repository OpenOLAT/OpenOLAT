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
package org.olat.modules.library.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.library.LibraryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * This controller is responsible for handling the review prozess of uploaded
 * files.
 * <p>
 * Initial Date: Sep 18, 2009 <br>
 * 
 * @author twuersch, gwassmann, www.frentix.com
 */
public class ReviewController extends BasicController {
	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_REJECT = "reject";
	private static final String CMD_ACCEPT = "accept";
	
	private TableController documentListTableController;
	private ReviewTableDataModel documentListModel;
	private RejectController rejectController;
	private CloseableModalController rejectModalController;
	private StepsMainRunController stepsMainRunController;
	
	private final VelocityContainer mainVC;
	
	@Autowired
	private LibraryManager libraryManager;

	public ReviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		// Build the table content
		List<VFSItem> documentsInVFS = libraryManager.getUploadFolder().getItems();
		documentListModel = new ReviewTableDataModel(documentsInVFS, ureq.getLocale());

		TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
		tableGuiConfiguration.setTableEmptyMessage(getTranslator().translate("table.empty"), null, "o_icon_library");
		documentListTableController = new TableController(tableGuiConfiguration, ureq, wControl, getTranslator());
		documentListTableController.addControllerListener(this);

		DefaultColumnDescriptor descr = new DefaultColumnDescriptor("table.header.filename", 0, "filename", getLocale());
		descr.setIsPopUpWindowAction(true, "height=700, width=900, location=yes, menubar=yes, resizable=yes, status=no, scrollbars=yes, toolbar=yes");
		documentListTableController.addColumnDescriptor(descr);
		
		descr = new DefaultColumnDescriptor("table.header.uploader", 1, null, getLocale());
		descr.setIsPopUpWindowAction(true, "height=700, width=900, location=yes, menubar=yes, resizable=yes, status=no, scrollbars=yes, toolbar=yes");
		documentListTableController.addColumnDescriptor(descr);
		
		descr = new DefaultColumnDescriptor("table.header.date", 2, null, getLocale());
		descr.setIsPopUpWindowAction(true, "height=700, width=900, location=yes, menubar=yes, resizable=yes, status=no, scrollbars=yes, toolbar=yes");
		documentListTableController.addColumnDescriptor(descr);
		
		StaticColumnDescriptor staticDescr = new StaticColumnDescriptor(CMD_DOWNLOAD, "table.header.download", getTranslator().translate("table.header.download"));
		staticDescr.setIsPopUpWindowAction(true, "height=700, width=900, location=yes, menubar=yes, resizable=yes, status=no, scrollbars=yes, toolbar=yes");
		documentListTableController.addColumnDescriptor(staticDescr);
		documentListTableController.addColumnDescriptor(new StaticColumnDescriptor(CMD_REJECT, "table.header.reject", getTranslator().translate("table.header.reject")));
		documentListTableController.addColumnDescriptor(new StaticColumnDescriptor(CMD_ACCEPT, "table.header.accept", getTranslator().translate("table.header.accept")));

		// set the data model (it's important to do this _after_ the column headers
		// have been set
		documentListTableController.setTableDataModel(documentListModel);
		
		mainVC = createVelocityContainer("review");
		mainVC.put("documents", documentListTableController.getInitialComponent());
		mainVC.contextPut("numOfDocuments", Integer.valueOf(documentsInVFS.size()));
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == documentListTableController && event instanceof TableEvent) {
			TableEvent e = (TableEvent) event;
			
			VFSLeaf file = (VFSLeaf) documentListModel.getValueAt(e.getRowId(), ReviewTableDataModel.Columns.accept.ordinal());
			if (CMD_REJECT.equals(e.getActionId())) {
				rejectController = new RejectController(ureq, getWindowControl(), file);
				listenTo(rejectController);
				rejectModalController = new CloseableModalController(getWindowControl(), translate("close"), rejectController.getInitialComponent());
				rejectModalController.activate();
			} else if (CMD_ACCEPT.equals(e.getActionId())) {
				if (libraryManager.getSharedFolder() == null) {
					showError("library.catalog.none.setup");
				} else {
					Step startStep = new MetadataAcceptStep(ureq, file.getName());
					ReviewFinishCallback finishReviewCallback = new ReviewFinishCallback();
					removeAsListenerAndDispose(stepsMainRunController);
					stepsMainRunController = new StepsMainRunController(ureq, getWindowControl(), startStep, finishReviewCallback, null,
							translate("accept.wizard.title"), "o_sel_library_review_wizard");
					listenTo(stepsMainRunController);
					getWindowControl().pushAsModalDialog(stepsMainRunController.getInitialComponent());
				}
			} else {
				MediaResource resource = new VFSMediaResource(file);
				ureq.getDispatchResult().setResultingMediaResource(resource);
			}
		} else if (source == stepsMainRunController) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(stepsMainRunController);
				//reload the documents
				reloadDocumentList(ureq);
				if (event == Event.CHANGED_EVENT) {
					fireEvent(ureq, event);
				}
			}
		} else if (source == rejectController) {
			if(event == Event.DONE_EVENT) {
				reloadDocumentList(ureq);
			}
			rejectModalController.deactivate();
			removeAsListenerAndDispose(rejectController);
			rejectController = null;
		}
	}
	
	private void reloadDocumentList(UserRequest ureq) {
		List<VFSItem> documentsInVFS = libraryManager.getUploadFolder().getItems();
		documentListModel = new ReviewTableDataModel(documentsInVFS, ureq.getLocale());
		documentListTableController.setTableDataModel(documentListModel);
		mainVC.contextPut("numOfDocuments", Integer.valueOf(documentsInVFS.size()));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// no events from components to fetch
	}
	
	public class ReviewFinishCallback implements StepRunnerCallback {
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			return StepsMainRunController.DONE_MODIFIED;
		}
	}
}
