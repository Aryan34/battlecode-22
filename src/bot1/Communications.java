package bot1;

import battlecode.common.*;

public class Communications {
    RobotController rc;
    Robot robot;

    Communications(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
    }

    void addFriendlyArchonLoc(MapLocation loc) throws GameActionException {
        int x = loc.x + 1;
        int y = loc.y + 1;
        int value = x + (y << 7);
        for (int i = 0; i < 4; ++i) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, value);
                break;
            }
        }
        System.out.println("addFriendlyArchonLoc was called an extra time somewhere. ");
    }

    void addEnemyArchonLoc(MapLocation loc) throws GameActionException {
        int x = loc.x + 1;
        int y = loc.y + 1;
        int value = x + (y << 7);
        for (int i = 4; i < 8; ++i) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, value);
                break;
            }
        }
        System.out.println("addFriendlyArchonLoc was called an extra time somewhere. ");
    }

    void updateRobotCount(RobotType type) throws GameActionException {
        switch (type) {
            case ARCHON:
                rc.writeSharedArray(8, rc.readSharedArray(8) + 1);
                break;
            case LABORATORY:
                rc.writeSharedArray(9, rc.readSharedArray(8) + 1);
                break;
            case WATCHTOWER:
                rc.writeSharedArray(10, rc.readSharedArray(8) + 1);
                break;
            case MINER:
                rc.writeSharedArray(11, rc.readSharedArray(8) + 1);
                break;
            case BUILDER:
                rc.writeSharedArray(12, rc.readSharedArray(8) + 1);
                break;
            case SOLDIER:
                rc.writeSharedArray(13, rc.readSharedArray(8) + 1);
                break;
            case SAGE:
                rc.writeSharedArray(14, rc.readSharedArray(8) + 1);
                break;
        }
    }

    void getRobotCount(RobotType type) throws GameActionException {
        switch (type) {
            case ARCHON:
                rc.readSharedArray(8);
                break;
            case LABORATORY:
                rc.readSharedArray(9);
                break;
            case WATCHTOWER:
                rc.readSharedArray(10);
                break;
            case MINER:
                rc.readSharedArray(11);
                break;
            case BUILDER:
                rc.readSharedArray(12);
                break;
            case SOLDIER:
                rc.readSharedArray(13);
                break;
            case SAGE:
                rc.readSharedArray(14);
                break;
        }
    }
}
