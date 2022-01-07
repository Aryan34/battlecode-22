package bot2;

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

        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
    }
}
