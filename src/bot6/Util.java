package bot6;

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

    MapLocation closestTarget() {
        int closestEnemyDist = 100000;
        MapLocation attackLoc = null;

        for (RobotInfo info : rc.senseNearbyRobots(robot.myType.visionRadiusSquared, rc.getTeam().opponent())) {
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

    MapLocation lowestHealthTarget() {
        int lowestEnemyHealth = 100000;
        MapLocation attackLoc = null;

        for (RobotInfo info : rc.senseNearbyRobots(robot.myType.visionRadiusSquared, rc.getTeam().opponent())) {
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

    int countNearbyFriendlyTroops(RobotType type) {
        int count = 0;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.type == type) {
                ++count;
            }
        }

        return count;
    }

    int countNearbyEnemyTroops(RobotType type) {
        int count = 0;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (info.type == type) {
                ++count;
            }
        }

        return count;
    }

    int countNearbyFriendlyTroops(RobotType type, RobotInfo[] nearby) {
        int count = 0;
        for (RobotInfo info : nearby) {
            if (info.type == type) {
                ++count;
            }
        }

        return count;
    }

    int countNearbyEnemyTroops(RobotType type, RobotInfo[] nearby) {
        int count = 0;
        for (RobotInfo info : nearby) {
            if (info.type == type) {
                ++count;
            }
        }

        return count;
    }

    int countNearbyFriendlyAttackers(RobotInfo[] nearby) {
        int count = 0;
        for (RobotInfo info : nearby) {
            if (info.type == RobotType.SAGE || info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER) {
                ++count;
            }
        }

        return count;
    }

    int countNearbyEnemyAttackers(RobotInfo[] nearby) {
        int count = 0;
        for (RobotInfo info : nearby) {
            if (info.type == RobotType.SAGE || info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER) {
                ++count;
            }
        }

        return count;
    }

    MapLocation getAttackerLocation(RobotInfo[] enemyInfo) {
        for (RobotInfo info : enemyInfo) {
            if (info.type == RobotType.SAGE || info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER) {
                return info.location;
            }
        }

        return null;
    }

    // higher int is greater priority
    int attackPriority(RobotType type) {
        switch (type) {
            case ARCHON:
                return 300;
            case LABORATORY:
                return 200;
            case WATCHTOWER:
                return 1000;
            case BUILDER:
                return 100;
            case MINER:
                return 500;
            case SAGE:
                return 650;
            case SOLDIER:
                return 800;
            default:
                return -1;
        }
    }
}
