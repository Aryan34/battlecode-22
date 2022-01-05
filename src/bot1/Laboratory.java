package bot1;

import battlecode.common.*;

public class Laboratory extends Building {
    public Laboratory(RobotController rc) {
        super(rc);
    }

    void playTurn() throws GameActionException {
        if (rc.getTeamLeadAmount(rc.getTeam()) > 1000 && rc.isActionReady()) {
            rc.transmute();
        }
    }
}
