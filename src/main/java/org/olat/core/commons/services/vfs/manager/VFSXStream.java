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
package org.olat.core.commons.services.vfs.manager;

import java.io.File;
import java.io.InputStream;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.version.RevisionFileImpl;
import org.olat.core.util.vfs.version.VersionsFileImpl;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 13 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSXStream {
	
	private static XStream mystream;
	static {
		mystream = XStreamHelper.createXStreamInstance();
		Class<?>[] types = new Class[] {
				VersionsFileImpl.class, RevisionFileImpl.class, VFSRevision.class,
				VFSMetadata.class, VFSMetadataImpl.class, Identity.class, IdentityImpl.class,
				LicenseType.class, LicenseTypeImpl.class
			};
		mystream.addPermission(new ExplicitTypePermission(types));
		
		mystream.alias("versions", VersionsFileImpl.class);
		mystream.alias("revision", RevisionFileImpl.class);
		mystream.omitField(VersionsFileImpl.class, "currentVersion");
		mystream.omitField(VersionsFileImpl.class, "versionFile");
		mystream.omitField(RevisionFileImpl.class, "current");
		mystream.omitField(RevisionFileImpl.class, "container");
		mystream.omitField(RevisionFileImpl.class, "file");
		mystream.aliasAttribute(RevisionFileImpl.class, "fileInitializedBy", "author");
		mystream.omitField(VFSMetadataImpl.class, "originFile");
		mystream.omitField(VFSMetadataImpl.class, "metaFile");
		mystream.omitField(VFSMetadataImpl.class, "lockedByIdentKey");
		mystream.aliasAttribute(VFSMetadataImpl.class, "cannotGenerateThumbnails", "cannotGenerateThumbnail");
		mystream.aliasAttribute(VFSMetadataImpl.class, "fileInitializedBy", "authorIdentKey");
		mystream.aliasAttribute(VFSMetadataImpl.class, "fileInitializedBy", "author");
		mystream.aliasAttribute(VFSMetadataImpl.class, "licenseType", "licenseTypeKey");
		mystream.alias("metadata", VFSMetadataImpl.class);
		mystream.omitField(VFSMetadataImpl.class, "thumbnail");
		mystream.omitField(VFSMetadataImpl.class, "thumbnails");

		mystream.registerLocalConverter(VFSMetadataImpl.class, "licenseType", new LicenseTypeConverter());
		mystream.registerLocalConverter(VFSMetadataImpl.class, "fileInitializedBy", new IdentityConverter());
		mystream.registerLocalConverter(RevisionFileImpl.class, "fileInitializedBy", new IdentityConverter());	
	}
	
	public static final Object read(InputStream in) {
		return XStreamHelper.readObject(mystream, in);
	}
	
	public static final Object read(File file) {
		return XStreamHelper.readObject(mystream, file);
	}
	
	public static final Object read(VFSLeaf leaf) {
		return XStreamHelper.readObject(mystream, leaf);
	}
	
	public static final void write(VFSLeaf leaf, VersionsFileImpl versions) {
		XStreamHelper.writeObject(mystream, leaf, versions);
	}
	
	private static class LicenseTypeConverter implements SingleValueConverter {

		@Override
		public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
			return type.equals(LicenseType.class) || type.equals(LicenseTypeImpl.class);
		}

		@Override
		public String toString(Object obj) {
			if(obj instanceof LicenseType) {
				Long key = ((LicenseType)obj).getKey();
				return key == null ? null : key.toString();
			}
			return null;
		}

		@Override
		public Object fromString(String str) {
			return CoreSpringFactory.getImpl(LicenseService.class).loadLicenseTypeByKey(str);
		}	
	}
	
	private static class IdentityConverter implements SingleValueConverter {

		@Override
		public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
			return type.equals(Identity.class) || type.equals(IdentityImpl.class);
		}

		@Override
		public String toString(Object obj) {
			if(obj instanceof Identity) {
				Long key = ((Identity)obj).getKey();
				return key == null ? null : key.toString();
			}
			return null;
		}

		@Override
		public Object fromString(String str) {
			Identity identity = null;
			if(StringHelper.containsNonWhitespace(str) && !"-".equals(str)) {
				if(StringHelper.isLong(str)) {
					Long identityKey = Long.valueOf(str);
					identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(identityKey);
				}
				if(identity == null && !"-".equals(str)) {
					identity = CoreSpringFactory.getImpl(BaseSecurity.class).findIdentityByName(str);
				}
			}
			return identity;
		}	
	}
}
