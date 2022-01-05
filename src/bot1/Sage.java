package bot1;

import battlecode.common.*;

public class Sage extends Droid {
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
