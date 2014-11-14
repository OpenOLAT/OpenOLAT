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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.tree;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * 
 * Initial date: 14.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MenuTreeEvent extends FormEvent {

	private static final long serialVersionUID = -9180938864452149371L;
	public static final String SELECT  = "select";
	public static final String DESELECT = "deselect";
	
	private String ident;
	
	public MenuTreeEvent(String command, FormItem source, String ident) {
		super(command, source, FormEvent.ONCLICK);
		this.ident = ident;
	}
	
	public String getIdent() {
		return ident;
	}
}
