package fr.adrienlombard.utlogs;

public enum BodyPart {

    HEAD("Head"),
    HELMET("Helmet"),
    TORSO("Torso"),
    KEVLAR("Kevlar"),
    ARMS("Arms"),
    LEGS("Legs"),
    BODY("Body"),
    UNKNOWN("-1");
    private final String id;

    BodyPart(String id) {
        this.id = id;
    }

    public static BodyPart fromValue(String id) {
        for (BodyPart part: BodyPart.values()) {
            if (part.id.equals(id)) {
                return part;
            }
        }
        throw new IllegalArgumentException(id);
    }

}
