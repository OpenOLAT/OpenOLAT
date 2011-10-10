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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.model.ViteroBooking;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date: 6 oct. 2011 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingsController extends BasicController {

	private final VelocityContainer runVC;
	private final TableController tableCtr;
	private final ViteroManager viteroManager;

	public ViteroBookingsController(UserRequest ureq, WindowControl wControl,
			BusinessGroup group, OLATResourceable ores) {
		super(ureq, wControl);

		viteroManager = (ViteroManager) CoreSpringFactory.getBean("viteroManager");

		List<ViteroBooking> bookings = viteroManager.getBookings(group, ores);
		TableDataModel tableData = new ViteroBookingDataModel(bookings);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("vc.table.empty"));
		tableConfig.setColumnMovingOffered(true);
		tableConfig.setSortingEnabled(true);
		tableCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.begin", ViteroBookingDataModel.Column.begin.ordinal(), null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", ViteroBookingDataModel.Column.end.ordinal(), null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("start", "start", translate("start")));

		tableCtr.setTableDataModel(tableData);
		tableCtr.setSortColumn(1, true);// timeframe
		listenTo(tableCtr);

		runVC = createVelocityContainer("run");
		runVC.put("bookings", tableCtr.getInitialComponent());

		putInitialPanel(runVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent)event;
				int row = e.getRowId();
				ViteroBooking booking = (ViteroBooking)tableCtr.getTableDataModel().getObject(row);
				if("open".equals(e.getActionId())) {
					openVitero(ureq, booking);
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	protected void openVitero(UserRequest ureq, ViteroBooking booking) {
		String url = viteroManager.getURLToBooking(ureq.getIdentity(), booking);
		RedirectMediaResource redirect = new RedirectMediaResource(url);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
	}
}