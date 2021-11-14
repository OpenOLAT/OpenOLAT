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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.StartBookingComparator;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroStatus;
import org.springframework.beans.factory.annotation.Autowired;

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
	private CloseableModalController cmc;
	private VelocityContainer viteroGroupVC;
	
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final String subIdentifier;
	@Autowired
	private ViteroManager viteroManager;

	public ViteroBookingsController(UserRequest ureq, WindowControl wControl,
			BusinessGroup group, OLATResourceable ores, String subIdentifier, boolean readOnly) {
		super(ureq, wControl);

		this.ores = ores;
		this.group = group;
		this.subIdentifier = subIdentifier;

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("table.empty"), null, "o_cal_icon");
		tableConfig.setSortingEnabled(true);
		tableCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("group.name", ViteroBookingDataModel.Column.name.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("booking.begin", ViteroBookingDataModel.Column.begin.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("booking.end", ViteroBookingDataModel.Column.end.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new OpenGroupColumnDescriptor("booking.group", ViteroBookingDataModel.Column.group.ordinal(), getLocale(), getTranslator()));

		if(!readOnly) {
			StartColumnDescriptor startRoom = new StartColumnDescriptor("start", "start", ureq.getLocale(), viteroManager, getTranslator());
			startRoom.setIsPopUpWindowAction(true, "");
			tableCtr.addColumnDescriptor(startRoom);
			
			tableCtr.addColumnDescriptor(new SignColumnDescriptor("signin", ViteroBookingDataModel.Column.sign.ordinal(), ureq.getLocale(), getTranslator()));
		}
		loadModel();
		
		tableCtr.setSortColumn(1, true);// timeframe
		listenTo(tableCtr);

		runVC = createVelocityContainer("run");
		runVC.put("bookings", tableCtr.getInitialComponent());

		putInitialPanel(runVC);
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
				if("start".equals(e.getActionId())) {
					openVitero(ureq, booking);
				} else if("signin".equals(e.getActionId())) {
					signInVitero(ureq, booking);
				} else if("signout".equals(e.getActionId())) {
					signOutVitero(ureq, booking);
				} else if("opengroup".equals(e.getActionId())) {
					openGroup(ureq, booking);
				}
			}
		} else if (source == cmc) {
			removeAsListenerAndDispose(cmc);
			viteroGroupVC = null;
			cmc = null;
		}
		super.event(ureq, source, event);
	}
	
	protected void loadModel() {
		try {
			List<ViteroBooking> bookings = viteroManager.getBookings(group, ores, subIdentifier);
			List<ViteroBooking> myBookings = viteroManager.getBookingInFutures(getIdentity());
			FilterBookings.filterMyFutureBookings(bookings, myBookings);
			Collections.sort(bookings, new StartBookingComparator());
			TableDataModel<ViteroBooking> tableData = new ViteroBookingDataModel(bookings, myBookings);
			tableCtr.setTableDataModel(tableData);
		} catch (VmsNotAvailableException e) {
			TableDataModel<ViteroBooking> tableData = new ViteroBookingDataModel();
			tableCtr.setTableDataModel(tableData);
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void openGroup(UserRequest ureq, ViteroBooking booking) {
		try {
			String url = viteroManager.getURLToGroup(ureq.getIdentity(), booking);
			viteroGroupVC = createVelocityContainer("opengroup");
			viteroGroupVC.contextPut("groupUrl", url);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), viteroGroupVC);
			listenTo(cmc);
			cmc.activate();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void signInVitero(UserRequest ureq, ViteroBooking booking) {
		try {
			ViteroStatus status = viteroManager.addToRoom(booking, ureq.getIdentity(), null);
			if(status.isOk()) {
				showInfo("signin.ok");
			} else {
				showError("signin.nok");
			}
			loadModel();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void signOutVitero(UserRequest ureq, ViteroBooking booking) {
		try {
			ViteroStatus status = viteroManager.removeFromRoom(booking, ureq.getIdentity());
			if(status.isOk()) {
				showInfo("signout.ok");
			} else {
				showError("signout.nok");
			}
			loadModel();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void openVitero(UserRequest ureq, ViteroBooking booking) {
		try {
			String url = viteroManager.getURLToBooking(ureq.getIdentity(), booking);
			RedirectMediaResource redirect = new RedirectMediaResource(url);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		} 
	}
}