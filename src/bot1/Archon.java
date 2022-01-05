package bot1;

import battlecode.common.*;

public class Archon extends Robot {
    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        comms.addFriendlyArchonLoc(rc.getLocation());
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        // Pick a direction to build in.
        Direction dir = Navigation.directions[rng.nextInt(Navigation.directions.length)];
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
        } else if (roundNum < 160) {
            rc.setIndicatorString("Trying to build a builder");
            if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                rc.buildRobot(RobotType.BUILDER, dir);
            }
        } else if (roundNum < 200) {
            // Let's try to build a soldier.
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        } else if (rc.getTeamLeadAmount(rc.getTeam()) < 5000) {
            // Let's try to build a miner.
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        } else {
            if (rng.nextBoolean() && roundNum < 1800) {
                // Let's try to build a miner.
                rc.setIndicatorString("Trying to build a sage");
                if (rc.canBuildRobot(RobotType.SAGE, dir)) {
                    rc.buildRobot(RobotType.SAGE, dir);
                }
            } else {
                // Let's try to build a soldier.
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        }
    }
}
