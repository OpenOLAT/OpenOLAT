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
*/

package org.olat.core.gui;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.olat.core.helpers.Settings;

public class GUIInterna {
	
	private static Boolean allowed = Settings.isAllowLoadtestMode();
	private static ThreadLocalData tld = new ThreadLocalData();

	private static Map<String,Map<Object,Object>> loadtestClients = new HashMap<String,Map<Object,Object>>();
	
	public static void begin (HttpServletRequest req){
		
		if (!allowed) return;
		if (req == null) return;
		String key = getCookie(req);
		if(key == null) return;
		
		String q = req.getQueryString();
		
		synchronized (loadtestClients) {
			
			if (q != null) {
				if (q.endsWith("noloadtest")) {
					if (loadtestClients.containsKey(key)) {
						loadtestClients.remove(key);
					}
				} else if (q.endsWith("loadtest")) {
					if (loadtestClients.containsKey(key)) {
						loadtestClients.remove(key);
					}
					loadtestClients.put(key, new HashMap<Object, Object>());
				} else if (q.equals("clearloadtests")) {
					loadtestClients.clear();
				}
			}
		
			if (loadtestClients.containsKey(key)) {
				tld.setBoolean(Boolean.TRUE);
				tld.setMap(loadtestClients.get(key));
			} else {
				tld.setBoolean(Boolean.FALSE);
			}	
		}
	}
	
	public static boolean isLoadPerformanceMode() {
		return allowed && tld.getBoolean();
	}

	public static void end (HttpServletRequest req){
		if (!allowed) return;
		if (req == null) return;
		
		if (tld.getBoolean()) {
			String c = getCookie(req);
			
			if (c != null) {
				synchronized (loadtestClients) {
					loadtestClients.put(c, tld.getMap());
				}
			}
		}	
		
		tld.remove();
	}

	private static String getCookie (HttpServletRequest req) {
		Cookie c[] = req.getCookies();
		for (int i=0; c!= null && i<c.length; i++) {
			if (c[i].getName().equals("JSESSIONID")) {
				return c[i].getValue();
			}
		}
		return null;
	}
	
	public static Map<Object,Object>  getReplayModeData () {
		return tld.getMap();
	}
	
	private static class ValueContainer {
		
		Boolean b = null;
		Map<Object,Object> m = null;
		
		ValueContainer() {
			// nothing to be done here
		}
		
	}
	
	private static class ThreadLocalData extends ThreadLocal<ValueContainer> {
		
		Boolean getBoolean() {
			ValueContainer vc = get();
			if (vc!=null && vc.b!=null) {
				return vc.b;
			}
			return Boolean.FALSE;
		}
		
		Map<Object,Object> getMap() {
			ValueContainer vc = get();
			if (vc!=null && vc.m!=null) {
				return vc.m;
			}
			return null;
		}
		
		void setBoolean(Boolean b) {
			ValueContainer vc = get();
			if (vc==null) {
				vc = new ValueContainer();
				set(vc);
			}
			vc.b = b;
		}
		
		void setMap(Map<Object,Object> m) {
			ValueContainer vc = get();
			if (vc==null) {
				vc = new ValueContainer();
				set(vc);
			}
			vc.m = m;
		}
		
	}
}
