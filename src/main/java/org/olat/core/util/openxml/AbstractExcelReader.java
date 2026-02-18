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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.Row;
import org.olat.core.util.DateUtils;

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
	
	public String getNumberAsString(Row r, int pos) {
		if(r.getCellCount() <= pos) return null;
		
		Cell cell = r.getCell(pos);
		if(cell == null || cell.getType() == CellType.EMPTY || cell.getType() == CellType.ERROR) {
			return null;
		}
		if(cell.getType() == CellType.NUMBER) {
			BigDecimal val = r.getCellAsNumber(pos).orElse(null);
			return val == null ? null : val.toString();
		}
		// Fallback to string
		return getString(r, pos);
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
	
	public ReaderLocalDate getDate(Row r, int pos) {
		if(r.getCellCount() <= pos) return null;
		
		Cell cell = r.getCell(pos);
		if(cell == null || cell.getType() == CellType.EMPTY || cell.getType() == CellType.ERROR) {
			return null;
		}
		if(cell.getType() == CellType.NUMBER) {
			LocalDateTime dateTime = r.getCellAsDate(pos).orElse(null);
			if(dateTime != null) {
				return ReaderLocalDate.valueOf(dateTime);
			}
		}
		
		String rawValue = getString(r, pos);
		if(rawValue != null) {
			return ReaderLocalDate.valueOf(rawValue);
		}
		return null;
	}
	
	public ReaderLocalTime getTime(Row r, int pos) {
		if(r.getCellCount() <= pos) return null;
		
		Cell cell = r.getCell(pos);
		if(cell == null || cell.getType() == CellType.EMPTY || cell.getType() == CellType.ERROR) {
			return null;
		}
		if(cell.getType() == CellType.NUMBER) {
			LocalDateTime dateTime = r.getCellAsDate(pos).orElse(null);
			if(dateTime != null) {
				return ReaderLocalTime.valueOf(dateTime);
			}
		}
		
		String rawValue = getString(r, pos);
		if(rawValue != null) {
			return ReaderLocalTime.valueOf(rawValue);
		}
		return null;
	}
	
	public static final Date toDate(ReaderLocalDate date, ReaderLocalTime time) {
		LocalDateTime dateTime;
		if(date == null || date.date() == null) {
			return null;
		} else if(time == null || time.time() == null) {
			dateTime = LocalDateTime.of(date.date(), LocalTime.MIDNIGHT);
		} else {
			dateTime = LocalDateTime.of(date.date(), time.time());
		}
		return DateUtils.toDate(dateTime);
	}
	
	public static final Date toEndOfDay(ReaderLocalDate date) {
		LocalDateTime dateTime;
		if(date == null || date.date() == null) {
			return null;
		} else {
			dateTime = LocalDateTime.of(date.date(), LocalTime.NOON);
		}
		return DateUtils.getEndOfDay(DateUtils.toDate(dateTime));
	}
	
	public record ReaderLocalDate(LocalDate date, String val) {

		public static final ReaderLocalDate valueOf(LocalDateTime dateTime) {
			return new ReaderLocalDate(LocalDate.from(dateTime), null);
		}
		
		public static final ReaderLocalDate valueOf(String val) {
			return new ReaderLocalDate(null, val);
		}
	}

	public record ReaderLocalTime(LocalTime time, String val) {
		
		public static final ReaderLocalTime valueOf(LocalDateTime dateTime) {
			return new ReaderLocalTime(LocalTime.from(dateTime), null);
		}
		
		public static final ReaderLocalTime valueOf(String val) {
			return new ReaderLocalTime(null, val);
		}
	}
}
