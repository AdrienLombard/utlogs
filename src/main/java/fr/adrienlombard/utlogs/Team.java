package fr.adrienlombard.utlogs;

public enum Team {
    RED("1"),
    BLUE("2"),
    FREE("0"),
    SPECTATOR("3"),
    UNKNOWN("");

    private final String code;

    private Team(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Team fromCode(String code) {
        for (Team team : values()) {
            if (team.code.equals(code)) {
                return team;
            }
        }
        return UNKNOWN;
    }
}
