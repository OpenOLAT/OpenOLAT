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

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.model.ViteroBooking;
import com.ibm.icu.util.Calendar;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroRoomsOverviewController extends BasicController {
	
	private final TableController tableCtr;
	private final ViteroManager viteroManager;
	
	public ViteroRoomsOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		
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
		
		Calendar cal = Calendar.getInstance();
		Date begin = cal.getTime();
		cal.add(Calendar.DATE, 2);
		Date end = cal.getTime();
		
		List<ViteroBooking> bookings = viteroManager.getBookingByDate(begin, end);
		ViteroBookingDataModel tableModel = new ViteroBookingDataModel(bookings);
		tableCtr.setTableDataModel(tableModel);
		
		putInitialPanel(tableCtr.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}


	
	

}
