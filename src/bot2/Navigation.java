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
    Robot robot;

    int[][] visited = new int[GameConstants.MAP_MAX_HEIGHT][GameConstants.MAP_MAX_WIDTH];

    Navigation(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
    }

    boolean moveRandom() throws GameActionException {
        int randX = (Robot.rng.nextInt(rc.getMapWidth()));
        int randY = (Robot.rng.nextInt(rc.getMapHeight()));

        MapLocation randLoc = new MapLocation(randX, randY);
        Direction randDir = robot.myLoc.directionTo(randLoc);

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
        if (!greedy(loc)) {
            rc.setIndicatorString("STUCKKKK");
            return false;
        }
        rc.setIndicatorString("Target: " + loc.x + ", " + loc.y);
        return true;
    }

    // treat all rubble above threshold as impassable, otherwise move towards target
    // greedily -> choose lowest rubble neighbor tile that doesn't go backwards
    // [directionToTarget.opposite, directionToTarget.opposite.{rotateRight, rotateLeft}]
    boolean greedy(MapLocation target) throws GameActionException {
        int minRubbleTile = GameConstants.MAX_RUBBLE;
        Direction bestDir = null;

        for (Direction dir : directions) {
            if (robot.myLoc.add(dir).distanceSquaredTo(target) < robot.myLoc.distanceSquaredTo(target)) {
                if (rc.senseRubble(robot.myLoc.add(dir)) < minRubbleTile) {
                    minRubbleTile = rc.senseRubble(robot.myLoc.add(dir));
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
