package bot1;

import battlecode.common.*;

public class Miner extends Droid {
    MapLocation depositLoc = null;

    public Miner(RobotController rc) {
        super(rc);
    }

    void playTurn() throws GameActionException {
        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }

    boolean findDeposit() throws GameActionException {
        int largestDeposit = 0;
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            if (rc.senseLead(loc) > largestDeposit) {
                largestDeposit = rc.senseLead(loc);
                depositLoc = loc;
                return true;
            }
        }

        return false;
    }
}
