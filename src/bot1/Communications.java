package bot1;

import battlecode.common.*;

public class Communications {
    RobotController rc;

    Communications(RobotController rc) {
        this.rc = rc;
    }

    int getArchonId(MapLocation loc) throws GameActionException {
        int encodedLoc = encodeLocation(loc);
        for (int i = 0; i < 4; ++i) {
            if (rc.readSharedArray(i) == encodedLoc) {
                return i;
            }
        }

        return -1;
    }

    int getBuildMutex() throws GameActionException {
        return rc.readSharedArray(16);
    }

    void updateBuildMutex() throws GameActionException {
        rc.writeSharedArray(16, (rc.readSharedArray(16) + 1) % rc.getArchonCount());
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

    int encodeLocation(MapLocation loc) {
        int x = loc.x + 1;
        int y = loc.y + 1;
        return x + (y << 7);
    }

    MapLocation decodeLocation(int value) {
        int x = value & 0b1111111;
        int y = (value >> 7) & 0b1111111;
        return new MapLocation(x - 1, y - 1);
    }

    void addFriendlyArchonLoc(MapLocation loc) throws GameActionException {
        int value = encodeLocation(loc);
        for (int i = 0; i < 4; ++i) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, value);
                return;
            }
        }
        System.out.println("addFriendlyArchonLoc was called an extra time somewhere. ");
    }

    void addEnemyArchonLoc(MapLocation loc) throws GameActionException {
        int value = encodeLocation(loc);
        for (int i = 4; i < 8; ++i) {
            if (rc.readSharedArray(i) == value) {
                break;
            }
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, value);
                return;
            }
        }
    }

    MapLocation[] getFriendlyArchonLocs() throws GameActionException {
        MapLocation[] locs = new MapLocation[4];
        for (int i = 0; i < 4; ++i) {
            int value = rc.readSharedArray(i);
            if (value != 0) {
                MapLocation loc = decodeLocation(value);
                locs[i] = loc;
            }
        }

        return locs;
    }

    MapLocation[] getEnemyArchonLocs() throws GameActionException {
        MapLocation[] locs = new MapLocation[4];
        for (int i = 4; i < 8; ++i) {
            int value = rc.readSharedArray(i);
            if (value != 0 && value != -1) {
                MapLocation loc = decodeLocation(value);
                locs[i - 4] = loc;
            }
        }

        return locs;
    }

    void chooseTargetEnemyArchon(int index) throws GameActionException {
        rc.writeSharedArray(15, index + 1);
    }

    void updateDestroyedEnemyArchon(MapLocation loc) throws GameActionException {
        int encoded = encodeLocation(loc);
        for (int i = 4; i < 8; ++i) {
            int value = rc.readSharedArray(i);
            if (value == encoded) {
                rc.writeSharedArray(i, -1);
            }
        }
    }

    MapLocation getTargetEnemyArchon() throws GameActionException {
        int index = rc.readSharedArray(15);
        if (index == 0) {
            return null;
        }
        return getEnemyArchonLocs()[index - 1];
    }

    void updateRobotCount(RobotType type, int update) throws GameActionException {
        switch (type) {
            case ARCHON:
                rc.writeSharedArray(8, rc.readSharedArray(8) + update);
                break;
            case LABORATORY:
                rc.writeSharedArray(9, rc.readSharedArray(9) + update);
                break;
            case WATCHTOWER:
                rc.writeSharedArray(10, rc.readSharedArray(10) + update);
                break;
            case MINER:
                rc.writeSharedArray(11, rc.readSharedArray(11) + update);
                break;
            case BUILDER:
                rc.writeSharedArray(12, rc.readSharedArray(12) + update);
                break;
            case SOLDIER:
                rc.writeSharedArray(13, rc.readSharedArray(13) + update);
                break;
            case SAGE:
                rc.writeSharedArray(14, rc.readSharedArray(14) + update);
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
