package bot2;

import battlecode.common.*;

public class Soldier extends Robot {
    enum Mode {
        SCOUT,
        ATTACK,
        DEFEND
    }

    static final int ATTACK_COUNT_THRESHOLD = 15;

    Mode mode;

    MapLocation possibleArchonLoc = null;
    MapLocation definiteArchonLoc = null;

    boolean incrementedAttackerCount = false;

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
        brownian();

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
        optimalAttack();

//        rc.setIndicatorString("DDDDDDDDDDD");
//        MapLocation enemyArchonLoc = comms.getTargetEnemyArchon();
//        if (enemyArchonLoc != null) {
//            rc.setIndicatorString("ATTACK: " + enemyArchonLoc);
//            if (myLoc.distanceSquaredTo(enemyArchonLoc) < myType.visionRadiusSquared) {
//                if (rc.senseRobotAtLocation(enemyArchonLoc) == null) {
//                    comms.updateDestroyedEnemyArchon(enemyArchonLoc);
//                }
//            } else {
//                nav.moveTowards(enemyArchonLoc);
//            }
//        }
//        checkAttackPossible();
        if (possibleArchonLoc == null ||
                (myLoc.distanceSquaredTo(possibleArchonLoc) < myType.visionRadiusSquared) &&
                        rc.senseRobotAtLocation(possibleArchonLoc) == null) {
            possibleArchonLoc = randomAttackTarget();
        }

        if (possibleArchonLoc != null) {
            if (!nav.moveTowards(possibleArchonLoc)) {
                brownian();
            }
        }
    }

    void defend() throws GameActionException {
        brownian();
    }

    void optimalAttack() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }

        MapLocation targetLoc = null;
        RobotType targetType = null;
        int targetHealth = 10000;

        for (RobotInfo info : rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared, opponentTeam)) {
            if (targetLoc == null) {
                targetLoc = info.location;
                targetType = info.type;
                targetHealth = info.health;
            } else {
                switch (info.type) {
                    case SAGE:
                    case WATCHTOWER:
                    case SOLDIER:
                        if ((targetType != RobotType.SAGE && targetType != RobotType.WATCHTOWER &&
                                targetType != RobotType.SOLDIER) || info.health < targetHealth) {
                            targetLoc = info.location;
                            targetType = info.type;
                            targetHealth = info.health;
                        }
                    case ARCHON:
                        if (targetType != RobotType.SAGE && targetType != RobotType.WATCHTOWER &&
                                targetType != RobotType.SOLDIER) {
                            targetLoc = info.location;
                            targetType = info.type;
                            targetHealth = info.health;
                        }
                    default:
                        if (targetType != RobotType.ARCHON && targetType != RobotType.WATCHTOWER &&
                                targetType != RobotType.SOLDIER && targetType != RobotType.SAGE
                                && info.health < targetHealth) {
                            targetLoc = info.location;
                            targetType = info.type;
                            targetHealth = info.health;
                        }
                }
            }
        }

        if (targetLoc != null) {
            rc.attack(targetLoc);
            if (rc.senseRobotAtLocation(targetLoc) == null) {
                comms.updateDestroyedEnemyArchon(targetLoc);
                rc.writeSharedArray(17, 0);
                rc.writeSharedArray(18, 0);
            }
        }
    }

    void checkAttackPossible() throws GameActionException {
        if (countNearbyAllies() < ATTACK_COUNT_THRESHOLD) {
            return;
        }

        MapLocation[] enemyArchonLocs = comms.getEnemyArchonLocs();
        for (int i = 0; i < 4; ++i) {
            MapLocation archonLoc = enemyArchonLocs[i];
            if (archonLoc != null) {
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

    MapLocation randomAttackTarget() throws GameActionException {
        int symmetryType = rng.nextInt(3);
        switch (symmetryType) {
            case 0:
                return nav.reflectHoriz(parentLoc);
            case 1:
                return nav.reflectVert(parentLoc);
            case 2:
                return nav.reflectDiag(parentLoc);
        }

        return nav.reflectDiag(parentLoc);
    }
}
