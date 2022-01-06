package bot1;

import battlecode.common.*;

public class Miner extends Robot {
    MapLocation depositLoc = null;

    int leadCount;

    public Miner(RobotController rc) {
        super(rc);
        leadCount = 0;
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        if (depositLoc == null) {
            findDeposit();
            if (depositLoc == null) {
                nav.moveRandom();
            }
        } else if (myLoc.distanceSquaredTo(depositLoc) > RobotType.MINER.actionRadiusSquared){
            nav.moveTowards(depositLoc);
        } else if (rc.senseLead(depositLoc) == 0) {
            depositLoc = null;
            nav.moveRandom();
        } else {
            while (rc.senseLead(depositLoc) > 0 && rc.canMineLead(depositLoc)) {
                rc.mineLead(depositLoc);
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
