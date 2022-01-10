package bot6;

import battlecode.common.*;

public class Sage extends Robot {
    enum Mode {
        ATTACK,
        DEFEND
    }

    Mode mode;

    public Sage(RobotController rc) {
        super(rc);
        mode = Mode.DEFEND;
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

    }

    void defend() throws GameActionException {

    }
}
