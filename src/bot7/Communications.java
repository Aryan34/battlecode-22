package bot7;

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
        return rc.readSharedArray(23);
    }

    void updateBuildMutex() throws GameActionException {
        rc.writeSharedArray(23, (rc.readSharedArray(23) + 1) % rc.getArchonCount());
    }

    int encodeLocation(MapLocation loc) {
        int x = loc.x + 1;
        int y = loc.y + 1;
        return x + (y << 7);
    }

    MapLocation decodeLocation(int value) {
        int x = value & 0b1111111;
        int y = (value >> 7) & 0b1111111;
        if (x == 0 && y == 0) {
            return null;
        }
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
    }

    void addEnemyArchonLoc(MapLocation loc) throws GameActionException {
        int value = encodeLocation(loc);
        for (int i = 4; i < 16; ++i) {
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
        MapLocation[] locs = new MapLocation[12];
        for (int i = 4; i < 16; ++i) {
            int value = rc.readSharedArray(i);
            if (value != 0 && value != 0xFFFF) {
                MapLocation loc = decodeLocation(value);
                locs[i - 4] = loc;
            }
        }

        return locs;
    }

    void updateDestroyedEnemyArchon(MapLocation loc) throws GameActionException {
        int encoded = encodeLocation(loc);
        for (int i = 4; i < 16; ++i) {
            int value = rc.readSharedArray(i);
            if (value == encoded) {
                rc.writeSharedArray(i, 0xFFFF);
            }
        }
    }

    void updateEscapedEnemyArchon(MapLocation loc) throws GameActionException {
        int encoded = encodeLocation(loc);
        for (int i = 4; i < 16; ++i) {
            int value = rc.readSharedArray(i);
            if (value == encoded) {
                rc.writeSharedArray(i, 0);
            }
        }
    }

    void updateCorrectGuess(MapLocation loc) throws GameActionException {
        int guessIndexMod = -1;
        int value = encodeLocation(loc);

        for (int i = 4; i < 16; ++i) {
            if (rc.readSharedArray(i) == value) {
                guessIndexMod = i % 3;
            }
        }

        for (int i = 4; i < 16; ++i) {
            if (i % 3 != guessIndexMod && rc.readSharedArray(i) != 0) {
                rc.writeSharedArray(i, 0);
            }
        }
    }

    void updateIncorrectGuess(MapLocation loc) throws GameActionException {
        int guessIndexMod = -1;
        int value = encodeLocation(loc);

        for (int i = 4; i < 16; ++i) {
            if (rc.readSharedArray(i) == value) {
                guessIndexMod = i % 3;
            }
        }

        for (int i = 4; i < 16; ++i) {
            if (i % 3 == guessIndexMod && rc.readSharedArray(i) != 0) {
                rc.writeSharedArray(i, 0);
            }
        }
    }

    int possibleEnemyArchonCount() throws GameActionException {
        int count = 0;
        for (int i = 4; i < 16; ++i) {
            if (rc.readSharedArray(i) != 0) {
                ++count;
            }
        }

        return count;
    }

    void addEnemyLocation(MapLocation loc) throws GameActionException {
        int encoded = encodeLocation(loc);
        for (int i = 23; i < 59; ++i) {
            int value = rc.readSharedArray(i);
            if (value == 0) {
                rc.writeSharedArray(i, encoded);
                return;
            } else if (loc.distanceSquaredTo(decodeLocation(value)) < 8) {
                return;
            }
        }
    }

    MapLocation[] getEnemyLocations() throws GameActionException {
        MapLocation[] enemyLocs = new MapLocation[36];
        for (int i = 23; i < 59; ++i) {
            int encoded = rc.readSharedArray(i);
            if (encoded != 0) {
                enemyLocs[i - 23] = decodeLocation(encoded);
            } else {
                enemyLocs[i - 23] = null;
            }
        }

        return enemyLocs;
    }

    void clearEnemyLocations(int clearCount) throws GameActionException {
        for (int i = 23; i < 59; ++i) {
            if (rc.readSharedArray(i) != 0) {
                rc.writeSharedArray(i, 0);
                --clearCount;
                if (clearCount == 0) {
                    return;
                }
            }
        }
    }

    int possibleAliveEnemyArchonCount() throws GameActionException {
        int count = 0;
        for (int i = 4; i < 16; ++i) {
            if (rc.readSharedArray(i) != 0 && rc.readSharedArray(i) != 0xFFFF) {
                ++count;
            }
        }

        return count;
    }

    void updateRobotCount(RobotType type, int update) throws GameActionException {
        switch (type) {
            case ARCHON:
                rc.writeSharedArray(16, rc.readSharedArray(16) + update);
                break;
            case LABORATORY:
                rc.writeSharedArray(17, rc.readSharedArray(17) + update);
                break;
            case WATCHTOWER:
                rc.writeSharedArray(18, rc.readSharedArray(18) + update);
                break;
            case MINER:
                rc.writeSharedArray(19, rc.readSharedArray(19) + update);
                break;
            case BUILDER:
                rc.writeSharedArray(20, rc.readSharedArray(20) + update);
                break;
            case SOLDIER:
                rc.writeSharedArray(21, rc.readSharedArray(21) + update);
                break;
            case SAGE:
                rc.writeSharedArray(22, rc.readSharedArray(22) + update);
                break;
        }
    }

    int getRobotCount(RobotType type) throws GameActionException {
        switch (type) {
            case ARCHON:
                return rc.readSharedArray(16);
            case LABORATORY:
                return rc.readSharedArray(17);
            case WATCHTOWER:
                return rc.readSharedArray(18);
            case MINER:
                return rc.readSharedArray(19);
            case BUILDER:
                return rc.readSharedArray(20);
            case SOLDIER:
                return rc.readSharedArray(21);
            case SAGE:
                return rc.readSharedArray(22);
            default:
                return 0;
        }
    }
}
