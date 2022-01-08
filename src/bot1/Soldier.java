package bot1;

import battlecode.common.*;

public class Soldier extends Robot {
    enum Mode {
        SCOUT,
        ATTACK,
        DEFEND
    }

    static final int ATTACK_COUNT_THRESHOLD = 4;

    Mode mode;

    boolean attackLeader = true;

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
        MapLocation targetLoc = util.lowestHealthAttackTarget();
        if (targetLoc != null && rc.canAttack(targetLoc)) {
            rc.attack(targetLoc);
        }

        MapLocation enemyArchonLoc = comms.getTargetEnemyArchon();
        if (enemyArchonLoc != null) {
            if (attackLeader && myLoc.distanceSquaredTo(enemyArchonLoc) < myType.visionRadiusSquared) {
                if (rc.senseRobotAtLocation(enemyArchonLoc) == null) {
                    comms.updateDestroyedEnemyArchon(enemyArchonLoc);
                    attackLeader = false;
                }
            } else {
                nav.moveTowards(enemyArchonLoc);
            }
        }

        checkAttackPossible();

        nav.moveAway(parentLoc);
    }

    void defend() throws GameActionException {
        nav.moveRandom();
    }

    void checkAttackPossible() throws GameActionException {
        if (countNearbyAllies() < ATTACK_COUNT_THRESHOLD) {
            return;
        }

        MapLocation[] enemyArchonLocs = comms.getEnemyArchonLocs();
        for (int i = 0; i < 4; ++i) {
            MapLocation archonLoc = enemyArchonLocs[i];
            if (archonLoc != null) {
                attackLeader = true;
                comms.chooseTargetEnemyArchon(i);
            }
        }
    }

    int countNearbyAllies() throws GameActionException {
        int count = 0;
        for (RobotInfo info : rc.senseNearbyRobots(myType.visionRadiusSquared, myTeam)) {
            if (info.type == RobotType.SOLDIER) {
                ++count;
            }
        }

        return count;
    }
}
