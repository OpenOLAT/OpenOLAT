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
package org.olat.core.commons.services.jmx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Manage the JMX beans
 * 
 * <P>
 * Initial Date:  01.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class JMXManager {
	
	private static final Logger log = Tracing.createLoggerFor(JMXManager.class);

	private MBeanServer mBeanServer;

	/**
	 * [spring]
	 */
	private JMXManager(MBeanServer mBeanServer) {
		this.mBeanServer = mBeanServer;
	}
	
	public boolean isActive() {
		return mBeanServer != null;
	}
	
	public MBeanServer getMBeanServer() {
		return mBeanServer;
	}
	
	public List<String> dumpJmx(String objectName) {
		try {
			ObjectName on = new ObjectName(objectName);
			MBeanAttributeInfo[] ainfo = mBeanServer.getMBeanInfo(on).getAttributes();
			List<MBeanAttributeInfo> mbal = Arrays.asList(ainfo);
			
			Collections.sort(mbal, new Comparator<MBeanAttributeInfo>(){
				@Override
				public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
					return o1.getName().compareTo(o2.getName());
				}});
			
			List<String> l = new ArrayList<>();
			for (MBeanAttributeInfo info : mbal) {
				String name = info.getName();
				Object res = mBeanServer.getAttribute(on, name);
				l.add(name+"="+res);
			}
			return l;
		} catch (Exception e) {
			List<String> l = new ArrayList<>();
			l.add("error while retrieving jmx values: "+e.getClass().getName()+":"+e.getMessage());
			return l;
		} 	
	}
	
	public String dumpAll() {
		try {
			StringBuilder sb = new StringBuilder();
			Set<ObjectInstance> mbeansset = mBeanServer.queryMBeans(null, null);
			List<ObjectInstance> mbeans = new ArrayList<>(mbeansset);
			Collections.sort(mbeans, new Comparator<ObjectInstance>(){
				@Override
				public int compare(ObjectInstance o1, ObjectInstance o2) {
					return o1.getObjectName().getCanonicalName().compareTo(o2.getObjectName().getCanonicalName());
				}});
			
			
			for (ObjectInstance instance : mbeans) {
				ObjectName on = instance.getObjectName();
				MBeanAttributeInfo[] ainfo = mBeanServer.getMBeanInfo(on).getAttributes();
				List<MBeanAttributeInfo> mbal = Arrays.asList(ainfo);
				Collections.sort(mbal, new Comparator<MBeanAttributeInfo>(){
					@Override
					public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
						return o1.getName().compareTo(o2.getName());
					}});
				String oname = on.getCanonicalName();
				// dump all attributes with their values (simply toString()'ed)
				
				for (MBeanAttributeInfo info : mbal) {
					String name = info.getName();
					try {
						Object res = mBeanServer.getAttribute(on, name);
						sb.append("<br />"+oname+"-> "+name+"="+res);
					} catch (Exception e) {
						sb.append("<br />ERROR: for attribute '"+name+"', exception:"+e+", message:"+e.getMessage());
					}
				}
			}
			return sb.toString();
		} catch (Exception e) {
			log.error("", e);
			return "error:"+e.getMessage();
		}
	}
}