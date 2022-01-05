package bot1;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Util {
    RobotController rc;
    Robot robot;

    Util(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
    }

    MapLocation optimalRepairTarget() {
        int lowestAllyHealth = rc.getHealth();
        MapLocation optimalTarget = rc.getLocation();

        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.health < lowestAllyHealth) {
                lowestAllyHealth = info.health;
                optimalTarget = info.location;
            }
        }

        return optimalTarget;
    }
}
