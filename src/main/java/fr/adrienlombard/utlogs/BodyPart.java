package fr.adrienlombard.utlogs;

public enum BodyPart {

    HEAD("Head"),
    HELMET("Helmet"),
    TORSO("Torso"),
    KEVLAR("Kevlar"),
    VEST("Vest"),
    GROIN("Groin"),
    BUTT("Butt"),
    ARMS("Arms"),
    LEFT_ARM("Left Arm"),
    RIGHT_ARM("Right Arm"),
    LEGS("Legs"),
    LEFT_UPPER_LEG("Left Upper Leg"),
    LEFT_LOWER_LEG("Left Lower Leg"),
    RIGHT_UPPER_LEG("Right Upper Leg"),
    RIGHT_LOWER_LEG("Right Lower Leg"),
    BODY("Body"),
    UNKNOWN("-1");
    private final String id;

    BodyPart(String id) {
        this.id = id;
    }

    public String value() {
        return this.id;
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
