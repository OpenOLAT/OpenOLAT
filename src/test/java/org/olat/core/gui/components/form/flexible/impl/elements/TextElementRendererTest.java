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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.render.StringOutput;

/**
 * 
 * Initial date: 12.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TextElementRendererTest {
	
	private static String AUTOCOMPLETE = "autocomplete";
	private static String AUTOCOMPLETE_NONE = "none";
	private static boolean ENABLED = true;
	private static boolean DISABLED = false;

	private TextElementComponent textElementComponentMock;
	private TextElementImpl textElementImplMock;
	private Form formMock;
	private StringOutput stringOutput;
	private TextElementRenderer sut;
	
	@Before
	public void setup() {
		formMock = mock(Form.class);
		textElementComponentMock = mock(TextElementComponent.class);
		textElementImplMock = mock(TextElementImpl.class);
		stringOutput = new StringOutput();
		sut = new TextElementRenderer();
	}

	@Test
	public void renderShouldAppendAutocompleteIfSet() {
		when(textElementImplMock.getRootForm()).thenReturn(formMock);
		when(textElementImplMock.getAutocomplete()).thenReturn(AUTOCOMPLETE_NONE);
		when(textElementComponentMock.isEnabled()).thenReturn(ENABLED);
		when(textElementComponentMock.getFormItem()).thenReturn(textElementImplMock);
		
		sut.render(null, stringOutput, textElementComponentMock, null, null, null, null);

		String autocompleteHtml = new StringBuilder()
				.append(AUTOCOMPLETE)
				.append("=\"")
				.append(AUTOCOMPLETE_NONE)
				.append("\"")
				.toString();
		assertThat(stringOutput.toString()).containsIgnoringCase(autocompleteHtml);
	}
	
	@Test
	public void renderShouldNotPringAutocompleteIfNull() {
		when(textElementImplMock.getRootForm()).thenReturn(formMock);
		when(textElementImplMock.isEnabled()).thenReturn(ENABLED);
		when(textElementImplMock.getAutocomplete()).thenReturn(null);
		when(textElementComponentMock.getFormItem()).thenReturn(textElementImplMock);
		
		sut.render(null, stringOutput, textElementComponentMock, null, null, null, null);

		assertThat(stringOutput.toString()).doesNotContain(AUTOCOMPLETE);
	}
	
	@Test
	public void renderShouldNotPringAutocompleteIfNotEnabled() {
		when(textElementImplMock.getRootForm()).thenReturn(formMock);
		when(textElementImplMock.isEnabled()).thenReturn(DISABLED);
		when(textElementImplMock.getAutocomplete()).thenReturn(AUTOCOMPLETE_NONE);
		when(textElementComponentMock.getFormItem()).thenReturn(textElementImplMock);
		
		sut.render(null, stringOutput, textElementComponentMock, null, null, null, null);

		assertThat(stringOutput.toString()).doesNotContain(AUTOCOMPLETE);
	}
	
}
