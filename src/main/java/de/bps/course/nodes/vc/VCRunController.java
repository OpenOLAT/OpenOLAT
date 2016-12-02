//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import de.bps.course.nodes.vc.provider.VCProvider;

/**
 *
 * Description:<br>
 * Run view for virtual classroom course node
 *
 * <P>
 * Initial Date:  18.01.2011 <br>
 * @author skoeber
 */
public class VCRunController extends BasicController {

  //objects for run view
  private VelocityContainer runVC;
  private Controller displayCtr;
  private TableController tableCtr;

  // data
  private VCConfiguration config;
  private VCDatesTableDataModel tableData;
  private List<MeetingDate> dateList = new ArrayList<MeetingDate>();
  private MeetingDate meeting;

  public VCRunController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description, VCConfiguration config, VCProvider provider,
		  boolean isModerator, boolean readOnly) {
    super(ureq, wControl);
    this.config = config;

    if(this.config.getMeetingDates() != null) dateList.addAll(this.config.getMeetingDates());

    // select actual meeting
    if(config.isUseMeetingDates()) {
      Date now = new Date((new Date()).getTime() + 15*60*1000); // allow to start meetings about 15 minutes before begin
      for(MeetingDate date : dateList) {
        Date begin = date.getBegin();
        Date end = date.getEnd();
        if(now.after(begin) & now.before(end)) {
          meeting = date;
        }
      }
    }

    tableData = new VCDatesTableDataModel(dateList);

    TableGuiConfiguration tableConfig = new TableGuiConfiguration();
    tableConfig.setTableEmptyMessage("<b>"+translate("vc.table.empty")+"</b>");
    tableConfig.setSortingEnabled(true);
    tableCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
    tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.title", 0, null, ureq.getLocale()));
    tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.description", 1, null, ureq.getLocale()));
    tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.begin", 2, null, ureq.getLocale()));
    tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", 3, null, ureq.getLocale()));
    tableCtr.setTableDataModel(tableData);
    tableCtr.setSortColumn(2, true);// timeframe
    listenTo(tableCtr);

    runVC = createVelocityContainer("run");
    runVC.put("datesTable", tableCtr.getInitialComponent());

    runVC.contextPut("isModerator", isModerator);
    boolean isUseDates = config.isUseMeetingDates();
    runVC.contextPut("useDates", isUseDates);
    boolean isMeeting = !isUseDates | meeting != null;
    runVC.contextPut("isMeeting", isMeeting);
    boolean show = isModerator | (isUseDates & isMeeting) | !isUseDates;
    runVC.contextPut("show", show);

    displayCtr = provider.createDisplayController(ureq, wControl, roomId, name, description, isModerator, readOnly, config);
    runVC.put("displayCtr", displayCtr.getInitialComponent());

    putInitialPanel(runVC);
  }

  @Override
  protected void event(UserRequest ureq, Component source, Event event) {
    // nothing to do
  }

  @Override
  protected void doDispose() {
    if(tableCtr != null) {
      removeAsListenerAndDispose(tableCtr);
      tableCtr = null;
    }
    if(displayCtr != null) {
      removeAsListenerAndDispose(displayCtr);
      displayCtr = null;
    }
  }

}
//</OLATCE-103>
