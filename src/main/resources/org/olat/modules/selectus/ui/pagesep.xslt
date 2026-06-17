<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
  <xsl:param name="versionParam" select="'1.0'"/> 

  <xsl:template match="separator">
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
			font-family="sans-serif" font-size="14pt"
			margin-top="1.0cm"><xsl:call-template name="document"></xsl:call-template></fo:block-container>
	</xsl:template>
  
	<xsl:template name="document">
		<fo:table table-layout="fixed" width="100%" border-collapse="collapse" margin-top="0.5cm">
			<fo:table-column column-width="3.80cm"/>
			<fo:table-column column-width="11.00cm"/>
			<fo:table-body>
				<xsl:call-template name="applicant-row">
					<xsl:with-param name="label">Document:</xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="@separator-name"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row">
					<xsl:with-param name="label">Position:</xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="position/positionTitle"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row">
					<xsl:with-param name="label">Application-ID:</xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="application/id"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row">
					<xsl:with-param name="label">Applicant:</xsl:with-param>
					<xsl:with-param name="value"><xsl:if test="application/enhancedTitle"><xsl:value-of select="application/enhancedTitle"/></xsl:if><xsl:value-of select="application/person/firstName"/>&#160;<xsl:value-of select="application/person/lastName"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
  
	<xsl:template name="applicant-row">
		<xsl:param name="label"/>
		<xsl:param name="value"/>
		<fo:table-row>
  		<fo:table-cell><fo:block font-weight="bold"><xsl:value-of select="$label"/></fo:block></fo:table-cell>
  		<fo:table-cell><fo:block><xsl:value-of select="$value"/></fo:block></fo:table-cell>
		</fo:table-row>
	</xsl:template>
  
</xsl:stylesheet>