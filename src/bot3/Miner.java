package bot3;

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
        nav.runFromEnemies();

        // move away from archon to allow it to continue building troops
        if (2 < turnCount && turnCount < 20 && parentLoc != null && myLoc.distanceSquaredTo(parentLoc) <= 2) {
            nav.moveAway(parentLoc);
        }

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
    }

    MapLocation randomSearchTarget() throws GameActionException {
        switch (rc.getID() % 3) {
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
        int optimalDepositValue = -100000;
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
