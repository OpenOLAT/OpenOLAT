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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.commons.services.image.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.image.Size;


/**
 * 
 * Initial date: 30 mars 2021<br>
 * @author tomgross
 *
 */
public class ImageHelperImplTest {

	@Test
	public void scaleImage() throws URISyntaxException, IOException {
		int maxHeight = 120;
		int maxWidth = 180;

	  	URL sourceURL = ImageHelperImpl.class.getResource("testimg.jpg");
	  	File sourceImg = new File(sourceURL.toURI());
		File scaledImg = File.createTempFile("image", "_img.png");

		ImageHelperImpl imageHelper = new ImageHelperImpl();
		Size size = imageHelper.scaleImage(sourceImg, "jpg", scaledImg, maxWidth, maxHeight, true);
		assertThat(size.getWidth()).isEqualTo(180);
		assertThat(size.getHeight()).isEqualTo(120);
		assertThat(size.getYOffset()).isZero();
		assertThat(size.getXOffset()).isEqualTo(90);
		
		Assert.assertTrue(scaledImg.exists());
		Assert.assertTrue(scaledImg.length() > 0l);
		
		Size realScaledSize = imageHelper.getSize(scaledImg, "png");
		Assert.assertEquals(maxHeight, realScaledSize.getHeight());
		Assert.assertEquals(maxWidth, realScaledSize.getWidth());

		scaledImg.delete();
	}
}