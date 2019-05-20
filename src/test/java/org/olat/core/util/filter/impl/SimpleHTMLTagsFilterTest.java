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
package org.olat.core.util.filter.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.util.filter.Filter;

/**
 * Description:<br>
 * This test case tests the simle html tags filter
 * 
 * <P>
 * Initial Date:  14.07.2009 <br>
 * @author gnaegi
 */
public class SimpleHTMLTagsFilterTest {

	private Filter filter;

	@Before
	public void setup() {
		filter = new SimpleHTMLTagsFilter();
	}

	@After
	public void tearDown() {
		filter = null;
	}

	private void t(String input, String result) {
		Assert.assertEquals(result, filter.filter(input));
	}
	
	@Test
	public void testPlainText() {
		t(null, null);
		t("", "");
		t("hello world", "hello world");
		t("hello \n \t \r world", "hello \n \t \n world");
		t("1+2=3", "1+2=3");
	}
	
	@Test
	public void testSimpleTags() {
		t("<b>hello</b> world", "hello world");
		t("<b><i>hello</i></b> world", "hello world");
		t("<b>h<i>ell</i>o</b> world", "hello world");
		t("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">", "");
		t("<a ref='#bla' \n title='gugus'>hello</b> world", "hello world");
	}
	
	@Test
	public void testBRAndPReplacement() {
		t("<br>", " ");
		t("<p>", " ");
		t("<br >", " ");
		t("<p >", " ");
		t("<br/>", " ");
		t("<p/>", " ");
		t("<br />", " ");
		t("<p />", " ");
	}

	@Test
	public void testTagsWithAttributes() {
		t("<font color='red'>hello</font> world", "hello world");
		t("<font color=\"red\">hello</font> world", "hello world");
		t("<a href=\"#top\" color='=>top'>go up</a>", "go up");
		t("<a href=\"#top\" title=\"a > b < c <=x\">blu blu</a>", "blu blu");
		t("<a href=\"#top\" title=\"a > b < c <=x\">blu <font color='red'>hello</font> world blu</a>", "blu hello world blu");
		t("<a href=\"#top\" title=\"a > b < c <=x\">blu\u00a0<font color='red'>hello</font>\u00a0world\u00a0blu</a>", "blu hello world blu");
	}

	// Boundary test: this filter does NOT decode HTML entities. Use the
	// HtmlFilter if you need this feature!
	@Test
	public void testTagsWithEntities() {
		t("Gn&auml;gi", "Gn&auml;gi");
		t("This is &copy; by frentix", "This is &copy; by frentix");
	}

}
