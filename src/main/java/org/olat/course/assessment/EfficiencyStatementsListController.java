/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.ControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.assessment.portfolio.EfficiencyStatementArtefact;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.artefacts.collect.ArtefactWizzardStepsController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * Shows the list of all efficiency statements for this user
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class EfficiencyStatementsListController extends BasicController {
	private static final String CMD_SHOW = "cmd.show";
	private static final String CMD_LAUNCH_COURSE = "cmd.launch.course";
	private static final String CMD_DELETE = "cmd.delete";
	private static final String CMD_ARTEFACT = "cmd.artefact";

	private TableController tableCtr;
	private EfficiencyStatementsListModel efficiencyStatementsListModel;
	private DialogBoxController confirmDeleteCtr;
	private UserEfficiencyStatementLight efficiencyStatement;
	private Controller ePFCollCtrl;
	private PortfolioModule portfolioModule;
	
	/**
	 * Constructor
	 * @param wControl
	 * @param ureq
	 */
	public EfficiencyStatementsListController(UserRequest ureq, WindowControl wControl) { 
		super(ureq, wControl);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("efficiencyStatementsPortlet.nostatements"));
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.course", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.score", 1, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
		tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", 2, translate("passed.true"), translate("passed.false")));
		StaticColumnDescriptor cd3 = new StaticColumnDescriptor(CMD_SHOW, "table.header.show", translate("table.action.show"));
		cd3.setIsPopUpWindowAction(true, "height=600, width=800, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
		tableCtr.addColumnDescriptor(cd3);
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_LAUNCH_COURSE, "table.header.launchcourse", translate("table.action.launchcourse")));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_DELETE, "table.header.delete", translate("table.action.delete")));

		portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
		if(portfolioModule.isEnabled() && artHandler != null && artHandler.isEnabled()) {
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.artefact", 5, CMD_ARTEFACT, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_CENTER, new AsArtefactCellRenderer()));
		}
		listenTo(tableCtr);
		
		EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
		List<UserEfficiencyStatementLight> efficiencyStatementsList = esm.findEfficiencyStatementsLight(ureq.getIdentity());
		efficiencyStatementsListModel = new EfficiencyStatementsListModel(efficiencyStatementsList);
		tableCtr.setTableDataModel(efficiencyStatementsListModel);

		putInitialPanel(tableCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				efficiencyStatement = efficiencyStatementsListModel.getEfficiencyStatementAt(rowid);
				if (actionid.equals(CMD_SHOW)) {					
					// will not be disposed on course run dispose, popus up as new browserwindow
					ControllerCreator ctrlCreator = new ControllerCreator() {
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							EfficiencyStatementController efficiencyCtrl = new EfficiencyStatementController(lwControl, lureq, efficiencyStatement.getCourseRepoKey());
							return new LayoutMain3ColsController(lureq, getWindowControl(), null, null, efficiencyCtrl.getInitialComponent(), null);
						}					
					};
					//wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
					//open in new browser window
					PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
					pbw.open(ureq);
					//
				} else if (actionid.equals(CMD_LAUNCH_COURSE)) {
					RepositoryManager rm = RepositoryManager.getInstance();
					RepositoryEntry re = rm.lookupRepositoryEntry(efficiencyStatement.getCourseRepoKey(), false);
					if (re == null) {
						showWarning("efficiencyStatements.course.noexists");
					} else if (!rm.isAllowedToLaunch(ureq, re)) {
						showWarning("efficiencyStatements.course.noaccess");
					} else {
						OLATResourceable ores = re.getOlatResource();
						//was brasato:: DTabs dts = getWindowControl().getDTabs();
						DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
						DTab dt = dts.getDTab(ores);
						if (dt == null) {
							// does not yet exist -> create and add
							//fxdiff BAKS-7 Resume function
							dt = dts.createDTab(ores, re, efficiencyStatement.getShortTitle());
							if (dt == null) return;
							Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dt.getWindowControl(), true);
							dt.setController(launchController);
							dts.addDTab(dt);
						}
						dts.activate(ureq, dt, null);  // null: do not activate to a certain view

					}
				} else if (actionid.equals(CMD_DELETE)) {					
					// show confirmation dialog
					confirmDeleteCtr = activateYesNoDialog(ureq, null, translate("efficiencyStatements.delete.confirm", efficiencyStatement.getShortTitle()), confirmDeleteCtr);
					return;
				} else if (actionid.equals(CMD_ARTEFACT)) {
					popupArtefactCollector(ureq);
				}
			}
		} else if (source == confirmDeleteCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				// delete efficiency statement manager
				EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
				esm.deleteEfficiencyStatement(efficiencyStatement);
				efficiencyStatementsListModel.getObjects().remove(efficiencyStatement);
				efficiencyStatement = null;
				tableCtr.modelChanged();
				showInfo("info.efficiencyStatement.deleted");
			}
		}
	}
	
	private void popupArtefactCollector(UserRequest ureq) {
		EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(EfficiencyStatementArtefact.ARTEFACT_TYPE);
		if(artHandler != null && artHandler.isEnabled()) {
			AbstractArtefact artefact = artHandler.createArtefact();
			artefact.setAuthor(getIdentity());//only author can create artefact
			//no business path becouse we cannot launch an efficiency statement
			artefact.setCollectionDate(new Date());
			artefact.setTitle(translate("artefact.title", new String[]{efficiencyStatement.getShortTitle()}));
			EfficiencyStatement fullStatement = EfficiencyStatementManager.getInstance().getUserEfficiencyStatementByKey(efficiencyStatement.getKey());
			artHandler.prefillArtefactAccordingToSource(artefact, fullStatement);
			ePFCollCtrl = new ArtefactWizzardStepsController(ureq, getWindowControl(), artefact, (VFSContainer)null);
			listenTo(ePFCollCtrl);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// DialogBoxController and TableController get disposed by BasicController
	}
	
	public class AsArtefactCellRenderer implements CustomCellRenderer {
		/**
		 * @see org.olat.core.gui.components.table.CustomCellRenderer#render(org.olat.core.gui.render.StringOutput, org.olat.core.gui.render.Renderer, java.lang.Object, java.util.Locale, int, java.lang.String)
		 */
		@SuppressWarnings("unused")
		@Override
		public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
			sb.append("<span class=\"b_eportfolio_add_again\" title=\"")
				.append(translate("table.add.as.artefact"))
				.append("\" style=\"background-repeat: no-repeat; background-position: center center; padding: 2px 8px;\">&nbsp;</span>");
		}
	}
}
