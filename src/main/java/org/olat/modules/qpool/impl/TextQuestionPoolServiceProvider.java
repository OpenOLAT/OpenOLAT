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
package org.olat.modules.qpool.impl;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolSPI;

/**
 * 
 * Initial date: 26.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextQuestionPoolServiceProvider implements QuestionPoolSPI {

	public static final String TXT_FORMAT = "txt";
	
	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public String getFormat() {
		return TXT_FORMAT;
	}
	
	@Override
	public boolean isCompatible(String filename, File file) {
		return filename.toLowerCase().endsWith(".txt");
	}

	@Override
	public boolean isCompatible(String filename, VFSLeaf file) {
		return isCompatible(filename, (File)null);
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		return null;
	}

	@Override
	public Controller getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		return null;
	}
}
