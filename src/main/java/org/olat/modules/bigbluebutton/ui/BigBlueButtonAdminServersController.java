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
package org.olat.modules.bigbluebutton.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonServerInfos;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonAdminServersTableModel.ServersCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonAdminServersController extends FormBasicController {
	
	private FlexiTableElement serversTableEl;
	private BigBlueButtonAdminServersTableModel serversTableModel;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonAdminServersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "servers_admin");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.status, new StatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.url, new ServerCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.capacityFactor, new CapacityFactorCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.numberMeetings));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.moderatorCount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.participantCount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.listenerCount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.voiceParticipantCount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.videoCount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.maxUsers));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.recordingMeetings));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.breakoutRecordingMeetings));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ServersCols.load, new CapacityFactorCellRenderer()));
		
		serversTableModel = new BigBlueButtonAdminServersTableModel(columnsModel);
		
		serversTableEl = uifactory.addTableElement(getWindowControl(), "servers", serversTableModel, 10, false, getTranslator(), formLayout);
		serversTableEl.setCustomizeColumns(true);
		serversTableEl.setEmptyTableMessageKey("bigbluebutton.servers.empty");
		
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("filter.all.instances"), "all"));
		filters.add(new FlexiTableFilter(translate("filter.this.instance"), "this"));
		serversTableEl.setFilters("", filters, false);
	}
	
	private void loadModel() {
		List<BigBlueButtonServer> servers = bigBlueButtonManager.getServers();
		
		List<BigBlueButtonServerInfos> serversInfos = bigBlueButtonManager.getServersInfos();
		Map<BigBlueButtonServer, BigBlueButtonServerInfos> serversToInfos = serversInfos.stream()
				.collect(Collectors.toMap(BigBlueButtonServerInfos::getServer, infos -> infos, (u, v) -> u));
		
		List<BigBlueButtonServerInfos> instanceServersInfos = bigBlueButtonManager.filterServersInfos(serversInfos);
		Map<BigBlueButtonServer, BigBlueButtonServerInfos> instanceServersToInfos = instanceServersInfos.stream()
				.collect(Collectors.toMap(BigBlueButtonServerInfos::getServer, infos -> infos, (u, v) -> u));
		
		List<BigBlueButtonServerRow> rows = servers.stream()
				.map(server -> new BigBlueButtonServerRow(server, serversToInfos.get(server), instanceServersToInfos.get(server)))
				.collect(Collectors.toList());
		
		serversTableModel.setObjects(rows);
		serversTableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
