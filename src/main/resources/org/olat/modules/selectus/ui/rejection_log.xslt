<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
  <xsl:param name="versionParam" select="'1.0'"/> 
  <!-- ========================= -->
  <!-- root element: projectteam -->
  <!-- ========================= -->
  <xsl:template match="export">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="simpleA4"
        	page-height="21cm" page-width="29.7cm"
        	margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body margin-top="12mm" margin-bottom="10mm"/>
          <fo:region-before extent="10mm"/>
          <fo:region-after extent="8mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="simpleA4">
      	<fo:static-content flow-name="xsl-region-before">
      		<xsl:call-template name="page-header"/>
      	</fo:static-content>
      	<fo:static-content flow-name="xsl-region-after">
      		<xsl:call-template name="page-footer"/>
      	</fo:static-content>
      
        <fo:flow flow-name="xsl-region-body">
					<xsl:if test="count(content/row) &gt; 0">
						<fo:block font-size="10pt" font-family="sans-serif">
							<fo:table table-layout="fixed" width="100%" border-collapse="collapse">
								<fo:table-column column-width="0.78cm"/>
								<fo:table-column column-width="1.78cm"/>
								<fo:table-column column-width="6.00cm"/>
								<fo:table-column column-width="6.01cm"/>
								<fo:table-column column-width="7.91cm"/>
								<fo:table-column column-width="3.21cm"/>
								<fo:table-header>
									<xsl:apply-templates select="headers"/>
								</fo:table-header>
								<fo:table-body>
									<xsl:apply-templates select="content"/>
								</fo:table-body>
							</fo:table>
						</fo:block>
					</xsl:if>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
  <!-- Page header and footer -->
  <xsl:template name="page-header">
  	<fo:block-container
  		border-color="#000000" border-style="solid"
      border-before-width="0.75pt" border-after-width="0.75pt"
      border-start-width="0.75pt" border-end-width="0.75pt"
      font-family="sans-serif" font-size="10pt" font-weight="bold"
      width="100%" height="0.75cm" space-after="0.62cm">
			<fo:block-container absolute-position="absolute"
				width="3cm" top="0.2cm" left="0.2cm"
				text-align="left">
				<fo:block color="red">Confidential</fo:block></fo:block-container>
  		<fo:block-container absolute-position="absolute"
				width="15cm" top="0.2cm" left="5.35cm"
				text-align="center"><fo:block><fo:inline>Email center list: </fo:inline><xsl:value-of select="position/title"/></fo:block></fo:block-container>
  		<fo:block-container absolute-position="absolute"
				width="5cm" top="0.2cm" left="20.5cm"
				text-align="right"><fo:block><xsl:value-of select="@output-date"/></fo:block></fo:block-container>
		</fo:block-container>
  </xsl:template>
  
  <xsl:template name="page-footer">
  	<fo:block line-height="10pt" font-size="10pt"
      font-family="sans-serif" color="#000000" text-align="center"
      ><fo:inline>Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:inline></fo:block>
  </xsl:template>
  
  <!-- Table headers -->
  <xsl:template match="headers">
  	<fo:table-row>
  		<fo:table-cell
				border-color="#000000" border-style="solid"
      	border-before-width="0.75pt" border-after-width="0.75pt"
      	border-start-width="0.75pt" border-end-width="0.75pt">
				<fo:block/></fo:table-cell>
  		<xsl:apply-templates select="header[@attribute-name = 'title']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'firstName']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'lastName']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'mail']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'logCreationDate']"/>
  	</fo:table-row>
  </xsl:template>
  
	<xsl:template match="header">
		<fo:table-cell
			border-color="#000000" border-style="solid"
      border-before-width="0.75pt" border-after-width="0.75pt"
      border-start-width="0pt" border-end-width="0.75pt">
			<fo:block
				color="#000000" font-size="10pt" font-weight="bold"
				margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"><xsl:value-of select="."/></fo:block >
		</fo:table-cell>
  </xsl:template>
  
  <!-- ========================= -->
  <!-- child element: member     -->
  <!-- ========================= -->
  <xsl:template match="content">
  	<xsl:apply-templates select="row"/>
  </xsl:template>
  
  <xsl:template match="row">
    <fo:table-row>
			<fo:table-cell
				border-color="#000000" border-style="solid"
      	border-before-width="0.5pt" border-after-width="0.5pt"
      	border-start-width="0.5pt" border-end-width="0.5pt">
				<fo:block
					margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"
					><xsl:value-of select="cell[@attribute-name = 'id']"/></fo:block></fo:table-cell>
  		<xsl:apply-templates select="cell[@attribute-name = 'title']"/>
  		<xsl:apply-templates select="cell[@attribute-name = 'firstName']"/>
  		<xsl:apply-templates select="cell[@attribute-name = 'lastName']"/>
  		<xsl:apply-templates select="cell[@attribute-name = 'mail']"/>
  		<xsl:apply-templates select="cell[@attribute-name = 'logCreationDate']"/>
  	</fo:table-row>
  </xsl:template>
  
  <xsl:template match="cell">
		<fo:table-cell
			border-color="black" border-style="solid"
      border-before-width="0.5pt" border-after-width="0.5pt"
      border-start-width="0pt" border-end-width="0.5pt"><fo:block 
			margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"
			><xsl:value-of select="."/></fo:block></fo:table-cell>
  </xsl:template>
</xsl:stylesheet>
