package bot2;

import battlecode.common.*;

public class Laboratory extends Robot {
    public Laboratory(RobotController rc) {
        super(rc);
    }

    void playTurn() throws GameActionException {
        super.playTurn();

        if (rc.getTeamLeadAmount(rc.getTeam()) > 1000 && rc.isActionReady()) {
            rc.transmute();
        }
    }
}
