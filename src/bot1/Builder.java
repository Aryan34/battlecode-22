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

    MapLocation parentArchonLoc;
    MapLocation repairTarget;

    public Builder(RobotController rc) {
        super(rc);
        mode = Mode.BUILD_LATTICE;
        for (RobotInfo info : rc.senseNearbyRobots(2, myTeam)) {
            if (info.type == RobotType.ARCHON) {
                parentArchonLoc = info.location;
                break;
            }
        }
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        switch (mode) {
            case BUILD_LATTICE:
                buildLattice();
                break;
            case REPAIR:
                repair();
                break;
            default:
                nav.moveRandom();
        }
    }

    void repair() throws GameActionException {
        if (repairTarget != null && rc.canRepair(repairTarget)) {
            rc.repair(repairTarget);
        }

        RobotInfo info = rc.senseRobotAtLocation(repairTarget);
        if (info.health == info.type.health) {
            mode = Mode.BUILD_LATTICE;
            repairTarget = null;
        }
    }

    void buildLattice() throws GameActionException {
        if ((myLoc.x - parentArchonLoc.x) % LATTICE_MOD == 0 && (myLoc.y - parentArchonLoc.y) % LATTICE_MOD == 0) {
            nav.moveRandomCardinal();
        }
        if ((myLoc.x - parentArchonLoc.x) % LATTICE_MOD != 0 || (myLoc.y - parentArchonLoc.y) % LATTICE_MOD != 0) {
            for (Direction dir : Navigation.cardinalDirections) {
                if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                    rc.buildRobot(RobotType.WATCHTOWER, dir);
                    
                }
            }
        }
    }
}
