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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.ICourse;

import com.frentix.olat.vitero.ui.ViteroBookingsController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroRunController extends BasicController {

	private final ViteroBookingsController bookingsController;


  public ViteroRunController(UserRequest ureq, WindowControl wControl, ICourse course) {
    super(ureq, wControl);
    
    bookingsController = new ViteroBookingsController(ureq, wControl, null, course);
    listenTo(bookingsController);
    
    putInitialPanel(bookingsController.getInitialComponent());
  }

  @Override
  protected void event(UserRequest ureq, Component source, Event event) {
    //nothing to do
  }

  @Override
  protected void doDispose() {
	  //nothing to do
  }
}