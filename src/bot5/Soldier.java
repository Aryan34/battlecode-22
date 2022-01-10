package bot5;

import battlecode.common.*;

public class Soldier extends Robot {
    static final int ATTACK_COUNT_THRESHOLD = 10;

    enum Mode {
        ATTACK,
        DEFEND
    }

    Mode mode;

    MapLocation possibleArchonLoc = null;
    MapLocation definiteArchonLoc = null;

    int possibleArchonLocsChecked = 0;

    public Soldier(RobotController rc) {
        super(rc);
        mode = Mode.ATTACK;

        int modulus = 3 + (roundNum / 100);
        if (rc.getID() % modulus == 0) {
            mode = Mode.DEFEND;
        }
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

//        if (definiteArchonLoc != null) {
//            if (myLoc.distanceSquaredTo(definiteArchonLoc) > 35) {
//                nav.moveTowards(definiteArchonLoc);
//            } else {
//                int attackerCount = util.countNearbyFriendlyTroops(RobotType.SOLDIER);
//                if (attackerCount >= ATTACK_COUNT_THRESHOLD) {
//                    nav.moveTowards(definiteArchonLoc);
//                } else {
//                    nav.moveRandom();
//                }
//            }
//        }
        if (possibleArchonLocsChecked == 3) {
            mode = Mode.DEFEND;
            defend();
            return;
        }

        if (possibleArchonLoc == null) {
            possibleArchonLoc = randomAttackTarget(rc.getID() % 3);
            // System.out.println("SET POSSIBLE LOC TO: " + possibleArchonLoc);
        }

        if ((rc.canSenseLocation(possibleArchonLoc) && rc.senseRobotAtLocation(possibleArchonLoc) == null)) {
            ++possibleArchonLocsChecked;
            possibleArchonLoc = randomAttackTarget(((rc.getID() + possibleArchonLocsChecked) % 3));
        }

        if (possibleArchonLoc != null) {
            if (!nav.moveTowards(possibleArchonLoc)) {
                brownian();
                // System.out.println("MOVING TOWARDS LOC FAILED: " + possibleArchonLoc);
            }
            // System.out.println("MOVING TOWARDS POSSIBLE LOC: " + possibleArchonLoc);
        }

        brownian();
    }

    void defend() throws GameActionException {
        fight(rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam));
        if (myLoc.distanceSquaredTo(parentLoc) > RobotType.ARCHON.visionRadiusSquared) {
            nav.moveTowards(parentLoc);
        }
        brownian();
    }

    void fight(RobotInfo[] enemyInfo) throws GameActionException {
        kite(enemyInfo);
        optimalAttack();
    }

    void kite(RobotInfo[] enemyInfo) throws GameActionException {
        if (util.countNearbyEnemyAttackers(enemyInfo) == 0) {
            MapLocation target = util.lowestHealthTarget();
            if (target != null) {
                if (myLoc.distanceSquaredTo(target) > 2) {
                    nav.moveTowards(target);
                } else {
                    Direction dir = directionToLowestRubbleTile(target);
                    if (dir != null && rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
            }
        } else if (util.countNearbyEnemyAttackers(enemyInfo) == 1) {
            MapLocation target = util.getAttackerLocation(enemyInfo);
            RobotInfo enemy = rc.senseRobotAtLocation(target);
            if (target != null && enemy != null && enemy.health <= rc.getHealth()) {
                nav.moveTowards(target);
            }
        } else if (util.countNearbyEnemyAttackers(enemyInfo) > util.countNearbyFriendlyTroops(RobotType.SOLDIER)){
            nav.retreatFromEnemies(enemyInfo);
        }
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
            RobotInfo target = rc.senseRobotAtLocation(targetLoc);
            if (target != null) {
                rc.attack(targetLoc);
                if (target.type == RobotType.ARCHON && rc.senseRobotAtLocation(targetLoc) == null) {
                    comms.updateDestroyedEnemyArchon(targetLoc);
                }
            }
        }
    }

    Direction directionToLowestRubbleTile(MapLocation target) throws GameActionException {
        if (!rc.isMovementReady()) {
            return null;
        }

        Direction bestDir = Direction.CENTER;
        int lowestRubble = rc.senseRubble(myLoc);
        int lowestDistance = 100;

        for (Direction dir : Navigation.directions) {
            MapLocation dest = myLoc.add(dir);
            if (rc.canMove(dir) && rc.onTheMap(dest)) {
                int rubble = rc.senseRubble(dest);
                if (rubble < lowestRubble ||
                        (rubble == lowestRubble && dest.distanceSquaredTo(target) < lowestDistance)) {
                    bestDir = dir;
                    lowestRubble = rubble;
                    lowestDistance = dest.distanceSquaredTo(target);
                }
            }
        }

        return bestDir;
    }

    MapLocation randomAttackTarget(int symmetryType) throws GameActionException {
        switch (symmetryType) {
            case 0:
                return nav.reflectHoriz(parentLoc);
            case 1:
                return nav.reflectVert(parentLoc);
            case 2:
                return nav.reflectDiag(parentLoc);
        }

        return null;
    }
}
