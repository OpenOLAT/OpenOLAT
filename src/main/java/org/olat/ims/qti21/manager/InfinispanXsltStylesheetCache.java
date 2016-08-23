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
package org.olat.ims.qti21.manager;

import javax.xml.transform.Templates;

import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;

/**
 * Bind the XSLT Templates cache for jacomax on our cache infrastructure.
 * 
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InfinispanXsltStylesheetCache implements XsltStylesheetCache {
	
	private CacheWrapper<Object,Templates> cache;

	@SuppressWarnings("static-access")
	@Autowired
	public InfinispanXsltStylesheetCache(CoordinatorManager coordinatorManager) {
		cache = coordinatorManager.getInstance().getCoordinator().getCacher().getCache("QTIWorks", "xsltStylesheets");
	}

	@Override
	public Templates getStylesheet(String key) {
		return cache.get(key);
	}

	@Override
	public void putStylesheet(String key, Templates stylesheet) {
		cache.put(key, stylesheet);
	}
}
