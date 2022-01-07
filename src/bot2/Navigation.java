package bot2;

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
        Direction[] close = {
                dir,
                dir.rotateLeft(),
                dir.rotateRight(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.opposite()
        };
        return close;
    }

    static final int RECENTLY_VISITED_THRESHOLD = 10;

    RobotController rc;

    int[][] visited = new int[GameConstants.MAP_MAX_HEIGHT][GameConstants.MAP_MAX_WIDTH];

    Navigation(RobotController rc, Robot robot) {
        this.rc = rc;
    }

    boolean moveRandom() throws GameActionException {
        int randX = (Robot.rng.nextInt(rc.getMapWidth()));
        int randY = (Robot.rng.nextInt(rc.getMapHeight()));

        MapLocation randLoc = new MapLocation(randX, randY);
        Direction randDir = rc.getLocation().directionTo(randLoc);

        if (rc.canMove(randDir)) {
            rc.setIndicatorString("Moved random to: " + randX + ", " + randY);
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

    // treat all rubble above threshold as impassable, otherwise move towards target
    // greedily -> choose lowest rubble neighbor tile that doesn't go backwards
    // [directionToTarget.opposite, directionToTarget.opposite.{rotateRight, rotateLeft}]
    boolean greedy(MapLocation target) throws GameActionException {
        int currDist = rc.getLocation().distanceSquaredTo(target);
        int bestDist = 10000000;
        int minRubbleTile = GameConstants.MAX_RUBBLE;
        Direction bestDir = null;

        for (Direction dir : closeDirections(rc.getLocation().directionTo(target))) {
            int newDist = rc.getLocation().add(dir).distanceSquaredTo(target);
            if (rc.canMove(dir) && newDist < currDist) {
                if (bestDir == null || rc.senseRubble(rc.getLocation().add(dir)) < minRubbleTile
                || (rc.senseRubble(rc.getLocation().add(dir)) == minRubbleTile && newDist < bestDist)) {
                    bestDist = newDist;
                    minRubbleTile = rc.senseRubble(rc.getLocation().add(dir));
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null) {
            rc.move(bestDir);
            return true;
        }
        return false;
    }
}
