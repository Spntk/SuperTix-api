package com.supertix.api.enums;

/**
 * Direction of the stage as seen from inside the zone.
 *
 * Used to flip / rotate the seat grid so that the stage label is rendered
 * on the correct side of the user's seat view (front, behind, left, right).
 *
 * Examples:
 *   - VIP zone (in front of the stage)        -> NORTH (stage above the seats)
 *   - Premium zone (behind VIP, facing stage) -> NORTH
 *   - Upper-left side stand                   -> EAST  (stage to the right of seats)
 *   - Upper-right side stand                  -> WEST  (stage to the left of seats)
 *   - Back-stage / behind-stage zone          -> SOUTH (stage below seats)
 */
public enum StageDirection {
    NORTH,
    SOUTH,
    EAST,
    WEST
}
