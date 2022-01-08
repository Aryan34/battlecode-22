package bot2;

import battlecode.common.*;

public class Builder extends Robot {
    enum Mode {
        BUILD,
        BUILD_LATTICE,
        REPAIR,
        MOVE
    }

    Mode mode;

    int labsBuilt = 0;
    boolean dontMove = false;
    boolean buildLattice = false;

    MapLocation repairTarget;

    public Builder(RobotController rc) {
        super(rc);
        mode = Mode.BUILD_LATTICE;
    }

    void playTurn() throws GameActionException {
        super.playTurn();
        runFromEnemies();

        switch (mode) {
            case BUILD_LATTICE:
                buildLattice();
                break;
            case REPAIR:
                repair();
                break;
            default:
                brownian();
        }
    }

    void repair() throws GameActionException {
        RobotInfo repairTargetInfo = rc.senseRobotAtLocation(repairTarget);
        if (repairTarget != null && rc.canRepair(repairTarget)) {
            if (repairTargetInfo.health != repairTargetInfo.type.health) {
                rc.repair(repairTarget);
            } else {
                repairTarget = null;
                mode = Mode.BUILD_LATTICE;
            }
        } else if (repairTargetInfo.health == repairTargetInfo.type.health) {
            mode = Mode.BUILD_LATTICE;
            repairTarget = null;
            brownian();
        }
    }

    void buildLattice() throws GameActionException {
        if (isLatticeTile(myLoc)) {
            nav.moveRandomCardinal();
        }
        if (!isLatticeTile(myLoc)) {
            for (Direction dir : Navigation.cardinalDirections) {
                MapLocation watchtowerLoc = myLoc.add(dir);
                if (watchtowerLoc.distanceSquaredTo(parentLoc) > 2 && rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                    if (isLatticeTile(watchtowerLoc)) {
                        rc.buildRobot(RobotType.WATCHTOWER, dir);
                        mode = Mode.REPAIR;
                        repairTarget = watchtowerLoc;
                        break;
                    }
                }
            }
        }

        if (labsBuilt == 0 && roundNum > 300 && rc.getID() % 8 == 0 && teamLead > (RobotType.LABORATORY.buildCostLead * 1.5)) {
            for (Direction dir : Navigation.directions) {
                MapLocation labLoc = myLoc.add(dir);
                if (!isLatticeTile(labLoc) && rc.canBuildRobot(RobotType.LABORATORY, dir)) {
                    rc.buildRobot(RobotType.LABORATORY, dir);
                    mode = Mode.REPAIR;
                    repairTarget = labLoc;
                    ++labsBuilt;
                    break;
                }
            }
        }

        for (RobotInfo info : rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam)) {
            if (info.type == RobotType.WATCHTOWER && rc.canMutate(info.location)) {
                if (info.level == 1 && teamLead > (RobotType.WATCHTOWER.getLeadMutateCost(2) * 2) ||
                        info.level == 2 && rc.getTeamGoldAmount(myTeam) > (RobotType.WATCHTOWER.getGoldMutateCost(3) * 2)) {
                    rc.mutate(info.location);
                }
            }
        }
        brownian();
    }

    boolean isLatticeTile(MapLocation loc) {
        return (loc.x - parentLoc.x) % LATTICE_MOD == 0 && (loc.y - parentLoc.y) % LATTICE_MOD == 0;
    }
}
