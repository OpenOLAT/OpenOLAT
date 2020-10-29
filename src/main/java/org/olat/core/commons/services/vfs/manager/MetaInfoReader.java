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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Initial date: 12 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetaInfoReader {

	private static final Logger log = Tracing.createLoggerFor(MetaInfoReader.class);
  
	private static SAXParser saxParser;
	static {
		try {
			saxParser = XMLFactories.newSAXParser();
		} catch(Exception ex) {
			log.error("", ex);
		}
	}
	
	private final VFSMetadataImpl meta;
	private final List<Thumbnail> thumbnails = new ArrayList<>();
	
	private LicenseService licenseService;
	private final BaseSecurity securityManager;
	
	public MetaInfoReader(VFSMetadataImpl meta, LicenseService licenseService, BaseSecurity securityManager) {
		this.meta = meta;
		this.licenseService = licenseService;
		this.securityManager = securityManager;
	}
	
	public VFSMetadataImpl getMetadata() {
		return meta;
	}
	
	public List<Thumbnail> getThumbnails() {
		return thumbnails;
	}
	
	public void fromBinaries(InputStream in) {
		try(InputStream bin = new BufferedInputStream(in)) {
			synchronized(saxParser) {
				saxParser.parse(bin, new MetaHandler(null));
			}
			
			// check if the lock data are complete, if not, remove the partial lock data
			if(meta != null && meta.isLocked() && meta.getLockedBy() == null) {
				meta.setLocked(false);
				meta.setLockedDate(null);
			}
		} catch(Exception ex) {
			log.error("Error while parsing binaries", ex);
		}
	}

	public boolean parseSAX(File fMeta) {
		if (fMeta == null || !fMeta.exists() || fMeta.isDirectory()) return false;

		try(InputStream in = new FileInputStream(fMeta);
				BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE)) {
			synchronized(saxParser) {
				saxParser.parse(bis, new MetaHandler(fMeta));
			}
			
			if(meta != null && meta.isLocked() && meta.getLockedBy() == null) {
				meta.setLocked(false);
				meta.setLockedDate(null);
			}
		} catch (SAXParseException ex) {
			if(!parseSAXFiltered(fMeta)) {
				log.warn("SAX Parser error while parsing {}", fMeta, ex);
			}
		} catch(Exception ex) {
			log.error("Error while parsing {}", fMeta, ex);
		}
		return true;
	}
	
	/**
	 * Try to rescue xml files with invalid characters
	 * @param fMeta
	 * @return true if rescue is successful
	 */
	private boolean parseSAXFiltered(File fMeta) {
		String original = FileUtils.load(fMeta, "UTF-8");
		if(original == null) return false;
		
		String filtered = FilterFactory.getXMLValidCharacterFilter().filter(original);
		if(!original.equals(filtered)) {
			try {
				synchronized(saxParser) {
					InputSource in = new InputSource(new StringReader(filtered));
					saxParser.parse(in, new MetaHandler(fMeta));
				}
				return true;
			} catch (Exception e) {
				//only a fallback, fail silently
			}
		}
		return false;
	}
	
	/**
	 * Writes the meta data to file. If no changes have been made,
	 * does not write anything.
	 * @return True upon success.
	 */
	public static final byte[] toBinaries(VFSMetadata metadata) {
		if (metadata == null) return new byte[0];
		
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();
				OutputStreamWriter sw = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
			
			sw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sw.write("<meta");
			if(StringHelper.containsNonWhitespace(metadata.getUuid())) {
				sw.write(" uuid=\"" + metadata.getUuid() + "\"");
			}
			sw.write(">");		
			
			Long authorIdentKey = metadata.getAuthor() == null ? null : metadata.getAuthor().getKey();
			Long lockedByIdentKey = metadata.getLockedBy() == null ? null : metadata.getLockedBy().getKey();
			Date lockedDate = metadata.getLockedDate();
			Long licenseTypeKey = metadata.getLicenseType() == null ? null : metadata.getLicenseType().getKey();
			String[] publicationDates = metadata.getPublicationDate();
			String pubYear = publicationDates[0];
			String pubMonth = publicationDates[1];
			
			sw.write("<author><![CDATA[" + (authorIdentKey == null ? "" : authorIdentKey.toString()) + "]]></author>");		
			sw.write("<lock locked=\"" + metadata.isLocked() + "\"" + (lockedDate == null ? "" : " date=\"" + lockedDate.getTime() + "\"")	+ "><![CDATA[" + (lockedByIdentKey == null ? "" : lockedByIdentKey) + "]]></lock>");
			sw.write("<comment><![CDATA[" + filterForCData(metadata.getComment()) + "]]></comment>");
			sw.write("<title><![CDATA[" + filterForCData(metadata.getTitle()) + "]]></title>");
			sw.write("<publisher><![CDATA[" + filterForCData(metadata.getPublisher()) + "]]></publisher>");
			sw.write("<creator><![CDATA[" + filterForCData(metadata.getCreator()) + "]]></creator>");
			sw.write("<source><![CDATA[" + filterForCData(metadata.getSource()) + "]]></source>");
			sw.write("<city><![CDATA[" + filterForCData(metadata.getCity()) + "]]></city>");
			sw.write("<pages><![CDATA[" + filterForCData(metadata.getPages()) + "]]></pages>");
			sw.write("<language><![CDATA[" + filterForCData(metadata.getLanguage()) + "]]></language>");
			sw.write("<url><![CDATA[" + filterForCData(metadata.getUrl()) + "]]></url>");
			sw.write("<licenseTypeKey><![CDATA[" + licenseTypeKey + "]]></licenseTypeKey>");
			sw.write("<licenseTypeName><![CDATA[" + filterForCData(metadata.getLicenseTypeName()) + "]]></licenseTypeName>");
			sw.write("<licenseText><![CDATA[" + filterForCData(metadata.getLicenseText()) + "]]></licenseText>");
			sw.write("<licensor><![CDATA[" + filterForCData(metadata.getLicensor()) + "]]></licensor>");
			sw.write("<publicationDate><month><![CDATA[" + (pubMonth != null ? pubMonth.trim() : "") + "]]></month><year><![CDATA[" + (pubYear != null ? pubYear.trim() : "") + "]]></year></publicationDate>");
			sw.write("<downloadCount><![CDATA[" + metadata.getDownloadCount() + "]]></downloadCount>");
			if(metadata instanceof VFSMetadataImpl) {
				Boolean cannotGenerateThumbnail = ((VFSMetadataImpl)metadata).getCannotGenerateThumbnails();
				sw.write("<thumbnails cannotGenerateThumbnail=\"" + (cannotGenerateThumbnail == null ? "" : cannotGenerateThumbnail) + "\"></thumbnails>");
			}
			sw.write("</meta>");
			sw.flush();
			return out.toByteArray();
		} catch (Exception e) { 
			return new byte[0]; 
		}
	}
	
	private static final String filterForCData(String original) {
		if(StringHelper.containsNonWhitespace(original)) {
			return FilterFactory.getXMLValidCharacterFilter().filter(original);
		}
		return "";
	}
	
	private class MetaHandler extends DefaultHandler {

		private StringBuilder current;
		private final File fMeta;
		
		public MetaHandler(File fMeta) {
			this.fMeta = fMeta;
		}
		
		@Override
		public final void startElement(String uri, String localName, String qName, Attributes attributes) {
			if("meta".equals(qName)) {
				 meta.setUuid(attributes.getValue("uuid"));
			} else if ("lock".equals(qName)) {
				meta.setLocked("true".equals(attributes.getValue("locked")));
				String date = attributes.getValue("date");
				if (date != null && date.length() > 0) {
					meta.setLockedDate(new Date(Long.parseLong(date)));
				}
			} else if ("thumbnails".equals(qName)) {
				String valueStr = attributes.getValue("cannotGenerateThumbnail");
				if(StringHelper.containsNonWhitespace(valueStr)) {
					meta.setCannotGenerateThumbnails(Boolean.parseBoolean(valueStr));
				}
			}else if ("thumbnail".equals(qName)) {
				Thumbnail thumbnail = new Thumbnail();
				thumbnail.setMaxHeight(Integer.parseInt(attributes.getValue("maxHeight")));
				thumbnail.setMaxWidth(Integer.parseInt(attributes.getValue("maxWidth")));
				thumbnail.setFinalHeight(Integer.parseInt(attributes.getValue("finalHeight")));
				thumbnail.setFinalWidth(Integer.parseInt(attributes.getValue("finalWidth")));
				thumbnail.setFill("true".equals(attributes.getValue("fill")));
				thumbnails.add(thumbnail);
			}
		}
		
		@Override
		public final void characters(char[] ch, int start, int length) {
			if(length == 0) return;
			if(current == null) {
				current = new StringBuilder();
			}
			current.append(ch, start, length);
		}

		@Override
		public final void endElement(String uri, String localName, String qName) {
			if(current == null) return;
			
			if("comment".equals(qName)) {
				meta.setComment(cutLenght(current.toString(), 32000));
			} else if ("author".equals(qName)) {
				Long authorKey = getLong();
				if(authorKey != null) {
					meta.setAuthor(securityManager.loadIdentityByKey(authorKey));
				}
			} else if ("lock".equals(qName)) {
				Long lockedByKey = getLong();
				if(lockedByKey != null) {
					meta.setLockedBy(securityManager.loadIdentityByKey(lockedByKey));
				}
			} else if ("title".equals(qName)) {
				meta.setTitle(cutLenght(current.toString(), 2000));
			} else if ("publisher".equals(qName)) {
				meta.setPublisher(cutLenght(current.toString(), 2000));
			} else if ("source".equals(qName)) {
				meta.setSource(cutLenght(current.toString(), 2000));
			} else if ("city".equals(qName)) {
				meta.setCity(cutLenght(current.toString(), 256));
			} else if ("pages".equals(qName)) {
				meta.setPages(cutLenght(current.toString(), 2000));
			} else if ("language".equals(qName)) {
				meta.setLanguage(cutLenght(current.toString(), 16));
			} else if ("downloadCount".equals(qName)) {
				Long key = getLong();
				if(key != null) {
					meta.setDownloadCount(key.intValue());
				}
			} else if ("month".equals(qName)) {
				meta.setPubMonth(cutLenght(current.toString(), 16));
			} else if ("year".equals(qName)) {
				meta.setPubYear(cutLenght(current.toString(), 16));
			} else if (qName.equals("creator")) {
				meta.setCreator(cutLenght(current.toString(), 2000));
			} else if (qName.equals("url")) {
				meta.setUrl(cutLenght(current.toString(), 1000));
			} else if (qName.equals("licenseTypeKey")) {
				//
			} else if (qName.equals("licenseTypeName")) {
				String licenseTypeName = current.toString().trim();
				if(StringHelper.containsNonWhitespace(licenseTypeName)) {
					meta.setLicenseTypeName(current.toString());
					meta.setLicenseType(licenseService.loadLicenseTypeByName(licenseTypeName));
				}
			} else if (qName.equals("licenseText")) {
				meta.setLicenseText(current.toString());
			} else if (qName.equals("licensor")) {
				meta.setLicensor(cutLenght(current.toString(), 4000));
			} else if (qName.equals("thumbnail")) {
				if(fMeta != null) {
					String finalName = current.toString();
					File thumbnailFile = new File(fMeta.getParentFile(), finalName);
					thumbnails.get(thumbnails.size() - 1).setThumbnailFile(thumbnailFile);
				}
			}
			current = null;
		}
		
		private Long getLong() {
			try {
				String val = current.toString();
				if(StringHelper.isLong(val)) {
					return Long.valueOf(val);
				}
			} catch (NumberFormatException nEx) {
				//nothing to say
			}
			return null;
		}
		
		private String cutLenght(String text, int maxLength) {
			if(text == null || text.length() < maxLength) {
				return text;
			}
			return text.substring(0, maxLength - 2);
		}
	}
	
	public static class Thumbnail implements Serializable {

		private static final long serialVersionUID = 29491661959555446L;
		
		private int maxWidth;
		private int maxHeight;
		private int finalWidth;
		private int finalHeight;
		private Boolean fill;
		private File thumbnailFile;
		
		public int getMaxWidth() {
			return maxWidth;
		}
		
		public void setMaxWidth(int maxWidth) {
			this.maxWidth = maxWidth;
		}
		
		public int getMaxHeight() {
			return maxHeight;
		}
		
		public void setMaxHeight(int maxHeight) {
			this.maxHeight = maxHeight;
		}
		
		public int getFinalWidth() {
			return finalWidth;
		}
		
		public void setFinalWidth(int finalWidth) {
			this.finalWidth = finalWidth;
		}
		
		public int getFinalHeight() {
			return finalHeight;
		}
		
		public void setFinalHeight(int finalHeight) {
			this.finalHeight = finalHeight;
		}
		
		public Boolean getFill() {
			return fill;
		}

		public void setFill(Boolean fill) {
			this.fill = fill;
		}

		public File getThumbnailFile() {
			return thumbnailFile;
		}
		
		public void setThumbnailFile(File thumbnailFile) {
			this.thumbnailFile = thumbnailFile;
		}
		
		public boolean exists() {
			return thumbnailFile != null && thumbnailFile.exists();
		}
	}
}