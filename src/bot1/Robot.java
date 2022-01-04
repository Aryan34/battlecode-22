package bot1;

import battlecode.common.*;

import java.util.Random;

public class Robot {
    static RobotController rc;
    static int turnCount;
    static final Random rng = new Random(6147);

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public Robot(RobotController rc) {
        Robot.rc = rc;
        Robot.turnCount = 0;
    }

    void playTurn() throws GameActionException {
        ++turnCount;
    }
}
