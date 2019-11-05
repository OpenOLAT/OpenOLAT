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
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolItemEditorController;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.AbstractQPoolServiceProvider;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.FilePreviewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 26.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("filePoolServiceProvider")
public class FileQPoolServiceProvider extends AbstractQPoolServiceProvider {
	
	public static final String FILE_FORMAT = "raw-file";
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QPoolFileStorage qpoolFileStorage;

	@Override
	public QPoolFileStorage getFileStorage() {
		return qpoolFileStorage;
	}

	@Override
	public QItemType getDefaultType() {
		return qpoolService.getItemType(QuestionType.ESSAY.name());
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String getFormat() {
		return FILE_FORMAT;
	}

	@Override
	public boolean isCompatible(String filename, File file) {
		return !filename.toLowerCase().endsWith(".xml")
				&& !filename.toLowerCase().endsWith(".txt")
				&& !filename.toLowerCase().endsWith(".zip");
	}

	@Override
	public List<QItemFactory> getItemfactories() {
		return Collections.emptyList();
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean summary) {
		return new FilePreviewController(ureq, wControl, item);
	}

	@Override
	public boolean isTypeEditable() {
		return false;
	}

	@Override
	public QPoolItemEditorController getEditableController(UserRequest ureq,	WindowControl wControl, QuestionItem item) {
		return new FilePreviewController(ureq, wControl, item);
	}

	@Override
	public Controller getReadOnlyController(UserRequest ureq,	WindowControl wControl, QuestionItem item) {
		return getEditableController(ureq, wControl, item);
	}
}