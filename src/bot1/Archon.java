package bot1;

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

        weightedThreatCount = countThreatsWeighted();
        if (weightedThreatCount > 0) {
            enemySoldierSensed = true;
            enemySoldierSensedRound = roundNum;
        } else {
            enemySoldierSensed = false;
        }

        if (roundNum == 100) {
            if (comms.getArchonCount() != rc.getArchonCount()) {
                rc.resign();
            }
        }

        if (enemySoldierSensed) {
//            rc.setIndicatorString("Case: Enemy");
            followBuildOrder(buildOrder1);
        } else if (comms.getRobotCount(RobotType.MINER)<20) {
            followBuildOrder(buildOrder4);
        } else if (comms.getRobotCount(RobotType.BUILDER)<4) {
            followBuildOrder(buildOrder2);
        } else if (buildIndex < 200) {
//            rc.setIndicatorString("Case: 200");
            followBuildOrder(buildOrder3);
        } else if (buildIndex < 300) {
//            rc.setIndicatorString("Case: 300");
            followBuildOrder(buildOrder1);
        } else if (buildIndex < 400) {
//            rc.setIndicatorString("Case: 400");
            if (buildersSpawned < 5) {
//                if (util.tryBuildRandom(RobotType.BUILDER)) {
//                    ++buildersSpawned;
//                }
                buildersSpawned++;
            } else {
                followBuildOrder(buildOrder3);
            }
        } else {
//            rc.setIndicatorString("Case: ELSE");
            if (rc.getTeamGoldAmount(myTeam) > RobotType.SAGE.buildCostGold * 1.5) {
                util.tryBuildRandom(RobotType.SAGE);
            } else {
                followBuildOrder(buildOrder1);
            }
        }
    }

    void followBuildOrder(RobotType[] buildOrder) throws GameActionException {
        rc.setIndicatorString("ID: " + comms.getArchonId(myLoc) + ", Mutex: " + comms.getBuildMutex());
        if (comms.getArchonId(myLoc) == comms.getBuildMutex()) {
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
