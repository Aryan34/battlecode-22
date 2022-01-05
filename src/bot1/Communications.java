package bot1;

import battlecode.common.*;

public class Communications {
    RobotController rc;
    Robot robot;

    Communications(RobotController rc, Robot robot) {
        this.rc = rc;
        this.robot = robot;
    }

    int getArchonCount() throws GameActionException {
        int count = 0;
        for (int i = 0; i < 4; ++i) {
            if (rc.readSharedArray(i) != 0) {
                ++count;
            }
        }

        return count;
    }

    int getDetectedEnemyArchonCount() throws GameActionException {
        int count = 0;
        for (int i = 4; i < 8; ++i) {
            if (rc.readSharedArray(i) != 0) {
                ++count;
            }
        }

        return count;
    }

    void addFriendlyArchonLoc(MapLocation loc) throws GameActionException {
        int x = loc.x + 1;
        int y = loc.y + 1;
        int value = x + (y << 7);
        for (int i = 0; i < 4; ++i) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, value);
                return;
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
                return;
            }
        }
        System.out.println("addEnemyArchonLoc was called an extra time somewhere. ");
    }

    MapLocation[] getFriendlyArchonLocs() throws GameActionException {
        MapLocation[] locs = new MapLocation[4];
        for (int i = 0; i < 4; ++i) {
            int value = rc.readSharedArray(i);
            if (value != 0) {
                int x = value & 0b1111111;
                int y = (value >> 7) & 0b1111111;
                MapLocation loc = new MapLocation(x, y);
                locs[i] = loc;
            }
        }

        return locs;
    }

    MapLocation[] getEnemyArchonLocs() throws GameActionException {
        MapLocation[] locs = new MapLocation[4];
        for (int i = 4; i < 8; ++i) {
            int value = rc.readSharedArray(i);
            if (value != 0) {
                int x = value & 0b1111111;
                int y = (value >> 7) & 0b1111111;
                MapLocation loc = new MapLocation(x, y);
                locs[i - 4] = loc;
            }
        }

        return locs;
    }

    void updateRobotCount(RobotType type, int update) throws GameActionException {
        switch (type) {
            case ARCHON:
                rc.writeSharedArray(8, rc.readSharedArray(8) + update);
                break;
            case LABORATORY:
                rc.writeSharedArray(9, rc.readSharedArray(8) + update);
                break;
            case WATCHTOWER:
                rc.writeSharedArray(10, rc.readSharedArray(8) + update);
                break;
            case MINER:
                rc.writeSharedArray(11, rc.readSharedArray(8) + update);
                break;
            case BUILDER:
                rc.writeSharedArray(12, rc.readSharedArray(8) + update);
                break;
            case SOLDIER:
                rc.writeSharedArray(13, rc.readSharedArray(8) + update);
                break;
            case SAGE:
                rc.writeSharedArray(14, rc.readSharedArray(8) + update);
                break;
        }
    }

    int getRobotCount(RobotType type) throws GameActionException {
        switch (type) {
            case ARCHON:
                return rc.readSharedArray(8);
            case LABORATORY:
                return rc.readSharedArray(9);
            case WATCHTOWER:
                return rc.readSharedArray(10);
            case MINER:
                return rc.readSharedArray(11);
            case BUILDER:
                return rc.readSharedArray(12);
            case SOLDIER:
                return rc.readSharedArray(13);
            case SAGE:
                return rc.readSharedArray(14);
            default:
                return 0;
        }
    }
}
