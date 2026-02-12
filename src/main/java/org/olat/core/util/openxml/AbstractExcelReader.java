/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.util.openxml;

import java.time.LocalDateTime;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.Row;

/**
 * 
 * Initial date: 9 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public abstract class AbstractExcelReader {
	
	public String getString(Row r, int pos) {
		if(r.getCellCount() <= pos) return null;
		
		Cell cell = r.getCell(pos);
		if(cell == null || cell.getType() == CellType.EMPTY || cell.getType() == CellType.ERROR) {
			return null;
		}
		if(cell.getType() == CellType.STRING) {
			return r.getCellAsString(pos).orElse(null);
		}
		return null;
	}
	
	public LocalDateTime getDateTime(Row r, int pos) {
		if(r.getCellCount() <= pos) return null;
		
		Cell cell = r.getCell(pos);
		if(cell == null || cell.getType() == CellType.EMPTY || cell.getType() == CellType.ERROR) {
			return null;
		}
		if(cell.getType() == CellType.NUMBER) {
			return r.getCellAsDate(pos).orElse(null);
		}
		return null;
	}

}
