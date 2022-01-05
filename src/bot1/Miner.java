package bot1;

import battlecode.common.*;

public class Miner extends Droid {
    MapLocation depositLoc = null;

    public Miner(RobotController rc) {
        super(rc);
    }

    void playTurn() throws GameActionException {
        if (depositLoc == null) {
            findDeposit();
        } else if (myLoc.distanceSquaredTo(depositLoc) > myType.actionRadiusSquared) {
            nav.moveTowards(depositLoc);
        } else {
            MapLocation me = rc.getLocation();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                    while (rc.canMineGold(mineLocation)) {
                        rc.mineGold(mineLocation);
                    }
                    while (rc.canMineLead(mineLocation)) {
                        rc.mineLead(mineLocation);
                    }
                }
            }
        }
    }

    boolean findDeposit() throws GameActionException {
        int largestDeposit = 0;
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(myLoc, myType.visionRadiusSquared)) {
            if (rc.senseLead(loc) > largestDeposit) {
                largestDeposit = rc.senseLead(loc);
                depositLoc = loc;
                return true;
            }
        }

        return false;
    }
}
