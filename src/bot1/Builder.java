package bot1;

import battlecode.common.*;

public class Builder extends Robot {
    enum Mode {
        BUILD,
        BUILD_LATTICE,
        REPAIR,
        MOVE
    }

    Mode mode;

    boolean dontMove = false;
    boolean buildLattice = false;

    public Builder(RobotController rc) {
        super(rc);
        mode = Mode.MOVE;
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        for (RobotInfo robotInfo : rc.senseNearbyRobots(RobotType.BUILDER.actionRadiusSquared, myTeam)) {
            if (robotInfo.type == RobotType.ARCHON) {
                buildLattice = true;
            }
            if (robotInfo.type == RobotType.WATCHTOWER || robotInfo.type == RobotType.LABORATORY) {
                if (robotInfo.health < robotInfo.type.health && rc.canRepair(robotInfo.location)) {
                    rc.repair(robotInfo.location);
                    dontMove = true;
                }
                if (robotInfo.level < 3 && rc.canMutate(robotInfo.location)) {
                    rc.mutate(robotInfo.location);
                    dontMove = true;
                }
            }
        }

        if (buildLattice) {
            for (int i = 0; i < Navigation.directions.length; i++) {
                Direction dir = Navigation.directions[i];
                if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                    if ((myLoc.x + dir.dx) % 2 == (myLoc.y + dir.dy) % 2) {
                        rc.buildRobot(RobotType.WATCHTOWER, dir);
                        comms.updateRobotCount(RobotType.WATCHTOWER, 1);
                        break;
                    } else if (comms.getRobotCount(RobotType.LABORATORY) == 0) {
                        rc.buildRobot(RobotType.LABORATORY, dir);
                        comms.updateRobotCount(RobotType.LABORATORY, 1);
                        break;
                    }
                }
            }
        }

        if (!dontMove) {
            nav.moveRandom();
        }
    }
}
