package common.src.main.Enum;

/**
 *   Flags the room class uses to respond to clients with
 */

public enum RoomResponseFlag {
    NEWPLAYER,
    PLAYERREMOVED,
    MESSAGE,
    CANVAS,
    GAMESTART,
    CHOOSEWORD,
    STARTTURN,
    TIMETICK,
    NEXTROUND,
    ENDGAME,
    STOPDRAW,
    ADDPOINTS,
    NULL
}
