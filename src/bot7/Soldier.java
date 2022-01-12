package bot7;

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
    MapLocation randomTarget = null;

    int possibleArchonLocsChecked = 0;

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
        possibleArchonLocs = comms.getEnemyArchonLocs();

        int lowestDist = 10000;
        for (MapLocation loc : possibleArchonLocs) {
            if (loc != null && myLoc.distanceSquaredTo(loc) < lowestDist) {
                lowestDist = myLoc.distanceSquaredTo(loc);
                possibleLoc = loc;
            }
        }

        if (possibleLoc != null) {
            if (myLoc.distanceSquaredTo(possibleLoc) > 35) {
                nav.moveTowards(possibleLoc);
                rc.setIndicatorString("1: " + possibleLoc);
            } else {
                int attackerCount = util.countNearbyFriendlyTroops(RobotType.SOLDIER);
                if (attackerCount >= ATTACK_COUNT_THRESHOLD) {
                    rc.setIndicatorString("2: " + possibleLoc);
                    nav.moveTowards(possibleLoc);
                } else {
                    kite(rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam));
                    if (randomTarget == null || myLoc.distanceSquaredTo(randomTarget) < myType.visionRadiusSquared) {
                        randomTarget = getRandomTarget();
                    }
                    if (nav.moveTowards(randomTarget)) {
                        rc.setIndicatorString("3: " + randomTarget);
                    }
                }
            }
        }

        if (randomTarget == null || myLoc.distanceSquaredTo(randomTarget) < myType.visionRadiusSquared) {
            randomTarget = getRandomTarget();
        }
        if (nav.moveTowards(randomTarget)) {
            rc.setIndicatorString("4: " + randomTarget);
        }
    }

    void defend() throws GameActionException {
        fight(rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam));
        if (myLoc.distanceSquaredTo(parentLoc) > RobotType.ARCHON.visionRadiusSquared) {
            nav.moveTowards(parentLoc);
        }
        brownian();
    }

    MapLocation closestEnemyLoc() throws GameActionException {
        MapLocation closest = null;
        int lowestDist = 10000;

        for (MapLocation loc : comms.getEnemyLocations()) {
            if (loc != null && myLoc.distanceSquaredTo(loc) < lowestDist) {
                closest = loc;
                lowestDist = myLoc.distanceSquaredTo(loc);
            }
        }

        return closest;
    }

    void fight(RobotInfo[] enemyInfo) throws GameActionException {
        kite(enemyInfo);
        optimalAttack();
    }

    // TODO: Sometimes teammates run away when others are fighting, causing a loss, so fix this
    void kite(RobotInfo[] enemyInfo) throws GameActionException {
        if (util.countNearbyEnemyAttackers(enemyInfo) == 0) {
            // System.out.println("0 ATTACKERS");
            MapLocation target = util.lowestHealthTarget();
            if (target != null) {
                if (myLoc.distanceSquaredTo(target) > 4) {
                    // System.out.println("MOVE TO TARGET");
                    nav.moveTowards(target);
                } else {
                    // System.out.println("GET TO LOWEST RUBBLE");
                    Direction dir = directionToLowestRubbleTile(target);
                    if (dir != null && rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
            }
        } else if (util.countNearbyEnemyAttackers(enemyInfo) == 1) {
            // System.out.println("1 ATTACKERS");
            MapLocation target = util.getAttackerLocation(enemyInfo);
            RobotInfo enemy = rc.senseRobotAtLocation(target);
            // System.out.println("TARGET: " + target + ", ENEMY: " + enemy);
            if (target != null && enemy != null) {
                if (enemy.health <= rc.getHealth()) {
                    if (myLoc.distanceSquaredTo(target) > 5) {
                        // System.out.println("TOO FAR FROM ATTACKER");
                        nav.moveTowards(target, true);
                    } else {
                        // System.out.println("CLOSE TO ATTACKER");
                        Direction dir = directionToLowestRubbleTile(target);
                        if (dir != null && rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }
                } else {
                    nav.retreatTowards(myLoc.directionTo(target).opposite());
                }
            }
        } else if (util.countNearbyEnemyAttackers(enemyInfo) > util.countNearbyFriendlyTroops(RobotType.SOLDIER)) {
            // System.out.println("TOO MANY ATTACKERS");
            nav.retreatFromEnemies(enemyInfo);
        }
    }

    void optimalAttack() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }

        MapLocation targetLoc = null;
        int targetValue = -10000;

        for (RobotInfo info : rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared, opponentTeam)) {
            int currValue = util.attackPriority(info.type) - info.health;
            if (currValue > targetValue) {
                targetValue = currValue;
                targetLoc = info.location;
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

    MapLocation getRandomTarget() {
        int randX = rng.nextInt(rc.getMapWidth());
        int randY = rng.nextInt(rc.getMapHeight());
        return new MapLocation(randX, randY);
    }
}
