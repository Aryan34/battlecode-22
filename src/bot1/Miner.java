package bot1;

import battlecode.common.*;

public class Miner extends Robot {

    int leadCount;

    public Miner(RobotController rc) {
        super(rc);
        leadCount = 0;
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        rc.setIndicatorString("Action: " + numRoundsNoActions + ", Move: " + numRoundsNoMove);
        MapLocation neighboringDepositLoc = findOptimalNeighboringDeposit();
        if (neighboringDepositLoc != null) {
            while (rc.senseLead(neighboringDepositLoc) > 1 && rc.canMineLead(neighboringDepositLoc)) {
                rc.mineLead(neighboringDepositLoc);
            }
        } else {
            MapLocation depositLoc = findOptimalDeposit();
            if (depositLoc != null) {
                if (!nav.moveTowards(depositLoc)) {
                    nav.moveAway(parentLoc);
                }
            } else {
                nav.moveAway(parentLoc);
            }
        }

//        disintegrate if miner gets stuck
//        if (numRoundsNoActions >= INACTION_TURNS_THRESHOLD && numRoundsNoMove >= INACTION_TURNS_THRESHOLD) {
//            rc.disintegrate();
//        }
//
//        if (turnCount > 25) {
//            updateInaction();
//        }
    }

    MapLocation findOptimalDeposit() throws GameActionException {
        MapLocation depositLoc = null;
        int optimalDepositValue = 0;
        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared)) {
            int value = rc.senseLead(loc) - (2 * myLoc.distanceSquaredTo(loc));
            if (value > optimalDepositValue) {
                optimalDepositValue = value;
                depositLoc = loc;
            }
        }

        return depositLoc;
    }

    MapLocation findOptimalNeighboringDeposit() throws GameActionException {
        MapLocation depositLoc = null;
        int optimalDepositValue = 1;
        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.actionRadiusSquared)) {
            int value = rc.senseLead(loc);
            if (value > optimalDepositValue) {
                optimalDepositValue = value;
                depositLoc = loc;
            }
        }

        return depositLoc;
    }
}
