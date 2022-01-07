package bot2;

import battlecode.common.*;

public class Miner extends Robot {
    MapLocation depositLoc = null;

    int leadCount;

    public Miner(RobotController rc) {
        super(rc);
        leadCount = 0;
    }

    void playTurn() throws GameActionException {
        MapLocation me = rc.getLocation();
        int count = rc.senseLead(me);
        if (count != 0) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                    while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
                        rc.mineGold(mineLocation);
                    }
                    while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                        rc.mineLead(mineLocation);
                    }
                }
            }
        } else {
            rc.disintegrate();
        }

        if (count < 2) {
            Direction dir2 = Navigation.directions[rng.nextInt(Navigation.directions.length)];
            if (rc.canMove(dir2)) {
                rc.move(dir2);
                System.out.println("I moved!");
            }
        }
    }

    boolean findOptimalDeposit() throws GameActionException {
        int optimalDepositValue = 0;
        for (MapLocation loc : rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared)) {
            int value = rc.senseLead(loc) - (2 * myLoc.distanceSquaredTo(loc));
            if (value > optimalDepositValue) {
                optimalDepositValue = value;
                depositLoc = loc;
            }
        }

        if (optimalDepositValue <= 1) {
            MapLocation[] depositLocs = comms.getLeadDepositLocs();
            for (MapLocation loc : depositLocs) {
                if (loc != null) {
                    if (depositLoc == null) {
                        depositLoc = loc;
                    } else if (myLoc.distanceSquaredTo(loc) < myLoc.distanceSquaredTo(depositLoc)) {
                        depositLoc = loc;
                    }
                }
            }
        }

        return depositLoc != null;
    }
}
