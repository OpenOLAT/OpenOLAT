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
package org.olat.portfolio.ui.artefacts.view;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm04;
import org.olat.portfolio.ui.artefacts.edit.EPReflexionWrapperController;
import org.olat.portfolio.ui.filter.PortfolioFilterController;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;

/**
 * Description:<br>
 * Controller to hold a table representation of artefacts
 * - used with a struct (inside a map) it allows to unlink artefact
 * - in chooseMode there is column to add artefact to struct
 * 
 * <P>
 * Initial Date:  20.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultipleArtefactsAsTableController extends BasicController implements EPMultiArtefactsController {

	private static final String CMD_CHOOSE = "choose";
	private static final String CMD_UNLINK = "unlink";
	private static final String CMD_REFLEXION = "refl";
	private static final String CMD_DELETEARTEFACT = "delartf";
	private static final String CMD_TITLE = "title";
	private static final String CMD_MOVE = "move";
	private VelocityContainer vC;
	private TableController artefactListTblCtrl;
	
	private CloseableModalController artefactBox;
	private PortfolioStructure struct;
	private EPFrontendManager ePFMgr;
	
	private boolean mapClosed = false;
	private boolean multiSelect = false;
	private boolean artefactChooseMode;
	private EPSecurityCallback secCallback;
	private PortfolioModule portfolioModule;
	private DialogBoxController deleteDialogController;
	private EPCollectStepForm04 moveTreeCtrl;
	private CloseableModalController moveTreeBox;

	public EPMultipleArtefactsAsTableController(UserRequest ureq, WindowControl wControl,
			List<AbstractArtefact> artefacts, PortfolioStructure struct, boolean artefactChooseMode, boolean multiSelect, EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.multiSelect = multiSelect;
		this.secCallback = secCallback;
		this.artefactChooseMode = artefactChooseMode;
		vC = createVelocityContainer("multiArtefactTable");
		this.struct = struct; 
		if(struct != null && struct.getRoot() instanceof PortfolioStructureMap) {
			mapClosed = StructureStatusEnum.CLOSED.equals(((PortfolioStructureMap)struct.getRoot()).getStatus());
		} else { 
			mapClosed = false; 
		}		
		portfolioModule = (PortfolioModule)CoreSpringFactory.getBean("portfolioModule");
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		
		if (artefacts!=null){
			initOrUpdateTable(ureq, artefacts);
		}
		
		putInitialPanel(vC);
	}

	private void initOrUpdateTable(UserRequest ureq, List<AbstractArtefact> artefacts) {
		ArtefactTableDataModel artefactListModel = new ArtefactTableDataModel(artefacts);
		artefactListModel.setLocale(getLocale());

		TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
		tableGuiConfiguration.setTableEmptyMessage(getTranslator().translate("table.empty"));
		tableGuiConfiguration.setPageingEnabled(true);
		tableGuiConfiguration.setDownloadOffered(struct == null); // offer download only when in artefact pool (no struct given)
		tableGuiConfiguration.setResultsPerPage(10);
		tableGuiConfiguration.setPreferencesOffered(true, "artefacts.as.table.prefs");
		artefactListTblCtrl = new TableController(tableGuiConfiguration, ureq, getWindowControl(), getTranslator());
		if(multiSelect) {
			artefactListTblCtrl.setMultiSelect(true);
			artefactListTblCtrl.addMultiSelectAction("select", "select");
		}
		listenTo(artefactListTblCtrl);

		String details = artefactChooseMode ? null : CMD_TITLE;
		DefaultColumnDescriptor descr = new DefaultColumnDescriptor("artefact.title", 0, details, getLocale());
		artefactListTblCtrl.addColumnDescriptor(descr);
		
		descr = new DefaultColumnDescriptor("artefact.description", 1, null, getLocale());
		descr.setEscapeHtml(EscapeMode.antisamy);
		artefactListTblCtrl.addColumnDescriptor(true, descr);
		
		descr = new DefaultColumnDescriptor("artefact.date", 2, null, getLocale());
		artefactListTblCtrl.addColumnDescriptor(true, descr);
		
		descr = new DefaultColumnDescriptor("artefact.author", 3, null, getLocale());
		artefactListTblCtrl.addColumnDescriptor(false, descr);

		descr = new DefaultColumnDescriptor("artefact.tags", 4, null, getLocale());
		artefactListTblCtrl.addColumnDescriptor(false, descr);

		descr = new CustomRenderColumnDescriptor("table.header.type", 5, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_CENTER, new ArtefactTypeImageCellRenderer(getLocale())){
			/**
			 * @see org.olat.core.gui.components.table.DefaultColumnDescriptor#compareTo(int, int)
			 */
			@Override
			public int compareTo(int rowa, int rowb) {
				Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
				Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
				String typeA = getArtefactTranslatedTypeName((AbstractArtefact)a);
				String typeB = getArtefactTranslatedTypeName((AbstractArtefact)b);
				return typeA.compareTo(typeB);
			}			
		};
		artefactListTblCtrl.addColumnDescriptor(false, descr);
		
		StaticColumnDescriptor staticDescr;
		
		if(!artefactChooseMode) {
			if(mapClosed || !secCallback.canEditReflexion()) { // change link-description in row, when map is closed or viewed by another person
				staticDescr = new StaticColumnDescriptor(CMD_REFLEXION, "table.header.reflexion", translate("table.header.view"));
			} else {
				staticDescr = new StaticColumnDescriptor(CMD_REFLEXION, "table.header.reflexion", translate("table.row.reflexion"));
			}
			artefactListTblCtrl.addColumnDescriptor(true, staticDescr);
		}
		
		if(struct == null){
			staticDescr = new StaticColumnDescriptor(CMD_DELETEARTEFACT, "delete.artefact", translate("delete.artefact"));
			artefactListTblCtrl.addColumnDescriptor(true,staticDescr);
		}
				
		if (artefactChooseMode) {
			staticDescr = new StaticColumnDescriptor(CMD_CHOOSE, "table.header.choose", translate("choose.artefact"));
			artefactListTblCtrl.addColumnDescriptor(true, staticDescr);
		}
		
		if(struct!=null && secCallback.canRemoveArtefactFromStruct()){
			staticDescr = new StaticColumnDescriptor(CMD_UNLINK, "table.header.unlink", translate("remove.from.map"));
			artefactListTblCtrl.addColumnDescriptor(true, staticDescr);			
		}
		
		if(struct!=null && secCallback.canRemoveArtefactFromStruct() && secCallback.canAddArtefact()){
			staticDescr = new StaticColumnDescriptor(CMD_MOVE, "table.header.move", translate("artefact.options.move"));
			artefactListTblCtrl.addColumnDescriptor(true, staticDescr);			
		}

		artefactListTblCtrl.setTableDataModel(artefactListModel);
		if (vC.getComponent("artefactTable")!=null) vC.remove(artefactListTblCtrl.getInitialComponent()); 
		vC.put("artefactTable", artefactListTblCtrl.getInitialComponent());		
	}
	
	// translate the type of artefact needed for sorting by type
	String getArtefactTranslatedTypeName(AbstractArtefact artefact){
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
		Translator handlerTrans = artHandler.getHandlerTranslator(getTranslator());
		String handlerClass = PortfolioFilterController.HANDLER_PREFIX + artHandler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
		String artType = handlerTrans.translate(handlerClass);
		return artType;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == artefactListTblCtrl) {
			if(event instanceof TableEvent) {
				TableEvent te = (TableEvent)event;
				AbstractArtefact artefact = (AbstractArtefact)artefactListTblCtrl.getTableDataModel().getObject(te.getRowId());
				String action = te.getActionId();
				if(CMD_TITLE.equals(action)) {
					popupArtefact(artefact, ureq);
				} else if (CMD_REFLEXION.equals(action)){
					EPReflexionWrapperController reflexionCtrl = new EPReflexionWrapperController(ureq, getWindowControl(), secCallback, artefact, struct);
					listenTo(reflexionCtrl);
				} else if (CMD_CHOOSE.equals(action)){
					fireEvent(ureq, new EPArtefactChoosenEvent(artefact));
				} else if (CMD_UNLINK.equals(action)){
					struct = ePFMgr.reloadPortfolioStructure(struct);
					ePFMgr.removeArtefactFromStructure(artefact, struct);
					artefactListTblCtrl.modelChanged();
					fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, struct));
				} else if (CMD_MOVE.equals(action)){
					showMoveTree(ureq, artefact);
				} else if (CMD_DELETEARTEFACT.equals(action)) {
					String text = translate("delete.artefact.text", StringHelper.escapeHtml(artefact.getTitle()));
					deleteDialogController = activateYesNoDialog(ureq, translate("delete.artefact"), text, deleteDialogController);
					deleteDialogController.setUserObject(artefact);
				}
			} else if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent)event;

				BitSet objectMarkers = tmse.getSelection();
				List<AbstractArtefact> selectedArtefacts = new ArrayList<>(objectMarkers.size());
				for(int i=objectMarkers.nextSetBit(0); i >= 0; i=objectMarkers.nextSetBit(i+1)) {
					AbstractArtefact entry =  (AbstractArtefact)artefactListTblCtrl.getTableDataModel().getObject(i);
					selectedArtefacts.add(entry);
				}
				fireEvent(ureq, new EPArtefactListChoosenEvent(selectedArtefacts));
			}
		} else if (source == moveTreeCtrl && event.getCommand().equals(EPStructureChangeEvent.CHANGED)){
			EPStructureChangeEvent epsEv = (EPStructureChangeEvent) event;
			PortfolioStructure newStruct = epsEv.getPortfolioStructure();
			showInfo("artefact.moved", newStruct.getTitle());
			fireEvent(ureq, event);
			moveTreeBox.deactivate();
		} else if(source == deleteDialogController){
			if (DialogBoxUIFactory.isYesEvent(event)) {
				AbstractArtefact artefact2delete = (AbstractArtefact)deleteDialogController.getUserObject();
				ePFMgr.deleteArtefact(artefact2delete);
				ArtefactTableDataModel model = (ArtefactTableDataModel)  artefactListTblCtrl.getTableDataModel();
				model.getObjects().remove(artefact2delete);
				artefactListTblCtrl.modelChanged();
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.REMOVED, struct));
			}
			removeAsListenerAndDispose(deleteDialogController);
			deleteDialogController = null;
		}
		
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		OLATResourceable ores = entries.get(0).getOLATResourceable();
		if("AbstractArtefact".equals(ores.getResourceableTypeName())) {
			Long resId = ores.getResourceableId();
			ArtefactTableDataModel model = (ArtefactTableDataModel)  artefactListTblCtrl.getTableDataModel();
			for(int i=0; i< model.getRowCount(); i++) {
				AbstractArtefact artefact = model.getObject(i);
				if(artefact.getKey().equals(resId)) {
					int artefactsPerPage = artefactListTblCtrl.getPageSize();
					int rest = (i % artefactsPerPage);
					int page = (i - rest) / artefactsPerPage;
					artefactListTblCtrl.setPage(new Integer(page + 1));
					break;
				}	
			}
		}
	}
	
	private void showMoveTree(UserRequest ureq, AbstractArtefact artefact){
		moveTreeCtrl = new EPCollectStepForm04(ureq, getWindowControl(), artefact, struct);
		listenTo(moveTreeCtrl);
		String title = translate("artefact.move.title");
		moveTreeBox = new CloseableModalController(getWindowControl(), title, moveTreeCtrl.getInitialComponent());
		listenTo(moveTreeBox);
		//moveTreeBox.setInitialWindowSize(450, 300);
		moveTreeBox.activate();
	}

	protected void popupArtefact(AbstractArtefact artefact, UserRequest ureq) {
		String title = translate("view.artefact.header");
		artefactBox = EPUIFactory.getAndActivatePopupArtefactController(artefact, ureq, getWindowControl(), title);
		listenTo(artefactBox);
	}

	@Override
	public void setNewArtefactsList(UserRequest ureq, List<AbstractArtefact> artefacts) {
		initOrUpdateTable(ureq, artefacts);		
	}
}
