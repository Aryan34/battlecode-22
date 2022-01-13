package bot7;

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
            RobotType.MINER,
            RobotType.MINER,
            RobotType.BUILDER,
            RobotType.BUILDER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
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
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER
    };

    static RobotType[] buildOrder5 = {
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


    int buildIndex;

    int enemySoldierSensedRound;
    int weightedThreatCount;
    int totalNearbyLead;
    int initialMinersNeeded;
    boolean enemySoldierSensed;

    int buildersSpawned;
    int minersSpawned;
    int soldiersSpawned;
    int sagesSpawned;

    RobotType[] recentlySpawned = new RobotType[5];

    public Archon(RobotController rc) throws GameActionException {
        super(rc);

        comms.addFriendlyArchonLoc(myLoc);
        comms.addEnemyArchonLoc(nav.reflectHoriz(myLoc));
        comms.addEnemyArchonLoc(nav.reflectVert(myLoc));
        comms.addEnemyArchonLoc(nav.reflectDiag(myLoc));

        buildIndex = 0;

        enemySoldierSensedRound = -10000;
        weightedThreatCount = 0;
        totalNearbyLead = senseTotalNearbyLead();
        enemySoldierSensed = false;

        // TODO: fine-tune these values
        if (totalNearbyLead < 50) {
            initialMinersNeeded = 2;
        } else if (totalNearbyLead < 150) {
            initialMinersNeeded = 3;
        } else if (totalNearbyLead < 350) {
            initialMinersNeeded = 4;
        } else {
            initialMinersNeeded = 5;
        }

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
            initialMinersNeeded -= 1;
        } else {
            enemySoldierSensed = false;
        }

        if (minersSpawned < initialMinersNeeded) {
            followBuildOrder(buildOrder4);
            buildIndex = 0;
        } else if (enemySoldierSensed) {
            followBuildOrder(buildOrder1);
        } else if (roundNum < 150) {
            if (senseTotalNearbyLead() > 200) {
                followBuildOrder(buildOrder4);
            } else if (rc.getTeamLeadAmount(myTeam) < RobotType.SOLDIER.buildCostLead * rc.getArchonCount()) {
                followBuildOrder(buildOrder3);
            } else {
                followBuildOrder(buildOrder1);
            }
        } else {
            if (teamLead > 750 && buildersSpawned < 8) {
                followBuildOrder(buildOrder2);
            } else {
                followBuildOrder(buildOrder5);
            }
        }

        tryRepair();
        // TODO: make this more efficient so archons split the clearing work evenly
        // comms.clearEnemyLocations(36);
    }

    void tryRepair() throws GameActionException {
        MapLocation repairTarget = util.lowestHealthRepairTarget();
        if (rc.canRepair(repairTarget)) {
            rc.repair(repairTarget);
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
            weightedThreatCount += util.attackPriority(info.type);
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

    int senseTotalNearbyLead() throws GameActionException {
        int total = 0;
        for (MapLocation loc : rc.senseNearbyLocationsWithLead(myType.visionRadiusSquared)) {
            total += rc.senseLead(loc);
        }
        return total;
    }
}
