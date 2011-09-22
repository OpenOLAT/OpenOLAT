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
* <p>
*/ 

package org.olat.ims.qti;

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
import org.olat.core.id.Identity;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.render.LocalizedXSLTransformer;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date:  12.01.2005
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
	private QTIResultTableModel tableModel;
	private TableController tableCtr;
	
	private CloseableModalController cmc;
	
	/**
	 * @param courseResourceableId
	 * @param nodeIdent
	 * @param identity
	 * @param re
	 * @param type
	 * @param ureq
	 * @param wControl
	 */
	public QTIResultDetailsController(Long courseResourceableId, String nodeIdent, Identity identity, RepositoryEntry re, String type, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
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
		tableModel = new QTIResultTableModel(
				qrm.getResultSets(courseResourceableId, nodeIdent, repositoryEntry.getKey(), identity));
		tableCtr.setTableDataModel(tableModel);
		listenTo(tableCtr);
		
		main.put("qtirestable", tableCtr.getInitialComponent());
		putInitialPanel(main);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
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
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			TableEvent tEvent = (TableEvent)event;
			if (tEvent.getActionId().equals("sel")) {
				QTIResultSet resultSet = tableModel.getResultSet(tEvent.getRowId());
				
				Document doc = FilePersister.retreiveResultsReporting(identity, type, resultSet.getAssessmentID());
				if (doc == null) {
					showInfo("error.resreporting.na");
					return;
				}
				StringBuilder resultsHTML = LocalizedXSLTransformer.getInstance(ureq.getLocale()).renderResults(doc);
				details.contextPut("reshtml", resultsHTML);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), details);
				listenTo(cmc);
				
				cmc.activate();
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

}
