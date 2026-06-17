<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
  <xsl:param name="versionParam" select="'1.0'"/> 

  <xsl:template match="reference">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="simpleA4"
        	page-height="29.7cm" page-width="21cm"
        	margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body margin-top="12mm" margin-bottom="10mm"/>
          <fo:region-before extent="10mm"/>
          <fo:region-after extent="8mm"/>
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
			font-family="sans-serif" font-size="18pt" font-weight="bold"
			margin-top="1.0cm"><fo:block><xsl:choose><xsl:when test="@reference-type = 'recommendation'">Letter of recommendation</xsl:when><xsl:otherwise>Expert assessment</xsl:otherwise></xsl:choose></fo:block></fo:block-container>
		<fo:block-container
			font-family="sans-serif" font-size="14pt"
			margin-top="1.0cm"><fo:block><xsl:choose><xsl:when test="@reference-type = 'recommendation'">The following documents have been submitted by a referee and not by the applicant.</xsl:when>
				<xsl:otherwise>The following documents have been submitted by an expert and not by the applicant.</xsl:otherwise></xsl:choose></fo:block></fo:block-container>
		<fo:block-container
			font-family="sans-serif" font-size="14pt"
			margin-top="1.0cm"><xsl:call-template name="document"></xsl:call-template></fo:block-container>
	</xsl:template>
  
	<xsl:template name="document">
		<fo:table table-layout="fixed" width="100%" border-collapse="collapse" margin-top="0.5cm">
			<fo:table-column column-width="6.80cm"/>
			<fo:table-column column-width="8.00cm"/>
			<fo:table-body>
				<xsl:call-template name="reference-row">
					<xsl:with-param name="label">Document:</xsl:with-param>
					<xsl:with-param name="value"><xsl:choose><xsl:when test="@reference-type = 'recommendation'">Recommendation </xsl:when><xsl:otherwise>Expert assessment </xsl:otherwise></xsl:choose><xsl:value-of select="@reference-pos"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="reference-row">
					<xsl:with-param name="label"><xsl:choose><xsl:when test="@reference-type = 'recommendation'">Referee</xsl:when><xsl:otherwise>Expert</xsl:otherwise></xsl:choose>:</xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="reference/firstName"/><xsl:value-of select="' '"/><xsl:value-of select="reference/lastName"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="reference-row">
					<xsl:with-param name="label">Organization / Institution:</xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="reference/institution"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
  
	<xsl:template name="reference-row">
		<xsl:param name="label"/>
		<xsl:param name="value"/>
		<fo:table-row>
  		<fo:table-cell><fo:block font-weight="bold"><xsl:value-of select="$label"/></fo:block></fo:table-cell>
  		<fo:table-cell><fo:block><xsl:value-of select="$value"/></fo:block></fo:table-cell>
		</fo:table-row>
	</xsl:template>
  
</xsl:stylesheet>