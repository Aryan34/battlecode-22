package bot1;

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

    public void brownian() throws GameActionException {
        double netX = 0;
        double netY = 0;
        double robotCharge = 100;

        // Repel off friendly robots of same type
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.getType() == rc.getType()) {
                double force = robotCharge / rc.getLocation().distanceSquaredTo(info.getLocation());
                double dx = (rc.getLocation().x - info.getLocation().x) * force;
                double dy = (rc.getLocation().y - info.getLocation().y) * force;
                netX += dx;
                netY += dy;
            }
        }
        System.out.println("After repelling off teammates, my net direction is: " + netX + ", " + netY);

        // Repel off walls
        double wallForce = 500;
        for (Direction dir : cardinalDirections) {
            MapLocation reachLoc = rc.getLocation();
            reachLoc = reachLoc.add(dir);
            while (robot.myLoc.distanceSquaredTo(reachLoc) <= robot.myType.visionRadiusSquared) {
                if (!rc.onTheMap(reachLoc)) {
                    System.out.println("Reach loc: " + reachLoc);
                    double force = wallForce / rc.getLocation().distanceSquaredTo(reachLoc);
                    double dx = (rc.getLocation().x - reachLoc.x) * force;
                    double dy = (rc.getLocation().y - reachLoc.y) * force;
                    netX += dx;
                    netY += dy;
                    break;
                }
                reachLoc = reachLoc.add(dir);
            }
        }
        System.out.println("After repelling off walls, my net direction is: " + netX + ", " + netY);

        // find overall direction to move in
        int dx = (int) Math.round(netX);
        int dy = (int) Math.round(netY);
        MapLocation brownianDest = robot.myLoc.translate(dx, dy);
        Direction dir = robot.myLoc.directionTo(brownianDest);

        if (dir != Direction.CENTER && rc.canMove(dir)) {
            if (rc.getRoundNum() - visited[brownianDest.x][brownianDest.y] < RECENTLY_VISITED_THRESHOLD) {
                // TODO: Implement a move to least recently visited tile and use that here
                System.out.println("Already visited this tile recently, so not moving here");
            } else {
                rc.move(dir);
                visited[brownianDest.x][brownianDest.y] = rc.getRoundNum();
            }
        }
    }

    void bugPath(MapLocation target) {
    }
}
