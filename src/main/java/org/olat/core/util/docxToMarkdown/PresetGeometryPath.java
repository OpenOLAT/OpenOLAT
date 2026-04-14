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
package org.olat.core.util.docxToMarkdown;

import java.util.Map;

/**
 * Provides SVG path data for OOXML preset shape geometries (ST_ShapeType).
 * Each method returns an SVG path 'd' attribute value in local coordinates
 * from (0,0) to (cx,cy).
 *
 * @author gnaegi, https://www.frentix.com
 */
class PresetGeometryPath {

    private PresetGeometryPath() { /* utility */ }

    /**
     * Returns SVG path 'd' attribute for a preset geometry.
     * Coordinates are in the shape's local space: (0,0) to (cx,cy).
     *
     * @param preset      the OOXML preset geometry name (e.g., "rect", "chevron")
     * @param cx          shape width (EMU or any unit - paths are proportional)
     * @param cy          shape height
     * @param adjustments named adjustment values from a:avLst (may be null)
     * @return SVG path data string, or null if the preset is not recognized
     */
    static String getPath(String preset, int cx, int cy, Map<String, Integer> adjustments) {
        if (cx <= 0 || cy <= 0) return null;
        return switch (preset) {
            // Basic shapes
            case "rect", "flowChartProcess"         -> rect(cx, cy);
            case "roundRect"                         -> roundRect(cx, cy, adj(adjustments, "adj", 16667));
            case "ellipse", "flowChartConnector"     -> ellipse(cx, cy);
            case "diamond", "flowChartDecision"      -> diamond(cx, cy);
            case "triangle"                          -> triangle(cx, cy, adj(adjustments, "adj", 50000));
            case "hexagon"                           -> hexagon(cx, cy, adj(adjustments, "adj", 25000));
            case "homePlate"                         -> homePlate(cx, cy, adj(adjustments, "adj", 50000));
            case "chevron"                           -> chevron(cx, cy, adj(adjustments, "adj", 50000));
            case "rightArrow"                        -> rightArrow(cx, cy,
                                                            adj(adjustments, "adj1", 50000),
                                                            adj(adjustments, "adj2", 50000));
            case "leftArrow"                         -> leftArrow(cx, cy,
                                                            adj(adjustments, "adj1", 50000),
                                                            adj(adjustments, "adj2", 50000));
            case "upArrow"                           -> upArrow(cx, cy,
                                                            adj(adjustments, "adj1", 50000),
                                                            adj(adjustments, "adj2", 50000));
            case "downArrow"                         -> downArrow(cx, cy,
                                                            adj(adjustments, "adj1", 50000),
                                                            adj(adjustments, "adj2", 50000));
            case "plus"                              -> plus(cx, cy, adj(adjustments, "adj", 25000));
            case "pentagon"                          -> pentagon(cx, cy);
            case "octagon"                           -> octagon(cx, cy, adj(adjustments, "adj", 29289));
            case "parallelogram"                     -> parallelogram(cx, cy, adj(adjustments, "adj", 25000));
            case "trapezoid"                         -> trapezoid(cx, cy, adj(adjustments, "adj", 25000));
            case "donut"                             -> donut(cx, cy, adj(adjustments, "adj", 25000));
            case "snipRoundRect"                     -> snipRoundRect(cx, cy,
                                                            adj(adjustments, "adj1", 16667),
                                                            adj(adjustments, "adj2", 16667));
            // Flowchart shapes
            case "flowChartTerminator"               -> flowChartTerminator(cx, cy);
            case "flowChartDocument"                 -> flowChartDocument(cx, cy);
            // Arrow variants
            case "leftRightArrow"                    -> leftRightArrow(cx, cy,
                                                            adj(adjustments, "adj1", 50000),
                                                            adj(adjustments, "adj2", 50000));
            case "upDownArrow"                       -> upDownArrow(cx, cy,
                                                            adj(adjustments, "adj1", 50000),
                                                            adj(adjustments, "adj2", 50000));
            case "notchedRightArrow"                 -> notchedRightArrow(cx, cy,
                                                            adj(adjustments, "adj1", 50000),
                                                            adj(adjustments, "adj2", 50000));
            // Stars
            case "star4"                             -> star(cx, cy, 4, adj(adjustments, "adj", 12500));
            case "star5"                             -> star(cx, cy, 5, adj(adjustments, "adj", 19098));
            case "star6"                             -> star(cx, cy, 6, adj(adjustments, "adj", 23570));
            // Connector
            case "straightConnector1"               -> straightConnector(cx, cy);
            // Other common shapes
            case "frame"                             -> frame(cx, cy, adj(adjustments, "adj", 12500));
            case "can"                               -> can(cx, cy, adj(adjustments, "adj", 25000));
            case "foldedCorner"                      -> foldedCorner(cx, cy, adj(adjustments, "adj", 16667));
            case "heart"                             -> heart(cx, cy);
            case "cloud"                             -> cloud(cx, cy);
            case "blockArc"                          -> blockArc(cx, cy,
                                                            adj(adjustments, "adj1", 10800000),
                                                            adj(adjustments, "adj2", 0),
                                                            adj(adjustments, "adj3", 25000));
            case "teardrop"                          -> teardrop(cx, cy, adj(adjustments, "adj", 100000));
            case "pie"                               -> pie(cx, cy,
                                                            adj(adjustments, "adj1", 0),
                                                            adj(adjustments, "adj2", 270000));
            case "round1Rect"                        -> round1Rect(cx, cy, adj(adjustments, "adj", 16667));
            case "round2SameRect"                    -> round2SameRect(cx, cy,
                                                            adj(adjustments, "adj1", 16667),
                                                            adj(adjustments, "adj2", 0));
            case "snip1Rect"                         -> snip1Rect(cx, cy, adj(adjustments, "adj", 16667));
            case "snip2SameRect"                     -> snip2SameRect(cx, cy,
                                                            adj(adjustments, "adj1", 16667),
                                                            adj(adjustments, "adj2", 0));
            case "wedgeRectCallout"                  -> wedgeRectCallout(cx, cy,
                                                            adj(adjustments, "adj1", 0),
                                                            adj(adjustments, "adj2", 50000));
            case "ribbon2"                           -> ribbon2(cx, cy, adj(adjustments, "adj", 16667));
            // Additional polygon and triangle variants
            case "heptagon"                          -> regularPolygon(cx, cy, 7, -90);
            case "rtTriangle"                        -> "M0,0 L" + cx + "," + cy + " L0," + cy + " Z";
            // More star variants
            case "star7"                             -> star(cx, cy, 7, adj(adjustments, "adj", 25000));
            case "star8"                             -> star(cx, cy, 8, adj(adjustments, "adj", 25000));
            case "star10"                            -> star(cx, cy, 10, adj(adjustments, "adj", 30000));
            case "star12"                            -> star(cx, cy, 12, adj(adjustments, "adj", 30000));
            case "star16"                            -> star(cx, cy, 16, adj(adjustments, "adj", 35000));
            case "star24"                            -> star(cx, cy, 24, adj(adjustments, "adj", 38000));
            case "star32"                            -> star(cx, cy, 32, adj(adjustments, "adj", 40000));
            // Arc
            case "arc"                               -> arc(cx, cy,
                                                            adj(adjustments, "adj1", 16200000),
                                                            adj(adjustments, "adj2", 0));
            // Complex arrow fallbacks
            case "bentArrow", "uturnArrow"           -> bentArrowFallback(cx, cy);
            case "circularArrow"                     -> circularArrow(cx, cy);
            // Ribbon variants
            case "ribbon"                            -> ribbon(cx, cy, adj(adjustments, "adj", 16667));
            case "ellipseRibbon", "ellipseRibbon2"   -> ribbon2(cx, cy, adj(adjustments, "adj", 16667));
            // Cross (alias for plus)
            case "cross"                             -> plus(cx, cy, adj(adjustments, "adj", 25000));
            // Other shapes
            case "funnel"                            -> funnel(cx, cy);
            case "pieWedge"                          -> pieWedge(cx, cy);
            case "gear6"                             -> gear(cx, cy, 6);
            case "gear9"                             -> gear(cx, cy, 9);
            case "lightningBolt"                     -> lightningBolt(cx, cy);
            case "moon"                              -> moon(cx, cy, adj(adjustments, "adj", 50000));
            case "bevel"                             -> bevel(cx, cy, adj(adjustments, "adj", 12500));
            default                                  -> null;
        };
    }

    /**
     * Returns true if this preset geometry uses a cutout path
     * that requires fill-rule="evenodd" for correct rendering.
     */
    static boolean isEvenOddShape(String preset) {
        return switch (preset) {
            case "donut", "frame", "foldedCorner", "moon", "gear6", "gear9" -> true;
            default -> false;
        };
    }

    // -----------------------------------------------------------------------
    // Adjustment helper
    // -----------------------------------------------------------------------

    private static int adj(Map<String, Integer> adjustments, String name, int defaultVal) {
        if (adjustments == null || adjustments.isEmpty()) return defaultVal;
        return adjustments.getOrDefault(name, defaultVal);
    }

    // -----------------------------------------------------------------------
    // Basic shapes
    // -----------------------------------------------------------------------

    /** Simple rectangle. */
    private static String rect(int cx, int cy) {
        return "M0,0 L" + cx + ",0 L" + cx + "," + cy + " L0," + cy + " Z";
    }

    /** Rectangle with four rounded corners. adj = corner radius as fraction * 1000 of min(cx,cy)/2. */
    private static String roundRect(int cx, int cy, int adj) {
        int r = (int) ((long) Math.min(cx, cy) * adj / 200000);
        if (r <= 0) return rect(cx, cy);
        return "M" + r + ",0"
                + " L" + (cx - r) + ",0"
                + " A" + r + "," + r + " 0 0,1 " + cx + "," + r
                + " L" + cx + "," + (cy - r)
                + " A" + r + "," + r + " 0 0,1 " + (cx - r) + "," + cy
                + " L" + r + "," + cy
                + " A" + r + "," + r + " 0 0,1 0," + (cy - r)
                + " L0," + r
                + " A" + r + "," + r + " 0 0,1 " + r + ",0 Z";
    }

    /** Ellipse approximated via two SVG arcs. */
    private static String ellipse(int cx, int cy) {
        int rx = cx / 2;
        int ry = cy / 2;
        int mx = cx / 2;
        int my = cy / 2;
        return "M0," + my
                + " A" + rx + "," + ry + " 0 1,0 " + cx + "," + my
                + " A" + rx + "," + ry + " 0 1,0 0," + my + " Z";
    }

    /** Diamond (rotated square). */
    private static String diamond(int cx, int cy) {
        return "M" + (cx / 2) + ",0"
                + " L" + cx + "," + (cy / 2)
                + " L" + (cx / 2) + "," + cy
                + " L0," + (cy / 2) + " Z";
    }

    /** Triangle. adj = top vertex X position as % * 1000. */
    private static String triangle(int cx, int cy, int adj) {
        int tx = (int) ((long) cx * adj / 100000);
        return "M" + tx + ",0 L" + cx + "," + cy + " L0," + cy + " Z";
    }

    /** Hexagon. adj = horizontal inset as fraction * 1000. */
    private static String hexagon(int cx, int cy, int adj) {
        int dx = (int) ((long) cx * adj / 100000);
        return "M" + dx + ",0"
                + " L" + (cx - dx) + ",0"
                + " L" + cx + "," + (cy / 2)
                + " L" + (cx - dx) + "," + cy
                + " L" + dx + "," + cy
                + " L0," + (cy / 2) + " Z";
    }

    /** Home plate (pentagon with flat left side). adj = arrow point depth as % * 1000. */
    private static String homePlate(int cx, int cy, int adj) {
        int dx = (int) ((long) cx * adj / 100000);
        return "M0,0"
                + " L" + (cx - dx) + ",0"
                + " L" + cx + "," + (cy / 2)
                + " L" + (cx - dx) + "," + cy
                + " L0," + cy + " Z";
    }

    /** Chevron. adj = point depth as % * 1000. */
    private static String chevron(int cx, int cy, int adj) {
        int dx = (int) ((long) cx * adj / 100000);
        return "M0,0"
                + " L" + (cx - dx) + ",0"
                + " L" + cx + "," + (cy / 2)
                + " L" + (cx - dx) + "," + cy
                + " L0," + cy
                + " L" + dx + "," + (cy / 2) + " Z";
    }

    /** Right-pointing arrow. adj1 = shaft height ratio, adj2 = head length ratio. */
    private static String rightArrow(int cx, int cy, int adj1, int adj2) {
        int headLen  = (int) ((long) cx * adj2 / 100000);
        int shaftTop = (int) ((long) cy * (100000 - adj1) / 200000);
        int shaftBot = cy - shaftTop;
        return "M0," + shaftTop
                + " L" + (cx - headLen) + "," + shaftTop
                + " L" + (cx - headLen) + ",0"
                + " L" + cx + "," + (cy / 2)
                + " L" + (cx - headLen) + "," + cy
                + " L" + (cx - headLen) + "," + shaftBot
                + " L0," + shaftBot + " Z";
    }

    /** Left-pointing arrow. adj1 = shaft height ratio, adj2 = head length ratio. */
    private static String leftArrow(int cx, int cy, int adj1, int adj2) {
        int headLen  = (int) ((long) cx * adj2 / 100000);
        int shaftTop = (int) ((long) cy * (100000 - adj1) / 200000);
        int shaftBot = cy - shaftTop;
        return "M" + cx + "," + shaftTop
                + " L" + headLen + "," + shaftTop
                + " L" + headLen + ",0"
                + " L0," + (cy / 2)
                + " L" + headLen + "," + cy
                + " L" + headLen + "," + shaftBot
                + " L" + cx + "," + shaftBot + " Z";
    }

    /** Up-pointing arrow. adj1 = shaft width ratio, adj2 = head height ratio. */
    private static String upArrow(int cx, int cy, int adj1, int adj2) {
        int headH      = (int) ((long) cy * adj2 / 100000);
        int shaftLeft  = (int) ((long) cx * (100000 - adj1) / 200000);
        int shaftRight = cx - shaftLeft;
        return "M" + (cx / 2) + ",0"
                + " L" + cx + "," + headH
                + " L" + shaftRight + "," + headH
                + " L" + shaftRight + "," + cy
                + " L" + shaftLeft + "," + cy
                + " L" + shaftLeft + "," + headH
                + " L0," + headH + " Z";
    }

    /** Down-pointing arrow. adj1 = shaft width ratio, adj2 = head height ratio. */
    private static String downArrow(int cx, int cy, int adj1, int adj2) {
        int headH      = (int) ((long) cy * adj2 / 100000);
        int shaftLeft  = (int) ((long) cx * (100000 - adj1) / 200000);
        int shaftRight = cx - shaftLeft;
        return "M" + (cx / 2) + "," + cy
                + " L" + cx + "," + (cy - headH)
                + " L" + shaftRight + "," + (cy - headH)
                + " L" + shaftRight + ",0"
                + " L" + shaftLeft + ",0"
                + " L" + shaftLeft + "," + (cy - headH)
                + " L0," + (cy - headH) + " Z";
    }

    /** Plus / cross. adj = arm width fraction * 1000. */
    private static String plus(int cx, int cy, int adj) {
        int a = (int) ((long) cx * adj / 100000);
        int b = (int) ((long) cy * adj / 100000);
        return "M" + a + ",0"
                + " L" + (cx - a) + ",0"
                + " L" + (cx - a) + "," + b
                + " L" + cx + "," + b
                + " L" + cx + "," + (cy - b)
                + " L" + (cx - a) + "," + (cy - b)
                + " L" + (cx - a) + "," + cy
                + " L" + a + "," + cy
                + " L" + a + "," + (cy - b)
                + " L0," + (cy - b)
                + " L0," + b
                + " L" + a + "," + b + " Z";
    }

    /** Regular pentagon. */
    private static String pentagon(int cx, int cy) {
        return regularPolygon(cx, cy, 5, -90.0);
    }

    /** Octagon. adj = corner cut fraction * 1000. */
    private static String octagon(int cx, int cy, int adj) {
        int dx = (int) ((long) cx * adj / 100000);
        int dy = (int) ((long) cy * adj / 100000);
        return "M" + dx + ",0"
                + " L" + (cx - dx) + ",0"
                + " L" + cx + "," + dy
                + " L" + cx + "," + (cy - dy)
                + " L" + (cx - dx) + "," + cy
                + " L" + dx + "," + cy
                + " L0," + (cy - dy)
                + " L0," + dy + " Z";
    }

    /** Parallelogram. adj = horizontal offset fraction * 1000. */
    private static String parallelogram(int cx, int cy, int adj) {
        int dx = (int) ((long) cx * adj / 100000);
        return "M" + dx + ",0"
                + " L" + cx + ",0"
                + " L" + (cx - dx) + "," + cy
                + " L0," + cy + " Z";
    }

    /** Trapezoid. adj = top edge inset per side. Always narrow top, wide bottom.
     *  When adj &gt; 50000, the computed insets cross over; we swap to keep valid geometry. */
    private static String trapezoid(int cx, int cy, int adj) {
        int x1 = (int) ((long) cx * adj / 100000);
        int x2 = cx - x1;
        // When adj > 50000, x1 > x2 — swap to prevent self-intersection
        int left = Math.min(x1, x2);
        int right = Math.max(x1, x2);
        return "M" + left + ",0"
                + " L" + right + ",0"
                + " L" + cx + "," + cy
                + " L0," + cy + " Z";
    }

    /**
     * Donut (annulus). adj = ring thickness fraction * 1000 relative to min(cx,cy)/2.
     * Uses fill-rule="evenodd" (caller must set this attribute).
     */
    private static String donut(int cx, int cy, int adj) {
        int thickness = (int) ((long) Math.min(cx, cy) * adj / 200000);
        int orx = cx / 2;
        int ory = cy / 2;
        int irx = orx - thickness;
        int iry = ory - thickness;
        if (irx <= 0 || iry <= 0) return ellipse(cx, cy);
        // Outer ellipse (clockwise)
        String outer = "M0," + ory
                + " A" + orx + "," + ory + " 0 1,0 " + cx + "," + ory
                + " A" + orx + "," + ory + " 0 1,0 0," + ory;
        // Inner ellipse (counter-clockwise, creates hole with evenodd)
        String inner = " M" + thickness + "," + ory
                + " A" + irx + "," + iry + " 0 1,1 " + (cx - thickness) + "," + ory
                + " A" + irx + "," + iry + " 0 1,1 " + thickness + "," + ory;
        return outer + inner + " Z";
    }

    /**
     * Snip-round rectangle: top-left rounded, top-right snipped (chamfered),
     * bottom corners sharp.
     * adj1 = round corner radius fraction * 1000, adj2 = snip size fraction * 1000.
     */
    private static String snipRoundRect(int cx, int cy, int adj1, int adj2) {
        int r    = (int) ((long) Math.min(cx, cy) * adj1 / 200000);
        int snip = (int) ((long) Math.min(cx, cy) * adj2 / 200000);
        if (r <= 0 && snip <= 0) return rect(cx, cy);
        return "M" + r + ",0"
                + " L" + (cx - snip) + ",0"
                + " L" + cx + "," + snip
                + " L" + cx + "," + cy
                + " L0," + cy
                + " L0," + r
                + " A" + r + "," + r + " 0 0,1 " + r + ",0 Z";
    }

    // -----------------------------------------------------------------------
    // Flowchart shapes
    // -----------------------------------------------------------------------

    /** Stadium / pill (flowchart terminator). */
    private static String flowChartTerminator(int cx, int cy) {
        int r = cy / 2;
        return "M" + r + ",0"
                + " L" + (cx - r) + ",0"
                + " A" + r + "," + r + " 0 0,1 " + (cx - r) + "," + cy
                + " L" + r + "," + cy
                + " A" + r + "," + r + " 0 0,1 " + r + ",0 Z";
    }

    /** Flowchart document: rectangle with wavy bottom edge (cubic bezier wave). */
    private static String flowChartDocument(int cx, int cy) {
        // Wave amplitude ~8% of height; approximated with one cubic bezier per half-wave
        int waveH = (int) ((long) cy * 8 / 100);
        int waveY = cy - waveH;
        int q1x = cx / 4;
        int q2x = cx / 2;
        int q3x = cx * 3 / 4;
        return "M0,0"
                + " L" + cx + ",0"
                + " L" + cx + "," + waveY
                + " C" + q3x + "," + waveY + " " + q3x + "," + cy + " " + q2x + "," + cy
                + " C" + q1x + "," + cy + " " + q1x + "," + waveY + " 0," + waveY + " Z";
    }

    // -----------------------------------------------------------------------
    // Arrow variants
    // -----------------------------------------------------------------------

    /** Double-headed horizontal arrow. */
    private static String leftRightArrow(int cx, int cy, int adj1, int adj2) {
        int headLen  = (int) ((long) cx * adj2 / 200000); // each head
        int shaftTop = (int) ((long) cy * (100000 - adj1) / 200000);
        int shaftBot = cy - shaftTop;
        return "M0," + (cy / 2)
                + " L" + headLen + ",0"
                + " L" + headLen + "," + shaftTop
                + " L" + (cx - headLen) + "," + shaftTop
                + " L" + (cx - headLen) + ",0"
                + " L" + cx + "," + (cy / 2)
                + " L" + (cx - headLen) + "," + cy
                + " L" + (cx - headLen) + "," + shaftBot
                + " L" + headLen + "," + shaftBot
                + " L" + headLen + "," + cy + " Z";
    }

    /** Double-headed vertical arrow. */
    private static String upDownArrow(int cx, int cy, int adj1, int adj2) {
        int headH      = (int) ((long) cy * adj2 / 200000); // each head
        int shaftLeft  = (int) ((long) cx * (100000 - adj1) / 200000);
        int shaftRight = cx - shaftLeft;
        return "M" + (cx / 2) + ",0"
                + " L" + cx + "," + headH
                + " L" + shaftRight + "," + headH
                + " L" + shaftRight + "," + (cy - headH)
                + " L" + cx + "," + (cy - headH)
                + " L" + (cx / 2) + "," + cy
                + " L0," + (cy - headH)
                + " L" + shaftLeft + "," + (cy - headH)
                + " L" + shaftLeft + "," + headH
                + " L0," + headH + " Z";
    }

    /** Notched right arrow: right arrow with a V-notch on the left edge. */
    private static String notchedRightArrow(int cx, int cy, int adj1, int adj2) {
        int headLen  = (int) ((long) cx * adj2 / 100000);
        int shaftTop = (int) ((long) cy * (100000 - adj1) / 200000);
        int shaftBot = cy - shaftTop;
        int notchDx  = shaftTop; // notch depth equals shaft top margin
        return "M0," + shaftTop
                + " L" + notchDx + "," + (cy / 2)
                + " L0," + shaftBot
                + " L" + (cx - headLen) + "," + shaftBot
                + " L" + (cx - headLen) + "," + cy
                + " L" + cx + "," + (cy / 2)
                + " L" + (cx - headLen) + ",0"
                + " L" + (cx - headLen) + "," + shaftTop + " Z";
    }

    // -----------------------------------------------------------------------
    // Stars
    // -----------------------------------------------------------------------

    /**
     * Generic N-pointed star.
     * adj = inner radius as fraction * 1000 of 50000 (so adj/50000 of outer radius).
     * Outer points are placed at the bounding box edge; inner points at adj ratio.
     */
    private static String star(int cx, int cy, int n, int adj) {
        double outerRx = cx / 2.0;
        double outerRy = cy / 2.0;
        double innerRatio = (double) adj / 50000.0;
        double innerRx = outerRx * innerRatio;
        double innerRy = outerRy * innerRatio;
        double cx2 = cx / 2.0;
        double cy2 = cy / 2.0;

        // Starting angle: -90° so first outer point is at top-center
        double startAngleDeg = -90.0;
        double stepDeg = 360.0 / n;
        double halfStepDeg = stepDeg / 2.0;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            double outerAngle = Math.toRadians(startAngleDeg + i * stepDeg);
            double innerAngle = Math.toRadians(startAngleDeg + i * stepDeg + halfStepDeg);

            int ox = (int) Math.round(cx2 + outerRx * Math.cos(outerAngle));
            int oy = (int) Math.round(cy2 + outerRy * Math.sin(outerAngle));
            int ix = (int) Math.round(cx2 + innerRx * Math.cos(innerAngle));
            int iy = (int) Math.round(cy2 + innerRy * Math.sin(innerAngle));

            if (i == 0) {
                sb.append("M").append(ox).append(",").append(oy);
            } else {
                sb.append(" L").append(ox).append(",").append(oy);
            }
            sb.append(" L").append(ix).append(",").append(iy);
        }
        sb.append(" Z");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Connector
    // -----------------------------------------------------------------------

    /** Straight connector: diagonal line (rendered stroked, no fill). */
    private static String straightConnector(int cx, int cy) {
        return "M0,0 L" + cx + "," + cy;
    }

    // -----------------------------------------------------------------------
    // Other common shapes
    // -----------------------------------------------------------------------

    /**
     * Frame (picture frame): outer rectangle minus inner rectangle.
     * adj = border thickness as fraction * 1000 of min(cx,cy).
     * Uses fill-rule="evenodd" (caller must set this attribute).
     */
    private static String frame(int cx, int cy, int adj) {
        int t = (int) ((long) Math.min(cx, cy) * adj / 100000);
        if (t <= 0) return rect(cx, cy);
        // Outer rectangle (clockwise)
        String outer = "M0,0 L" + cx + ",0 L" + cx + "," + cy + " L0," + cy + " Z";
        // Inner rectangle (counter-clockwise, creates hole)
        String inner = " M" + t + "," + t
                + " L" + t + "," + (cy - t)
                + " L" + (cx - t) + "," + (cy - t)
                + " L" + (cx - t) + "," + t + " Z";
        return outer + inner;
    }

    /**
     * Can (cylinder). adj = lid height as fraction * 1000 of cy.
     * Top ellipse + sides + bottom ellipse arc.
     */
    private static String can(int cx, int cy, int adj) {
        int lidH = (int) ((long) cy * adj / 100000);
        if (lidH <= 0) lidH = 1;
        int rx   = cx / 2;
        int lidY = lidH;
        // Side rectangle + bottom semi-ellipse + top full ellipse
        return "M0," + lidH
                + " L0," + cy
                + " A" + rx + "," + lidH + " 0 0,0 " + cx + "," + cy
                + " L" + cx + "," + lidH
                + " A" + rx + "," + lidH + " 0 1,1 0," + lidY + " Z";
    }

    /**
     * Folded corner: rectangle with bottom-right corner folded.
     * adj = fold size as fraction * 1000 of min(cx,cy).
     */
    private static String foldedCorner(int cx, int cy, int adj) {
        int fold = (int) ((long) Math.min(cx, cy) * adj / 100000);
        // Main rectangle body with cutout corner
        return "M0,0"
                + " L" + cx + ",0"
                + " L" + cx + "," + (cy - fold)
                + " L" + (cx - fold) + "," + cy
                + " L0," + cy + " Z"
                // Fold triangle
                + " M" + (cx - fold) + "," + (cy - fold)
                + " L" + cx + "," + (cy - fold)
                + " L" + (cx - fold) + "," + cy + " Z";
    }

    /** Heart shape using cubic bezier curves. */
    private static String heart(int cx, int cy) {
        // Classic heart: two humps on top, pointed bottom
        double mx  = cx / 2.0;
        double my  = cy * 0.30;
        // Control points for left hump
        double lx1 = cx * 0.00, ly1 = cy * 0.00;
        double lx2 = cx * 0.00, ly2 = cy * 0.55;
        double lx3 = cx * 0.50, ly3 = cy * 0.75;
        // Control points for right hump
        double rx1 = cx * 1.00, ry1 = cy * 0.55;
        double rx2 = cx * 1.00, ry2 = cy * 0.00;
        double rx3 = cx * 0.50, ry3 = my;

        // Peak of left hump
        double lPeakX = cx * 0.25;
        double lPeakY = 0.0;

        // Peak of right hump
        double rPeakX = cx * 0.75;
        double rPeakY = 0.0;

        return fmt("M%.0f,%.0f", mx, my)
                + fmt(" C%.0f,%.0f %.0f,%.0f %.0f,%.0f", lx1, ly1, lx1, ly1 * 0.5, lPeakX, lPeakY)
                + fmt(" C%.0f,%.0f %.0f,%.0f %.0f,%.0f", mx * 0.5, lPeakY, lx2, ly2 * 0.4, lx3, ly3)
                + fmt(" C%.0f,%.0f %.0f,%.0f %.0f,%.0f", (float) (cx * 0.75), (float) (cy * 0.85), (float) (cx * 0.6), (float) (cy * 0.92), (float) mx, (float) cy)
                + fmt(" C%.0f,%.0f %.0f,%.0f %.0f,%.0f", (float) (cx * 0.4), (float) (cy * 0.92), (float) (cx * 0.25), (float) (cy * 0.85), lx3, ly3)
                + fmt(" C%.0f,%.0f %.0f,%.0f %.0f,%.0f", rx1 - cx, ly2 * 0.4, rPeakX * 0.5 + mx * 0.5, rPeakY, rPeakX, rPeakY)
                + fmt(" C%.0f,%.0f %.0f,%.0f %.0f,%.0f", rx2, ry2 * 0.5, rx2, ry1, mx, my)
                + " Z";
    }

    /** Cloud shape approximated with overlapping arcs. */
    private static String cloud(int cx, int cy) {
        // Six bumps approximated with arcs along the top and flat bottom
        int ry = cy / 3;
        int b  = cy - ry / 2; // flat bottom Y
        // Bump centres (x, y) and radii
        int r1 = (int) (cx * 0.18), cx1 = (int) (cx * 0.22), cy1 = b - (int) (cy * 0.10);
        int r2 = (int) (cx * 0.22), cx2 = (int) (cx * 0.50), cy2 = b - (int) (cy * 0.20);
        int r3 = (int) (cx * 0.17), cx3 = (int) (cx * 0.78), cy3 = b - (int) (cy * 0.10);

        // Simple approximation: three arcs on top, straight bottom
        return "M" + (cx1 - r1) + "," + cy1
                + " A" + r1 + "," + r1 + " 0 1,1 " + (cx1 + r1) + "," + cy1
                + " A" + r2 + "," + r2 + " 0 1,1 " + (cx2 + r2) + "," + cy2
                + " A" + r3 + "," + r3 + " 0 1,1 " + (cx3 + r3) + "," + cy3
                + " L" + cx + "," + b
                + " L0," + b
                + " Z";
    }

    /**
     * Block arc (sector of a donut).
     * adj1 = start angle in 60000ths of a degree, adj2 = swing angle in same unit,
     * adj3 = thickness fraction * 1000 of min(cx,cy)/2.
     */
    private static String blockArc(int cx, int cy, int adj1, int adj2, int adj3) {
        double startDeg = adj1 / 60000.0;
        double swingDeg = adj2 / 60000.0;
        if (swingDeg == 0) swingDeg = 270.0;
        double endDeg = startDeg + swingDeg;

        double orx = cx / 2.0;
        double ory = cy / 2.0;
        int thickness  = (int) ((long) Math.min(cx, cy) * adj3 / 200000);
        double irx = orx - thickness;
        double iry = ory - thickness;
        if (irx <= 0 || iry <= 0) return ellipse(cx, cy);

        double startRad = Math.toRadians(startDeg);
        double endRad   = Math.toRadians(endDeg);

        int osx = (int) Math.round(orx + orx * Math.cos(startRad));
        int osy = (int) Math.round(ory + ory * Math.sin(startRad));
        int oex = (int) Math.round(orx + orx * Math.cos(endRad));
        int oey = (int) Math.round(ory + ory * Math.sin(endRad));
        int isx = (int) Math.round(orx + irx * Math.cos(startRad));
        int isy = (int) Math.round(ory + iry * Math.sin(startRad));
        int iex = (int) Math.round(orx + irx * Math.cos(endRad));
        int iey = (int) Math.round(ory + iry * Math.sin(endRad));

        int largeArc = swingDeg > 180.0 ? 1 : 0;
        int oRx = (int) orx;
        int oRy = (int) ory;
        int iRx = (int) irx;
        int iRy = (int) iry;

        return "M" + osx + "," + osy
                + " A" + oRx + "," + oRy + " 0 " + largeArc + ",1 " + oex + "," + oey
                + " L" + iex + "," + iey
                + " A" + iRx + "," + iRy + " 0 " + largeArc + ",0 " + isx + "," + isy
                + " Z";
    }

    /**
     * Teardrop: circle with a pointed tail extending to the top-right.
     * adj = tail length ratio * 1000 relative to min(cx,cy).
     */
    private static String teardrop(int cx, int cy, int adj) {
        // Circle radius fits the shorter axis
        int r    = (int) (Math.min(cx, cy) * 0.40);
        int cxC  = cx / 2;
        int cyC  = cy / 2 + r / 4; // shift circle slightly down
        // Point of the teardrop
        int tipX = cx;
        int tipY = 0;
        // Approximate with large arc + two quadratic beziers to the tip and back
        return "M" + (cxC - r) + "," + cyC
                + " A" + r + "," + r + " 0 1,1 " + cxC + "," + (cyC - r)
                + " Q" + cx + ",0 " + tipX + "," + tipY
                + " Q" + cx + "," + cyC + " " + (cxC + r) + "," + cyC
                + " Z";
    }

    /**
     * Pie / sector shape.
     * adj1 = start angle in thousandths of a degree, adj2 = swing angle in same unit.
     */
    private static String pie(int cx, int cy, int adj1, int adj2) {
        double startDeg = adj1 / 1000.0;
        double swingDeg = adj2 / 1000.0;
        if (swingDeg == 0) swingDeg = 270.0;
        double endDeg = startDeg + swingDeg;

        double rx = cx / 2.0;
        double ry = cy / 2.0;

        double startRad = Math.toRadians(startDeg);
        double endRad   = Math.toRadians(endDeg);

        int sx = (int) Math.round(rx + rx * Math.cos(startRad));
        int sy = (int) Math.round(ry + ry * Math.sin(startRad));
        int ex = (int) Math.round(rx + rx * Math.cos(endRad));
        int ey = (int) Math.round(ry + ry * Math.sin(endRad));

        int largeArc = swingDeg > 180.0 ? 1 : 0;
        int iRx = (int) rx;
        int iRy = (int) ry;

        return "M" + (int) rx + "," + (int) ry
                + " L" + sx + "," + sy
                + " A" + iRx + "," + iRy + " 0 " + largeArc + ",1 " + ex + "," + ey + " Z";
    }

    /** Rectangle with only the top-right corner rounded. adj = radius fraction * 1000. */
    private static String round1Rect(int cx, int cy, int adj) {
        int r = (int) ((long) Math.min(cx, cy) * adj / 200000);
        if (r <= 0) return rect(cx, cy);
        return "M0,0"
                + " L" + (cx - r) + ",0"
                + " A" + r + "," + r + " 0 0,1 " + cx + "," + r
                + " L" + cx + "," + cy
                + " L0," + cy + " Z";
    }

    /** Rectangle with top-left and top-right corners rounded. adj1 = left radius, adj2 = right radius. */
    private static String round2SameRect(int cx, int cy, int adj1, int adj2) {
        int r1 = (int) ((long) Math.min(cx, cy) * adj1 / 200000);
        int r2 = (int) ((long) Math.min(cx, cy) * adj2 / 200000);
        if (r1 <= 0 && r2 <= 0) return rect(cx, cy);
        StringBuilder sb = new StringBuilder();
        sb.append("M").append(r1).append(",0");
        if (r2 > 0) {
            sb.append(" L").append(cx - r2).append(",0")
              .append(" A").append(r2).append(",").append(r2).append(" 0 0,1 ").append(cx).append(",").append(r2);
        } else {
            sb.append(" L").append(cx).append(",0");
        }
        sb.append(" L").append(cx).append(",").append(cy)
          .append(" L0,").append(cy)
          .append(" L0,").append(r1);
        if (r1 > 0) {
            sb.append(" A").append(r1).append(",").append(r1).append(" 0 0,1 ").append(r1).append(",0");
        }
        sb.append(" Z");
        return sb.toString();
    }

    /** Rectangle with one corner snipped (chamfered, top-right). adj = snip size fraction * 1000. */
    private static String snip1Rect(int cx, int cy, int adj) {
        int snip = (int) ((long) Math.min(cx, cy) * adj / 200000);
        if (snip <= 0) return rect(cx, cy);
        return "M0,0"
                + " L" + (cx - snip) + ",0"
                + " L" + cx + "," + snip
                + " L" + cx + "," + cy
                + " L0," + cy + " Z";
    }

    /** Rectangle with two same-side corners snipped (top-left and top-right). */
    private static String snip2SameRect(int cx, int cy, int adj1, int adj2) {
        int s1 = (int) ((long) Math.min(cx, cy) * adj1 / 200000);
        int s2 = (int) ((long) Math.min(cx, cy) * adj2 / 200000);
        if (s1 <= 0 && s2 <= 0) return rect(cx, cy);
        return "M" + s1 + ",0"
                + " L" + (cx - s2) + ",0"
                + " L" + cx + "," + s2
                + " L" + cx + "," + cy
                + " L0," + cy
                + " L0," + s1 + " Z";
    }

    /**
     * Wedge rectangle callout: rectangle with a triangular pointer at the bottom.
     * adj1 = pointer X offset from centre (fraction * 1000 of cx), adj2 = pointer tip Y (fraction * 1000 of cy).
     */
    private static String wedgeRectCallout(int cx, int cy, int adj1, int adj2) {
        // Body occupies ~80% of height; pointer extends below
        int bodyH  = (int) (cy * 0.80);
        int tipX   = (int) ((long) cx * (50000 + adj1) / 100000);
        int tipY   = (int) ((long) cy * adj2 / 100000);
        // Pointer base width ~15% of cx on each side of centre bottom
        int baseOff = (int) (cx * 0.08);
        int baseY   = bodyH;
        int leftX   = Math.max(0, cx / 2 - baseOff);
        int rightX  = Math.min(cx, cx / 2 + baseOff);
        return "M0,0"
                + " L" + cx + ",0"
                + " L" + cx + "," + bodyH
                + " L" + rightX + "," + baseY
                + " L" + tipX + "," + tipY
                + " L" + leftX + "," + baseY
                + " L0," + bodyH + " Z";
    }

    /**
     * Ribbon2 (up ribbon / banner): horizontal banner with folded ends at top.
     * adj = fold height fraction * 1000 of cy.
     */
    private static String ribbon2(int cx, int cy, int adj) {
        int foldH  = (int) ((long) cy * adj / 100000);
        int foldW  = (int) (cx * 0.10);
        int bodyY  = foldH;
        int bodyH  = cy - foldH;
        // Main body
        return "M0," + bodyY
                + " L" + cx + "," + bodyY
                + " L" + cx + "," + cy
                + " L" + (cx - foldW) + "," + (bodyY + bodyH / 2)
                + " L" + cx + "," + bodyY
                // right fold triangle already drawn; close left side
                + " M0," + bodyY
                + " L" + foldW + "," + (bodyY + bodyH / 2)
                + " L0," + cy
                + " L" + cx + "," + cy
                + " L" + (cx - foldW) + "," + (bodyY + bodyH / 2)
                + " L" + cx + "," + bodyY
                + " L0," + bodyY + " Z";
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /** Build a regular convex polygon scaled to fit (cx, cy), starting at startAngleDeg. */
    private static String regularPolygon(int cx, int cy, int n, double startAngleDeg) {
        double rx  = cx / 2.0;
        double ry  = cy / 2.0;
        double cx2 = cx / 2.0;
        double cy2 = cy / 2.0;
        double step = 360.0 / n;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            double angle = Math.toRadians(startAngleDeg + i * step);
            int px = (int) Math.round(cx2 + rx * Math.cos(angle));
            int py = (int) Math.round(cy2 + ry * Math.sin(angle));
            if (i == 0) {
                sb.append("M").append(px).append(",").append(py);
            } else {
                sb.append(" L").append(px).append(",").append(py);
            }
        }
        sb.append(" Z");
        return sb.toString();
    }

    private static String arc(int cx, int cy, int stAng, int swAng) {
        int rx = cx / 2, ry = cy / 2;
        double startRad = Math.toRadians(stAng / 60000.0);
        double sweepDeg = swAng / 60000.0;
        double sweepRad = Math.toRadians(sweepDeg);
        double endRad = startRad + sweepRad;
        int x1 = rx + (int)(rx * Math.cos(startRad));
        int y1 = ry + (int)(ry * Math.sin(startRad));
        int x2 = rx + (int)(rx * Math.cos(endRad));
        int y2 = ry + (int)(ry * Math.sin(endRad));
        int largeArc = Math.abs(sweepDeg) > 180 ? 1 : 0;
        int sweep = sweepDeg > 0 ? 1 : 0;
        return "M" + x1 + "," + y1 + " A" + rx + "," + ry + " 0 " + largeArc + " " + sweep + " " + x2 + "," + y2;
    }

    private static String circularArrow(int cx, int cy) {
        // Curved arrow: arc from bottom-right sweeping counterclockwise, with arrowhead
        int rx = cx / 2, ry = cy / 2;
        int innerRx = (int)(rx * 0.7), innerRy = (int)(ry * 0.7);
        int arrowSize = Math.min(cx, cy) / 8;
        // Outer arc from ~30° to ~330° (leaving gap at bottom for arrowhead)
        double startDeg = 30, endDeg = 330;
        double startRad = Math.toRadians(startDeg), endRad = Math.toRadians(endDeg);
        int ox1 = rx + (int)(rx * Math.cos(startRad)), oy1 = ry + (int)(ry * Math.sin(startRad));
        int ox2 = rx + (int)(rx * Math.cos(endRad)), oy2 = ry + (int)(ry * Math.sin(endRad));
        int ix1 = rx + (int)(innerRx * Math.cos(startRad)), iy1 = ry + (int)(innerRy * Math.sin(startRad));
        int ix2 = rx + (int)(innerRx * Math.cos(endRad)), iy2 = ry + (int)(innerRy * Math.sin(endRad));
        // Arrowhead at end (pointing clockwise)
        int midX = rx + (int)((rx + arrowSize) * Math.cos(endRad));
        int midY = ry + (int)((ry + arrowSize) * Math.sin(endRad));
        return "M" + ox1 + "," + oy1
            + " A" + rx + "," + ry + " 0 1,0 " + ox2 + "," + oy2
            + " L" + midX + "," + midY  // arrowhead outer point
            + " L" + ix2 + "," + iy2    // arrowhead to inner arc
            + " A" + innerRx + "," + innerRy + " 0 1,1 " + ix1 + "," + iy1
            + " Z";
    }

    private static String bentArrowFallback(int cx, int cy) {
        return "M0," + (cy / 4) + " L" + (3 * cx / 4) + "," + (cy / 4)
            + " L" + (3 * cx / 4) + ",0 L" + cx + "," + (cy / 2)
            + " L" + (3 * cx / 4) + "," + cy + " L" + (3 * cx / 4) + "," + (3 * cy / 4)
            + " L0," + (3 * cy / 4) + " Z";
    }

    private static String ribbon(int cx, int cy, int adjVal) {
        int foldH = (int)((long)cy * adjVal / 100000);
        return "M0," + (cy - foldH) + " L" + (cx / 6) + "," + cy
            + " L" + (5 * cx / 6) + "," + cy + " L" + cx + "," + (cy - foldH)
            + " L" + (5 * cx / 6) + "," + (cy - foldH) + " L" + (5 * cx / 6) + ",0"
            + " L" + (cx / 6) + ",0 L" + (cx / 6) + "," + (cy - foldH) + " Z";
    }

    private static String funnel(int cx, int cy) {
        // Cone-shaped bowl: elliptical opening at top, smooth curved walls to narrow bottom
        int topRy = cy / 8;
        int topRx = cx / 2;
        int botL = (int)(cx * 0.40);
        int botR = (int)(cx * 0.60);
        // Smooth cone: bezier curves from wide top to narrow bottom, no straight tube
        return "M0," + topRy
            + " A" + topRx + "," + topRy + " 0 0,1 " + cx + "," + topRy               // top elliptical arc
            + " C" + cx + "," + (cy * 2 / 5) + " " + botR + "," + (cy * 3 / 4) + " " + botR + "," + cy  // right wall curve
            + " L" + botL + "," + cy                                                     // narrow bottom edge
            + " C" + botL + "," + (cy * 3 / 4) + " 0," + (cy * 2 / 5) + " 0," + topRy  // left wall curve
            + " Z";
    }

    private static String pieWedge(int cx, int cy) {
        // Quarter-circle sector per OOXML: right-angle corner at bottom-right,
        // straight edges on right and bottom, arc curving outward on top-left.
        return "M0," + cy                                            // bottom-left
            + " A" + cx + "," + cy + " 0 0,1 " + cx + ",0"         // arc to top-right
            + " L" + cx + "," + cy                                   // right edge down
            + " Z";                                                   // bottom edge back
    }

    private static String gear(int cx, int cy, int teeth) {
        StringBuilder path = new StringBuilder();
        double centerX = cx / 2.0, centerY = cy / 2.0;
        double outerR = Math.min(cx, cy) * 0.5;
        double innerR = outerR * 0.7;
        double toothR = outerR * 0.85;
        int totalPoints = teeth * 4;
        for (int i = 0; i < totalPoints; i++) {
            double angle = 2 * Math.PI * i / totalPoints - Math.PI / 2;
            double r = (i % 4 == 1 || i % 4 == 2) ? outerR : toothR;
            int px = (int)(centerX + r * Math.cos(angle));
            int py = (int)(centerY + r * Math.sin(angle));
            path.append(i == 0 ? "M" : " L").append(px).append(",").append(py);
        }
        path.append(" Z");
        int ir = (int)innerR;
        int icx = cx / 2, icy = cy / 2;
        path.append(" M").append(icx + ir).append(",").append(icy)
            .append(" A").append(ir).append(",").append(ir).append(" 0 1 0 ").append(icx - ir).append(",").append(icy)
            .append(" A").append(ir).append(",").append(ir).append(" 0 1 0 ").append(icx + ir).append(",").append(icy).append(" Z");
        return path.toString();
    }

    private static String lightningBolt(int cx, int cy) {
        return "M" + (int)(cx * 0.6) + ",0 L" + (int)(cx * 0.2) + "," + (cy / 2)
            + " L" + (int)(cx * 0.5) + "," + (cy / 2) + " L" + (int)(cx * 0.4) + "," + cy
            + " L" + (int)(cx * 0.8) + "," + (cy / 2) + " L" + (int)(cx * 0.5) + "," + (cy / 2) + " Z";
    }

    private static String moon(int cx, int cy, int adjVal) {
        int rx = cx / 2, ry = cy / 2;
        String outer = ellipse(cx, cy);
        int innerOffX = (int)((long)cx * adjVal / 100000);
        return outer + " M" + (innerOffX + rx) + "," + ry
            + " A" + rx + "," + ry + " 0 1 0 " + innerOffX + ",0"
            + " A" + rx + "," + ry + " 0 1 1 " + (innerOffX + rx) + "," + ry + " Z";
    }

    private static String bevel(int cx, int cy, int adjVal) {
        int bev = (int)((long)Math.min(cx, cy) * adjVal / 100000);
        return "M" + bev + ",0 L" + (cx - bev) + ",0 L" + cx + "," + bev
            + " L" + cx + "," + (cy - bev) + " L" + (cx - bev) + "," + cy
            + " L" + bev + "," + cy + " L0," + (cy - bev) + " L0," + bev + " Z";
    }

    /** Formatted string helper (avoids varargs ambiguity for float casts). */
    private static String fmt(String format, Object... args) {
        return String.format(format, args);
    }
}
