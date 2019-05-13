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
package org.olat.admin.sysinfo.manager;

import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.logging.log4j.Logger;
import org.olat.admin.sysinfo.model.DatabaseConnectionVO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.jmx.JMXManager;
import org.olat.core.logging.Tracing;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DatabaseStatsManager {
	
	private static final Logger log = Tracing.createLoggerFor(DatabaseStatsManager.class);
	
	
	public DatabaseConnectionVO getConnectionInfos() {
		DatabaseConnectionVO vo = new DatabaseConnectionVO();
		vo.setActiveConnectionCount(0);
		vo.setCurrentConnectionCount(0);
		try {
			JMXManager jmxManager = CoreSpringFactory.getImpl(JMXManager.class);
			MBeanServer mBeanServer = jmxManager.getMBeanServer();
			boolean found = searchHikariDataSources(mBeanServer, vo) || searchTomcatDataSources(mBeanServer, vo) || searchC3P0DataSources(mBeanServer, vo);
			if(log.isDebugEnabled()) {
				log.debug("MBean for datasource found: " + found);
			}	
		} catch (Exception e) {
			log.error("", e);
		}
		return vo;
	}
	
	private boolean searchC3P0DataSources(MBeanServer mBeanServer, DatabaseConnectionVO vo) {
		try {
			ObjectName poolName = new ObjectName("com.mchange.v2.c3p0:type=*,*");
			Set<ObjectName> names = mBeanServer.queryNames(poolName, null);
			if(names.size() > 0) {
				int activeConnectionCount = 0;
				int currentConnectionCount = 0;

				for(ObjectName name:names) {
					String cName = name.getCanonicalName();
					if(cName.startsWith("com.mchange.v2.c3p0:") && cName.indexOf("type=PooledDataSource") > 0) {
						MBeanInfo info = mBeanServer.getMBeanInfo(name);
						MBeanAttributeInfo[] attrs = info.getAttributes();

						for(MBeanAttributeInfo attr:attrs) {
							String attrName = attr.getName();
							if("numBusyConnectionsAllUsers".equals(attrName)) {
								Number obj = (Number)mBeanServer.getAttribute(name, "numBusyConnectionsAllUsers");
								activeConnectionCount += obj.intValue();
							} else if("numConnectionsAllUsers".equals(attrName)) {
								Number obj = (Number)mBeanServer.getAttribute(name, "numConnectionsAllUsers");
								currentConnectionCount += obj.intValue();
							}
						}
					}
				}

				vo.setActiveConnectionCount(activeConnectionCount);
				vo.setCurrentConnectionCount(currentConnectionCount);
				return true;
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}
	
	private boolean searchHikariDataSources(MBeanServer mBeanServer, DatabaseConnectionVO vo) {
		try {
			ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=*");
			Set<ObjectName> names = mBeanServer.queryNames(poolName, null);
			if(names.size() > 0) {
				int activeConnectionCount = 0;
				int currentConnectionCount = 0;

				for(ObjectName name:names) {
					String cName = name.getCanonicalName();
					if(cName.startsWith("com.zaxxer.hikari:") && cName.indexOf("type=Pool") > 0 && cName.indexOf("type=PoolConfig") <= 0) {
						MBeanInfo info = mBeanServer.getMBeanInfo(name);
						MBeanAttributeInfo[] attrs = info.getAttributes();
						for(MBeanAttributeInfo attr:attrs) {
							String attrName = attr.getName();
							if("ActiveConnections".equals(attrName)) {
								Number obj = (Number)mBeanServer.getAttribute(name, "ActiveConnections");
								activeConnectionCount += obj.intValue();
							} else if("TotalConnections".equals(attrName)) {
								Number obj = (Number)mBeanServer.getAttribute(name, "TotalConnections");
								currentConnectionCount += obj.intValue();
							}
						}
					}
				}

				vo.setActiveConnectionCount(activeConnectionCount);
				vo.setCurrentConnectionCount(currentConnectionCount);
				return true;
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}
	
	private boolean searchTomcatDataSources(MBeanServer mBeanServer, DatabaseConnectionVO vo) {
		try {
			ObjectName poolName = new ObjectName("Catalina:type=DataSource,*");
			Set<ObjectName> names = mBeanServer.queryNames(poolName, null);
			if(names.size() > 0) {
				int activeConnectionCount = 0;
				int idleConnectionCount = 0;
				
				for(ObjectName name:names) {
					MBeanInfo info = mBeanServer.getMBeanInfo(name);
					MBeanAttributeInfo[] attrs = info.getAttributes();
					for(MBeanAttributeInfo attr:attrs) {
						String attrName = attr.getName();
						if("numActive".equals(attrName)) {
							Number obj = (Number)mBeanServer.getAttribute(name, "numActive");
							activeConnectionCount += obj.intValue();
						} else if("numIdle".equals(attrName)) {
							Number obj = (Number)mBeanServer.getAttribute(name, "numIdle");
							idleConnectionCount += obj.intValue();
						}
					}
				}

				vo.setActiveConnectionCount(activeConnectionCount);
				vo.setCurrentConnectionCount(activeConnectionCount + idleConnectionCount);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}
}
