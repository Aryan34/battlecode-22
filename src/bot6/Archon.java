package bot6;

import battlecode.common.*;

public class Archon extends Robot {
    static RobotType[] buildOrder1 = {
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.SOLDIER
    };

    static RobotType[] buildOrder2 = {
            RobotType.BUILDER,
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
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
    };

    static RobotType[] buildOrder4 = {
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER
    };

    static RobotType[] buildOrder5 = {
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.BUILDER,
            RobotType.BUILDER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
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
    int sagesSpawned;

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
        sagesSpawned = 0;
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        weightedThreatCount = countThreatsWeighted();
        if (weightedThreatCount > 0) {
            enemySoldierSensed = true;
            enemySoldierSensedRound = roundNum;
        } else {
            enemySoldierSensed = false;
        }

        if (enemySoldierSensed) {
            followBuildOrder(buildOrder1);
        } else if (comms.getRobotCount(RobotType.MINER) < 12) {
            followBuildOrder(buildOrder4);
        } else if (roundNum < 150 && rc.getTeamLeadAmount(myTeam) < 500) {
            followBuildOrder(buildOrder3);
        } else {
            if (teamLead > 750 && buildersSpawned < 8) {
                followBuildOrder(buildOrder2);
            } else {
                followBuildOrder(buildOrder5);
            }
        }
    }

    void followBuildOrder(RobotType[] buildOrder) throws GameActionException {
        if (comms.getArchonId(myLoc) == comms.getBuildMutex() || roundNum > 200) {
            if (tryBuild(buildOrder[buildIndex % 10])) {
                ++buildIndex;
                comms.updateBuildMutex();
            }
        }
    }

    boolean tryBuild(RobotType type) throws GameActionException {
        Direction dir = Navigation.directions[Robot.rng.nextInt(Navigation.directions.length)];
        if (type == RobotType.MINER) {
            dir = findBestLeadDeposit();
        }

        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            updateRecentlySpawned(type);
            switch (type) {
                case BUILDER:
                    ++buildersSpawned;
                case MINER:
                    ++minersSpawned;
                case SOLDIER:
                    ++soldiersSpawned;
                case SAGE:
                    ++sagesSpawned;
            }
            return true;
        } else {
            for (Direction closeDir : Navigation.closeDirections(dir)) {
                if (rc.canBuildRobot(type, closeDir)) {
                    rc.buildRobot(type, closeDir);
                    updateRecentlySpawned(type);
                    switch (type) {
                        case BUILDER:
                            ++buildersSpawned;
                        case MINER:
                            ++minersSpawned;
                        case SOLDIER:
                            ++soldiersSpawned;
                        case SAGE:
                            ++sagesSpawned;
                    }
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
                    weightedThreatCount += 7;
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
