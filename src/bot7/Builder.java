package bot7;

import battlecode.common.*;

public class Builder extends Robot {
    enum Mode {
        BUILD,
        BUILD_LATTICE,
        REPAIR,
        MOVE
    }
    Mode mode;

    MapLocation repairTarget;

    public Builder(RobotController rc) {
        super(rc);
        mode = Mode.BUILD_LATTICE;
    }

    void playTurn() throws GameActionException {
        super.playTurn();
        nav.retreatFromEnemies(rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam));
        myLoc = rc.getLocation();

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
        if (repairTarget == null || !rc.canSenseLocation(repairTarget)) {
            return;
        }

        RobotInfo repairTargetInfo = rc.senseRobotAtLocation(repairTarget);
        if (repairTargetInfo == null) {
            return;
        }

        if (rc.canRepair(repairTarget)) {
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
            myLoc = rc.getLocation();
        }

        if (!isLatticeTile(myLoc)) {
            for (Direction dir : Navigation.directions) {
                MapLocation watchtowerLoc = myLoc.add(dir);
                if (isLatticeTile(watchtowerLoc)) {
                    if (watchtowerLoc.distanceSquaredTo(parentLoc) > 2 && rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                        rc.buildRobot(RobotType.WATCHTOWER, dir);
                        comms.updateRobotCount(RobotType.WATCHTOWER, 1);
                        mode = Mode.REPAIR;
                        repairTarget = watchtowerLoc;
                        break;
                    }
                }
            }
        }

        if (comms.getRobotCount(RobotType.LABORATORY) == 0 && roundNum > 300 && teamLead > RobotType.LABORATORY.buildCostLead) {
            for (Direction dir : Navigation.directions) {
                MapLocation labLoc = myLoc.add(dir);
                if (!isLatticeTile(labLoc) && labLoc.distanceSquaredTo(parentLoc) > 2 && rc.canBuildRobot(RobotType.LABORATORY, dir)) {
                    rc.buildRobot(RobotType.LABORATORY, dir);
                    comms.updateRobotCount(RobotType.LABORATORY, 1);
                    mode = Mode.REPAIR;
                    repairTarget = labLoc;
                    break;
                }
            }
        }

        for (RobotInfo info : rc.senseNearbyRobots(myType.actionRadiusSquared, myTeam)) {
            if (info.type == RobotType.WATCHTOWER && rc.canMutate(info.location)) {
                if (info.level == 1 && teamLead > (RobotType.WATCHTOWER.getLeadMutateCost(2) * 2) ||
                        info.level == 2 && rc.getTeamGoldAmount(myTeam) > (RobotType.WATCHTOWER.getGoldMutateCost(3))) {
                    rc.mutate(info.location);
                }
            }
        }

        if (mode != Mode.REPAIR) {
            brownian();
        }
    }

    boolean isLatticeTile(MapLocation loc) {
        return (loc.x - parentLoc.x) % LATTICE_MOD == 0 && (loc.y - parentLoc.y) % LATTICE_MOD == 0;
    }
}
