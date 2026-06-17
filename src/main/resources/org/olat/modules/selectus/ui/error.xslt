<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
  <xsl:param name="versionParam" select="'1.0'"/>

  <xsl:template match="error">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="simpleA4"
        	page-height="29.7cm" page-width="21cm"
        	margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body margin-top="0mm" margin-bottom="0mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="simpleA4">
        <fo:flow flow-name="xsl-region-body">
        	<xsl:call-template name="page-body" />
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
	<xsl:template name="page-body">
		<fo:block-container
			font-family="sans-serif" font-size="18pt"
			border-color="#a94442" border-style="solid"
      		border-before-width="0pt" border-after-width="0pt"
      		border-start-width="3pt" border-end-width="0pt"
      		background-color="#f2dede"
			margin-top="1cm" margin-right="5mm" margin-left="5mm"><fo:block
				color="#000000" font-size="10pt" font-weight="bold"
				margin-top="1cm" margin-right="1cm" margin-bottom="1cm" margin-left="1cm">The document <xsl:if test="@document">"<xsl:value-of select="@document"/>"</xsl:if> cannot be read</fo:block></fo:block-container>
	</xsl:template>
  
</xsl:stylesheet>