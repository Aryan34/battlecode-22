package bot8;

import battlecode.common.*;

public class Miner extends Robot {

    int leadCount;
    MapLocation searchTarget;

    public Miner(RobotController rc) {
        super(rc);
        leadCount = 0;
        searchTarget = null;
    }

    // TODO: as you get further from the parentLoc, explore less aggressively
    void playTurn() throws GameActionException {
        super.playTurn();

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam);
        for (RobotInfo info : nearbyEnemies) {
            if (info.type == RobotType.SAGE || info.type == RobotType.SOLDIER || info.type == RobotType.WATCHTOWER) {
                nav.retreatFromEnemies(nearbyEnemies);
                rc.setIndicatorString("RETREAT");
                break;
            }
        }

        // TODO: possibly research for best tile each time we mine, instead of mining multiple times at the same loc
        MapLocation neighboringDepositLoc = largestNeighboringDeposit(true);
        if (neighboringDepositLoc != null) {
            while (rc.senseGold(neighboringDepositLoc) > 0 && rc.canMineGold(neighboringDepositLoc)) {
                rc.mineGold(neighboringDepositLoc);
            }
            while (rc.senseLead(neighboringDepositLoc) > 1 && rc.canMineLead(neighboringDepositLoc)) {
                rc.mineLead(neighboringDepositLoc);
            }
        }

        // TODO: in move towards, favor directions that have better neighboring deposits
        MapLocation depositLoc = largestDeposit(true);
        if (depositLoc != null) {
            nav.moveTowards(depositLoc);
            rc.setIndicatorString("1: " + depositLoc);
        } else if (searchTarget == null || myLoc.distanceSquaredTo(searchTarget) < myType.visionRadiusSquared) {
            searchTarget = randomSearchTarget();
            rc.setIndicatorString("2: " + searchTarget);
        } else {
            rc.setIndicatorString("3: " + searchTarget);
            nav.moveTowards(searchTarget);
        }
    }

    // true: consider gold deposits as well (gold weighted at highestLead * 5); false: only looks for lead deposits
    MapLocation largestDeposit(boolean searchForGold) throws GameActionException {
        MapLocation depositLoc = null;
        int highestGold = 0;
        int highestLead = 0; // TODO: maybe set to -25 to allow for neg. values too?

        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared)) {
            if (rc.senseLead(loc) >= 2) {
                int value = rc.senseLead(loc) - myLoc.distanceSquaredTo(loc);
                if (value > highestLead) {
                    highestLead = value;
                    depositLoc = loc;
                }
            }
        }

        if (searchForGold) {
            highestGold = highestLead * 5; // TODO: once we get better lab code, find a better constant than 5
            for (MapLocation loc : rc.senseNearbyLocationsWithGold(RobotType.MINER.visionRadiusSquared)) {
                int value = rc.senseGold(loc) - myLoc.distanceSquaredTo(loc);
                if (value > highestGold) {
                    highestGold = value;
                    depositLoc = loc;
                }
            }
        }

        return depositLoc;
    }

    // true: if gold nearby, mine that first; false: ignore gold, only mine lead
    MapLocation largestNeighboringDeposit(boolean searchForGold) throws GameActionException {
        MapLocation depositLoc = null;
        int highestGold = 0;
        int highestLead = 1;

        if (searchForGold) {
            for (MapLocation loc : rc.senseNearbyLocationsWithGold(myType.actionRadiusSquared)) {
                int value = rc.senseLead(loc);
                if (value > highestGold) {
                    highestGold = value;
                    depositLoc = loc;
                }
            }
        }

        if (depositLoc != null) {
            return depositLoc;
        }

        for (MapLocation loc : rc.senseNearbyLocationsWithLead(myType.actionRadiusSquared)) {
            int value = rc.senseLead(loc);
            if (value > highestLead) {
                highestLead = value;
                depositLoc = loc;
            }
        }

        return depositLoc;
    }

    // TODO: get better miner exploration code
    MapLocation randomSearchTarget() throws GameActionException {
        int randX = rng.nextInt(rc.getMapWidth());
        int randY = rng.nextInt(rc.getMapHeight());
        return new MapLocation(randX, randY);
    }
}
