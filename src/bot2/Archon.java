package bot2;

import battlecode.common.*;

public class Archon extends Robot {
    static RobotType[] buildOrder1 = {
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.SOLDIER
    };

    static RobotType[] buildOrder2 = {
            RobotType.MINER,
            RobotType.MINER,
            RobotType.BUILDER,
            RobotType.BUILDER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.BUILDER,
            RobotType.BUILDER,
            RobotType.SOLDIER
    };

    static RobotType[] buildOrder3 = {
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER
    };

    static RobotType[] buildOrder4 = {
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER
    };


    int buildIndex;

    int enemySoldierSensedRound;
    int weightedThreatCount;
    boolean enemySoldierSensed;

    int buildersSpawned;
    int minersSpawned;
    int soldiersSpawned;

    RobotType[] recentlySpawned = new RobotType[5];

    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        comms.addFriendlyArchonLoc(rc.getLocation());

        buildIndex = 0;

        enemySoldierSensedRound = -10000;
        weightedThreatCount = 0;
        enemySoldierSensed = false;

        buildersSpawned = 0;
        minersSpawned = 0;
        soldiersSpawned = 0;
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        Direction dir = Navigation.directions[rng.nextInt(Navigation.directions.length)];
        int rn = rc.getRoundNum();
        RobotInfo[] robs = rc.senseNearbyRobots();
        int watchs = 0;
        for (RobotInfo rob : robs) {
            if (rob.type == RobotType.WATCHTOWER) {
                watchs++;
            }
        }

        if (rn <= 1) {
            rc.writeSharedArray(10, rc.getLocation().x);
            rc.writeSharedArray(11, rc.getLocation().y);
        }
        if (rn < 100) {
            if (rng.nextBoolean()) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a miner");
                if (rc.canBuildRobot(RobotType.MINER, dir)) {
                    rc.buildRobot(RobotType.MINER, dir);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        } else if (rc.getTeamLeadAmount(rc.getTeam()) > 2000 && watchs < 4) {
            rc.setIndicatorString("Trying to build a builder");
            if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                rc.buildRobot(RobotType.BUILDER, dir);
            }
        } else if (rn < 200) {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        } else if (rc.getTeamLeadAmount(rc.getTeam()) < 5000) {
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        } else {
            if (rng.nextBoolean() && rn < 1800) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a sage");
                if (rc.canBuildRobot(RobotType.SAGE, dir)) {
                    rc.buildRobot(RobotType.SAGE, dir);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        }
    }

    void followBuildOrder(RobotType[] buildOrder) throws GameActionException {
        if (tryBuild(buildOrder[buildIndex % 10])) {
            ++buildIndex;
        }
    }

    boolean tryBuild(RobotType type) throws GameActionException {
        Direction dir = Navigation.directions[Robot.rng.nextInt(Navigation.directions.length)];
        if (type == RobotType.MINER) {
            dir = findBestLeadDeposit();
        }

        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else {
            for (Direction closeDir : Navigation.closeDirections(dir)) {
                if (rc.canBuildRobot(type, closeDir)) {
                    rc.buildRobot(type, closeDir);
                    updateRecentlySpawned(type);
                    return true;
                }
            }
        }

        return false;
    }

    int countThreatsWeighted() {
        int weightedThreatCount = 0;
        for (RobotInfo info : rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam)) {
            switch (info.type) {
                case SOLDIER:
                    weightedThreatCount += 1;
                    break;
                case SAGE:
                    weightedThreatCount += 10;
                    break;
                case WATCHTOWER:
                    weightedThreatCount += 5;
                    break;
                default:
                    break;
            }
        }

        return weightedThreatCount;
    }

    void updateRecentlySpawned(RobotType type) {
        for (int i = 0; i < recentlySpawned.length; ++i) {
            if (recentlySpawned[i] == null) {
                recentlySpawned[i] = type;
                return;
            }
        }

        for (int i = 0; i < recentlySpawned.length - 1; ++i) {
            recentlySpawned[i] = recentlySpawned[i + 1];
        }
        recentlySpawned[recentlySpawned.length - 1] = type;
    }

    int countLastNSpawned(RobotType type, int lastN) {
        int count = 0;
        for (int i = 0; i < lastN; ++i) {
            if (recentlySpawned[recentlySpawned.length - 1 - i] == type) {
                ++count;
            }
        }

        return count;
    }

    Direction findBestLeadDeposit() throws GameActionException {
        int leadCount = 0;
        Direction dir = Navigation.directions[Robot.rng.nextInt(Navigation.directions.length)];

        for (MapLocation loc : rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared)) {
            if (rc.senseLead(loc) > leadCount) {
                leadCount = rc.senseLead(loc);
                dir = myLoc.directionTo(loc);
            }
        }

        return dir;
    }
}
