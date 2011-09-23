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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.core.id.context;


/**
 * Description:<br>
 * use for e.g. a key to store stuff, and to get a context for the search engine, 
 * to later open the correct navigational paths.<br>
 * <br>
 * the business control is a immutable object.<br>
 * <br>
 * usage:
 * <pre>
 *    final ores = ... (a OLATResourceable)
 *    ContextEntry ce = new ContextEntry() {
 *			public OLATResourceable getOLATResourceable() {
 *				return ores;
 *			}};
 *		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
 * </pre>
 * <P>
 * Initial Date:  14.06.2006 <br>
 *
 * @author Felix Jost
 */
public interface BusinessControl {
	
	public final static String START_TAG = "[";
	public final static String END_TAG = "]";
	public final static String DELIMITER_TAG = ":";
		
	/**
	 * Get String represation of BusinessControl. Used to save it as string (serializing)
	 * @return
	 */
	public String getAsString();
	
	/**
	 * pop context entry for further processing, this is used for spawning controllers
	 * @return
	 */
	public ContextEntry popLauncherContextEntry();
	
	/**
	 * @return The current context entry
	 */
	public ContextEntry getCurrentContextEntry();
	
	/**
	 * set new context entry which reflects latest state of a controller, which can be reactivated
	 * @param cw
	 */
	public void setCurrentContextEntry(ContextEntry cw);
	
	/**
	 * used when the current contextentry cannot be resolved (e.g. a coursenode that no longer is accessible or existing)
	 *
	 */
	public void dropLauncherEntries();


	/**
	 * if still some more context entries for spawning are available
	 * @return
	 */
	public boolean hasContextEntry();
	
	
	
	//public int getStackedCount();
}
