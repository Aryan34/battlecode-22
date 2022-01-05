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

        leadCount = rc.senseLead(myLoc);
        if (leadCount != 0){
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation mineLocation = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                    while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation)>1) {
                        rc.mineGold(mineLocation);
                    }
                    while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation)>1) {
                        rc.mineLead(mineLocation);
                    }
                }
            }
        }

        if (leadCount < 2){
            nav.moveRandom();
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
