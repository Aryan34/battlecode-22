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
            RobotType.SOLDIER,
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
    int watchtowersSpawned;

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
        watchtowersSpawned = 0;
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        RobotInfo[] robs = rc.senseNearbyRobots();
        for (RobotInfo rob : robs) {
            if (rob.type == RobotType.WATCHTOWER) {
                watchtowersSpawned++;
            }
        }

        if (roundNum <= 1) {
            rc.writeSharedArray(10, rc.getLocation().x);
            rc.writeSharedArray(11, rc.getLocation().y);
        }

        if (roundNum < 50) {
            followBuildOrder(buildOrder4); // mostly miners
        } else if (roundNum < 100) {
            followBuildOrder(buildOrder3); // half miners, half soldiers
        } else if (rc.getTeamLeadAmount(rc.getTeam()) > 2000 && watchtowersSpawned < 4) {
            followBuildOrder(buildOrder2); // half builders, rest split between miners and soldiers
        } else if (roundNum < 200) {
            followBuildOrder(buildOrder1); // mostly soldiers
        } else if (rc.getTeamLeadAmount(rc.getTeam()) < 5000) {
            followBuildOrder(buildOrder4); // mostly miners
        } else {
            if (rng.nextBoolean() && roundNum < 1800) {
                // Let's try to build a sage.
                rc.setIndicatorString("Trying to build a sage");
                tryBuild(RobotType.SAGE);
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                tryBuild(RobotType.SOLDIER);
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
