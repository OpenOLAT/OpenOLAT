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
package org.olat.core.gui.components.form.flexible.impl;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.Event;

/**
 * Initial Date:  11.12.2006 <br>
 * @author patrickb
 */
public class FormEvent extends Event {

	private static final long serialVersionUID = -3538521396051263132L;
	
	public static final int ONDBLCLICK = 1;
	public static final int ONCLICK = 2; 
	// Don't use onchange events for radiobuttons / checkboxes, this does not work
	// in IE <= 8. Use onclick instead (OLAT-5753). Note that when activating a 
	// checkbox / radio button the onclick will also be fired although nothing has 
	// been clicked. 
	public static final int ONCHANGE = 4;
	public static final int ONKEYUP = 8;
	public static final int ONBLUR = 16;
	public static final int ONVALIDATION = 32;
		
	//sorted x0 > x1 > x2 .. > xn 
	protected static final int[] ON_DOTDOTDOT = new int[]{ONDBLCLICK, ONCLICK, ONCHANGE, ONKEYUP, ONBLUR, ONVALIDATION};
	public static final FormEvent RESET = new FormEvent("reset",null);

	private final int trigger;
	private final FormItem source;
	
	public FormEvent(String command, FormItem source, int action) {
		super(command);
		this.source = source;
		this.trigger = action;
	}
	
	public FormEvent(String command, FormItem source) {
		this(command, source, ONCLICK);
	}
	
	public FormEvent(Event event, FormItem source, int action){
		this(event.getCommand(), source, action);
	}
	
	
	
	public FormItem getFormItemSource(){
		return source;
	}

	public boolean wasTriggerdBy(int events){
		//FIXME:pb: make it multiple event capable
		return trigger - events == 0;
	}
}
