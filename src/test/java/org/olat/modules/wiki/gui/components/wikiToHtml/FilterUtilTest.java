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
package org.olat.modules.wiki.gui.components.wikiToHtml;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 25.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilterUtilTest {

	/**
	 * It test the current behavior
	 */
	@Test
	public void testNormalizeWikiLink() {
		//newsletter -> Newsletter
		String newsLinkLowerCase = FilterUtil.normalizeWikiLink("newsletter");
		Assert.assertEquals("Newsletter", newsLinkLowerCase);
		
		//Newsletter -> Newsletter
		String newsLinkUpperCase = FilterUtil.normalizeWikiLink("Newsletter");
		Assert.assertEquals("Newsletter", newsLinkUpperCase);
		
		//newsletter/today -> newsletter/Today
		String slashLowerCase = FilterUtil.normalizeWikiLink("newsletter/today");
		Assert.assertEquals("newsletter/Today", slashLowerCase);
		
		//newsletter/
		String endSlashLowerCase = FilterUtil.normalizeWikiLink("newsletter/");
		Assert.assertEquals("newsletter/", endSlashLowerCase);
	}

}
