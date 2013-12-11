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
package org.olat.core.gui.components.form.flexible.impl.elements;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 11.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbstractTextElementTest extends OlatTestCase {
	
	@Test @Ignore
	public void filterPrintControlCharacter() {
		TestTextElement element = new TestTextElement("test");
		element.setValue("Hello world");
		Assert.assertEquals("Dummy test", "Hello world", element.getValue());
		
		//print control
		element.setValue("Hello\u0002 world");
		Assert.assertEquals("Print \\x02 test", "Hello world", element.getValue());

		//print control
		element.setValue("Hello\u001F world");
		Assert.assertEquals("Print \\x02 like test", "Hello world", element.getValue());

		//it's a 0
		element.setValue("Hello\u0030 world");
		Assert.assertEquals("Print \\x02 test", "Hello0 world", element.getValue());
			
		//it's a u umlaut
		element.setValue("Hello\u00FC world");
		Assert.assertEquals("Umlaut test", "Hello\u00FC world", element.getValue());
					
		//it's a kanji
		element.setValue("Hello\u30b0 world");
		Assert.assertEquals("Umlaut test", "Hello\u30b0 world", element.getValue());
		
		//it's a return
		element.setValue("Hello\n world");
		Assert.assertEquals("Umlaut test", "Hello\n world", element.getValue());
		
		//it's a unicode emoticons
		element.setValue("Hello\u1F605 world");
		Assert.assertEquals("Umlaut test", "Hello\u1F605 world", element.getValue());
	}
	
	public static class TestTextElement extends AbstractTextElement {
		
		public TestTextElement(String name) {
			super(name, name, false);
		}

		@Override
		protected Component getFormItemComponent() {
			return null;
		}

		@Override
		public void evalFormRequest(UserRequest ureq) {
			//
		}
		
	}
}
