package common.src.main.Enum;

/**
 *   Flags used to communicate with the server
 */


public enum ServerFlag {
    //These are user request
    CONNECTED,
    HOST,
    JOIN,

    //These are between server and creationHandler
    CHECKROOM,
    SETROOM,
    GENERATEROOM,

    //These are between creationHandler and ROOM
    ROOMOK,

    //These are server responses
    OK,
    KO
}
