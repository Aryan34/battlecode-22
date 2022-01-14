package bot8;

import battlecode.common.*;

import java.util.Arrays;

public class Soldier extends Robot {
    enum Mode {
        ATTACK,
        DEFEND
    }

    Mode mode;

    MapLocation[] possibleArchonLocs;
    MapLocation possibleLoc = null;
    MapLocation randomTarget = null;

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
        if (!rc.isMovementReady()) {
            return;
        }

        if (turnCount % 10 == 0) {
            possibleArchonLocs = comms.getEnemyArchonLocs();
            possibleLoc = util.closestInArray(possibleArchonLocs);
        }
        if (possibleLoc != null) {
            if (myLoc.distanceSquaredTo(possibleLoc) > myType.actionRadiusSquared) {
                nav.moveTowards(possibleLoc);
                rc.setIndicatorString("1: " + possibleLoc);
            } else {
                kite(rc.senseNearbyRobots(myType.visionRadiusSquared, opponentTeam));
                rc.setIndicatorString("2: KITING");
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

    void fight(RobotInfo[] enemyInfo) throws GameActionException {
        kite(enemyInfo);
        optimalAttack();
    }

    // TODO: Sometimes teammates run away when others are fighting, causing a loss, so fix this
    // TODO: time to kill calculations for soldier micro
    void kite(RobotInfo[] enemyInfo) throws GameActionException {
        int nearbyEnemyAttackers = util.countNearbyEnemyAttackers(enemyInfo);
        if (nearbyEnemyAttackers == 0) {
            // System.out.println("0 ATTACKERS");
            MapLocation target = util.lowestHealthTarget();
            if (target != null) {
                if (myLoc.distanceSquaredTo(target) > 4) {
                    // System.out.println("MOVE TO TARGET: " + target);
                    nav.moveTowards(target);
                } else {
                    Direction dir = nav.directionToLowestRubbleTile(target);
                    // System.out.println("DIR TO LOWEST RUBBLE TILE: " + dir);
                    nav.tryMove(dir);
                }
            }
        } else if (nearbyEnemyAttackers == 1) {
            // System.out.println("1 ATTACKERS");
            MapLocation target = util.getAttackerLocation(enemyInfo);
            RobotInfo enemy = rc.senseRobotAtLocation(target);
            // System.out.println("TARGET: " + target + ", ENEMY: " + enemy);
            if (target != null && enemy != null) {
                if (enemy.health <= rc.getHealth() && rc.senseRubble(myLoc) <= rc.senseRubble(target)) {
                    if (myLoc.distanceSquaredTo(target) > 5) {
                        // System.out.println("TOO FAR FROM ATTACKER");
                        nav.moveTowards(target, true);
                    } else {
                        // System.out.println("CLOSE TO ATTACKER");
                        Direction dir = nav.directionToLowestRubbleTile(target);
                        // System.out.println("DIR TO LOWEST RUBBLE TILE: " + dir);
                        nav.tryMove(dir);
                    }
                } else {
                    // System.out.println("MY HP: " + rc.getHealth() + ", ENEMY HP: " + enemy.health);
                    // System.out.println("MY RUBBLE: " + rc.senseRubble(myLoc) + ", ENEMY RUBBLE: " + rc.senseRubble(enemy.location));
                    // System.out.println("RETREAT TOWARDS: " + myLoc.directionTo(target).opposite());
                    nav.retreatFrom(target);
                }
            }
        } else if (rc.getHealth() < util.nearbyEnemyAttackersTotalHealth(enemyInfo)) {
            kiteMultiple(enemyInfo);
        }
    }

    void kiteMultiple(RobotInfo[] enemyInfo) throws GameActionException {
        System.out.println("TOO MANY ATTACKERS");
        MapLocation closestEnemyAttacker = util.closestAttackTarget();
        if (closestEnemyAttacker != null) {
            nav.retreatFrom(closestEnemyAttacker);
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

    MapLocation getRandomTarget() {
        int randX = rng.nextInt(rc.getMapWidth());
        int randY = rng.nextInt(rc.getMapHeight());
        return new MapLocation(randX, randY);
    }
}
