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

package org.olat.core.gui.components.table;

import java.util.Locale;
import java.util.Map;

/**
 * Description:<br>
 * TODO: patrickb Class Description for TableReplayableEvent
 * 
 * <P>
 * Initial Date:  09.07.2006 <br>
 * @author patrickb
 */
public class TableReplayableEvent  {
	private static final String M_DATA = "mData";
	private static final String I_MODE = "iMode";
	static final int SORT = 0;
	static final int MOVE_R = 1;
	static final int MOVE_L = 2;
	static final int PAGE_ACTION = 3;
	static final int ROW_ACTION = 4;
	static final int MULTISELECT_ACTION = 5;

	private int cmd;
	private Map recMap;
	private String title;
  
	/**
	 * for replaying
	 * @param baseTypeMap
	 */
	public TableReplayableEvent(final Map<String,Object> baseTypeMap){
		recMap = (Map)baseTypeMap.get(M_DATA);
		cmd = ((Integer)baseTypeMap.get(I_MODE)).intValue();
	}
	
	public TableReplayableEvent(final int recCmd, final Map recMap) {
		this.recMap = recMap;
		this.cmd = recCmd;
	}

	/**
	 * @see org.olat.core.gui.control.recorder.ReplayableEvent#getDescription(java.util.Locale)
	 */
	public String getDescription(final Locale locale) {
		return null;
	}

	/**
	 * @see org.olat.core.gui.control.recorder.ReplayableEvent#getTitle(java.util.Locale)
	 */
	public String getTitle(final Locale locale) {
		return title;
	}

	/**
	 * @see org.olat.core.gui.control.recorder.ReplayableEvent#saveTo(java.util.Map)
	 */
	public void saveTo(final Map<String,Object> baseTypeMap) {
		baseTypeMap.put(M_DATA,recMap);
		baseTypeMap.put(I_MODE,Integer.valueOf(cmd));
	}

	/**
	 * @return Returns the cmd.
	 */
	public int getCmd() {
		return cmd;
	}

	/**
	 * @param cmd The cmd to set.
	 */
	public void setCmd(final int cmd) {
		this.cmd = cmd;
	}

	/**
	 * @return Returns the recMap.
	 */
	public Map getRecMap() {
		return recMap;
	}

	/**
	 * @param recMap The recMap to set.
	 */
	public void setRecMap(final Map recMap) {
		this.recMap = recMap;
	}

	/**
	 * @param replayTitle
	 */
	public void setTitle(final String title) {
		this.title = title;
		
	}

}
