package bot1;

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
        runFromEnemies();

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
                    brownian();
                }
            } else if (turnCount % 5 == 0) {
                brownian();
            } else if (searchTarget == null || myLoc.distanceSquaredTo(searchTarget) < myType.visionRadiusSquared) {
                searchTarget = randomSearchTarget();
            }

            if (searchTarget != null) {
                if (!nav.moveTowards(searchTarget)) {
                    brownian();
                }
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

    MapLocation randomSearchTarget() throws GameActionException {
        int symmetryType = rng.nextInt(3);
        switch (symmetryType) {
            case 0:
                return nav.reflectHoriz(parentLoc);
            case 1:
                return nav.reflectVert(parentLoc);
            case 2:
                return nav.reflectDiag(parentLoc);
        }

        return nav.reflectDiag(parentLoc);
    }

    MapLocation findOptimalDeposit() throws GameActionException {
        MapLocation depositLoc = null;
        int optimalDepositValue = 0;
        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared)) {
            if (rc.senseLead(loc) >= 10) {
                int value = rc.senseLead(loc) - (2 * myLoc.distanceSquaredTo(loc));
                if (value > optimalDepositValue) {
                    optimalDepositValue = value;
                    depositLoc = loc;
                }
            }
        }

        return depositLoc;
    }

    MapLocation findOptimalNeighboringDeposit() throws GameActionException {
        MapLocation depositLoc = null;
        int optimalDepositValue = 1;
        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.actionRadiusSquared)) {
            if (rc.senseLead(loc) >= 10) {
                int value = rc.senseLead(loc);
                if (value > optimalDepositValue) {
                    optimalDepositValue = value;
                    depositLoc = loc;
                }
            }
        }

        return depositLoc;
    }
}
