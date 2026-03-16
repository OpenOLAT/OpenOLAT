<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
	<xsl:import href="default_library_v2.xslt"/>
	<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
	<xsl:param name="versionParam" select="'1.0'"/>

	<xsl:template name="position">
		<fo:table>
			<xsl:call-template name="columns-def"/>
			<fo:table-body>
				<xsl:call-template name="applicant-row-big-conditional">
					<xsl:with-param name="label">Position</xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="position/positionTitle"/></xsl:with-param>
				</xsl:call-template>
				<xsl:call-template name="applicant-row-big-conditional">
					<xsl:with-param name="label">Planning-ID</xsl:with-param>
					<xsl:with-param name="value"><xsl:value-of select="position/planingsNumber"/></xsl:with-param>
				</xsl:call-template>
			</fo:table-body>
		</fo:table>
	</xsl:template>
	
	<xsl:template match="application">
		<xsl:apply-templates select="person" />
		<xsl:apply-templates select="contact" />
		<xsl:apply-templates select="businessInformations" />
		<xsl:apply-templates select="businessAddress" />
		<xsl:apply-templates select="address" />
		<xsl:apply-templates select="personalCustomAttributes" />
		<xsl:apply-templates select="academicalBackground" />
		<xsl:apply-templates select="project" />
		<xsl:apply-templates select="customSteps" />
		<xsl:apply-templates select="documents-v2" />
	</xsl:template>

</xsl:stylesheet>