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
    static final Random rng = new Random(6147);

    static int targetx = -1;
    static int targety = -1;
    static int randnum = -1;

    MapLocation myLoc;
    RobotType myType;
    Team myTeam, opponentTeam;

    public Robot(RobotController rc) {
        Robot.rc = rc;
        comms = new Communications(rc, this);
        nav = new Navigation(rc, this);
        util = new Util(rc, this);

        roundNum = rc.getRoundNum();
        turnCount = 0;

        myLoc = rc.getLocation();
        myType = rc.getType();
        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();
    }

    void playTurn() throws GameActionException {
        roundNum = rc.getRoundNum();
        myLoc = rc.getLocation();
        ++turnCount;
    }
}
