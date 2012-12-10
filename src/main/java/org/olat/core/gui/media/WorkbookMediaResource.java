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
package org.olat.core.gui.media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;

/**
 * Export an excel file
 * 
 * Initial date: 10.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WorkbookMediaResource extends FileMediaResource {

	public WorkbookMediaResource(Workbook wb) {
		super(null, true);
		
		FileOutputStream fos = null;
		try {
			File f = new File(FolderConfig.getCanonicalTmpDir(), "TableExport" + CodeHelper.getRAMUniqueID() + ".xls");
			fos = new FileOutputStream(f);
			wb.write(fos);
			fos.close();
			this.file = f;
		} catch (IOException e) {
			throw new AssertException("error preparing media resource for XLS Table Export", e);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
	
	/**
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	public void release() {
		file.delete();
	}


}
