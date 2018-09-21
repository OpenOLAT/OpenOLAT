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
package org.olat.modules.ceditor;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.ceditor.model.ImageHorizontalAlignment;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.ImageSize;
import org.olat.modules.ceditor.model.ImageTitlePosition;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.model.TextSettings;

/**
 * 
 * Initial date: 5 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorXStreamTest {
	
	@Test
	public void imageSettingsToXmlAndFrom() {
		ImageSettings settings = new ImageSettings();
		settings.setAlignment(ImageHorizontalAlignment.middle);
		settings.setSize(ImageSize.fill);
		settings.setCaption("Caption");
		settings.setDescription("Description");
		settings.setShowDescription(true);
		settings.setShowSource(false);
		settings.setStyle("o_image");
		settings.setTitle("A title");
		settings.setTitlePosition(ImageTitlePosition.above);
		settings.setTitleStyle("o_image_title_style");
		// serialize
		String xml = ContentEditorXStream.toXml(settings);
		// read
		ImageSettings deserializedSettings = ContentEditorXStream.fromXml(xml, ImageSettings.class);
		//check
		Assert.assertNotNull(deserializedSettings);
		Assert.assertEquals(ImageHorizontalAlignment.middle, deserializedSettings.getAlignment());
		Assert.assertEquals(ImageSize.fill, deserializedSettings.getSize());
		Assert.assertEquals("Caption", deserializedSettings.getCaption());
		Assert.assertEquals("Description", settings.getDescription());
		Assert.assertEquals(true, deserializedSettings.isShowDescription());
		Assert.assertEquals(false, deserializedSettings.isShowSource());
		Assert.assertEquals("o_image", deserializedSettings.getStyle());
		Assert.assertEquals("A title", deserializedSettings.getTitle());
		Assert.assertEquals(ImageTitlePosition.above, deserializedSettings.getTitlePosition());
		Assert.assertEquals("o_image_title_style", deserializedSettings.getTitleStyle());
	}
	
	@Test
	public void textSettingsToXmlAndFrom() {
		TextSettings settings = new TextSettings();
		settings.setNumOfColumns(3);
		// serialize
		String xml = ContentEditorXStream.toXml(settings);
		// read
		TextSettings deserializedSettings = ContentEditorXStream.fromXml(xml, TextSettings.class);
		//check
		Assert.assertNotNull(deserializedSettings);
		Assert.assertEquals(3, deserializedSettings.getNumOfColumns());
	}
	
	@Test
	public void tableContentToXmlAndFrom() {
		TableContent table = new TableContent();
		table.addContent(0, 0,"Hello world");

		// serialize
		String xml = ContentEditorXStream.toXml(table);
		// read
		TableContent deserializedTable = ContentEditorXStream.fromXml(xml, TableContent.class);
		//check
		Assert.assertNotNull(deserializedTable);
		Assert.assertEquals("Hello world", deserializedTable.getRows().get(0).getColumns().get(0).getContent());
	}
	
	@Test
	public void tableContentWithHtmlToXmlAndFrom() {
		TableContent table = new TableContent();
		table.addContent(0, 0, "<p><strong>Hello</strong> world</p>");

		// serialize
		String xml = ContentEditorXStream.toXml(table);
		// read
		TableContent deserializedTable = ContentEditorXStream.fromXml(xml, TableContent.class);
		//check
		Assert.assertNotNull(deserializedTable);
		Assert.assertEquals("<p><strong>Hello</strong> world</p>", deserializedTable.getRows().get(0).getColumns().get(0).getContent());
	}
	
	@Test
	public void tableSettingsToXmlAndFrom() {
		TableSettings settings = new TableSettings();
		settings.setBordered(true);
		settings.setColumnHeaders(true);
		settings.setRowHeaders(true);
		settings.setStriped(true);
		settings.setTableStyle("o_oo_my_style");
		
		// serialize
		String xml = ContentEditorXStream.toXml(settings);
		// read
		TableSettings deserializedSettings = ContentEditorXStream.fromXml(xml, TableSettings.class);
		//check
		Assert.assertNotNull(deserializedSettings);
		Assert.assertTrue(deserializedSettings.isBordered());
		Assert.assertTrue(deserializedSettings.isColumnHeaders());
		Assert.assertTrue(deserializedSettings.isRowHeaders());
		Assert.assertTrue(deserializedSettings.isStriped());
		Assert.assertEquals("o_oo_my_style", deserializedSettings.getTableStyle());
	}

}
