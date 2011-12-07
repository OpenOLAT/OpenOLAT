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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.commons.servlets.util.URLEncoder;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;


/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  14.06.2006 <br>
 *
 * @author Felix Jost
 */
public class BusinessControlFactory {
	
	private static final BusinessControlFactory INSTANCE = new BusinessControlFactory();
	final BusinessControl EMPTY; // for performance
	
	private static final Pattern PAT_CE = Pattern.compile("\\[([^\\]]*)\\]");

	private BusinessControlFactory() {
		// singleton
	
		EMPTY = new BusinessControl() {
			
			public String toString() {
				return "[EMPTY(cnt:0, curPos:1) ]";
			}
			
			public String getAsString() {
				return "";
			}

			@Override
			//fxdiff BAKS-7 Resume function
			public List<ContextEntry> getEntries() {
				return Collections.<ContextEntry>emptyList();
			}

			public ContextEntry popLauncherContextEntry() {
				return null;
			}

			public void dropLauncherEntries() {
				throw new AssertException("dropping all entries, even though EMPTY");
			}

			public boolean hasContextEntry() {
				return false;
			}

			public int getStackedCount() {
				return 0;
			}

			@Override
			public ContextEntry getCurrentContextEntry() {
				return null;
			}

			public void setCurrentContextEntry(ContextEntry cw) {
				throw new AssertException("wrong call");
			}
			
		};
	}
	
	public static BusinessControlFactory getInstance() {
		return INSTANCE;
	}	
	
	/**
	 *
	 * to be used when a new window is opened (see references to this method as an example)
	 *
	 * @param contextEntry
	 * @param windowWControl
	 * @param businessWControl
	 * @return
	 */
	public WindowControl createBusinessWindowControl(final ContextEntry contextEntry, WindowControl windowWControl, WindowControl businessWControl) {
		BusinessControl origBC = businessWControl.getBusinessControl();
		
		BusinessControl bc;
		if (contextEntry != null) {
			bc = new StackedBusinessControl(contextEntry, origBC);
		} else {
			// pass through
			bc = origBC;
		}
		WindowControl wc = new StackedBusinessWindowControl(windowWControl, bc);
		return wc;
	}
	
	public BusinessControl createBusinessControl(ContextEntry ce, BusinessControl origBC) {
		if (origBC == null) {
			origBC = EMPTY;
		}
		BusinessControl bc = new StackedBusinessControl(ce, origBC);
		return bc;
	}

	/**
	 * to be used when a new controller (but not in a new window) is opened (a controller with a contextual business id, that is, the
	 * parent opening the controller provides a id = how it will "call" the newly generated controller). it needs to be able to reopen the same controller
	 * upon e.g. request by the search engine when a user clicks on a search result.
	 * @param contextEntry
	 * @param origWControl
	 * @return
	 */
	public WindowControl createBusinessWindowControl(final ContextEntry contextEntry, WindowControl origWControl) {
		BusinessControl origBC = origWControl.getBusinessControl();
		BusinessControl bc = new StackedBusinessControl(contextEntry, origBC);
		WindowControl wc = new StackedBusinessWindowControl(origWControl, bc);
		return wc;
	}
	
	/**
	 * The method check for duplicate entries!!!
	 * @param ores
	 * @param wControl
	 * @return
	 */
	//fxdiff BAKS-7 Resume function
	public WindowControl createBusinessWindowControl(final OLATResourceable ores, StateEntry state, WindowControl wControl) {
		WindowControl bwControl;
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ores);
		if(ce.equals(wControl.getBusinessControl().getCurrentContextEntry())) {
			bwControl = wControl;
			wControl.getBusinessControl().getCurrentContextEntry().setTransientState(state);
		} else {
			bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
			ce.setTransientState(state);
		}
		return bwControl;
	}
	
	/**
	 * The method check for duplicate entries!!!
	 * @param ores
	 * @param wControl
	 * @return
	 */
	//fxdiff BAKS-7 Resume function
	public WindowControl createBusinessWindowControl(UserRequest ureq, final OLATResourceable ores, StateEntry state,
			WindowControl wControl, boolean addToHistory) {
		WindowControl bwControl = createBusinessWindowControl(ores, state, wControl);
		if(addToHistory) {
			ureq.getUserSession().addToHistory(ureq, bwControl.getBusinessControl());
		}
		return bwControl;
	}
	
	//fxdiff BAKS-7 Resume function
	public void addToHistory(UserRequest ureq, WindowControl wControl) {
		if(wControl == null || wControl.getBusinessControl() == null) return;
		ureq.getUserSession().addToHistory(ureq, wControl.getBusinessControl());
	}
	
	public void addToHistory(UserRequest ureq, HistoryPoint historyPoint) {
		ureq.getUserSession().addToHistory(ureq, historyPoint);
	}

	public WindowControl createBusinessWindowControl(BusinessControl businessControl, WindowControl origWControl) {
		WindowControl wc = new StackedBusinessWindowControl(origWControl, businessControl);
		return wc;
	}
	
	public BusinessControl getEmptyBusinessControl() {
		// immutable, so therefore we can reuse it
		return EMPTY;
	}
	
	public ContextEntry createContextEntry(OLATResourceable ores) {
		return new MyContextEntry(ores);	
	}
	
	public ContextEntry createContextEntry(Identity identity) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
		return new MyContextEntry(ores);	
	}

	public String getAsString(BusinessControl bc) {
		return bc.getAsString();
	}
	
	
	public BusinessControl createFromString(String businessControlString) {
		final List<ContextEntry> ces = createCEListFromString(businessControlString);
		
		ContextEntry rootEntry = null;
		if (ces.isEmpty() || ((rootEntry = ces.get(0))==null)) {
			Tracing.logWarn("OLAT-4103, OLAT-4047, empty or invalid business controll string. list is empty. string is "+businessControlString, new Exception("stacktrace"), getClass());
		}
		return createFromContextEntries(ces);
	}

	//fxdiff BAKS-7 Resume function
	public BusinessControl createFromContextEntries(final List<ContextEntry> ces) {
		ContextEntry rootEntry = null;
		if (ces.isEmpty() || ((rootEntry = ces.get(0))==null)) {
			Tracing.logWarn("OLAT-4103, OLAT-4047, empty or invalid business controll string. list is empty.", new Exception("stacktrace"), getClass());
		}
		
		//Root businessControl with RootContextEntry which must be defined (i.e. not null)
		BusinessControl bc = new StackedBusinessControl(rootEntry, null) {

			public ContextEntry popLauncherContextEntry() {
				return popInternalLaucherContextEntry();
			}

			ContextEntry popInternalLaucherContextEntry(){
				if (ces.size() == 0) return null;
				ContextEntry ce = ces.remove(0);
				return ce;
			}
			
			
			public void dropLauncherEntries() {
				ces.clear();
			}

			public boolean hasContextEntry() {
				return ces.size() > 0;
			}
		};
		

		return bc;
	}
	
	public List<ContextEntry> createCEListFromString(String businessControlString) {
		// e.g. [repo:123][CourseNode:345][folder][path=sdfsd/sdfd/]
		List<ContextEntry> entries = new ArrayList<ContextEntry>();
		BusinessControlFactory bcf = BusinessControlFactory.getInstance();
		
		Matcher m = PAT_CE.matcher(businessControlString);
		while (m.find()) {
			String ces = m.group(1);
			int pos = ces.indexOf(':');
			OLATResourceable ores;
			// FIXME:chg: 'path=' define only once, same path in SearchResourceContext
			if ( (ces.startsWith("path="))||(pos == -1) ) {
				ces = ces.replace("|", "/");
				ores = OresHelper.createOLATResourceableTypeWithoutCheck(ces);
			} else {
				String type = ces.substring(0, pos);
				String keyS = ces.substring(pos+1);
				try {
					Long key = Long.parseLong(keyS);
					ores = OresHelper.createOLATResourceableInstanceWithoutCheck(type, key);
				} catch (NumberFormatException e) {
					Tracing.logWarn("Cannot parse business path:" + businessControlString, e, BusinessControlFactory.class);
					return Collections.emptyList();
				}
			}
			ContextEntry ce = bcf.createContextEntry(ores);
			entries.add(ce);
		}
		return entries;
	}
	
	/**
	 * Return an URL in the form of http://www.olat.org:80/olat/url/RepsoitoryEntry/49358
	 * @param bc
	 * @param normalize If true, prevent duplicate entry (it can happen)
	 * @return
	 */
	public String getAsURIString(BusinessControl bc, boolean normalize) {
		String businessPath = bc.getAsString();
		List<ContextEntry> ceList = createCEListFromString(businessPath);
		String restUrl = getAsURIString(ceList, normalize);
		return restUrl;
	}
	
	/**
	 * Return an URL in the form of http://www.olat.org:80/olat/url/RepsoitoryEntry/49358
	 * @param ceList
	 * @param normalize If true, prevent duplicate entries (it can happen)
	 * @return
	 */
	public String getAsURIString(List<ContextEntry> ceList, boolean normalize) {
		if(ceList == null || ceList.isEmpty()) return "";
		
		StringBuilder retVal = new StringBuilder();
		retVal.append(Settings.getServerContextPathURI())
			.append("/url/");

		//see code in JumpInManager, cannot be used, as it needs BusinessControl-Elements, not the path
		String lastEntryString = null;
		for (ContextEntry contextEntry : ceList) {
			String ceStr = contextEntry != null ? contextEntry.toString() : "NULL_ENTRY";
			if(normalize) {
				if(lastEntryString == null){
					lastEntryString = ceStr;
				} else if (lastEntryString.equals(ceStr)) {
					continue;
				}
			}
			
			if(ceStr.startsWith("[path")) {
				//the %2F make a problem on browsers.
				//make the change only for path which is generally used
				//TODO: find a better method or a better separator as |
				ceStr = ceStr.replace("%2F", "~~");
			}
			ceStr = ceStr.replace(':', '/');
			ceStr = ceStr.replaceFirst("\\]", "/");
			ceStr= ceStr.replaceFirst("\\[", "");
			retVal.append(ceStr);
		}
		return retVal.substring(0, retVal.length()-1);
	}
	
	//fxdiff BAKS-7 Resume function
	public String getPath(ContextEntry entry) {
		String path = entry.getOLATResourceable().getResourceableTypeName();
		path = path.endsWith(":0") ? path.substring(0, path.length() - 2) : path;
		path = path.startsWith("path=") ? path.substring(5, path.length()) : path;
		return path;
	}
	
	public String getBusinessPathAsURIFromCEList(List<ContextEntry> ceList){
		if(ceList == null || ceList.isEmpty()) return "";
		
		StringBuilder retVal = new StringBuilder();
		//see code in JumpInManager, cannot be used, as it needs BusinessControl-Elements, not the path
		for (ContextEntry contextEntry : ceList) {
			String ceStr = contextEntry != null ? contextEntry.toString() : "NULL_ENTRY";
			if(ceStr.startsWith("[path")) {
				//the %2F make a problem on browsers.
				//make the change only for path which is generally used
				//TODO: find a better method or a better separator as |
				ceStr = ceStr.replace("%2F", "~~");
			}
			ceStr = ceStr.replace(':', '/');
			ceStr = ceStr.replaceFirst("\\]", "/");
			ceStr= ceStr.replaceFirst("\\[", "");
			retVal.append(ceStr);
		}
		return retVal.substring(0, retVal.length()-1);
	}
	
	public String formatFromURI(String restPart) {
		try {
			restPart = URLDecoder.decode(restPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			//log.error("Unsupported encoding", e);
		}
		
		String[] split = restPart.split("/");
		if (split.length % 2 != 0) {
			return null;
		}
		String businessPath = "";
		for (int i = 0; i < split.length; i=i+2) {
			String key = split[i];
			if(key != null && key.startsWith("path=")) {
				key = key.replace("~~", "/");
			}
			String value = split[i+1];
			businessPath += "[" + key + ":" + value +"]";
		}
		return businessPath;
	}
}	

class MyContextEntry implements ContextEntry {
	private final OLATResourceable olatResourceable;

	//fxdiff BAKS-7 Resume function
	private StateEntry state;

	MyContextEntry(OLATResourceable ores) {
		this.olatResourceable = ores;
	}
	
	/**
	 * @return Returns the olatResourceable.
	 */
	public OLATResourceable getOLATResourceable() {
		return olatResourceable;
	}
	
	@Override
	//fxdiff BAKS-7 Resume function
	public StateEntry getTransientState() {
		return state;
	}

	@Override
	//fxdiff BAKS-7 Resume function
	public void setTransientState(StateEntry state) {
		this.state = state;
	}
	
	@Override
	public ContextEntry clone() {
		MyContextEntry entry = new MyContextEntry(olatResourceable);
		if(state != null) {
			entry.state = state.clone();
		}
		return entry;
	}
	
	@Override
	public String toString(){
		URLEncoder urlE = new URLEncoder();
		String resource =urlE.encode(this.olatResourceable.getResourceableTypeName());
		return "["+resource+":"+this.olatResourceable.getResourceableId()+"]";
	}
	
	@Override
	public int hashCode() {
		return (olatResourceable==null) ? super.hashCode() : olatResourceable.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (olatResourceable==null) {
			return super.equals(obj);
		} else if (obj instanceof MyContextEntry) {
			MyContextEntry mce = (MyContextEntry)obj;
			
			// safe comparison including null value checks
			Long myResId = olatResourceable.getResourceableId();
			Long itsResId = mce.olatResourceable.getResourceableId();
			if (myResId==null && itsResId!=null) return false;
			if (myResId!=null && itsResId==null) return false;
			if (myResId!=null && itsResId!=null) {
				if (!myResId.equals(itsResId)) return false;
			}
			
			String myResName = olatResourceable.getResourceableTypeName();
			String itsResName = mce.olatResourceable.getResourceableTypeName();
			if (myResName==null && itsResName!=null) return false;
			if (myResName!=null && itsResName==null) return false;
			if (myResName!=null && itsResName!=null) {
				if (!myResName.equals(itsResName)) {
					return false;
				}
			}
			
			if(state == null && mce.state == null) {
				return true;
			} else if (state != null && state.equals(mce.state)) {
				return true;
			}
			return false;
		} else {
			return super.equals(obj);
		}
	}
}

