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
            if (!findOptimalDeposit()) {
                nav.moveRandom();
            } else {
                comms.addLeadDepositLoc(depositLoc);
            }
        }

        if (myLoc.distanceSquaredTo(depositLoc) > RobotType.MINER.actionRadiusSquared){
            nav.moveTowards(depositLoc);
        } else if (rc.senseLead(depositLoc) == 0) {
            comms.removeLeadDepositLoc(depositLoc);
            depositLoc = null;
            nav.moveRandom();
        } else {
            while (rc.senseLead(depositLoc) > 0 && rc.canMineLead(depositLoc)) {
                rc.mineLead(depositLoc);
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
                    } else if (myLoc.distanceSquaredTo(loc) < myLoc.distanceSquaredTo(depositLoc)){
                        depositLoc = loc;
                    }
                }
            }
        }

        return depositLoc != null;
    }
}
