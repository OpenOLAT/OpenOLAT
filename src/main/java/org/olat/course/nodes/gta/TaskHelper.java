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
package org.olat.course.nodes.gta;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.util.io.SystemFileFilter;

/**
 * 
 * Initial date: 09.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskHelper {
	
	private final static DecimalFormat formatFileSize = new DecimalFormat("#0.#", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public static String format(double value) {
		synchronized(formatFileSize) {
			return formatFileSize.format(value);
		}
	}
	
	public static DeliveryOptions getStandardDeliveryOptions() {
		DeliveryOptions config = new DeliveryOptions();
		config.setjQueryEnabled(Boolean.TRUE);
		config.setOpenolatCss(Boolean.TRUE);
		return config;
	}
	
	public static int countDocuments(File directory) {
		int count = 0;
		if(directory.isDirectory()) {
			count = directory.listFiles(SystemFileFilter.FILES_ONLY).length;
		}
		return count;
	}
	
	public static File[] getDocuments(File directory) {
		File[] files = null;
		if(directory.isDirectory()) {
			files = directory.listFiles(SystemFileFilter.FILES_ONLY);
		}
		return files;
	}
	
	public static boolean hasDocuments(File directory) {
		int count = 0;
		if(directory.isDirectory()) {
			count = directory.listFiles(SystemFileFilter.FILES_ONLY).length;
		}
		return count > 0;
	}
	
	public static boolean inOrNull(Task task, TaskProcess... steps) {
		if(task == null) return true;
		
		if(steps != null && steps.length > 0) {
			for(int i=steps.length; i-->0; ) {
				if(steps[i] != null && steps[i] == task.getTaskStatus()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	

}
