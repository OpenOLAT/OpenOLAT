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

package org.olat.course.groupsandrights.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Util;
import org.olat.course.groupsandrights.CourseGroupManager;

/**
 * Description:<BR/>
 * Controller to handle a popup business group or business group area selection 
 * as a checkbox list. After selecting the items, the values will be put
 * together to a comma separated string and written to the original window
 * via javascript.
 * 
 * Initial Date:  Oct 5, 2004
 *
 * @author gnaegi 
 */
public class GroupAndAreaSelectController extends DefaultController {
    private static final String PACKAGE = Util.getPackageName(GroupAndAreaSelectController.class);
    private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);
    
    /** Configuration flag: use strings for business groups */
    public static final int TYPE_GROUP = 1;
    /** Configuration flag: use strings for business group areas */
    public static final int TYPE_AREA = 2;
    
    private StringListTableDataModel stringModel;
    private VelocityContainer main;
    private Choice stringChoice;
    private String htmlElemId;

    /**
     * @param ureq The user request
     * @param cgm The course group manager
     * @param type The choice type: group or area (use controller public constants)
     * @param preselectedNames String containing the comma separated names that should be preselected
     * @param htmlElemId the name of the html id of the window that opened this popup controller
     */
    public GroupAndAreaSelectController(UserRequest ureq, WindowControl wControl, CourseGroupManager cgm, int type, String preselectedNames, String htmlElemId) {
        super(wControl);
        Translator trans = new PackageTranslator(PACKAGE,  ureq.getLocale());
        List namesList;
        this.htmlElemId = htmlElemId;
        // main window containg title and the cooser list
        main = new VelocityContainer("main",VELOCITY_ROOT + "/groupandareaselect.html", trans, this);

        // initialize some type specific stuff
        switch (type) {
        case 1:
            namesList = cgm.getUniqueBusinessGroupNames();            
            main.contextPut("title", trans.translate("groupandareaselect.groups.title"));
            main.contextPut("noChoicesText", trans.translate("groupandareaselect.groups.nodata"));
            break;
        case 2:
            namesList = cgm.getUniqueAreaNames();            
            main.contextPut("title", trans.translate("groupandareaselect.areas.title"));
            main.contextPut("noChoicesText", trans.translate("groupandareaselect.areas.nodata"));
            break;
        default:
            throw new OLATRuntimeException("Must use valid type. type::" + type, null);
        }
        
        // get preselected List from the comma separated string
		List preselectedNamesList;
		if (preselectedNames == null) {
		    preselectedNamesList = new ArrayList(); 
		}
		else {
		    preselectedNamesList = stringToList(preselectedNames);
		}

        if (namesList.size() > 0) {
	        stringModel = new StringListTableDataModel(namesList, preselectedNamesList);        
	        stringChoice = new Choice("stringChoice", trans);
	        stringChoice.setSubmitKey("select");
	        stringChoice.setCancelKey("cancel");
	        stringChoice.setTableDataModel(stringModel);
	        stringChoice.addListener(this);
	        main.put("stringChoice", stringChoice);
	        main.contextPut("hasChoices", Boolean.TRUE);
        }
        else {
	        main.contextPut("hasChoices", Boolean.FALSE);            
        }
        setInitialComponent(main);
    }

    /** 
     * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
     */
    public void event(UserRequest ureq, Component source, Event event) {
		if (source == stringChoice) {
			if (event == Choice.EVNT_FORM_CANCELLED) {
				main.setPage(VELOCITY_ROOT+ "/cancelled.html");
			}
			else {
			    List selectedList = stringChoice.getSelectedRows();
			    List selectedEntries = new ArrayList();
			    for (Iterator iter = selectedList.iterator(); iter.hasNext();) {
					Integer sel = (Integer) iter.next();
					String obj = stringModel.getString(sel.intValue());
					selectedEntries.add(obj);
				}
			    String selectedString = listToString(selectedEntries);
				main.setPage(VELOCITY_ROOT+ "/closing.html");
				main.contextPut("var", htmlElemId);
				main.contextPut("val", selectedString);
			}
		}
    }


    /**
     * Converts a list of strings to a comma separated string
     * @param myList
     * @return String
     */
	private String listToString(List myList) {
	    boolean first = true;
	    StringBuilder sb = new StringBuilder();
	    Iterator iterator = myList.iterator();
	    while (iterator.hasNext()) {
            String name = (String) iterator.next();
	        if (!first) { 
	        	sb.append(",");
	        }
	        else {
	        	first = false;
	        }
            sb.append(name);
        }
	    return sb.toString();
	}
	
	/**
	 * Converts a coma separated string to a list containing the strings
	 * @param s the comma separated string
	 * @return the List of strings
	 */
	private List stringToList(String s) {
		String[] sArray = s.split(",");
		List result = new ArrayList();
		for (int i = 0; i < sArray.length; i++) {
			result.add(sArray[i].trim());
		}
		return result;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// nothing to dispose
	}

}
