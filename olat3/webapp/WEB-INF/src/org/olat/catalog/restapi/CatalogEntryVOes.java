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
package org.olat.catalog.restapi;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * This is an helper class for the WADL documentation. Don't use it
 * in the REST API!!!
 * 
 * <P>
 * Initial Date:  5 may 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "catalogEntryVOes")
public class CatalogEntryVOes {
	
	@XmlElement(name="catalogEntryVO")
	private List<CatalogEntryVO> entries = new ArrayList<CatalogEntryVO>();
	
	public CatalogEntryVOes() {
		//make JAXB happy
	}

	public List<CatalogEntryVO> getEntries() {
		return entries;
	}

	public void setEntries(List<CatalogEntryVO> entries) {
		this.entries = entries;
	}
}
