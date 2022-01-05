package bot1;

import battlecode.common.*;

public class Watchtower extends Robot {
    enum DefenseMode {
        DISTANCE,
        HEALTH
    }

    static final int DEFENSE_MODE_THRESHOLD = 13;

    DefenseMode mode;

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
}
