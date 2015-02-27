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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;

/**
 * Export an excel file
 * 
 * Initial date: 10.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WorkbookMediaResource implements MediaResource {

	private final Workbook wb;
	
	public WorkbookMediaResource(Workbook wb) {
		this.wb = wb;
	}
	
	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/vnd.ms-excel; charset=utf-8";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		hres.setCharacterEncoding("utf-8");
		String name = "TableExport" + CodeHelper.getRAMUniqueID();
		hres.setHeader("Content-Disposition", "attachment; filename=" + name + ".xls");
		hres.setHeader("Content-Description", "OpenOLAT Generated data");
		try {
			wb.write(hres.getOutputStream());
		} catch (IOException e) {
			throw new AssertException("error preparing media resource for XLS Table Export", e);
		}
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	@Override
	public void release() {
		//
	}


}
