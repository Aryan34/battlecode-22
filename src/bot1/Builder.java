package bot1;

import battlecode.common.*;

public class Builder extends Droid {
    enum Mode {
        BUILD,
        REPAIR,
        MOVE
    }

    Mode mode;

    public Builder(RobotController rc) {
        super(rc);
    }

    void playTurn() throws GameActionException {
    }
}
