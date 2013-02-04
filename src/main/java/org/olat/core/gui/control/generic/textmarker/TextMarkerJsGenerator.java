/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.core.gui.control.generic.textmarker;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.commons.modules.glossary.GlossaryItem;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.util.Encoder;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;


/**
 * 
 * Generates Java-Script code for text-marker component.
 * 
 * @author Christian Guretzki
 * @author Roman Haag, frentix GmbH, frentix.com
 * 
 */
public class TextMarkerJsGenerator {

	
	public static String loadGlossaryItemListAsJSCommandsString(VFSContainer glossaryFolder, String domID) {
		ArrayList<GlossaryItem> glossaryItemArr = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);
		StringBuilder sb = new StringBuilder();		
		sb.append("o_info.glossaryTermArray_").append(domID).append(" = ").append(buildJSArrayString(glossaryItemArr));
		// start highlighting process with this array
		sb.append("Ext.onReady(function() {o_tm_highlightFromArray(o_info.glossaryTermArray_").append(domID).append(", \"").append(domID).append("\")});");
		
		return sb.toString();
	}
	
	public static String loadGlossaryItemListAsJSArray(VFSContainer glossaryFolder) {
		ArrayList<GlossaryItem> glossaryItemArr = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);

		//FIXME: gloss: use helper method to encode folder -> id  
		//String glossaryId = Encoder.encrypt(((LocalFolderImpl)glossaryFolder).getBasefile().toString());
		StringBuilder sb = new StringBuilder();		
		//sb.append("o_glossaries[").append(glossaryId).append("] = ").append(buildJSArrayString(glossaryItemArr));
		sb.append(buildJSArrayString(glossaryItemArr));

		return sb.toString();		
	}
	
	
	/*
	 * build array of glossaryTerms containing array with term, flexion, synonym...
	 */
	public static StringBuilder buildJSArrayString(ArrayList<GlossaryItem> glossaryItemArr){
		StringBuilder sb = new StringBuilder();
		sb.append("new Array(");
		for (Iterator iterator = glossaryItemArr.iterator(); iterator.hasNext();) {
			GlossaryItem glossaryItem = (GlossaryItem) iterator.next();
			ArrayList<String> allHighlightStrings = glossaryItem.getAllStringsToMarkup();
			sb.append("new Array(\"");
			for (Iterator iterator2 = allHighlightStrings.iterator(); iterator2.hasNext();) {
				String termFlexionSynonym = (String) iterator2.next();
				//fxdiff:  FXOLAT-235  fix quotationsmarks that break the js code
				termFlexionSynonym = StringEscapeUtils.escapeJava(termFlexionSynonym);
				
				sb.append(termFlexionSynonym);
				sb.append("\"");
				if (iterator2.hasNext()) sb.append(",\"");
			}
			sb.append(")");
			if (iterator.hasNext()) sb.append(",");
		}
		
		sb.append(");");
		return sb;
	}
	
	
	
}
