package bot1;

import battlecode.common.*;

public class Util {
    RobotController rc;
    Robot robot;

    Util(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
    }

    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
        } else {
            for (Direction closeDir : Navigation.closeDirections(dir)) {
                if (rc.canBuildRobot(type, closeDir)) {
                    rc.buildRobot(type, closeDir);
                    return true;
                }
            }
        }

        return false;
    }

    boolean tryBuildRandom(RobotType type) throws GameActionException {
        Direction dir = Navigation.directions[Robot.rng.nextInt(Navigation.directions.length)];
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
        } else {
            for (Direction closeDir : Navigation.closeDirections(dir)) {
                if (rc.canBuildRobot(type, closeDir)) {
                    rc.buildRobot(type, closeDir);
                    return true;
                }
            }
        }

        return false;
    }

    MapLocation closestFriendlyArchon() throws GameActionException {
        int closestArchonDist = 100000;
        MapLocation closest = null;

        MapLocation[] archonLocs = Robot.comms.getFriendlyArchonLocs();
        for (int i = 0; i < 4; ++i) {
            if (archonLocs[i] != null && archonLocs[i].distanceSquaredTo(robot.myLoc) < closestArchonDist) {
                closestArchonDist = archonLocs[i].distanceSquaredTo(robot.myLoc);
                closest = archonLocs[i];
            }
        }

        return closest;
    }

    MapLocation closestEnemyArchon() throws GameActionException {
        int closestArchonDist = 100000;
        MapLocation closest = null;

        MapLocation[] archonLocs = Robot.comms.getEnemyArchonLocs();
        for (int i = 0; i < 4; ++i) {
            if (archonLocs[i] != null && archonLocs[i].distanceSquaredTo(robot.myLoc) < closestArchonDist) {
                closestArchonDist = archonLocs[i].distanceSquaredTo(robot.myLoc);
                closest = archonLocs[i];
            }
        }

        return closest;
    }

    int distanceToClosestFriendlyArchon() throws GameActionException {
        int closestArchonDist = 100000;
        MapLocation[] archonLocs = Robot.comms.getFriendlyArchonLocs();

        for (int i = 0; i < 4; ++i) {
            if (archonLocs[i] != null && archonLocs[i].distanceSquaredTo(robot.myLoc) < closestArchonDist) {
                closestArchonDist = archonLocs[i].distanceSquaredTo(robot.myLoc);
            }
        }

        return closestArchonDist;
    }

    int distanceToClosestEnemyArchon() throws GameActionException {
        int closestArchonDist = 100000;
        MapLocation[] archonLocs = Robot.comms.getEnemyArchonLocs();

        for (int i = 0; i < 4; ++i) {
            if (archonLocs[i] != null && archonLocs[i].distanceSquaredTo(robot.myLoc) < closestArchonDist) {
                closestArchonDist = archonLocs[i].distanceSquaredTo(robot.myLoc);
            }
        }

        return closestArchonDist;
    }

    MapLocation closestAttackTarget() {
        int closestEnemyDist = 100000;
        MapLocation attackLoc = null;

        for (RobotInfo info : rc.senseNearbyRobots(robot.myType.actionRadiusSquared, rc.getTeam().opponent())) {
            if (info.location.distanceSquaredTo(robot.myLoc) < closestEnemyDist) {
                closestEnemyDist = info.location.distanceSquaredTo(robot.myLoc);
                attackLoc = info.location;
            }
        }

        return attackLoc;
    }

    MapLocation lowestHealthAttackTarget() {
        int lowestEnemyHealth = 100000;
        MapLocation attackLoc = null;

        for (RobotInfo info : rc.senseNearbyRobots(robot.myType.actionRadiusSquared, rc.getTeam().opponent())) {
            if (info.health < lowestEnemyHealth) {
                lowestEnemyHealth = info.health;
                attackLoc = info.location;
            }
        }

        return attackLoc;
    }

    MapLocation lowestHealthRepairTarget() {
        int lowestAllyHealth = rc.getHealth();
        MapLocation repairLoc = rc.getLocation();

        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.health < lowestAllyHealth) {
                lowestAllyHealth = info.health;
                repairLoc = info.location;
            }
        }

        return repairLoc;
    }
}
