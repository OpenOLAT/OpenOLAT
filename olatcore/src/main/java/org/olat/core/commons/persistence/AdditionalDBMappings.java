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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.commons.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Description:<br>
 * Wrapper class for additional db mappings which will be loaded on startup.
 * Provide the list of annotated classes or hbm.xml files via spring config.
 * 
 * <P>
 * Initial Date:  23 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AdditionalDBMappings {
	
	private List<Class<?>> annotatedClasses;
	private List<String> xmlFiles;
	
	/**
	 * [spring only]
	 */
	private AdditionalDBMappings() {/**/}
	
	public List<String> getXmlFiles() {
		if(xmlFiles == null) {
			xmlFiles = new ArrayList<String>();
		}
		return xmlFiles;
	}
	
	/**
	 * [spring]
	 * @param xmlFiles
	 */
	public void setXmlFiles(List<String> xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

	public List<Class<?>> getAnnotatedClasses() {
		if(annotatedClasses == null) {
			annotatedClasses = new ArrayList<Class<?>>();
		}
		return annotatedClasses;
	}

	/**
	 * [spring]
	 * @param classes
	 */
	public void setAnnotatedClasses(List<Class<?>> classes) {
		this.annotatedClasses = classes;
	}
}
