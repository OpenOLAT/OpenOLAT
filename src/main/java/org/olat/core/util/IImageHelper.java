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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.util;

import java.io.InputStream;

import org.olat.core.util.ImageHelper.Size;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * Description:<br>
 * yet a dummy interface for ImageHelper to allow later replacement (i.e.
 * ImageMagick) by config
 * 
 * <P>
 * Initial Date: 04.02.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public interface IImageHelper {

	Size scaleImage(InputStream image, VFSLeaf scaledImage, int maxWidth,
			int maxHeight);

}
