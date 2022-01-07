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

        // Try to attack someone
        MapLocation me = rc.getLocation();
        boolean cont = false;
        boolean dontmove = false;
        RobotInfo[] li = rc.senseNearbyRobots();
        int count = 0;
        boolean maketower = true;
        for (int a = 0; a < li.length; a++) {
            if (li[a].type == RobotType.ARCHON) {
                cont = true;
            }
            if (li[a].type == RobotType.WATCHTOWER || li[a].type == RobotType.LABORATORY) {
                if (li[a].health < li[a].type.health && rc.canRepair(li[a].location)) {
                    rc.repair(li[a].location);
                    dontmove = true;
                }

                if (li[a].level < 3 && rc.canMutate(li[a].location)) {
                    rc.mutate(li[a].location);
                    dontmove = true;
                }
            }
            if (rc.getTeam() == li[a].team && !(li[a].type == RobotType.WATCHTOWER || li[a].type == RobotType.LABORATORY)) {
                count++;
            }
            if (li[a].type == RobotType.WATCHTOWER && rc.getTeam() == li[a].team) {
                maketower = false;
            }
        }

        if (cont) {
            for (int i = 0; i < Navigation.directions.length; i++) {
                Direction dir = Navigation.directions[i];
                MapLocation ml = new MapLocation(me.x + dir.dx, me.y + dir.dy);

                if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                    if ((me.x + dir.dx) % 2 == (me.y + dir.dy) % 2) {
                        rc.buildRobot(RobotType.WATCHTOWER, dir);
                        break;
                    } else if (rc.readSharedArray(0) == 0) {
                        rc.writeSharedArray(0, 1);
                        rc.buildRobot(RobotType.LABORATORY, dir);
                        break;
                    }
                }
            }
        } else if (count > 3 && maketower) {
            Direction dirt = Navigation.directions[rng.nextInt(Navigation.directions.length)];
            if (rc.canBuildRobot(RobotType.WATCHTOWER, dirt)) {
                rc.buildRobot(RobotType.WATCHTOWER, dirt);
            }
        }

        Direction dir2 = Navigation.directions[rng.nextInt(Navigation.directions.length)];
        if (!dontmove && rc.canMove(dir2)) {
            rc.move(dir2);
            System.out.println("I moved!");
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
