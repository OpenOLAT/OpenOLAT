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
package org.olat.core.commons.services.doceditor.model;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 6 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NoEditorAvailable implements DocEditorDisplayInfo {
	
	private static final NoEditorAvailable INFO = new NoEditorAvailable();
	
	public static final NoEditorAvailable get() {
		return INFO;
	}
	
	private NoEditorAvailable() {
		//
	}

	@Override
	public Mode getMode() {
		return null;
	}

	@Override
	public boolean isEditorAvailable() {
		return false;
	}

	@Override
	public String getModeIcon() {
		return null;
	}

	@Override
	public String getModeButtonLabel(Translator trans) {
		return null;
	}

	@Override
	public boolean isNewWindow() {
		return false;
	}

}
