package com.supertix.api.enums;

/**
 * Geometric shape used to render a zone on the venue map.
 *
 *   RECT       - axis-aligned rectangle (default)
 *   ROUND_RECT - rectangle with rounded corners
 *   ELLIPSE    - ellipse / oval
 *   POLYGON    - arbitrary polygon — points are read from
 *                {@code ZoneModel.polygonPoints} as a JSON array of
 *                [x, y] pairs in venue-canvas coordinates.
 */
public enum ZoneShape {
    RECT,
    ROUND_RECT,
    ELLIPSE,
    POLYGON
}
