<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" indent="yes" />

	<!-- XSLT Template to copy anything, priority="-1" -->
	<xsl:template match="@*|node()|text()|comment()|processing-instruction()" priority="-1">
		<xsl:call-template name="copyall"/>
	</xsl:template>

	<xsl:template name="copyall">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()|text()|comment()|processing-instruction()"/>
	  </xsl:copy>
	</xsl:template>
	
	<xsl:template match="include">
		<xsl:apply-templates select="document(@file)/ui-map-include/child::*" />
	</xsl:template>

</xsl:stylesheet>