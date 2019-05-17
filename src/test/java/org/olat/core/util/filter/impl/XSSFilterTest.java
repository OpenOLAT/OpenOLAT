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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter.Variant;

/**
 * Description:<br>
 * This test case tests special cases of the cross site scripting filter
 * 
 * <P>
 * Initial Date:  14.07.2009 <br>
 * @author gnaegi
 * @author Roman Haag, roman.haag@frentix.com
 */
public class XSSFilterTest {


	private void t(String input, String result, Filter f) {
		String filterRes = f.filter(input);
		if (filterRes == result || filterRes.equals(result)){
			System.out.println("------------------------------------------------");
		} else {
			System.out.println("---------------- E R R O R ---------------------");
		}
		System.out.println("           Expected: " + result);
		System.out.println("************************************************\n\n");
		Assert.assertEquals(result, filterRes);
	}

	@Test
	public void test_edusharing() {
		String html = "<img src=\"/olat/edusharing/preview?objectUrl=ccrep://OpenOLAT/d5130470-14b4-4ad4-88b7-dfb3ebe943da&version=1.0\" data-es_identifier=\"2083dbe64f00b07232b11608ec0842fc\" data-es_objecturl=\"ccrep://OpenOLAT/d5130470-14b4-4ad4-88b7-dfb3ebe943da\" data-es_version=\"1.0\" data-es_version_current=\"1.0\" data-es_mediatype='i23' data-es_mimetype=\"image/png\" data-es_width=\"1000\" data-es_height=\"446\" data-es_first_edit=\"false\" class=\"edusharing\" alt=\"Bildschirmfoto 2018-11-07 um 16.09.49.png\" title=\"Bildschirmfoto 2018-11-07 um 16.09.49.png\" width=\"1000\" height=\"446\">";
		
		// t() did not work, because antisamy changed the order of the attributes
		Filter vFilter = new OWASPAntiSamyXSSFilter(-1, true);
		String filtered = vFilter.filter(html);
		assertThat(filtered).contains("src");
		assertThat(filtered).contains("width");
		assertThat(filtered).contains("height");
		assertThat(filtered).contains("es_identifier");
		assertThat(filtered).contains("es_objecturl");
		assertThat(filtered).contains("es_version");
		assertThat(filtered).contains("es_mimetype");
		assertThat(filtered).contains("es_mediatype");
		assertThat(filtered).contains("es_width");
		assertThat(filtered).contains("es_height");
	}
	
	@Test
	public void test_rawText() {
		OWASPAntiSamyXSSFilter intlFilter = new OWASPAntiSamyXSSFilter(-1, false, Variant.tinyMce, true);
		t("Stéphane Rossé", "Stéphane Rossé", intlFilter);
	}
	
	@Test
	public void test_rawTextAttaqu() {
		OWASPAntiSamyXSSFilter intlFilter = new OWASPAntiSamyXSSFilter(-1, false, Variant.tinyMce, true);
		t("&lt;script&gt;alert('hello');&lt;//script&gt;", "&lt;script&gt;alert('hello');&lt;//script&gt;", intlFilter);
	}

}
