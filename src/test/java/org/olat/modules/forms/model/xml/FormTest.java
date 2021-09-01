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
package org.olat.modules.forms.model.xml;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.model.ContainerSettings;


/**
 * 
 * Initial date: 1 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormTest {
	
	@Test
	public void addElement() {
		Form form = new Form();
		
		Title title = new Title();
		title.setId("title-1");
		form.addElement(title);
		
		HTMLParagraph paragraph = new HTMLParagraph();
		paragraph.setId("paragraph-2");
		form.addElement(paragraph);
		
		Spacer spacer = new Spacer();
		spacer.setId("spacer-3");
		form.addElement(spacer);
		
		List<AbstractElement> elements = form.getElements();
		Assert.assertEquals(3, elements.size());
		Assert.assertEquals(title, elements.get(0));
		Assert.assertEquals(paragraph, elements.get(1));
		Assert.assertEquals(spacer, elements.get(2));
	}
	
	@Test
	public void moveDown() {
		Form form = new Form();
		
		Title title = new Title();
		title.setId("title-4");
		form.addElement(title);
		
		HTMLParagraph paragraph = new HTMLParagraph();
		paragraph.setId("paragraph-5");
		form.addElement(paragraph);
		
		Spacer spacer = new Spacer();
		spacer.setId("spacer-6");
		form.addElement(spacer);
		
		List<AbstractElement> elements = form.getElements();
		Assert.assertEquals(3, elements.size());
		Assert.assertEquals(title, elements.get(0));
		Assert.assertEquals(paragraph, elements.get(1));
		Assert.assertEquals(spacer, elements.get(2));
		
		// move down
		form.moveDownElement(title);
		
		// check
		List<AbstractElement> movedElements = form.getElements();
		Assert.assertEquals(3, movedElements.size());
		Assert.assertEquals(paragraph, movedElements.get(0));
		Assert.assertEquals(title, movedElements.get(1));
		Assert.assertEquals(spacer, movedElements.get(2));

		// move twice
		form.moveDownElement(title);
		
		// check
		List<AbstractElement> movedTwiceElements = form.getElements();
		Assert.assertEquals(3, movedTwiceElements.size());
		Assert.assertEquals(paragraph, movedTwiceElements.get(0));
		Assert.assertEquals(spacer, movedTwiceElements.get(1));
		Assert.assertEquals(title, movedTwiceElements.get(2));
	}
	

	@Test
	public void moveUp() {
		Form form = new Form();
		
		Title title = new Title();
		title.setId("title-20");
		form.addElement(title);
		
		HTMLParagraph paragraph = new HTMLParagraph();
		paragraph.setId("paragraph-21");
		form.addElement(paragraph);
		
		Spacer spacer = new Spacer();
		spacer.setId("spacer-22");
		form.addElement(spacer);
		
		List<AbstractElement> elements = form.getElements();
		Assert.assertEquals(3, elements.size());
		Assert.assertEquals(title, elements.get(0));
		Assert.assertEquals(paragraph, elements.get(1));
		Assert.assertEquals(spacer, elements.get(2));
		
		// move up
		form.moveUpElement(spacer);
		
		// check
		List<AbstractElement> movedElements = form.getElements();
		Assert.assertEquals(3, movedElements.size());
		Assert.assertEquals(title, movedElements.get(0));
		Assert.assertEquals(spacer, movedElements.get(1));
		Assert.assertEquals(paragraph, movedElements.get(2));

		// move twice
		form.moveUpElement(spacer);
		
		// check
		List<AbstractElement> movedTwiceElements = form.getElements();
		Assert.assertEquals(3, movedTwiceElements.size());
		Assert.assertEquals(spacer, movedTwiceElements.get(0));
		Assert.assertEquals(title, movedTwiceElements.get(1));
		Assert.assertEquals(paragraph, movedTwiceElements.get(2));
	}
	
	@Test
	public void moveUpInContainer() {
		Form form = new Form();
		
		Title title = new Title();
		title.setId("title-10");
		form.addElement(title);
		
		Container container = new Container();
		container.setId("container-11");
		form.addElement(container);

		HTMLParagraph paragraph = new HTMLParagraph();
		paragraph.setId("paragraph-12");
		form.addElement(paragraph);
		
		Spacer spacer = new Spacer();
		spacer.setId("spacer-13");
		form.addElement(spacer);
		
		ContainerSettings containerSettings = container.getContainerSettings();
		containerSettings.setNumOfColumns(1);
		List<String> elementIds = new ArrayList<>();
		elementIds.add(paragraph.getId());
		elementIds.add(spacer.getId());
		containerSettings.getColumn(0).setElementIds(elementIds);
		container.setLayoutOptions(ContentEditorXStream.toXml(containerSettings));
		
		Title subTitle = new Title();
		subTitle.setId("subtitle-14");
		form.addElement(subTitle);

		List<AbstractElement> elements = form.getElements();
		Assert.assertEquals(5, elements.size());

		// move down
		form.moveUpElement(subTitle);

		// check
		List<AbstractElement> movedElements = form.getElements();
		Assert.assertEquals(5, movedElements.size());
		Assert.assertEquals(title, movedElements.get(0));
		Assert.assertEquals(subTitle, movedElements.get(1));
		Assert.assertEquals(container, movedElements.get(2));
	}
	
	

}
