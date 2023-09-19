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
package org.olat.core.commons.services.doceditor;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.gui.control.Controller;

/**
 * 
 * Initial date: 13 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface DocEditorOpenInfo {
	
	/**
	 * The editor may open in a lightbox (image, video). In that case a controller is returned and has to be listenTo().
	 * In the other case the editor is opened in a new window and no controller is returned.
	 */
	public Controller getController();
	
	/*
	 * The mode of the doc editor.
	 */
	public Mode getMode();

}
