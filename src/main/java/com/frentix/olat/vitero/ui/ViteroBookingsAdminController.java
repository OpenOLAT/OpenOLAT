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
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.RedirectMediaResource;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.model.ViteroBooking;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingsAdminController extends BasicController {
	
	private final ViteroManager viteroManager;
	
	private DialogBoxController dialogCtr;
	private final TableController tableCtr;
	
	public ViteroBookingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		viteroManager = (ViteroManager) CoreSpringFactory.getBean("viteroManager");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("vc.table.empty"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(true);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.begin", ViteroBookingDataModel.Column.begin.ordinal(), null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", ViteroBookingDataModel.Column.end.ordinal(), null, ureq.getLocale()));
		
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("start", "start", translate("start")));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "delete", translate("delete")));

		reloadModel();
		
		putInitialPanel(tableCtr.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//auto disposed
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent)event;
				int row = e.getRowId();
				ViteroBooking booking = (ViteroBooking)tableCtr.getTableDataModel().getObject(row);
				if("start".equals(e.getActionId())) {
					openVitero(ureq, booking);
				} else if("delete".equals(e.getActionId())) {
					confirmDeleteVitero(ureq, booking);
				}
			}
		} else if(source == dialogCtr) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				ViteroBooking booking = (ViteroBooking)dialogCtr.getUserObject();
				deleteBooking(ureq, booking);
			}
		}
	}
	
	protected void deleteBooking(UserRequest ureq, ViteroBooking booking) {
		if( viteroManager.deleteBooking(booking)) {
			showInfo("vc.table.delete");
		} else {
			showError("vc.table.delete");
		}
		reloadModel();
	}
	
	protected void confirmDeleteVitero(UserRequest ureq, ViteroBooking booking) {
		String title = translate("vc.table.delete");
		String text = translate("vc.table.delete.confirm");
		dialogCtr = activateOkCancelDialog(ureq, title, text, dialogCtr);
		dialogCtr.setUserObject(booking);
	}
	
	protected void openVitero(UserRequest ureq, ViteroBooking booking) {
		String url = viteroManager.getURLToBooking(ureq.getIdentity(), booking);
		RedirectMediaResource redirect = new RedirectMediaResource(url);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
	}
	
	protected void reloadModel() {
		List<ViteroBooking> bookings = viteroManager.getBookings();
		ViteroBookingDataModel tableModel = new ViteroBookingDataModel(bookings);
		tableCtr.setTableDataModel(tableModel);
	}
}