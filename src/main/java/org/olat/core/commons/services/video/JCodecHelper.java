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
package org.olat.core.commons.services.video;

import java.awt.image.BufferedImage;

import org.apache.logging.log4j.Logger;
import org.jcodec.api.transcode.PixelStore;
import org.jcodec.api.transcode.PixelStore.LoanerPicture;
import org.jcodec.api.transcode.PixelStoreImpl;
import org.jcodec.api.transcode.filters.ScaleFilter;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.olat.core.commons.services.image.Size;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 7 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JCodecHelper {
	
	private static final Logger log = Tracing.createLoggerFor(JCodecHelper.class);
	
	private JCodecHelper() {
		//
	}
	
	public static BufferedImage scale(Size movieSize, Picture picture, BufferedImage bufImg) {
		if(movieSize != null && picture != null && bufImg != null
				&& (bufImg.getWidth() != movieSize.getWidth() || bufImg.getHeight() != movieSize.getHeight())) {
			try {
				ScaleFilter filter = new ScaleFilter(movieSize.getWidth(), movieSize.getHeight());
				PixelStore store = new PixelStoreImpl();
				LoanerPicture lPicture = filter.filter(picture, store);
				bufImg = AWTUtil.toBufferedImage(lPicture.getPicture());
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return bufImg;
	}

}
