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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.archiver;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * @author schneider
 * 
 * Comment: Shows or don't shows three checkboxes appropriate the parameters in the constructor.
 * At least on of them has to be true.
 */
public class LogFileChooserForm extends FormBasicController {
	
    private boolean admin, u, a, s;
    
    private SelectionElement sE,aE,uE;
    
  	private DateChooser beginDate;
  	private DateChooser endDate;
  	
    /**
     * 
     * @param ureq
     * @param wControl
     * @param isAdministrator
     * @param a adminLogVisibility
     * @param u userLogVisibility
     * @param s statisticLogVisibility
     */
    
    public LogFileChooserForm(UserRequest ureq, WindowControl wControl, boolean isAdministrator, boolean a, boolean u, boolean s) {
        super(ureq, wControl);
        
        this.admin = isAdministrator;
        
        this.u = u;
        this.a = a; 
        this.s = s;
        
        initForm (ureq);
    }
    
    @Override
    public boolean validateFormLogic(UserRequest ureq) {
    	boolean logChecked = false;
    	boolean beginLessThanEndOk = true;
    	
    	aE.clearError();
    	uE.clearError();
    	sE.clearError();
      if(aE.isSelected(0) || uE.isSelected(0) || sE.isSelected(0)){
      	logChecked = true;
      }else{
      	if (sE.isVisible()) {
      		sE.setErrorKey("course.logs.error", null);
      	} else if (uE.isVisible()) {
      		uE.setErrorKey("course.logs.error", null);
      	} else {
      		aE.setErrorKey("course.logs.error", null);
      	}
      }
      
      // note: we're no longer restricting to have both a begin and an end
      //       - there is no underlying reason for limiting this
      beginDate.clearError();
      if((beginDate.getDate() != null)&&(endDate.getDate() != null)){
      	if (beginDate.getDate().after(endDate.getDate())){
      		beginLessThanEndOk= false;
      		beginDate.setErrorKey("logfilechooserform.endlessthanbegin", null);
      	}
      }
      
      return logChecked && beginLessThanEndOk;
    }
    
    /**
     * @return true if logAdmin is checked
     */
    public boolean logAdminChecked() {
        return aE.isSelected(0);
    }
    
    /**
     * @return true if logUser is checked
     */
    public boolean logUserChecked() {
        return uE.isSelected(0);
    }

    /**
     * @return true if logStat is checked
     */
    public boolean logStatChecked() {
        return sE.isSelected(0);
    }
    
    public Date getBeginDate(){
    	return this.beginDate.getDate();
    }
    
    public Date getEndDate(){
    	return this.endDate.getDate();
    }

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent (ureq, Event.DONE_EVENT);
		}
		
		@Override
		protected void formCancelled(UserRequest ureq) {
			flc.reset();
			flc.setDirty(false);
			fireEvent (ureq, Event.CANCELLED_EVENT);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("menu.archivelogfiles");
			setFormDescription("course.logs.intro");
			setFormContextHelp("manual_user/course_operation/Record_of_Course_Activities/");

			aE = uifactory.addCheckboxesVertical("a", "logfilechooserform.logadmin", formLayout, new String[]{"xx"}, new String[]{""}, 1);
			uE = uifactory.addCheckboxesVertical("u", "logfilechooserform.loguser",  formLayout, new String[]{"xx"}, new String[]{""}, 1);
			sE = uifactory.addCheckboxesVertical("s", "logfilechooserform.logstat",  formLayout, new String[]{"xx"}, new String[]{""}, 1);
			
			aE.setVisible(admin || a);
			uE.setVisible(admin || u);
			sE.setVisible(admin || s);
			
			uifactory.addSpacerElement("spacer1", formLayout, true);
			
			beginDate = uifactory.addDateChooser("startdate", "logfilechooserform.begindate", null, formLayout);
			endDate = uifactory.addDateChooser("enddate", "logfilechooserform.enddate", null, formLayout);

			uifactory.addFormSubmitButton("submit", "logfilechooserform.archive", formLayout);
		}
}
