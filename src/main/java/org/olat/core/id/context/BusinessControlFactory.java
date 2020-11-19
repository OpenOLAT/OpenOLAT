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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.id.context;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.servlets.URLEncoder;


/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  14.06.2006 <br>
 *
 * @author Felix Jost
 */
public class BusinessControlFactory {
	
	private static final Logger log = Tracing.createLoggerFor(BusinessControlFactory.class);
	
	private static final BusinessControlFactory INSTANCE = new BusinessControlFactory();
	final BusinessControl EMPTY; // for performance
	
	private static final Pattern PAT_CE = Pattern.compile("\\[([^\\]]*)\\]");
	private static final DateFormat ceDateFormat = new SimpleDateFormat("yyyyMMdd");

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
			public List<ContextEntry> getEntries() {
				return Collections.<ContextEntry>emptyList();
			}
			
			@Override
			public List<ContextEntry> getEntriesDownTheControls() {
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
	public WindowControl createBusinessWindowControl(UserRequest ureq, final OLATResourceable ores, StateEntry state,
			WindowControl wControl, boolean addToHistory) {
		WindowControl bwControl = createBusinessWindowControl(ores, state, wControl);
		if(addToHistory) {
			ureq.getUserSession().addToHistory(ureq, bwControl.getBusinessControl());
		}
		return bwControl;
	}
	
	public void addToHistory(UserRequest ureq, WindowControl wControl) {
		if(wControl == null || wControl.getBusinessControl() == null) return;
		ureq.getUserSession().addToHistory(ureq, wControl.getBusinessControl());
	}
	
	public void addToHistory(UserRequest ureq, HistoryPoint historyPoint) {
		ureq.getUserSession().addToHistory(ureq, historyPoint);
	}
	
	public void removeFromHistory(UserRequest ureq, WindowControl wControl) {
		if(wControl == null || wControl.getBusinessControl() == null) return;
		ureq.getUserSession().removeFromHistory(wControl.getBusinessControl());
	}

	public WindowControl createBusinessWindowControl(BusinessControl businessControl, WindowControl origWControl) {
		WindowControl wc = new StackedBusinessWindowControl(origWControl, businessControl);
		return wc;
	}
	
	public WindowControl createBusinessWindowControl(WindowControl origWControl, OLATResourceable... ores) {
		List<ContextEntry> ces;
		if(ores != null && ores.length > 0) {
			ces = new ArrayList<>(ores.length);
			for(OLATResourceable o:ores) {
				ces.add(createContextEntry(o));
			}
		} else {
			ces = Collections.emptyList();
		}
		BusinessControl bc = createFromContextEntries(ces);
		return createBusinessWindowControl(bc, origWControl);
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
		if (ces.isEmpty() || ces.get(0) ==null) {
			log.warn("OLAT-4103, OLAT-4047, empty or invalid business controll string. list is empty. string is "+businessControlString, new Exception("stacktrace"));
		}
		return createFromContextEntries(ces);
	}
	
	public BusinessControl createFromPoint(HistoryPoint point) {
		final List<ContextEntry> ces = point.getEntries();
		if (ces.isEmpty() || ces.get(0) == null) {
			log.warn("OLAT-4103, OLAT-4047, empty or invalid business controll string. list is empty. string is " + point.getBusinessPath(), new Exception("stacktrace"));
		}
		return createFromContextEntries(ces);
	}

	public List<ContextEntry> cloneContextEntries(final List<ContextEntry> ces) {
		final List<ContextEntry> clones = new ArrayList<>(ces.size());
		for(ContextEntry ce:ces) {
			OLATResourceable clone = OresHelper.clone(ce.getOLATResourceable());
			clones.add(new MyContextEntry(clone));
		}
		return clones;
	}

	public BusinessControl createFromContextEntries(final List<ContextEntry> ces) {
		ContextEntry rootEntry = null;
		if (ces.isEmpty() || ((rootEntry = ces.get(0))==null)) {
			log.warn("OLAT-4103, OLAT-4047, empty or invalid business controll string. list is empty.", new Exception("stacktrace"));
		}
		
		//Root businessControl with RootContextEntry which must be defined (i.e. not null)
		BusinessControl bc = new StackedBusinessControl(rootEntry, null) {

			@Override
			public ContextEntry popLauncherContextEntry() {
				return popInternalLaucherContextEntry();
			}

			@Override
			ContextEntry popInternalLaucherContextEntry(){
				if (ces.size() == 0) return null;
				ContextEntry ce = ces.remove(0);
				return ce;
			}
			
			@Override
			public List<ContextEntry> getEntriesDownTheControls() {
				List<ContextEntry> allEntries = new ArrayList<>();
				List<ContextEntry> entries = super.getEntries();
				if(entries != null) {
					allEntries.addAll(entries);
				}
				if(ces != null) {
					allEntries.addAll(ces);
				}
				return allEntries;
			}

			@Override
			public void dropLauncherEntries() {
				ces.clear();
			}

			@Override
			public boolean hasContextEntry() {
				return ces.size() > 0;
			}
		};
		return bc;
	}
	
	public List<ContextEntry> createCEListFromString(OLATResourceable... resources) {
		List<ContextEntry> entries = new ArrayList<>();
		if(resources != null && resources.length > 0) {
			for(OLATResourceable resource:resources) {
				entries.add(createContextEntry(resource));
			}
		}
		return entries;
	}
	
	public List<ContextEntry> createCEListFromResourceable(OLATResourceable resource, StateEntry stateEntry) {
		List<ContextEntry> entries = new ArrayList<>();
		ContextEntry entry = createContextEntry(resource);
		entry.setTransientState(stateEntry);
		entries.add(entry);
		return entries;
	}
	
	/**
	 * helloworld will be an entry helloworld:0
	 * @param resourceType
	 * @return
	 */
	public List<ContextEntry> createCEListFromResourceType(String resourceType) {
		List<ContextEntry> entries = new ArrayList<>(3);
		if(StringHelper.containsNonWhitespace(resourceType)) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(resourceType, 0l);
			ContextEntry entry = createContextEntry(ores);
			entries.add(entry);
		}
		return entries;
	}
	
	/**
	 * e.g. [repo:123][CourseNode:345][folder][path=/sdfsd/sdfd:0]
	 * @param businessControlString
	 * @return
	 */
	public List<ContextEntry> createCEListFromString(String businessControlString) {
		List<ContextEntry> entries = new ArrayList<>();
		if(!StringHelper.containsNonWhitespace(businessControlString)) {
			return entries;
		}

		Matcher m = PAT_CE.matcher(businessControlString);
		while (m.find()) {
			String ces = m.group(1);
			int pos = ces.lastIndexOf(':');
			OLATResourceable ores;
			if(pos == -1) {
				if(ces.startsWith("path=")) {
					ces = ces.replace("|", "/");
				}
				ores = OresHelper.createOLATResourceableTypeWithoutCheck(ces);
			} else {
				String type = ces.substring(0, pos);
				String keyS = ces.substring(pos+1);
				if(type.startsWith("path=")) {
					ces = type.replace("|", "/");
				}
				try {
					Long key = Long.parseLong(keyS);
					ores = OresHelper.createOLATResourceableInstanceWithoutCheck(type, key);
				} catch (NumberFormatException e) {
					log.warn("Cannot parse business path:" + businessControlString, e);
					return entries;//return what we decoded
				}
			}
			ContextEntry ce = createContextEntry(ores);
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
		
		return appendToURIString(retVal, ceList, normalize);
	}

	public String getAsAuthURIString(List<ContextEntry> ceList, boolean normalize) {
		StringBuilder retVal = new StringBuilder();
		retVal.append(Settings.getServerContextPathURI())
			.append("/auth/");
		
		if(ceList == null || ceList.isEmpty()) {
			return retVal.toString();
		}
		return appendToURIString(retVal, ceList, normalize);
	}
	
	public String getAsRestPart(List<ContextEntry> ceList, boolean normalize) {
		StringBuilder retVal = new StringBuilder();
		if(ceList == null || ceList.isEmpty()) {
			return retVal.toString();
		}
		return appendToURIString(retVal, ceList, normalize);
	}
	
	private String appendToURIString(StringBuilder retVal, List<ContextEntry> ceList, boolean normalize) {
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
				ceStr = ceStr.replace("%2F", "~~");
			}
			ceStr = ceStr.replace(':', '/');
			ceStr = ceStr.replaceFirst("\\]", "/");
			ceStr= ceStr.replaceFirst("\\[", "");
			retVal.append(ceStr);
		}
		return retVal.substring(0, retVal.length()-1);
	}
	
	/**
	 * Return the standard format for date: [date=20120223:0]
	 * @param date
	 * @return
	 */
	public String getContextEntryStringForDate(Date date) {
		StringBuilder sb = new StringBuilder("[date=");
		synchronized(ceDateFormat) {//DateFormat isn't thread safe but costly to create, we reuse it
			sb.append(ceDateFormat.format(date));
		}
		sb.append(":0]");
		return sb.toString();
	}
	
	public Date getDateFromContextEntry(ContextEntry entry) {
		String dateEntry = entry.getOLATResourceable().getResourceableTypeName();
		
		Date date = null;
		if(dateEntry.startsWith("date=")) {
			try {
				int sepIndex = dateEntry.indexOf(':');
				int lastIndex = (sepIndex > 0 ? sepIndex : dateEntry.length());
				if(lastIndex > 0) {
					String dateStr = dateEntry.substring("date=".length(), lastIndex);
					synchronized(ceDateFormat) {//DateFormat isn't thread safe but costly to create, we reuse it
						date = ceDateFormat.parse(dateStr);
					}
				}
			} catch (ParseException e) {
				log.warn("Error parsing the date after activate: {}", dateEntry, e);
			}
		}
		return date;
	}

	public String getPath(ContextEntry entry) {
		String path = entry.getOLATResourceable().getResourceableTypeName();
		path = path.endsWith(":0") ? path.substring(0, path.length() - 2) : path;
		path = path.startsWith("path=") ? path.substring(5, path.length()) : path;
		return path;
	}
	
	public String getBusinessPathAsURIFromCEList(List<ContextEntry> ceList){
		if(ceList == null || ceList.isEmpty()) return "";
		
		StringBuilder retVal = new StringBuilder();
		for (ContextEntry contextEntry : ceList) {
			String ceStr = contextEntry != null ? contextEntry.toString() : "NULL_ENTRY";
			if(ceStr.startsWith("[path")) {
				//the %2F make a problem on browsers.
				//make the change only for path which is generally used
				ceStr = ceStr.replace("%2F", "~~");
			}
			ceStr = ceStr.replace(':', '/');
			ceStr = ceStr.replaceFirst("\\]", "/");
			ceStr= ceStr.replaceFirst("\\[", "");
			retVal.append(ceStr);
		}
		return retVal.substring(0, retVal.length()-1);
	}
	
	public String getURLFromBusinessPathString(String bPathString){
		return getURLFromBusinessPathString("url", bPathString);
	}
	
	public String getAuthenticatedURLFromBusinessPathStrings(String... bPathString) {
		StringBuilder sb = new StringBuilder();
		for(String path:bPathString) {
			if(path != null) {
				sb.append(path);
			}
		}
		return getAuthenticatedURLFromBusinessPathString(sb.toString());
	}
	
	public String getAuthenticatedURLFromBusinessPathString(String bPathString) {
		return getURLFromBusinessPathString("auth", bPathString);
	}
	
	private String getURLFromBusinessPathString(String dispatcherPath, String bPathString) {
		if(!StringHelper.containsNonWhitespace(bPathString)) {
			return null;
		}
		
		try {
			BusinessControlFactory bCF = BusinessControlFactory.getInstance(); 
			List<ContextEntry> ceList = bCF.createCEListFromString(bPathString);
			String busPath = getBusinessPathAsURIFromCEList(ceList); 
			
			StringBuilder sb = new StringBuilder(64);
			sb.append(Settings.getServerContextPathURI())
			  .append("/").append(dispatcherPath).append("/")
			  .append(busPath);
			return sb.toString();
		} catch(Exception e) {
			log.error("Error with business path: " + bPathString, e);
			return null;
		}
	}
	
	public String getRelativeURLFromBusinessPathString(String bPathString){
		if(!StringHelper.containsNonWhitespace(bPathString)) {
			return null;
		}
		
		try { 
			List<ContextEntry> ceList = createCEListFromString(bPathString);
			String busPath = getBusinessPathAsURIFromCEList(ceList); 
			return WebappHelper.getServletContextPath() + "/url/" + busPath;
		} catch(Exception e) {
			log.error("Error with business path: " + bPathString, e);
			return null;
		}
	}
	
	public String formatFromURI(String restPart) {
		try {
			restPart = URLDecoder.decode(restPart, "UTF8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unsupported encoding", e);
		}
		
		String[] split = restPart.split("/");
		if (split.length % 2 != 0) {
			return null;
		}
		return formatFromSplittedURI(split);
	}
	
	public String formatFromSplittedURI(String[] split) {
		StringBuilder businessPath = new StringBuilder(64);
		for (int i = 0; i < split.length; i=i+2) {
			String key = split[i];
			if(key != null && key.startsWith("path=")) {
				key = key.replace("~~", "/");
			}
			String value = split[i+1];
			businessPath.append("[").append(key).append(":").append(value).append("]");
		}
		return businessPath.toString();
	}
}	

class MyContextEntry implements ContextEntry, Serializable {

	private static final long serialVersionUID = 949522581806327579L;
	private OLATResourceable olatResourceable;

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
	public void upgradeOLATResourceable(OLATResourceable ores) {
		olatResourceable = ores;
	}

	@Override
	public StateEntry getTransientState() {
		return state;
	}

	@Override
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

