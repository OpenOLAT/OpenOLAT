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
package org.olat.admin.jmx;

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

import org.olat.core.CoreSpringFactory;
import org.olat.core.manager.BasicManager;
import org.springframework.jmx.support.MBeanServerFactoryBean;

/**
 * Description:<br>
 * TODO:
 * 
 * <P>
 * Initial Date:  01.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class JMXManager extends BasicManager {
	private static JMXManager INSTANCE;
	private boolean initDone = true;
	private MBeanServer mBeanServer;
	
	public static JMXManager getInstance() {
		return INSTANCE;
	}

	/**
	 * [spring]
	 */
	private JMXManager(MBeanServer mBeanServer) {
		this.mBeanServer = mBeanServer;
		INSTANCE = this;
	}
	
	boolean isActive() {
		return initDone;
	}
	
	void init() {
		initDone = true;
	}
	
	public List<String> dumpJmx(String objectName) {
		try {
			ObjectName on = new ObjectName(objectName);
			MBeanAttributeInfo[] ainfo = mBeanServer.getMBeanInfo(on).getAttributes();
			List<MBeanAttributeInfo> mbal = Arrays.asList(ainfo);
			
			Collections.sort(mbal, new Comparator<MBeanAttributeInfo>(){
				public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
					return o1.getName().compareTo(o2.getName());
				}});
			
			List<String> l = new ArrayList<String>();
			for (MBeanAttributeInfo info : mbal) {
				String name = info.getName();
				Object res = mBeanServer.getAttribute(on, name);
				l.add(name+"="+res);
			}
			return l;
		} catch (Exception e) {
			List l = new ArrayList();
			l.add("error while retrieving jmx values: "+e.getClass().getName()+":"+e.getMessage());
			//TODO: this is just version 0.1 of dumping jmx values... need a better interface
			return l;
		} 	
	}
	
	public String dumpAll() {
		try {
			StringBuilder sb = new StringBuilder();
			MBeanServer server = (MBeanServer) CoreSpringFactory.getBean(MBeanServerFactoryBean.class);
			Set<ObjectInstance> mbeansset = server.queryMBeans(null, null);
			List<ObjectInstance> mbeans = new ArrayList<ObjectInstance>(mbeansset);
			Collections.sort(mbeans, new Comparator<ObjectInstance>(){
				public int compare(ObjectInstance o1, ObjectInstance o2) {
					return o1.getObjectName().getCanonicalName().compareTo(o2.getObjectName().getCanonicalName());
				}});
			
			
			for (ObjectInstance instance : mbeans) {
				ObjectName on = instance.getObjectName();
				MBeanAttributeInfo[] ainfo = server.getMBeanInfo(on).getAttributes();
				List<MBeanAttributeInfo> mbal = Arrays.asList(ainfo);
				Collections.sort(mbal, new Comparator<MBeanAttributeInfo>(){
					public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
						return o1.getName().compareTo(o2.getName());
					}});
				String oname = on.getCanonicalName();
				// dump all attributes with their values (simply toString()'ed)
				
				for (MBeanAttributeInfo info : mbal) {
					String name = info.getName();
					try {
						Object res = server.getAttribute(on, name);
						sb.append("<br />"+oname+"-> "+name+"="+res);
					} catch (Exception e) {
						sb.append("<br />ERROR: for attribute '"+name+"', exception:"+e+", message:"+e.getMessage());
					}
				}
			}
			return sb.toString();
		} catch (Exception e) {
			return "error:"+e.getMessage();
		} 	
		
	}

}
