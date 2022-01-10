package bot6;

import battlecode.common.*;

import java.util.Map;

public class Watchtower extends Robot {
    enum DefenseMode {
        DISTANCE,
        HEALTH
    }
    DefenseMode mode;

    static final int DEFENSE_MODE_THRESHOLD = 13;

    boolean moved = false;

    public Watchtower(RobotController rc) throws GameActionException {
        super(rc);

        if (util.distanceToClosestFriendlyArchon() < DEFENSE_MODE_THRESHOLD) {
            mode = DefenseMode.DISTANCE;
        } else {
            mode = DefenseMode.HEALTH;
        }
    }

    void playTurn() throws GameActionException {
        super.playTurn();

//        if (rc.getMode() != RobotMode.PROTOTYPE && comms.getRobotCount(RobotType.WATCHTOWER) >= 50) {
//            charge();
//        }

        MapLocation attackLoc = null;
        switch (mode) {
            case DISTANCE:
                attackLoc = util.closestAttackTarget();
                break;
            case HEALTH:
                attackLoc = util.lowestHealthAttackTarget();
                break;
        }

        if (attackLoc != null && rc.canAttack(attackLoc)) {
            rc.attack(attackLoc);
        }
    }

    void charge() throws GameActionException {
        if (rc.getMode() == RobotMode.TURRET && roundNum % 20 != 0) {
            return;
        }

        System.out.println("MODE: " + rc.getMode() + ", ROUND NUM: " + roundNum);
        MapLocation[] possibleLocs = comms.getEnemyArchonLocs();
        MapLocation target = null;

        for (MapLocation loc : possibleLocs) {
            if (loc != null) {
                target = loc;
                break;
            }
        }

        if (target == null) {
            return;
        }

        System.out.println("TARGET: " + target);
        boolean movePossible = false;
        if (rc.getMode() == RobotMode.TURRET && rc.canTransform()) {
            for (Direction dir : Navigation.evenCloserDirections(myLoc.directionTo(target))) {
                if (rc.senseRobotAtLocation(myLoc.add(dir)) == null) {
                    movePossible = true;
                    break;
                }
            }
            if (movePossible) {
                rc.transform();
            }
        } else if (rc.getMode() == RobotMode.PORTABLE) {
            if (!moved) {
                if (rc.isMovementReady()) {
                    nav.moveTowards(target);
                    moved = true; // even if we didn't move, just transform back to turret to not waste time losing hp
                }
            } else if (rc.canTransform()) {
                rc.transform();
                moved = false;
            }
        }
    }
}
