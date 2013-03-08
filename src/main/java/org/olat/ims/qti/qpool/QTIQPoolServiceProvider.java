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
package org.olat.ims.qti.qpool;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.qti.QTI12PreviewController;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qtiPoolServiceProvider")
public class QTIQPoolServiceProvider implements QPoolSPI {
	
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	public QTIQPoolServiceProvider() {
		//
	}

	@Override
	public int getPriority() {
		return 10;
	}

	@Override
	public String getFormat() {
		return QTIConstants.QTI_12_FORMAT;
	}

	@Override
	public boolean isCompatible(String filename, File file) {
		boolean ok = new ItemFileResourceValidator().validate(filename, file);
		return ok;
	}
	@Override
	public boolean isCompatible(String filename, VFSLeaf file) {
		boolean ok = new ItemFileResourceValidator().validate(filename, file);
		return ok;
	}

	@Override
	public List<QuestionItem> importItems(Identity owner, String filename, File file) {
		QTIImportProcessor processor = new QTIImportProcessor(owner, filename, file, questionItemDao);
		return processor.process();
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		QTI12PreviewController previewCtrl = new QTI12PreviewController(ureq, wControl, item);
		return previewCtrl;
	}

	@Override
	public Controller getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		QTI12PreviewController previewCtrl = new QTI12PreviewController(ureq, wControl, item);
		return previewCtrl;
	}
	

}