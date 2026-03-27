package fr.adrienlombard.utlogs;

public enum Weapon {

    KNIFE("1"),
    BERETTA("2"),
    DEAGLE("3"),
    SPAS("4"),
    MP5K("5"),
    UMP("6"),
    HK69("7"),
    LR300("8"),
    G36("9"),
    PSG1("10"),
    HE_GREN("11"),
    SMOKE_GREN("13"),
    SR8("14"),
    AK("15"),
    BOMB("16"),
    NEGEV("17"),
    M4("19"),
    GLOCK("20"),
    COLT1911("21"),
    MAC11("22"),
    FRF1("23"),
    BENELLI("24"),
    P90("25"),
    MAGNUM("26"),
    UNKNOWN("-1");

    private final String id;

    Weapon(String id) {
        this.id = id;
    }

    public static Weapon fromValue(String id) {
        for (Weapon weapon : Weapon.values()) {
            if (weapon.id.equals(id)) {
                return weapon;
            }
        }
        throw new IllegalArgumentException("Unknown weapon id: " + id);
    }

}