package bot4;

import battlecode.common.*;

import java.util.Random;
// move away from HQ code removed from this file
// for run away, we now run away if we can sense enemy, not only if we r within their action radius
public class Robot {
    static RobotController rc;
    static Communications comms;
    static Navigation nav;
    static Util util;

    static int low_x_bound;
    static int low_y_bound;
    static int high_x_bound;
    static int high_y_bound;

    static int roundNum;
    static int turnCount;
    boolean considerDead;
    boolean enemyArchonCountFlag;

    static final Random rng = new Random(6147);
    static final int LATTICE_MOD = 2;
    static final int INACTION_TURNS_THRESHOLD = 10;

    int teamLead;

    MapLocation myLoc;
    MapLocation parentLoc;
    RobotType myType;
    Team myTeam, opponentTeam;

    public Robot(RobotController rc) {
        Robot.rc = rc;
        comms = new Communications(rc);
        nav = new Navigation(rc);
        util = new Util(rc, this);

        roundNum = rc.getRoundNum();
        turnCount = 0;
        considerDead = false;
        enemyArchonCountFlag = false;

        teamLead = 0;

        myLoc = rc.getLocation();
        myType = rc.getType();
        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();

        low_x_bound = myLoc.x;
        low_y_bound = myLoc.y;
        high_x_bound = myLoc.x;
        high_y_bound = myLoc.y;

        if (myType != RobotType.ARCHON) {
            for (RobotInfo info : rc.senseNearbyRobots(2, myTeam)) {
                if (info.type == RobotType.ARCHON) {
                    parentLoc = info.location;
                    break;
                }
            }
        }
    }

    void playTurn() throws GameActionException {
        roundNum = rc.getRoundNum();
        myLoc = rc.getLocation();
        teamLead = rc.getTeamLeadAmount(myTeam);

        if (rc.canSenseLocation(new MapLocation(low_x_bound - 1, myLoc.y))) {
            low_x_bound--;
        }
        if (rc.canSenseLocation(new MapLocation(high_x_bound + 1, myLoc.y))) {
            high_x_bound++;
        }
        if (rc.canSenseLocation(new MapLocation(myLoc.x, low_y_bound - 1))) {
            low_y_bound--;
        }
        if (rc.canSenseLocation(new MapLocation(myLoc.x, high_y_bound + 1))) {
            high_y_bound++;
        }

        if (turnCount == 0) {
            comms.updateRobotCount(myType, 1);
        }
        if (!considerDead && rc.getHealth() < 6) {
            considerDead = true;
            comms.updateRobotCount(myType, -1);
        } else if (considerDead && rc.getHealth() >= 6) {
            considerDead = false;
            comms.updateRobotCount(myType, 1);
        }

        if (!enemyArchonCountFlag) {
            if (comms.getDetectedEnemyArchonCount() == rc.getArchonCount()) {
                enemyArchonCountFlag = true;
            } else if (comms.getDetectedEnemyArchonCount() < rc.getArchonCount()) {
                for (RobotInfo info : rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam)) {
                    if (info.type == RobotType.ARCHON) {
                        comms.addEnemyArchonLoc(info.location);
                    }
                }
            }
        }

        ++turnCount;
    }

    boolean brownian() throws GameActionException {
        if (!rc.isMovementReady()) {
            return false;
        }

        double force_dx = 0;
        double force_dy = 0;
        double momentum_dx = 0;
        double momentum_dy = 0;

        int x = myLoc.x;
        int y = myLoc.y;

        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            MapLocation repel_loc = info.location;
            force_dx -= (double) (repel_loc.x - myLoc.x) / myLoc.distanceSquaredTo(repel_loc);
            force_dy -= (double) (repel_loc.y - myLoc.y) / myLoc.distanceSquaredTo(repel_loc);
        }

        if ((low_x_bound - x - 1) * (low_x_bound - x - 1) <= 30) {
            force_dx -= (double) (2 * low_x_bound - 2 * x - 2) / (low_x_bound - x - 1) / (low_x_bound - x - 1);
        }
        if ((high_x_bound - x + 1) * (high_x_bound - x + 1) <= 30) {
            force_dx -= (double) (2 * high_x_bound - 2 * x + 2) / (high_x_bound - x + 1) / (high_x_bound - x + 1);
        }
        if ((low_y_bound - y - 1) * (low_y_bound - y - 1) <= 30) {
            force_dy -= (double) (2 * low_y_bound - 2 * y - 2) / (low_y_bound - y - 1) / (low_y_bound - y - 1);
        }
        if ((high_y_bound - y + 1) * (high_y_bound - y + 1) <= 30) {
            force_dy -= (double) (2 * high_y_bound - 2 * y + 2) / (high_y_bound - y + 1) / (high_y_bound - y + 1);
        }

        double r = Math.sqrt(force_dx * force_dx + force_dy * force_dy);
        if (r > 1e-10) {
            force_dx = force_dx / r;
            force_dy = force_dy / r;
        }

        momentum_dx += force_dx;
        momentum_dy += force_dy;
        r = Math.sqrt(momentum_dx * momentum_dx + momentum_dy * momentum_dy);
        if (r > 2) {
            momentum_dx = momentum_dx / r * 2;
            momentum_dy = momentum_dy / r * 2;
        }

        MapLocation target = myLoc.translate((int) (1000 * momentum_dx), (int) (1000 * momentum_dy));
        return nav.greedy(target);
    }
}
