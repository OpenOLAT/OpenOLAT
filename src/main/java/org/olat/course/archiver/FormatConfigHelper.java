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
package org.olat.course.archiver;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * this class reads and writes XML serialized config data to personal gui prefs and retrieves them
 * 
 * Initial Date: 21.04.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class FormatConfigHelper {
	
	private static final String QTI_EXPORT_ITEM_FORMAT_CONFIG = "QTIExportItemFormatConfig";
	private static final Logger log = Tracing.createLoggerFor(FormatConfigHelper.class);
	
	private static final XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { ExportFormat.class };
		configXstream.addPermission(new ExplicitTypePermission(types));
		configXstream.alias(QTI_EXPORT_ITEM_FORMAT_CONFIG, ExportFormat.class);
	}

	public static ExportFormat loadExportFormat(UserRequest ureq) {
		ExportFormat formatConfig = null;
		if (ureq != null) {
			try {
				Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
				String formatConfigString = (String) guiPrefs.get(ExportOptionsController.class, QTI_EXPORT_ITEM_FORMAT_CONFIG);
				if(StringHelper.containsNonWhitespace(formatConfigString)) {
					formatConfig = (ExportFormat)configXstream.fromXML(formatConfigString);
				} else {
					formatConfig = new ExportFormat(true, true, true, true, true);
				}
			} catch (Exception e) {
				log.error("could not establish object from xml", e);
				formatConfig = new ExportFormat(true, true, true, true, true);
			}
		}
		return formatConfig;
	}
	
	public static void updateExportFormat(UserRequest ureq, boolean responsecols, boolean poscol, boolean pointcol, boolean timecols, boolean commentcol) {
		// save new config in GUI prefs
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			ExportFormat formatConfig = new ExportFormat(responsecols, poscol, pointcol, timecols, commentcol);
			try {
				String formatConfigString = configXstream.toXML(formatConfig);
				guiPrefs.putAndSave(ExportOptionsController.class, QTI_EXPORT_ITEM_FORMAT_CONFIG, formatConfigString);
			} catch (Exception e) {
				log.error("",e);
			}
		}
	}
}
