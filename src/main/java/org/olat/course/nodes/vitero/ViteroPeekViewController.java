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
package org.olat.course.nodes.vitero;

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
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.StartBookingComparator;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.ui.FilterBookings;
import org.olat.modules.vitero.ui.ViteroBookingDataModel;

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


	public ViteroPeekViewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, String subIdentifier) {
		super(ureq, wControl);

		ViteroManager viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class,
				userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		
		List<ViteroBooking> bookings;
		try {
			bookings = viteroManager.getBookings(null, ores, subIdentifier);
			List<ViteroBooking> myBookings = viteroManager.getBookingInFutures(getIdentity());
			FilterBookings.filterMyFutureBookings(bookings, myBookings);
			Collections.sort(bookings, new StartBookingComparator());
		} catch (VmsNotAvailableException e) {
			bookings = Collections.emptyList();
			showError(VmsNotAvailableException.I18N_KEY);
		}

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("table.empty"), null, "o_cal_icon");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		
		TableController tableCtrl = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtrl);
		
		// dummy header key, won't be used since setDisplayTableHeader is set to false
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.group", ViteroBookingDataModel.Column.name.ordinal(), null, ureq.getLocale()));
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.begin", ViteroBookingDataModel.Column.begin.ordinal(), null, ureq.getLocale()));
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", ViteroBookingDataModel.Column.end.ordinal(), null, ureq.getLocale()));
		tableCtrl.setTableDataModel(new ViteroBookingDataModel(bookings));

		putInitialPanel(tableCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
