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
package org.olat.core.gui.components.tree;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;

/**
 * 
 * Description:<br>
 * Mapper which return feedback to the mouse over
 * 
 * <P>
 * Initial Date:  23 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-9: drag and drop in menu tree
public class DnDFeedbackMapper implements Mapper {
	
	private static final String ENCODING_UTF_8 = "utf-8";
	private static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript;";
	
	private final MenuTree menuTree;
	
	public DnDFeedbackMapper(MenuTree tree) {
		menuTree = tree;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {	
		String dropNodeId = request.getParameter(MenuTree.NODE_IDENT);
		String targetNodeId = request.getParameter(MenuTree.TARGET_NODE_IDENT);
		boolean sibling = "end".equals(request.getParameter(MenuTree.SIBLING_NODE))
		  || "yes".equals(request.getParameter(MenuTree.SIBLING_NODE));
		
		StringMediaResource jsonResource = new StringMediaResource();
		jsonResource.setEncoding(ENCODING_UTF_8);
		jsonResource.setContentType(CONTENT_TYPE_JAVASCRIPT);
		if(menuTree.canDrop(dropNodeId, targetNodeId, sibling)) {
			jsonResource.setData("{\"dropAllowed\":true}");
		} else {
			jsonResource.setData("{\"dropAllowed\":false}");
		}
		return jsonResource;
	}
}
