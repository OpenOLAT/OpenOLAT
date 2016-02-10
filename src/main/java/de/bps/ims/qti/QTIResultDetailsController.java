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
* <p>
* Initial code contributed and copyrighted by<br>
* BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
* <p>
*/
package de.bps.ims.qti;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.QTIResultTableModel;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.render.LocalizedXSLTransformer;
import org.olat.repository.RepositoryEntry;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;
import de.bps.webservices.clients.onyxreporter.ReporterRole;

/**
 * Initial Date: 12.01.2005
 * 
 * @author Mike Stock
 */
public class QTIResultDetailsController extends BasicController {

	private Long courseResourceableId;
	private String nodeIdent;
	private Identity identity;
	private RepositoryEntry repositoryEntry;
	private String type;

	private VelocityContainer main, details;
	private VelocityContainer onyxReporterVC;
	private QTIResultTableModel tableModel;
	private TableController tableCtr;

	private CloseableModalController cmc;
	//<ONYX-705>
	private final static OLog log = Tracing.createLoggerFor(QTIResultDetailsController.class);
	private CloseableModalController onyxCmc;
	//</ONYX-705>
	/**
	 * @param courseResourceableId
	 * @param nodeIdent
	 * @param identity
	 * @param re
	 * @param type
	 * @param ureq
	 * @param wControl
	 */
	public QTIResultDetailsController(Long courseResourceableId, String nodeIdent, Identity identity, final RepositoryEntry re, final String type,
			UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		Translator translator = Util.createPackageTranslator(org.olat.ims.qti.QTI12ResultDetailsController.class, getTranslator().getLocale(), getTranslator());
		setTranslator(translator);

		this.courseResourceableId = courseResourceableId;
		this.nodeIdent = nodeIdent;
		this.identity = identity;
		this.repositoryEntry = re;
		this.type = type;

		init(ureq);
	}

	private void init(UserRequest ureq) {
		main = createVelocityContainer("qtires");
		details = createVelocityContainer("qtires_details");

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("column.header.date", 0, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("column.header.duration", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("column.header.assesspoints", 2, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("sel", "column.header.details", getTranslator().translate("select")));

		QTIResultManager qrm = QTIResultManager.getInstance();
		tableModel = new QTIResultTableModel(qrm.getResultSets(courseResourceableId, nodeIdent, repositoryEntry.getKey(), identity), getTranslator());
		tableCtr.setTableDataModel(tableModel);
		listenTo(tableCtr);

		main.put("qtirestable", tableCtr.getInitialComponent());
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == main) {
			if (event.getCommand().equals("close")) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			TableEvent tEvent = (TableEvent) event;
			if (tEvent.getActionId().equals("sel")) {
				if (OnyxModule.isOnyxTest(repositoryEntry.getOlatResource())) {
					QTIResultSet resultSet = tableModel.getResultSet(tEvent.getRowId());
					onyxReporterVC = createVelocityContainer("onyxreporter");
					if (showOnyxReporter(ureq, resultSet.getAssessmentID())) {
						onyxCmc = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), onyxReporterVC);
						onyxCmc.activate();
						listenTo(onyxCmc);
					} else {
						getWindowControl().setError(getTranslator().translate("onyxreporter.error"));
					}
				} else {
				QTIResultSet resultSet = tableModel.getResultSet(tEvent.getRowId());
				
				Document doc = FilePersister.retreiveResultsReporting(identity, type, resultSet.getAssessmentID());
				if (doc == null) {
					showInfo("error.resreporting.na");
					return;
				}
				String resultsHTML = LocalizedXSLTransformer.getInstance(ureq.getLocale()).renderResults(doc);
				details.contextPut("reshtml", resultsHTML);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), details);
				listenTo(cmc);
				
				cmc.activate();
				}
			}
			} else if (source == onyxCmc && CloseableModalController.CLOSE_MODAL_EVENT.equals(event)) {
				QTIResultManager qrm = QTIResultManager.getInstance();
				tableModel = new QTIResultTableModel(qrm.getResultSets(courseResourceableId, nodeIdent, repositoryEntry.getKey(), identity), getTranslator());
				tableCtr.setTableDataModel(tableModel);
			}
	}

	/**
	 * This methods calls the OnyxReporter and shows it in an iframe.
	 * 
	 * @param ureq The UserRequest for getting the identity and role of the current user.
	 */
	private boolean showOnyxReporter(UserRequest ureq, long assassmentId) {
			//<ONYX-705>
			OnyxReporterConnector onyxReporter = null;
			
			try{
				onyxReporter = new OnyxReporterConnector();
			} catch (OnyxReporterException e) {
				log.error(e.getMessage(), e);
			}
			//</ONYX-705>
			if (onyxReporter != null) {
				//make a list of this one student because onyxReporter needs a list
				List<Identity> identityList = new ArrayList<Identity>();
				identityList.add(identity);
				
				CourseNode cn = CourseFactory.loadCourse(courseResourceableId).getEditorTreeModel().getCourseNode(this.nodeIdent);
				//<ONYX-705>
				String iframeSrc = "";
				try {
					iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), identityList, cn, assassmentId, ReporterRole.ASSESSMENT);
					//</ONYX-705>
				} catch (OnyxReporterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				onyxReporterVC.contextPut("showBack", Boolean.TRUE);
				onyxReporterVC.contextPut("iframeOK", Boolean.TRUE);
				onyxReporterVC.contextPut("onyxReportLink", iframeSrc);
				return true;
			} else {
				return false;
			}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}

}
