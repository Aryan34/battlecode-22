package bot1;

import battlecode.common.*;

import java.util.Random;

public class Robot {
    static RobotController rc;
    static Communications comms;
    static Navigation nav;
    static Util util;

    static int roundNum;
    static int turnCount;
    boolean considerDead;

    static final Random rng = new Random(6147);
    static final int LATTICE_MOD = 2;
    static final int INACTION_TURNS_THRESHOLD = 10;

    int numRoundsNoActions;
    int numRoundsNoMove;

    MapLocation myLoc;
    MapLocation parentLoc;
    RobotType myType;
    Team myTeam, opponentTeam;

    public Robot(RobotController rc) {
        Robot.rc = rc;
        comms = new Communications(rc);
        nav = new Navigation(rc);
        util = new Util(rc, this);

        roundNum = rc.getRoundNum();
        turnCount = 0;
        considerDead = false;

        numRoundsNoActions = 0;
        numRoundsNoMove = 0;

        myLoc = rc.getLocation();
        myType = rc.getType();
        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();

        if (myType != RobotType.ARCHON) {
            for (RobotInfo info : rc.senseNearbyRobots(2, myTeam)) {
                if (info.type == RobotType.ARCHON) {
                    parentLoc = info.location;
                    break;
                }
            }
        }
    }

    void playTurn() throws GameActionException {
        roundNum = rc.getRoundNum();
        myLoc = rc.getLocation();
        if (turnCount==0){
            comms.updateRobotCount(myType, 1);
        }
        if (!considerDead && rc.getHealth() < 6) {
            considerDead = true;
            comms.updateRobotCount(myType, -1);
        } else if (considerDead && rc.getHealth() >= 6) {
            considerDead = false;
            comms.updateRobotCount(myType, 1);
        }

        ++turnCount;
    }

    void updateInaction() throws GameActionException {
        numRoundsNoActions = rc.getActionCooldownTurns() == 0 ? numRoundsNoActions + 1 : 0;
        numRoundsNoMove = rc.isMovementReady() ? numRoundsNoMove + 1 : 0;
    }
}
