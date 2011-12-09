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

package org.olat.admin.sysinfo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

public class FileSystemTestForm extends FormBasicController {
	private IntegerElement loops;
	private IntegerElement maxNbrDirs;
	private IntegerElement maxNbrFiles;
	private IntegerElement nbrCharInFile;
	private SingleSelection callFsSync;
	private SingleSelection checkWithRetries;
	
	public FileSystemTestForm(UserRequest ureq, WindowControl wControl, Translator translator) {
		super(ureq, wControl);
		setTranslator(translator);
		initForm(ureq);		
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer verticalL = FormLayoutContainer.createVerticalFormLayout("verticalL", getTranslator());
		formLayout.add(verticalL);
		loops = uifactory.addIntegerElement("loops.dirs", "filesystemtest.loops.label", 1, verticalL);
		maxNbrDirs = uifactory.addIntegerElement("maxnbr.dirs", "filesystemtest.maxnbr.dirs.label", 100, verticalL);
		maxNbrFiles = uifactory.addIntegerElement("maxnbr.files", "filesystemtest.maxnbr.files.label", 100, verticalL);
		nbrCharInFile = uifactory.addIntegerElement("nbr.char.in.file", "filesystemtest.nbrCharInFile.label", 100, verticalL);
		callFsSync = uifactory.addRadiosHorizontal("call.fs.sync", "filesystemtest.call.fs.sync.label", verticalL, new String[] {"yes","no"}, new String[] {"yes","no"});
		callFsSync.select("no", true);
		checkWithRetries = uifactory.addRadiosHorizontal("check.with.retries","filesystemtest.check.with.retries.label", verticalL, new String[] {"yes","no"}, new String[] {"yes","no"});
		checkWithRetries.select("no", true);
		Submit saveButton = new FormSubmit("save","save");
		formLayout.add(saveButton);	
	}	
	
	@Override
	protected void doDispose() {
		//empty		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);  
	}
	
	@Override
	protected void formResetted(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);      
	}	

	public int getMaxNbrDirs() {
		return maxNbrDirs.getIntValue();
	}

	public int getMaxNbrFiles() {
		return maxNbrFiles.getIntValue();
	}

	public int getNbrCharInFile() {
		return nbrCharInFile.getIntValue();
	}

	public int getLoops() {
		return loops.getIntValue();
	}
	
	public boolean isFsSyncEnabled() {
		return callFsSync.getSelectedKey().equalsIgnoreCase("yes");
	}

	public boolean isCheckWithRetriesEnabled() {
		return checkWithRetries.getSelectedKey().equalsIgnoreCase("yes");
	}

}
	
