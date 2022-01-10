package bot6;

import battlecode.common.*;

public class Soldier extends Robot {
    static final int ATTACK_COUNT_THRESHOLD = 10;

    enum Mode {
        ATTACK,
        DEFEND
    }
    Mode mode;

    MapLocation[] possibleArchonLocs;
    MapLocation possibleLoc = null;

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
        possibleArchonLocs = comms.getEnemyArchonLocs();

        int lowestDist = 10000;
        for (MapLocation loc : possibleArchonLocs) {
            if (loc != null && myLoc.distanceSquaredTo(loc) < lowestDist) {
                lowestDist = myLoc.distanceSquaredTo(loc);
                possibleLoc = loc;
            }
        }

        if (possibleLoc != null) {
            if (myLoc.distanceSquaredTo(possibleLoc) > 0) {
                nav.moveTowards(possibleLoc);
            } else {
                int attackerCount = util.countNearbyFriendlyTroops(RobotType.SOLDIER);
                if (attackerCount >= ATTACK_COUNT_THRESHOLD) {
                    nav.moveTowards(possibleLoc);
                } else {
                    nav.moveRandom();
                }
            }
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

    // TODO: Sometimes teammates run away when others are fighting, causing a loss, so fix this
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
                        break;
                    default:
                        if (targetType != RobotType.WATCHTOWER &&
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
