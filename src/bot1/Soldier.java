package bot1;

import battlecode.common.*;

public class Soldier extends Robot {
    enum Mode {
        SCOUT,
        ATTACK,
        DEFEND
    }

    Mode mode;

    public Soldier(RobotController rc) {
        super(rc);
        mode = Mode.ATTACK;
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        switch (mode) {
            case SCOUT:
                scout();
                break;
            case ATTACK:
                attack();
                break;
            case DEFEND:
                defend();
                break;
        }
    }

    void scout() throws GameActionException {
        nav.moveRandom();

        // still have more enemy archons to find
        if (comms.getDetectedEnemyArchonCount() < comms.getArchonCount()) {
            for (RobotInfo info : rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam)) {
                if (info.type == RobotType.ARCHON) {
                    comms.addEnemyArchonLoc(info.location);
                }
            }
        } else { // detected all enemy archons, attack mode now
            mode = Mode.ATTACK;
        }
    }

    void attack() throws GameActionException {
        MapLocation attackLoc = util.lowestHealthAttackTarget();
        if (rc.canAttack(attackLoc)) {
            rc.attack(attackLoc);
        }
        nav.moveRandom();
    }

    void defend() {

    }
}
