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
* <p>
*/ 

package org.olat.core.gui.components.form;

import org.olat.core.gui.control.Event;

/**
 * enclosing_type Description: <br>
 * 
 * #OO-40 : refactored: now only holds some legacy static values
 * @author Felix Jost
 */

public final class Form  {

	/**
	 * Comment for <code>CANCEL_IDENTIFICATION</code>
	 */
	public static final String CANCEL_IDENTIFICATION = "olat_foca";
	/**
	 * Comment for <code>SUBMIT_IDENTIFICATION</code>
	 */
	public static final String SUBMIT_IDENTIFICATION = "olat_fosm";
	/**
	 * Comment for <code>ELEM_BUTTON_COMMAND_ID</code>
	 */
	// don't change, is used also in functins.js
	// and choice renderer..
	public static final String ELEM_BUTTON_COMMAND_ID = "olatcmd_";

	/**
	 * Comment for <code>EVENT_VALIDATION_OK</code>
	 */
	public static final Event EVNT_VALIDATION_OK = new Event("validation ok");
	/**
	 * Comment for <code>EVENT_VALIDATION_NEXT</code>
	 */
	public static final Event EVNT_VALIDATION_NEXT = new Event("validation next");
	/**
	 * Comment for <code>EVENT_VALIDATION_FINISH</code>
	 */
	public static final Event EVNT_VALIDATION_FINISH = new Event("validation finish");
	/**
	 * Comment for <code>EVENT_VALIDATION_NOK</code>
	 */
	public static final Event EVNT_VALIDATION_NOK = new Event("validation nok");
	/**
	 * Comment for <code>EVENT_FORM_CANCELLED</code>
	 */
	public static final Event EVNT_FORM_CANCELLED = new Event("form_cancelled");

	

	// this variables are used in functions.js - do not change
	/** html form id prependix * */
	public static final String JSFORMID = "bfo_";

}