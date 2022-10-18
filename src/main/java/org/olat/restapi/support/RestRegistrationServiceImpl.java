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
package org.olat.restapi.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Description:<br>
 * Register resource classes and singletons
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestRegistrationServiceImpl implements RestRegistrationService {
	
	private static final Logger log = Tracing.createLoggerFor(RestRegistrationServiceImpl.class);

	private final Set<Object> singletons = new HashSet<>();
	private final Set<Class<?>> classes = new HashSet<>();
	
	public RestRegistrationServiceImpl() {
		//
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<>(classes);
	}
	
	//[for spring]
	public List<String> getClassnames() {
		List<String> classnames = new ArrayList<>(classes.size());
		for(Class<?> cl:classes) {
			classnames.add(cl.getName());
		}
		return classnames;
	}
	
	public void setClassnames(List<String> classnames) {
		for(String classname: classnames) {
			try {
				Class<?> cl = Class.forName(classname);
				classes.add(cl);
			} catch (ClassNotFoundException e) {
				log.error("Class not found: {}", classname, e); 
			}
		}
	}
	
	@Override
	public void addClass(Class<?> cl) {
		classes.add(cl);
	}
	
	@Override
	public void removeClass(Class<?> cl) {
		classes.remove(cl);
	}

	@Override
	public Set<Object> getSingletons() {
		return new HashSet<>(singletons);
	}
	
	//[spring]
	public List<Object> getSingletonBeans() {
		List<Object> beans = new ArrayList<>();
		beans.addAll(singletons);
		return beans;
	}
	
	public void setSingletonBeans(List<Object> beans) {
		singletons.addAll(beans);
	}

	@Override
	public void addSingleton(Object singleton) {
		singletons.add(singleton);
	}

	@Override
	public void removeSingleton(Object singleton) {
		singletons.remove(singleton);
	}
}