package bot1;

import battlecode.common.*;

public class Archon extends Robot {
    int buildOrderIndex = 0;

    public Archon(RobotController rc) throws GameActionException {
        super(rc);
        comms.addFriendlyArchonLoc(rc.getLocation());
    }

    static RobotType[] firstBuildOrder = {
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.SOLDIER,
            RobotType.SOLDIER,
            RobotType.MINER,
            RobotType.BUILDER,
            RobotType.BUILDER,
            RobotType.MINER,
            RobotType.BUILDER,
            RobotType.BUILDER,
            RobotType.MINER,
            RobotType.BUILDER,
            RobotType.MINER,
            RobotType.SOLDIER
    };

    void playTurn() throws GameActionException {
        super.playTurn();

        // Pick a direction to build in.
        Direction dir = Navigation.directions[rng.nextInt(Navigation.directions.length)];
        if (roundNum < 20) {
            spawnFirst20Rounds();
        } else {
            if (rng.nextBoolean()) {
                if (rng.nextBoolean()) {
                    rc.setIndicatorString("Trying to build a miner");
                    if (rc.canBuildRobot(RobotType.MINER, dir)) {
                        rc.buildRobot(RobotType.MINER, dir);
                    }
                } else {
                    rc.setIndicatorString("Trying to build a builder");
                    if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                        rc.buildRobot(RobotType.BUILDER, dir);
                    }
                }
            } else {
                rc.setIndicatorString("Trying to build a soldier");
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }
            }
        }
    }

    void spawnFirst20Rounds() throws GameActionException {
        Direction dir = Navigation.directions[rng.nextInt(Navigation.directions.length)];
        if (rc.canBuildRobot(firstBuildOrder[buildOrderIndex], dir)) {
            rc.buildRobot(firstBuildOrder[buildOrderIndex], dir);
            ++buildOrderIndex;
        }
    }
}
