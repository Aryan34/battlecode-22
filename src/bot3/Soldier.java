package bot3;

import battlecode.common.*;

public class Soldier extends Robot {
    enum Mode {
        SCOUT,
        ATTACK,
        DEFEND
    }

    static final int ATTACK_COUNT_THRESHOLD = 10;

    Mode mode;

    MapLocation possibleArchonLoc = null;
    MapLocation definiteArchonLoc = null;

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

        int encodedAttackTarget = rc.readSharedArray(17);
        if (encodedAttackTarget != 0) {
            definiteArchonLoc = comms.decodeLocation(encodedAttackTarget);
        }

        if (definiteArchonLoc != null) {
            if (myLoc.distanceSquaredTo(definiteArchonLoc) > 35) {
                nav.moveTowards(definiteArchonLoc);
            } else {
                int attackerCount= util.countNearbyFriendlyTroops(RobotType.SOLDIER);
                if (attackerCount >= ATTACK_COUNT_THRESHOLD) {
                    nav.moveTowards(definiteArchonLoc);
                } else {
                    nav.moveRandom();
                }
            }
        } else {
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
            brownian();
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
