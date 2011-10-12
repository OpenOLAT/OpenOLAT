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
package com.frentix.olat.course.nodes.vitero;

import java.util.Collections;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.run.userview.UserCourseEnvironment;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.manager.VmsNotAvailableException;
import com.frentix.olat.vitero.model.StartBookingComparator;
import com.frentix.olat.vitero.model.ViteroBooking;
import com.frentix.olat.vitero.ui.FilterBookings;
import com.frentix.olat.vitero.ui.ViteroBookingDataModel;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroPeekViewController extends BasicController {


	public ViteroPeekViewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		ViteroManager viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class,
				userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		
		List<ViteroBooking> bookings;
		try {
			bookings = viteroManager.getBookings(null, ores);
			List<ViteroBooking> myBookings = viteroManager.getBookingInFutures(getIdentity());
			FilterBookings.filterMyFutureBookings(bookings, myBookings);
			Collections.sort(bookings, new StartBookingComparator());
		} catch (VmsNotAvailableException e) {
			bookings = Collections.emptyList();
			showError(VmsNotAvailableException.I18N_KEY);
		}

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("vc.table.empty"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("b_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		
		TableController tableCtrl = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtrl);
		
		// dummy header key, won't be used since setDisplayTableHeader is set to false
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.begin", ViteroBookingDataModel.Column.begin.ordinal(), null, ureq.getLocale()));
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", ViteroBookingDataModel.Column.end.ordinal(), null, ureq.getLocale()));
		tableCtrl.setTableDataModel(new ViteroBookingDataModel(bookings));

		putInitialPanel(tableCtrl.getInitialComponent());
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
