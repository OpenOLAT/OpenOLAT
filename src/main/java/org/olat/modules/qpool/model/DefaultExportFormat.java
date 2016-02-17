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
package org.olat.modules.qpool.model;

import org.olat.modules.qpool.ExportFormatOptions;

/**
 * 
 * Initial date: 18.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultExportFormat implements ExportFormatOptions {
	
	public static final ExportFormatOptions ZIP_EXPORT_FORMAT = new DefaultExportFormat("Zip");
	public static final ExportFormatOptions DOCX_EXPORT_FORMAT = new DefaultExportFormat("Docx");
	
	private final String format;
	private final Outcome outcome;
	private final String typeFormat;
	
	public DefaultExportFormat(String format) {
		this(format, Outcome.download, null);
	}
	
	public DefaultExportFormat(String format, Outcome outcome, String typeFormat) {
		this.format = format;
		this.outcome = outcome;
		this.typeFormat = typeFormat;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public Outcome getOutcome() {
		return outcome;
	}

	@Override
	public String getResourceTypeFormat() {
		return typeFormat;
	}

	@Override
	public int hashCode() {
		return (format == null ? 37469 : format.hashCode())
				+ (outcome == null ? 29 : outcome.hashCode())
				+ (typeFormat == null ? 4678 : typeFormat.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DefaultExportFormat) {
			DefaultExportFormat export = (DefaultExportFormat)obj;
			return (format != null && format.equals(export.format))
					&& (outcome != null && outcome.equals(export.outcome))
					&& ((typeFormat == null && export.typeFormat == null)
							|| (typeFormat != null && typeFormat.equals(export.typeFormat)));	
		}
		return false;
	}
}