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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.dispatcher.jumpin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.servlets.util.URLEncoder;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StackedBusinessControl;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;

/**
 * Description: <br>
 * Initial Date: 23.02.2005 <br>
 * 
 * @author Felix Jost
 */
public class JumpInManager  extends BasicManager {
	private static JumpInManager INSTANCE;
	private static Map handlers;
	private Set paths;

	// Const for new 5.1 JumpIn with Resource URL
	public static final String CONST_EXTLINK = "resource/go";
	public static final String CONST_RESOURCE_URL = "resourceurl";

	/**
	 * [spring]
	 * @param jumpinConfig
	 */
	private JumpInManager(JumpinConfig jumpinConfig) {
		handlers = jumpinConfig.getJumpinhandlers();
		paths = handlers.keySet();
		INSTANCE = this;
	}

	/**
	 * @return the Manager (singleton)
	 */
	public static JumpInManager getInstance() {
		return INSTANCE;
	}

	/**
	 * @param ureq
	 * @return the handler or null if no matching handler for this uri could be
	 *         found
	 */
	public JumpInReceptionist getJumpInReceptionist(UserRequest ureq) {
		String uri = ureq.getNonParsedUri();
		//
		JumpInHandlerFactory handler = null;
		for (Iterator iter = paths.iterator(); iter.hasNext();) {
			String path = (String) iter.next();
			if(uri.startsWith(path)){
				//found the handler, create JumpInReceptionist
				handler = (JumpInHandlerFactory)handlers.get(path);
				return handler.createJumpInHandler(ureq);
			}
		}
		//no handler found
		return null;
	}
	
	/**
	 * Returns a direkt jump-in URI for certain BusinessControl.
	 * e.g. http://olathost.org/olat/resource/go?resourceurl=[BusinessGroup=123456]
	 * @param bc  BusinessControl
	 * @return JumpIn URI e.g. http://olathost.org/olat/resource/go?resourceurl=[BusinessGroup=123456]
	 */
	public static String getJumpInUri(BusinessControl bc) {
		StringBuilder sb = new StringBuilder();
		sb.append(Settings.getServerContextPathURI()).append(DispatcherAction.PATH_AUTHENTICATED).append(CONST_EXTLINK).append("?");
		sb.append(CONST_RESOURCE_URL);
		sb.append("=");
		sb.append( new URLEncoder().encode(bc.getAsString()) );
		return sb.toString();
	}

	/**
	 * Returns a REST like URL for a certain BusinessControl. Or "EMPTY_BUSINESS_PATH/" if not applicable
	 * @param bc  BusinessControl
	 * @return JumpIn URI e.g. /RepoEntry/12341234/CourseNode/13123/message/34432
	 */	
	public static String getRestJumpInUri(BusinessControl bc){
		String retVal = "EMPTY_BUSINESS_PATH/";
		if(bc instanceof StackedBusinessControl){
			retVal ="";
			StackedBusinessControl sbc =(StackedBusinessControl) bc;
			List<ContextEntry> businessControls = sbc.getBusinessControls();
			if(businessControls != null){
				for (ContextEntry contextEntry : businessControls) {
					//retVal = contextEntry+retVal;
					/**
					 * possible way to convert
					 */
					URLEncoder olatUrlEncoder = new URLEncoder();
					String ceStr = contextEntry != null ? contextEntry.toString() : "NULL_ENTRY";
					ceStr = ceStr.replace(':', '/');
					ceStr = ceStr.replaceFirst("\\]", "/");
					ceStr= ceStr.replaceFirst("\\[", "");
					retVal = ceStr + retVal;
					/**/
					
				}
			}else{
				//System.err.println("Empty List*********************");
			}
		}
		return retVal.length() > 0 ? retVal.substring(0, retVal.length()-1) : "EMPTY_BUSINESS_PATH";
	}
	
}
