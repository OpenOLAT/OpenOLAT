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
package de.bps.course.nodes.vc.provider.wimba;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.core.gui.translator.Translator;

/**
 *
 * Description:<br>
 * Display controller for Wimba Classroom usage at run-time
 *
 * <P>
 * Initial Date:  06.01.2011 <br>
 * @author skoeber
 */
public class WimbaDisplayController extends BasicController {

  private static String COMMAND_JOIN_MODERATOR = "cmd.join.moderator";
  private static String COMMAND_JOIN_LEARNER = "cmd.join.learner";
  private static String COMMAND_JOIN_GUEST = "cmd.join.guest";
  private static String COMMAND_START_MEETING = "cmd.start.meeting";
  private static String COMMAND_CLOSE_MEETING = "cmd.close.meeting";
  private static String COMMAND_UNCLOSE_MEETING = "cmd.unclose.meeting";
  private static String COMMAND_UPDATE_MEETING = "cmd.update.meeting";
  private static String COMMAND_OPEN_MANAGEROOM = WimbaClassroomProvider.TARGET_OPEN_MANAGEROOM;
  private static String COMMAND_OPEN_POLLRESULTS = WimbaClassroomProvider.TARGET_OPEN_POLLRESULTS;
  private static String COMMAND_OPEN_TRACKING = WimbaClassroomProvider.TARGET_OPEN_TRACKING;
  private static String COMMAND_OPEN_WIZARD = WimbaClassroomProvider.TARGET_OPEN_WIZARD;
  private static String COMMAND_OPEN_RECORDING = "cmd.open.recording";
  private static String COMMAND_TOGGLESTATUS_RECORDING = "cmd.togglestatus.recording";
  private static String COMMAND_DELETE_RECORDING = "cmd.delete.recording";

  // GUI
  private VelocityContainer runVC;
  private Link joinModerator, joinLearner, joinGuest;
  private Link startMeeting, closeMeeting, uncloseMeeting, updateMeeting;
  private Link openWizard, openManageRoom, openPollResults, openTracking;
  private TableController recTable;

  //data
  private WimbaClassroomConfiguration config;
  private String name, description;
  private String roomId;
  private RecordingsTableModel recTableModel;

  private WimbaClassroomProvider wimba;

  protected WimbaDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description, boolean isModerator,
      WimbaClassroomConfiguration config, WimbaClassroomProvider provider) {
    super(ureq, wControl);
    this.wimba = provider;
    this.config = config;
    this.roomId = roomId;
    this.name = name;
    this.description = description;

    runVC = createVelocityContainer("run");

    boolean exists = wimba.existsClassroom(roomId, config);
    boolean closed = exists ? wimba.isPreviewMode(roomId, false) : false;
    boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();
    boolean showGuestLink = config.isGuestAccessAllowed();
    
    if(isModerator) {
    	wimba.createModerator(ureq.getIdentity(), roomId);
    } else if(!isGuest) {
    	wimba.createUser(ureq.getIdentity(), roomId);
    }

    String guestLink = wimba.createClassroomGuestUrl(roomId, ureq.getIdentity(), config).toString();

    joinModerator = LinkFactory.createButton(COMMAND_JOIN_MODERATOR, runVC, this);
    joinLearner = LinkFactory.createButton(COMMAND_JOIN_LEARNER, runVC, this);
    joinGuest = LinkFactory.createButton(COMMAND_JOIN_GUEST, runVC, this);
    startMeeting = LinkFactory.createButton(COMMAND_START_MEETING, runVC, this);
    closeMeeting = LinkFactory.createButton(COMMAND_CLOSE_MEETING, runVC, this);
    uncloseMeeting = LinkFactory.createButton(COMMAND_UNCLOSE_MEETING, runVC, this);
    updateMeeting = LinkFactory.createButton(COMMAND_UPDATE_MEETING, runVC, this);
    // set target to be able to open new browser window on event
    joinGuest.setTarget("_blank");
    joinLearner.setTarget("_blank");
    joinModerator.setTarget("_blank");

    joinLearner.setEnabled(!closed);
    joinGuest.setEnabled(!closed);

    runVC.contextPut("exists", exists);
    runVC.contextPut("closed", closed);
    runVC.contextPut("isGuest", isGuest);
    runVC.contextPut("isModerator", isModerator);
    runVC.contextPut("showGuestLink", showGuestLink);
    runVC.contextPut("guestLink", guestLink);

    // convenience links
    openWizard = LinkFactory.createButton(COMMAND_OPEN_WIZARD, runVC, this);
    openWizard.setTarget("_blank");

    // moderator links
    if(isModerator) {
      openManageRoom = LinkFactory.createButton(COMMAND_OPEN_MANAGEROOM, runVC, this);
      openPollResults = LinkFactory.createButton(COMMAND_OPEN_POLLRESULTS, runVC, this);
      openTracking = LinkFactory.createButton(COMMAND_OPEN_TRACKING, runVC, this);
      openManageRoom.setTarget("_blank");
      openPollResults.setTarget("_blank");
      openTracking.setTarget("_blank");
    }

    // show recordings
    TableGuiConfiguration tableConfig = new TableGuiConfiguration();
    tableConfig.setDisplayRowCount(true);
    tableConfig.setPageingEnabled(true);
    tableConfig.setTableEmptyMessage(translate("table.recordings.empty"));
    recTable = new TableController(tableConfig, ureq, wControl, getTranslator());
    DefaultColumnDescriptor recCol = new DefaultColumnDescriptor("table.recordings.name", 1, COMMAND_OPEN_RECORDING, getLocale());
    recCol.setIsPopUpWindowAction(true, null);
    recTable.addColumnDescriptor(recCol);
    if(isModerator) {
      recTable.addColumnDescriptor(new DefaultColumnDescriptor("table.recordings.status", 2, COMMAND_TOGGLESTATUS_RECORDING, getLocale()));
      recTable.addColumnDescriptor(new StaticColumnDescriptor(COMMAND_DELETE_RECORDING, "table.recordings.action", translate("table.recordings.delete")));
    }
    Map<String, String> recordings = wimba.listRecordings(roomId);
    List<String> keys = new ArrayList<String>(recordings.keySet());
    Collections.sort(keys);
    List<Object[]> recordingData = new ArrayList<Object[]>();
    for(String key : keys) {
      String title = recordings.get(key);
      boolean preview = wimba.isPreviewMode(key, true);
      /*
       * for moderators: add all recordings and show actions
       * for users: add only recordings that are not in preview mode
       */
      if(!preview | isModerator) recordingData.add(new Object[] {key, title, preview});
    }
    recTableModel = new RecordingsTableModel(recordingData, getTranslator());
    recTable.setTableDataModel(recTableModel);
    listenTo(recTable);
    runVC.put("recordingsTable", recTable.getInitialComponent());

    putInitialPanel(runVC);
  }

  @Override
  protected void event(UserRequest ureq, Component source, Event event) {
    if(source == startMeeting) {
      boolean success = wimba.createClassroom(roomId, name, description, null, null, config);
      if(success) {
        runVC.contextPut("exists", true);
        runVC.setDirty(true);
      } else {
        getWindowControl().setError(translate("error.create.room"));
      }
    } else if(source == joinModerator) {
      boolean success = wimba.existsClassroom(roomId, config);
      // update rights for user to moderate meeting
      if(success) {
        success = wimba.createModerator(ureq.getIdentity(), roomId);
      } else {
        // room not found, should not appear
        getWindowControl().setError(translate("error.no.room"));
        return;
      }
      // login the user as moderator
      if(success) {
        success = wimba.login(ureq.getIdentity(), null);
      } else {
        // could not create moderator or update the rights
        getWindowControl().setError(translate("error.update.rights"));
        return;
      }
      // redirect to the meeting
      if(success) {
        joinMeeting(ureq, false);
      } else {
        // login failed
        getWindowControl().setError(translate("error.no.login"));
        return;
      }
      return;
    } else if(source == joinLearner) {
      joinMeeting(ureq, false);
      return;
    } else if(source == joinGuest) {
      joinMeeting(ureq, true);
      return;
    } else if(source == closeMeeting) {
      boolean success = wimba.setPreviewMode(roomId, true, false);
      if(success) {
        runVC.contextPut("closed", true);
        runVC.setDirty(true);
      } else {
        // closing failed
        getWindowControl().setError(translate("error.remove.room"));
      }
    } else if(source == uncloseMeeting) {
      boolean success = wimba.setPreviewMode(roomId, false, false);
      if(success) {
        runVC.contextPut("closed", false);
        runVC.setDirty(true);
      } else {
        // reopen failed
        getWindowControl().setError(translate("error.update.room"));
      }
    } else if(source == updateMeeting) {
      boolean success = wimba.updateClassroom(roomId, name, description, null, null, config);
      if(success) {
        getWindowControl().setInfo(translate("success.update.room"));
      } else {
        // update failed
        getWindowControl().setError(translate("error.update.room"));
      }
    } else if(source == openWizard | source == openManageRoom | source == openPollResults | source == openTracking) {
      openWimbaUrl(ureq, ((Link)source).getCommand());
      return;
    }
  }

  @Override
  protected void event(UserRequest ureq, Controller source, Event event) {
    if(source == recTable) {
      if(event instanceof TableEvent) {
        TableEvent tEvent = (TableEvent)event;
        String action = tEvent.getActionId();
        int row = tEvent.getRowId();
        String key = (String) recTable.getTableDataModel().getValueAt(row, 0);
        if(action.equals(COMMAND_OPEN_RECORDING)) {
          wimba.login(ureq.getIdentity(), null);
          URL url = wimba.createClassroomRecordingUrl(key, ureq.getIdentity());
          RedirectMediaResource rmr = new RedirectMediaResource(url.toString());
          ureq.getDispatchResult().setResultingMediaResource(rmr);
          return;
        } else if(action.equals(COMMAND_TOGGLESTATUS_RECORDING)) {
        	Object[] entry = (Object[]) recTable.getTableDataModel().getObject(row);
        	Boolean preview = (Boolean) entry[2];
        	if(wimba.setPreviewMode(key, !preview, true)) {
        		String text = preview ? "table.recordings.unclose.success" : "table.recordings.close.success";
        		getWindowControl().setInfo(translate(text));
        		recTableModel.toggleStatus(row);
        		recTable.modelChanged();
        		runVC.setDirty(true);
        	} else {
        		String text = preview ? "table.recordings.unclose.error" : "table.recordings.close.error";
        		getWindowControl().setError(translate(text));
        	}
        } else if(action.equals(COMMAND_DELETE_RECORDING)) {
          if(wimba.removeClassroomRecording(key)) {
            getWindowControl().setInfo(translate("table.recordings.delete.success"));
            recTableModel.removeRecording(row);
            recTable.modelChanged();
            runVC.setDirty(true);
          } else {
            getWindowControl().setError(translate("table.recordings.delete.error"));
          }
        }
      }
    }
  }

  private void openWimbaUrl(UserRequest ureq, String target) {
	  boolean success = false;
	  if (target.equals(COMMAND_OPEN_WIZARD)) {
		  String url = wimba.createServiceUrl(target, null);
		  RedirectMediaResource rmr = new RedirectMediaResource(url);
		  ureq.getDispatchResult().setResultingMediaResource(rmr);
	  }
	  else {
		  success = wimba.createModerator(ureq.getIdentity(), roomId);
		  if(success) {
			  wimba.login(ureq.getIdentity(), null);
			  String url = wimba.createServiceUrl(target, roomId);
			  RedirectMediaResource rmr = new RedirectMediaResource(url);
			  ureq.getDispatchResult().setResultingMediaResource(rmr);
		  } else {
			  // could not create moderator or update the rights
			  getWindowControl().setError(translate("error.update.rights"));
			  return;
		  }
	  }
  }

  private void joinMeeting(UserRequest ureq, boolean guest) {
    URL url;
    if(guest) {
      url = wimba.createClassroomGuestUrl(roomId, ureq.getIdentity(), config);
    } else {
      boolean success = wimba.login(ureq.getIdentity(), null);
      // no success, maybe the user account doesn't exist, create it and try the login again
      if(!success) wimba.createUser(ureq.getIdentity(), roomId);
      wimba.login(ureq.getIdentity(), null);
      url = wimba.createClassroomUrl(roomId, ureq.getIdentity(), config);
    }
    RedirectMediaResource rmr = new RedirectMediaResource(url.toString());
    ureq.getDispatchResult().setResultingMediaResource(rmr);
  }

  @Override
  protected void doDispose() {
    // nothing to dispose
  }

}

class RecordingsTableModel implements TableDataModel<Object[]> {

  private List<Object[]> recordings = new ArrayList<Object[]>();
  private Translator translator;

  public RecordingsTableModel(List<Object[]> recordings, Translator translator) {
  	this.recordings.addAll(recordings);
  	this.translator = translator;
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public int getRowCount() {
    return recordings.size();
  }

  @Override
  public Object getValueAt(int row, int col) {
    Object[] recording = this.recordings.get(row);
    switch(col) {
    	case 0: return recording[col];//key
    	case 1: return recording[col];//title
    	case 2: //action string depending on status
    		Boolean preview = (Boolean) recording[col];
    		return preview
    				? translator.translate("table.recordings.unclose")
    				: translator.translate("table.recordings.close");
  		default: return recording[col]; 
    }
  }

  @Override
  public Object[] getObject(int row) {
    return recordings.get(row);
  }

  @Override
  public void setObjects(List<Object[]> objects) {
    this.recordings = objects;
  }

  @Override
  public Object createCopyWithEmptyList() {
    // not used
    return "";
  }

  public void removeRecording(int row) {
    this.recordings.remove(row);
  }
  
  public void toggleStatus(int row) {
  	Boolean status = (Boolean) this.recordings.get(row)[2];
  	this.recordings.get(row)[2] = !status;
  }
}
//</OLATCE-103>
