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
package org.olat.core.util.openxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;

/**
 * 
 * Initial date: 22.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXmlWorkbookTest {
	
	/**
	 * Sadly, I can only test if our system produce a non empty file.
	 * @throws IOException
	 */
	@Test
	public void creationOfWorkbook() throws IOException {
		File file = File.createTempFile("workbook", "_min.xlsx");
		FileOutputStream fileOut = new FileOutputStream(file);
		
		OpenXMLWorkbook workbook = new OpenXMLWorkbook(fileOut, 1);
		OpenXMLWorksheet sheet = workbook.nextWorksheet();
		sheet.setHeaderRows(1);
		//0 empty
		sheet.newRow();
		
		Row row1 = sheet.newRow();
		row1.addCell(1, "Title", null);
		row1.addCell(2, "Titre", null);
		row1.addCell(1, 1.0d, null);
		
		Row row2 = sheet.newRow();
		row2.addCell(2, "Hello", null);
		row2.addCell(2, "Border", workbook.getStyles().getBorderRightStyle());

		sheet.newRow();
		Row row4 = sheet.newRow();
		row4.addCell(5, new Date(), workbook.getStyles().getDateStyle());

		workbook.close();
		fileOut.flush();
		IOUtils.closeQuietly(fileOut);
		
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.length() > 4096);
		file.delete();
	}
	
	/**
	 * Don't forget that the argument of getColumn is zero based
	 * @throws IOException
	 */
	@Test
	public void getExcelColumn() throws IOException {
		String col_27 = OpenXMLWorksheet.getColumn(27);
		Assert.assertEquals("AB", col_27);
		String col_702 = OpenXMLWorksheet.getColumn(702);
		Assert.assertEquals("AAA", col_702);
	}
}
