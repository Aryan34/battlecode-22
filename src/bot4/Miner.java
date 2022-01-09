package bot4;

import battlecode.common.*;

public class Miner extends Robot {

    int leadCount;
    MapLocation searchTarget;

    public Miner(RobotController rc) {
        super(rc);
        leadCount = 0;
        searchTarget = null;
    }

    void playTurn() throws GameActionException {
        super.playTurn();
        nav.retreatFromEnemies(rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam));

        // move away from archon to allow it to continue building troops
        if (parentLoc != null && myLoc.distanceSquaredTo(parentLoc) <= 2) {
            nav.moveAwayFromArchon(parentLoc);
        }

        // TODO: possibly research for best tile each time we mine, instead of mining multiple times at the same loc
        MapLocation neighboringDepositLoc = largestNeighboringDeposit(true);
        if (neighboringDepositLoc != null) {
            while ((rc.senseGold(neighboringDepositLoc) > 0 && rc.canMineGold(neighboringDepositLoc)) ||
                    (rc.senseLead(neighboringDepositLoc) > 1 && rc.canMineLead(neighboringDepositLoc))) {
                rc.mineLead(neighboringDepositLoc);
            }
        }

        MapLocation depositLoc = largestDeposit(true);
        if (depositLoc != null) {
            if (!nav.moveTowards(depositLoc)) {
                brownian();
            }
        } else {
            brownian();
        }
    }

    // true: consider gold deposits as well (gold weighted at highestLead * 5); false: only looks for lead deposits
    MapLocation largestDeposit(boolean searchForGold) throws GameActionException {
        MapLocation depositLoc = null;
        int highestGold = 0;
        int highestLead = 0; // TODO: maybe set to -25 to allow for neg. values too?

        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared)) {
            if (rc.senseLead(loc) >= 10) {
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
        int highestLead = 9;

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
}
