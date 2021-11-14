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
package org.olat.ims.lti.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.LTIOutcome;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 15.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTIResultDetailsController extends BasicController {
	
	private TableController summaryTableCtr;
	
	private final Identity assessedIdentity;
	private final OLATResource resource;
	private final String resSubPath;
	private LTIManager ltiManager;

	public LTIResultDetailsController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, OLATResource resource, String resSubPath) {
		super(ureq, wControl);
		
		this.assessedIdentity = assessedIdentity;
		this.resource = resource;
		this.resSubPath = resSubPath;
		ltiManager = CoreSpringFactory.getImpl(LTIManager.class);
		init(ureq);
	}
	
	protected void init(UserRequest ureq) {
		TableGuiConfiguration summaryTableConfig = new TableGuiConfiguration();
		summaryTableConfig.setDownloadOffered(true);
		
		summaryTableCtr = new TableController(summaryTableConfig, ureq, getWindowControl(), getTranslator());
		summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.date", 0, null, ureq.getLocale()));
		summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.action", 1, null, ureq.getLocale()));
		summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.key", 2, null, ureq.getLocale()));
		summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.value", 3, null, ureq.getLocale()));

		List<LTIOutcome> outcomes = ltiManager.loadOutcomes(assessedIdentity, resource, resSubPath);
		summaryTableCtr.setTableDataModel(new OutcomeTableDataModel(outcomes));
		listenTo(summaryTableCtr);
		putInitialPanel(summaryTableCtr.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public static class OutcomeTableDataModel extends DefaultTableDataModel<LTIOutcome> {
		public OutcomeTableDataModel(List<LTIOutcome> datas) {
			super(datas);
		}

		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int row, int col) {
			LTIOutcome data = getObject(row);
			switch(col) {
				case 0: return data.getCreationDate();
				case 1: return data.getAction();
				case 2: return data.getOutcomeKey();
				case 3: return data.getOutcomeValue();
				default: return "ERROR";
			}
		}
	}
}