package bot3;

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
        Direction dir = cardinalDirections[Robot.rng.nextInt(cardinalDirections.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
            return true;
        }

        return false;
    }

    boolean moveTowards(MapLocation loc) throws GameActionException {
        return greedy(loc);
    }

    boolean moveAway(MapLocation loc) throws GameActionException {
        Direction[] awayDirs = evenCloserDirections(rc.getLocation().directionTo(loc).opposite());
        for (Direction dir : awayDirs) {
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            }
        }

        return false;
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

        if (bestDir != null) {
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
