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
package org.olat.util;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalHtmlUtil {
	private final static OLog log = Tracing.createLoggerFor(FunctionalHtmlUtil.class);
	
	/**
	 * @param html
	 * @param insertNewlines
	 * @return
	 * 
	 * Strips all markup of specified string.
	 */
	public String stripTags(String html, boolean insertNewlines){
		StringBuffer textBuffer = new StringBuffer();
		int offset = 0;
		int nextOffset = 0;
		
		html = html.substring(html.indexOf('>', html.indexOf("<body")) + 1, html.indexOf("</body"));
		
		while((nextOffset = html.indexOf('<', offset)) != -1){
			String currentText = html.substring(offset, nextOffset);
			
			if(!currentText.matches("^[\\s]+$")){
				textBuffer.append(currentText.trim());
				
				if(insertNewlines && !currentText.endsWith("\n")){
					textBuffer.append('\n');
				}
			}
			
			offset = html.indexOf('>', nextOffset) + 1;
		}
		 
		String currentText = html.substring(offset);
		
		if(!currentText.matches("^[\\s]+$")){
			textBuffer.append(currentText);
			
			if(insertNewlines && !currentText.endsWith("\n")){
				textBuffer.append('\n');
			}
		}
		
		return(textBuffer.toString());
	}
}
