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
package de.bps.course.nodes.den;

import java.util.BitSet;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;

import de.bps.course.nodes.DENCourseNode;

public class DENManageDatesController extends BasicController {

	private DENCourseNode denCourseNode;
	private OLATResourceable ores;
	
	//objects for dates management view
	private CloseableModalController editDateModalCntrll;
	private DENDatesForm manageDatesForm, editSingleDateForm, editMultipleDatesForm;
	private DENEditTableDataModel editTableData;
	private TableController editDENTable;
	private List<KalendarEvent> editTableDataList;
	private VelocityContainer manageVc;
	private BitSet selectedDates;
	
	private DENManager denManager;
	
	public DENManageDatesController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, DENCourseNode courseNode) {
		super(ureq, wControl);
		
		this.ores= ores;
		this.denCourseNode = courseNode;
		this.denManager = DENManager.getInstance();
		
		//prepare form for managing dates
		ICourse course = CourseFactory.loadCourse(ores);
	
		manageVc = createVelocityContainer("datemanagement");
		manageDatesForm = new DENDatesForm(ureq, getWindowControl(), getTranslator(), DENDatesForm.CREATE_DATES_LAYOUT);
		manageDatesForm.addControllerListener(this);
		editTableDataList = denManager.getDENEvents(course.getResourceableId(), denCourseNode.getIdent());
		editTableData = new DENEditTableDataModel(editTableDataList, getTranslator());
		editDENTable = denManager.createManageDatesTable(ureq, getWindowControl(), getTranslator(), editTableData);
		listenTo(editDENTable);
		//add Components
		manageVc.put("datesForm", manageDatesForm.getInitialComponent());
		manageVc.put("datesTable", editDENTable.getInitialComponent());
		
		putInitialPanel(manageVc);
	}

	@Override
	protected void doDispose() {
		if(editDENTable != null) {
			removeAsListenerAndDispose(editDENTable);
			editDENTable = null;
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		ICourse course = CourseFactory.loadCourse(ores);
		if(manageDatesForm == source) {
			//generate the dates and put them into the table
			editTableDataList = denManager.generateDates(manageDatesForm.getSubject(), manageDatesForm.getComment(), manageDatesForm
					.getLocation(), manageDatesForm.getDuration(), manageDatesForm.getPause(), manageDatesForm.getBeginDate(), manageDatesForm
					.getRetakes(), manageDatesForm.getNumParts(), editTableDataList, denCourseNode.getIdent());
			denManager.persistDENSettings(editTableDataList, course, denCourseNode);
			editDENTable.setTableDataModel(editTableData);
		} else if(source == editDENTable) {
			TableMultiSelectEvent tmse = (TableMultiSelectEvent)event;
			selectedDates = tmse.getSelection();
			//clicked button to edit one date or more dates
			if (tmse.getAction().equals(DENEditTableDataModel.CHANGE_ACTION) && selectedDates.cardinality() > 0) {
				if(selectedDates.cardinality() == 1) {
					//if only one date is choosen, we can prefill some entries
					removeAsListenerAndDispose(editSingleDateForm);
					editSingleDateForm = new DENDatesForm(ureq, getWindowControl(), getTranslator(), DENDatesForm.EDIT_SINGLE_DATE_LAYOUT);
					listenTo(editSingleDateForm);
					
					KalendarEvent calEvent = editTableData.getObjects(selectedDates).get(0);//in this case only one date is possible
					editSingleDateForm.setSubject(calEvent.getSubject());
					editSingleDateForm.setComment(calEvent.getComment());
					editSingleDateForm.setLocation(calEvent.getLocation());
					editSingleDateForm.setNumParts(calEvent.getNumParticipants());
					editSingleDateForm.setFormDate(calEvent.getBegin());
					editSingleDateForm.setDuration(denManager.getDurationAsString(calEvent));
					
					removeAsListenerAndDispose(editDateModalCntrll);
					editDateModalCntrll = new CloseableModalController(getWindowControl(), "close", editSingleDateForm.getInitialComponent(), true, translate("dates.edit"));
					listenTo(editDateModalCntrll);

				} else if(selectedDates.cardinality() > 1) {
					removeAsListenerAndDispose(editMultipleDatesForm);
					editMultipleDatesForm = new DENDatesForm(ureq, getWindowControl(), getTranslator(), DENDatesForm.EDIT_MULTIPLE_DATES_LAYOUT);
					listenTo(editMultipleDatesForm);
					
					removeAsListenerAndDispose(editDateModalCntrll);
					editDateModalCntrll = new CloseableModalController(getWindowControl(), "close", editMultipleDatesForm.getInitialComponent(), true, translate("dates.edit"));
					listenTo(editDateModalCntrll);
				}
				//persist dates
				denManager.persistDENSettings(editTableData.getObjects(), course, denCourseNode);
				editDateModalCntrll.activate();
			} else if (tmse.getAction().equals(DENEditTableDataModel.DELETE_ACTION)) {
				//delete selected dates
				editTableData.removeEntries(tmse.getSelection());
				editDENTable.setTableDataModel(editTableData);
				//persist dates
				denManager.persistDENSettings(editTableData.getObjects(), course, denCourseNode);
			}
		} else if(source == editSingleDateForm) {
			//save changes for one date
			editTableData.setObjects(denManager.updateDateInList(editSingleDateForm.getSubject(), editSingleDateForm.getComment(),
					editSingleDateForm.getLocation(), editSingleDateForm.getDuration(), editSingleDateForm.getBeginDate(),
					editSingleDateForm.getNumParts(), editTableData.getObjects(), selectedDates.nextSetBit(0)));//only one bit is set
			editDENTable.setTableDataModel(editTableData);
			denManager.persistDENSettings(editTableData.getObjects(), course, denCourseNode);
			editDateModalCntrll.deactivate();
		} else if(source == editMultipleDatesForm) {
			//save changes for multiple dates
			editTableData.setObjects(denManager.updateMultipleDatesInList(editMultipleDatesForm.getSubject(), editMultipleDatesForm.getComment(),
					editMultipleDatesForm.getLocation(), editMultipleDatesForm.getMovementGap(), editMultipleDatesForm.getNumParts(),
					editTableData.getObjects(), selectedDates));
			editDENTable.setTableDataModel(editTableData);
			denManager.persistDENSettings(editTableData.getObjects(), course, denCourseNode);
			editDateModalCntrll.deactivate();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

}
