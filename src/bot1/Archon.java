package bot1;

import battlecode.common.*;

public class Archon extends Building {
    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        comms.addFriendlyArchonLoc(rc.getLocation());
    }

    void playTurn() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];

        if (roundNum < 100) {
            if (rng.nextBoolean()) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a miner");
                if (rc.canBuildRobot(RobotType.MINER, dir)) {
                    rc.buildRobot(RobotType.MINER, dir);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        } else if (roundNum < 120) {
            rc.setIndicatorString("Trying to build a builder");
            if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                rc.buildRobot(RobotType.BUILDER, dir);
            }
        } else if (roundNum < 180) {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        } else if (rc.getTeamLeadAmount(rc.getTeam()) > 1000) {
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        } else if (rc.getTeamGoldAmount(rc.getTeam()) > RobotType.SAGE.buildCostGold){
            rc.setIndicatorString("Trying to build a sage" + roundNum);
            if (rc.canBuildRobot(RobotType.SAGE, dir)) {
                rc.buildRobot(RobotType.SAGE, dir);
            }
        } else {
            MapLocation repairLoc = util.optimalRepairTarget();
            if (rc.canRepair(repairLoc)) {
                rc.repair(repairLoc);
            }
        }
    }
}
