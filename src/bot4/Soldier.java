package bot4;

import battlecode.common.*;

public class Soldier extends Robot {
    enum Mode {
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
            case ATTACK:
                attack();
                break;
            case DEFEND:
                defend();
                break;
        }
    }

    void attack() throws GameActionException {
        fight(rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam));

        int encodedAttackTarget = rc.readSharedArray(17);
        if (encodedAttackTarget != 0) {
            definiteArchonLoc = comms.decodeLocation(encodedAttackTarget);
        }

        if (definiteArchonLoc != null) {
            if (myLoc.distanceSquaredTo(definiteArchonLoc) > 35) {
                nav.moveTowards(definiteArchonLoc);
            } else {
                int attackerCount = util.countNearbyFriendlyTroops(RobotType.SOLDIER);
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

    void fight(RobotInfo[] enemyInfo) throws GameActionException {
        kite(enemyInfo);
        optimalAttack();
    }

    void kite(RobotInfo[] enemyInfo) throws GameActionException {
        if (util.countNearbyEnemyAttackers(enemyInfo) == 0) {
            MapLocation target = util.lowestHealthTarget();
            if (target != null) {
                nav.moveTowards(target);
            }
        } else {
            int x = myLoc.x;
            int y = myLoc.y;
            double force_x = 0;
            double force_y = 0;
            for (RobotInfo info : enemyInfo) {
                switch (info.type) {
                    case SAGE:
                        force_x += (double) (x - info.location.x) / myLoc.distanceSquaredTo(info.location);
                        force_y += (double) (y - info.location.y) / myLoc.distanceSquaredTo(info.location);
                        break;
                    case SOLDIER:
                        force_x += 1.25 * (double) (x - info.location.x) / myLoc.distanceSquaredTo(info.location);
                        force_y += 1.25 * (double) (y - info.location.y) / myLoc.distanceSquaredTo(info.location);
                        break;
                    case WATCHTOWER:
                        force_x += 1.5 * (double) (x - info.location.x) / myLoc.distanceSquaredTo(info.location);
                        force_y += 1.5 * (double) (y - info.location.y) / myLoc.distanceSquaredTo(info.location);
                        break;
                    default:
                        break;
                }
            }
            MapLocation forceResult = myLoc.translate(100 * (int) force_x, 100 * (int) force_y);
            nav.moveTowards(forceResult);
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
