package bot2;

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

        rc.setIndicatorString("" + targetx + " " + targety + " " + randnum);
        if (targetx == -1 || targety == -1 || (rc.canSenseRobotAtLocation(new MapLocation(targetx, targety)) && rc.senseRobotAtLocation(new MapLocation(targetx, targety)).type != RobotType.ARCHON)) {
            int r = 0;
            if (randnum == -1) {
                r = rng.nextInt(3);
                randnum = r;
            } else {
                r = (randnum + 1) % 3;
                randnum = r;
            }

            if (r == 0) {
                targetx = rc.getMapWidth() - rc.readSharedArray(10) - 1;
                targety = rc.getMapHeight() - rc.readSharedArray(11) - 1;
            } else if (r == 1) {
                targetx = rc.getMapWidth() - rc.readSharedArray(10) - 1;
                targety = rc.readSharedArray(11);
            } else {
                targetx = rc.readSharedArray(10);
                targety = rc.getMapHeight() - rc.readSharedArray(11) - 1;
            }
        }

        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        MapLocation temp = new MapLocation(targetx, targety);
        Direction dir = rc.getLocation().directionTo(temp);
        if (rc.senseNearbyRobots(radius, rc.getTeam()).length > 7) {
            if (rc.canMove(dir)) {
                rc.move(dir);
                //System.out.println("I moved!");
            } else {
                dir = Navigation.directions[rng.nextInt(Navigation.directions.length)];
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    //System.out.println("I moved!");
                }
            }
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
        nav.moveRandom();
    }

    void defend() throws GameActionException {
        nav.moveRandom();
    }
}
