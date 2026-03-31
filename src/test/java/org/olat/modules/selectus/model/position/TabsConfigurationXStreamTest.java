package org.olat.modules.selectus.model.position;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.util.FileUtils;

/**
 * 
 * Initial date: 31 mars 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TabsConfigurationXStreamTest {
	
	@Test
	@Ignore
	public void configurationOpenOlat() throws Exception {
		URL input = TabsConfigurationXStreamTest.class.getResource("configuration_openolat.xml");
		File inputFile = new File(input.getFile());
		String xml = FileUtils.load(inputFile, "UTF-8");
		
		TabsConfiguration config = TabsConfigurationXStream.fromXml(xml);
		Assert.assertNotNull(config);
	}
	
	@Test
	public void writeReadConfigurationOpenOlat() throws Exception {
		TabsConfiguration config = new TabsConfiguration();
		TabConfiguration tab = new TabConfiguration();
		tab.setHelp("Help serialize");
		config.setConfiguration(TabsConfiguration.Tab.confirmation, tab);

		String xml = TabsConfigurationXStream.toXml(config);
		Assert.assertNotNull(xml);
		
		TabsConfiguration serializedConfig = TabsConfigurationXStream.fromXml(xml);
		Assert.assertNotNull(serializedConfig);
		TabConfiguration serializedTab = serializedConfig.getConfiguration(TabsConfiguration.Tab.confirmation);
		Assert.assertNotNull(serializedTab);
		Assert.assertEquals("Help serialize", serializedTab.getHelp());
	}

}
