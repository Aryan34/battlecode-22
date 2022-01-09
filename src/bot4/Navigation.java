package bot4;

import battlecode.common.*;

public class Navigation {
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final Direction[] cardinalDirections = {
            Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH
    };

    static final Direction[] nonCardinalDirections = {
            Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST
    };

    static boolean isCardinal(Direction dir) {
        for (Direction card : cardinalDirections) {
            if (card.equals(dir)) {
                return true;
            }
        }
        return false;
    }

    static final int RUBBLE_THRESHOLD = 70;

    static Direction[] closeDirections(Direction dir) {
        return new Direction[]{
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.opposite()
        };
    }

    static Direction[] evenCloserDirections(Direction dir) {
        return new Direction[]{
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight(),
        };
    }

    static final int RECENTLY_VISITED_THRESHOLD = 10;


    RobotController rc;

    int[][] visited = new int[GameConstants.MAP_MAX_HEIGHT][GameConstants.MAP_MAX_WIDTH];

    Navigation(RobotController rc) {
        this.rc = rc;
    }

    boolean moveRandom() throws GameActionException {
        int randX = (Robot.rng.nextInt(rc.getMapWidth()));
        int randY = (Robot.rng.nextInt(rc.getMapHeight()));

        MapLocation randLoc = new MapLocation(randX, randY);
        Direction randDir = rc.getLocation().directionTo(randLoc);

        if (rc.canMove(randDir)) {
            rc.move(randDir);
            return true;
        }
        return false;
    }

    boolean moveRandomCardinal() throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        Direction dir = cardinalDirections[Robot.rng.nextInt(cardinalDirections.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        return false;
    }

    boolean moveAwayFromArchon(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        int bestMoveRubble = 1000;
        int bestMoveLead = -1000;
        Direction bestDir = null;
        MapLocation myLoc = rc.getLocation();

        for (Direction dir : directions) {
            MapLocation dest = myLoc.add(dir);
            int moveRubble = rc.senseRubble(dest);
            int moveLead = rc.senseLead(dest);

            if (!rc.onTheMap(dest) || !rc.canMove(dir) || dest.distanceSquaredTo(loc) <= 2) {
                continue;
            }

            if (moveRubble < bestMoveRubble) {
                bestMoveRubble = moveRubble;
                bestMoveLead = moveLead;
                bestDir = dir;
            } else if (5 * (moveRubble - bestMoveRubble) < (moveLead - bestMoveLead)) {
                bestMoveRubble = moveRubble;
                bestMoveLead = moveLead;
                bestDir = dir;
            }
        }

        if (bestDir != null && rc.canMove(bestDir)) {
            rc.move(bestDir);
            return true;
        }
        return false;
    }

    boolean moveTowards(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        return greedy(loc);
    }

    // prioritizes low rubble tiles to best enable for defensive combat
    boolean retreatTowards(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        MapLocation myLoc = rc.getLocation();
        Direction retreatDir = myLoc.directionTo(loc);
        Direction bestDir = null;

        // if rubble at myLoc is the least, then droid will stay put and fight => most dmg done to enemy
        int leastRubble = rc.senseRubble(myLoc);
        for (Direction dir : closeDirections(retreatDir)) {
            MapLocation newLoc = myLoc.add(dir);
            if (rc.onTheMap(newLoc) && rc.canSenseLocation(newLoc)) {
                if (rc.senseRubble(newLoc) < leastRubble) {
                    leastRubble = rc.senseRubble(newLoc);
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null && rc.canMove(bestDir)) {
            rc.move(bestDir);
            return true;
        }
        return false;
    }

    boolean moveAway(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        int minRubble = 10000;
        int currRubble = 10000;
        Direction bestDir = null;

        Direction[] awayDirs = closeDirections(rc.getLocation().directionTo(loc).opposite());
        for (Direction dir : awayDirs) {
            if (!rc.onTheMap(rc.getLocation().add(dir))) {
                continue;
            }
            currRubble = rc.senseRubble(rc.getLocation().add(dir));
            if (currRubble < minRubble) {
                minRubble = currRubble;
                bestDir = dir;
            }
        }

        if (bestDir != null && rc.canMove(bestDir)) {
            rc.move(bestDir);
            return true;
        }
        return false;
    }

    boolean retreatFromEnemies(RobotInfo[] enemyInfo) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        MapLocation myLoc = rc.getLocation();
        int x = myLoc.x;
        int y = myLoc.y;
        double force_x = 0;
        double force_y = 0;

        for (RobotInfo info : enemyInfo) {
            switch (info.type) {
                case SAGE:
                    force_x += (double) (x - info.location.x) / myLoc.distanceSquaredTo(info.location);
                    force_y += (double) (y - info.location.y) / myLoc.distanceSquaredTo(info.location);
                    break;
                case SOLDIER:
                    force_x += 1.25 * (double) (x - info.location.x) / myLoc.distanceSquaredTo(info.location);
                    force_y += 1.25 * (double) (y - info.location.y) / myLoc.distanceSquaredTo(info.location);
                    break;
                case WATCHTOWER:
                    force_x += 1.5 * (double) (x - info.location.x) / myLoc.distanceSquaredTo(info.location);
                    force_y += 1.5 * (double) (y - info.location.y) / myLoc.distanceSquaredTo(info.location);
                    break;
                default:
                    break;
            }
        }

        MapLocation forceResult = myLoc.translate(100 * (int) force_x, 100 * (int) force_y);
        return retreatTowards(forceResult);
    }

    // treat all rubble above threshold as impassable, otherwise move towards target
    // greedily -> choose lowest rubble neighbor tile that doesn't go backwards
    // [directionToTarget.opposite, directionToTarget.opposite.{rotateRight, rotateLeft}]
    boolean greedy(MapLocation target) throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        MapLocation myLoc = rc.getLocation();
        int currDist = myLoc.distanceSquaredTo(target);
        int bestDist = 100000;
        int minRubble = GameConstants.MAX_RUBBLE;
        Direction bestDir = null;

        MapLocation enemyLoc = null;
        int enemyDist = 10000;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (info.type == RobotType.SOLDIER || info.type == RobotType.SAGE || info.type == RobotType.WATCHTOWER) {
                int dist = myLoc.distanceSquaredTo(info.location);
                if (dist < info.type.actionRadiusSquared && dist < enemyDist) {
                    enemyDist = dist;
                    enemyLoc = info.location;
                }
            }
        }

        if (enemyLoc != null) {
            bestDir = myLoc.directionTo(enemyLoc).opposite();
        } else {
            Direction[] closeDirs = evenCloserDirections(myLoc.directionTo(target));
            for (Direction dir : closeDirs) {
                int newDist = myLoc.add(dir).distanceSquaredTo(target);
                if (rc.canMove(dir) && newDist < currDist) {
                    if (bestDir == null || rc.senseRubble(myLoc.add(dir)) < minRubble ||
                            (rc.senseRubble(myLoc.add(dir)) == minRubble && newDist < bestDist)) {
                        bestDist = newDist;
                        minRubble = rc.senseRubble(myLoc.add(dir));
                        bestDir = dir;
                    }
                }
            }
        }

        if (bestDir != null && rc.canMove(bestDir)) {
            rc.move(bestDir);
            return true;
        }
        return false;
    }

    MapLocation reflectHoriz(MapLocation loc) {
        int new_x = rc.getMapWidth() - loc.x - 1;
        int new_y = loc.y;
        return new MapLocation(new_x, new_y);
    }

    MapLocation reflectVert(MapLocation loc) {
        int new_x = loc.x;
        int new_y = rc.getMapHeight() - loc.y - 1;
        return new MapLocation(new_x, new_y);
    }

    MapLocation reflectDiag(MapLocation loc) {
        int new_x = rc.getMapWidth() - loc.x - 1;
        int new_y = rc.getMapHeight() - loc.y - 1;
        return new MapLocation(new_x, new_y);
    }
}
