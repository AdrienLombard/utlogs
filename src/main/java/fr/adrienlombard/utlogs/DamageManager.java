package fr.adrienlombard.utlogs;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping courtesy of https://github.com/chburd/utloganalyzer
 */
public class DamageManager {

    public record Hit(Weapon weapon, BodyPart bodyPart) {
    }

    private static final DamageManager instance = new DamageManager();

    private Map<Hit, Integer> damages;

    private DamageManager() {
        init();
    }

    private void init() {

        this.damages = new HashMap<>();

        // HK69,SPAS12 : no precise damage: go to default (0)

        // --- Knife ---
        addDamage(Weapon.KNIFE, BodyPart.HEAD, 100);
        addDamage(Weapon.KNIFE, BodyPart.HELMET, 60);
        addDamage(Weapon.KNIFE, BodyPart.TORSO, 44);
        addDamage(Weapon.KNIFE, BodyPart.KEVLAR, 35);
        addDamage(Weapon.KNIFE, BodyPart.LEFT_ARM, 20);
        addDamage(Weapon.KNIFE, BodyPart.RIGHT_ARM, 20);
        addDamage(Weapon.KNIFE, BodyPart.GROIN, 40);
        addDamage(Weapon.KNIFE, BodyPart.BUTT, 37);
        addDamage(Weapon.KNIFE, BodyPart.LEFT_UPPER_LEG, 20);
        addDamage(Weapon.KNIFE, BodyPart.RIGHT_UPPER_LEG, 20);
        addDamage(Weapon.KNIFE, BodyPart.LEFT_LOWER_LEG, 18);
        addDamage(Weapon.KNIFE, BodyPart.RIGHT_LOWER_LEG, 18);
        addDamage(Weapon.KNIFE, BodyPart.LEFT_FOOT, 15);
        addDamage(Weapon.KNIFE, BodyPart.RIGHT_FOOT, 15);

        // --- Beretta 92FS ---
        addDamage(Weapon.BERETTA, BodyPart.HEAD, 100);
        addDamage(Weapon.BERETTA, BodyPart.HELMET, 40);
        addDamage(Weapon.BERETTA, BodyPart.TORSO, 33);
        addDamage(Weapon.BERETTA, BodyPart.KEVLAR, 22);
        addDamage(Weapon.BERETTA, BodyPart.LEFT_ARM, 13);
        addDamage(Weapon.BERETTA, BodyPart.RIGHT_ARM, 13);
        addDamage(Weapon.BERETTA, BodyPart.GROIN, 24);
        addDamage(Weapon.BERETTA, BodyPart.BUTT, 22);
        addDamage(Weapon.BERETTA, BodyPart.LEFT_UPPER_LEG, 15);
        addDamage(Weapon.BERETTA, BodyPart.RIGHT_UPPER_LEG, 15);
        addDamage(Weapon.BERETTA, BodyPart.LEFT_LOWER_LEG, 13);
        addDamage(Weapon.BERETTA, BodyPart.RIGHT_LOWER_LEG, 13);
        addDamage(Weapon.BERETTA, BodyPart.LEFT_FOOT, 11);
        addDamage(Weapon.BERETTA, BodyPart.RIGHT_FOOT, 11);

        // --- Desert Eagle ---
        addDamage(Weapon.DEAGLE, BodyPart.HEAD, 100);
        addDamage(Weapon.DEAGLE, BodyPart.HELMET, 66);
        addDamage(Weapon.DEAGLE, BodyPart.TORSO, 57);
        addDamage(Weapon.DEAGLE, BodyPart.KEVLAR, 38);
        addDamage(Weapon.DEAGLE, BodyPart.LEFT_ARM, 22);
        addDamage(Weapon.DEAGLE, BodyPart.RIGHT_ARM, 22);
        addDamage(Weapon.DEAGLE, BodyPart.GROIN, 42);
        addDamage(Weapon.DEAGLE, BodyPart.BUTT, 40);
        addDamage(Weapon.DEAGLE, BodyPart.LEFT_UPPER_LEG, 28);
        addDamage(Weapon.DEAGLE, BodyPart.RIGHT_UPPER_LEG, 28);
        addDamage(Weapon.DEAGLE, BodyPart.LEFT_LOWER_LEG, 22);
        addDamage(Weapon.DEAGLE, BodyPart.RIGHT_LOWER_LEG, 22);
        addDamage(Weapon.DEAGLE, BodyPart.LEFT_FOOT, 18);
        addDamage(Weapon.DEAGLE, BodyPart.RIGHT_FOOT, 18);

        // --- Glock 18 ---
        addDamage(Weapon.GLOCK, BodyPart.HEAD, 100);
        addDamage(Weapon.GLOCK, BodyPart.HELMET, 45);
        addDamage(Weapon.GLOCK, BodyPart.TORSO, 35);
        addDamage(Weapon.GLOCK, BodyPart.KEVLAR, 29);
        addDamage(Weapon.GLOCK, BodyPart.LEFT_ARM, 15);
        addDamage(Weapon.GLOCK, BodyPart.RIGHT_ARM, 15);
        addDamage(Weapon.GLOCK, BodyPart.GROIN, 29);
        addDamage(Weapon.GLOCK, BodyPart.BUTT, 27);
        addDamage(Weapon.GLOCK, BodyPart.LEFT_UPPER_LEG, 20);
        addDamage(Weapon.GLOCK, BodyPart.RIGHT_UPPER_LEG, 20);
        addDamage(Weapon.GLOCK, BodyPart.LEFT_LOWER_LEG, 15);
        addDamage(Weapon.GLOCK, BodyPart.RIGHT_LOWER_LEG, 15);
        addDamage(Weapon.GLOCK, BodyPart.LEFT_FOOT, 11);
        addDamage(Weapon.GLOCK, BodyPart.RIGHT_FOOT, 11);

        // --- Colt 1911 ---
        addDamage(Weapon.COLT1911, BodyPart.HEAD, 100);
        addDamage(Weapon.COLT1911, BodyPart.HELMET, 60);
        addDamage(Weapon.COLT1911, BodyPart.TORSO, 40);
        addDamage(Weapon.COLT1911, BodyPart.KEVLAR, 30);
        addDamage(Weapon.COLT1911, BodyPart.LEFT_ARM, 15);
        addDamage(Weapon.COLT1911, BodyPart.RIGHT_ARM, 15);
        addDamage(Weapon.COLT1911, BodyPart.GROIN, 32);
        addDamage(Weapon.COLT1911, BodyPart.BUTT, 30);
        addDamage(Weapon.COLT1911, BodyPart.LEFT_UPPER_LEG, 22);
        addDamage(Weapon.COLT1911, BodyPart.RIGHT_UPPER_LEG, 22);
        addDamage(Weapon.COLT1911, BodyPart.LEFT_LOWER_LEG, 15);
        addDamage(Weapon.COLT1911, BodyPart.RIGHT_LOWER_LEG, 15);
        addDamage(Weapon.COLT1911, BodyPart.LEFT_FOOT, 11);
        addDamage(Weapon.COLT1911, BodyPart.RIGHT_FOOT, 11);

        // --- Magnum ---
        addDamage(Weapon.MAGNUM, BodyPart.HEAD, 100);
        addDamage(Weapon.MAGNUM, BodyPart.HELMET, 82);
        addDamage(Weapon.MAGNUM, BodyPart.TORSO, 66);
        addDamage(Weapon.MAGNUM, BodyPart.KEVLAR, 50);
        addDamage(Weapon.MAGNUM, BodyPart.LEFT_ARM, 33);
        addDamage(Weapon.MAGNUM, BodyPart.RIGHT_ARM, 33);
        addDamage(Weapon.MAGNUM, BodyPart.GROIN, 57);
        addDamage(Weapon.MAGNUM, BodyPart.BUTT, 52);
        addDamage(Weapon.MAGNUM, BodyPart.LEFT_UPPER_LEG, 40);
        addDamage(Weapon.MAGNUM, BodyPart.RIGHT_UPPER_LEG, 40);
        addDamage(Weapon.MAGNUM, BodyPart.LEFT_LOWER_LEG, 33);
        addDamage(Weapon.MAGNUM, BodyPart.RIGHT_LOWER_LEG, 33);
        addDamage(Weapon.MAGNUM, BodyPart.LEFT_FOOT, 25);
        addDamage(Weapon.MAGNUM, BodyPart.RIGHT_FOOT, 25);

        // --- SPAS-12 ---
        addDamage(Weapon.SPAS, BodyPart.HEAD, 100);
        addDamage(Weapon.SPAS, BodyPart.HELMET, 80);
        addDamage(Weapon.SPAS, BodyPart.TORSO, 80);
        addDamage(Weapon.SPAS, BodyPart.KEVLAR, 40);
        addDamage(Weapon.SPAS, BodyPart.LEFT_ARM, 32);
        addDamage(Weapon.SPAS, BodyPart.RIGHT_ARM, 32);
        addDamage(Weapon.SPAS, BodyPart.GROIN, 59);
        addDamage(Weapon.SPAS, BodyPart.BUTT, 59);
        addDamage(Weapon.SPAS, BodyPart.LEFT_UPPER_LEG, 40);
        addDamage(Weapon.SPAS, BodyPart.RIGHT_UPPER_LEG, 40);
        addDamage(Weapon.SPAS, BodyPart.LEFT_LOWER_LEG, 40);
        addDamage(Weapon.SPAS, BodyPart.RIGHT_LOWER_LEG, 40);
        addDamage(Weapon.SPAS, BodyPart.LEFT_FOOT, 40);
        addDamage(Weapon.SPAS, BodyPart.RIGHT_FOOT, 40);

        // --- Benelli M4 ---
        addDamage(Weapon.BENELLI, BodyPart.HEAD, 100);
        addDamage(Weapon.BENELLI, BodyPart.HELMET, 100);
        addDamage(Weapon.BENELLI, BodyPart.TORSO, 90);
        addDamage(Weapon.BENELLI, BodyPart.KEVLAR, 67);
        addDamage(Weapon.BENELLI, BodyPart.LEFT_ARM, 32);
        addDamage(Weapon.BENELLI, BodyPart.RIGHT_ARM, 32);
        addDamage(Weapon.BENELLI, BodyPart.GROIN, 60);
        addDamage(Weapon.BENELLI, BodyPart.BUTT, 50);
        addDamage(Weapon.BENELLI, BodyPart.LEFT_UPPER_LEG, 35);
        addDamage(Weapon.BENELLI, BodyPart.RIGHT_UPPER_LEG, 35);
        addDamage(Weapon.BENELLI, BodyPart.LEFT_LOWER_LEG, 30);
        addDamage(Weapon.BENELLI, BodyPart.RIGHT_LOWER_LEG, 30);
        addDamage(Weapon.BENELLI, BodyPart.LEFT_FOOT, 20);
        addDamage(Weapon.BENELLI, BodyPart.RIGHT_FOOT, 20);

        // --- MP5K ---
        addDamage(Weapon.MP5K, BodyPart.HEAD, 50);
        addDamage(Weapon.MP5K, BodyPart.HELMET, 34);
        addDamage(Weapon.MP5K, BodyPart.TORSO, 30);
        addDamage(Weapon.MP5K, BodyPart.KEVLAR, 20);
        addDamage(Weapon.MP5K, BodyPart.LEFT_ARM, 11);
        addDamage(Weapon.MP5K, BodyPart.RIGHT_ARM, 11);
        addDamage(Weapon.MP5K, BodyPart.GROIN, 22);
        addDamage(Weapon.MP5K, BodyPart.BUTT, 20);
        addDamage(Weapon.MP5K, BodyPart.LEFT_UPPER_LEG, 15);
        addDamage(Weapon.MP5K, BodyPart.RIGHT_UPPER_LEG, 15);
        addDamage(Weapon.MP5K, BodyPart.LEFT_LOWER_LEG, 13);
        addDamage(Weapon.MP5K, BodyPart.RIGHT_LOWER_LEG, 13);
        addDamage(Weapon.MP5K, BodyPart.LEFT_FOOT, 11);
        addDamage(Weapon.MP5K, BodyPart.RIGHT_FOOT, 11);

        // --- UMP45 ---
        addDamage(Weapon.UMP, BodyPart.HEAD, 100);
        addDamage(Weapon.UMP, BodyPart.HELMET, 51);
        addDamage(Weapon.UMP, BodyPart.TORSO, 44);
        addDamage(Weapon.UMP, BodyPart.KEVLAR, 29);
        addDamage(Weapon.UMP, BodyPart.LEFT_ARM, 17);
        addDamage(Weapon.UMP, BodyPart.RIGHT_ARM, 17);
        addDamage(Weapon.UMP, BodyPart.GROIN, 31);
        addDamage(Weapon.UMP, BodyPart.BUTT, 28);
        addDamage(Weapon.UMP, BodyPart.LEFT_UPPER_LEG, 21);
        addDamage(Weapon.UMP, BodyPart.RIGHT_UPPER_LEG, 21);
        addDamage(Weapon.UMP, BodyPart.LEFT_LOWER_LEG, 17);
        addDamage(Weapon.UMP, BodyPart.RIGHT_LOWER_LEG, 17);
        addDamage(Weapon.UMP, BodyPart.LEFT_FOOT, 14);
        addDamage(Weapon.UMP, BodyPart.RIGHT_FOOT, 14);

        // --- MAC-11 ---
        addDamage(Weapon.MAC11, BodyPart.HEAD, 50);
        addDamage(Weapon.MAC11, BodyPart.HELMET, 29);
        addDamage(Weapon.MAC11, BodyPart.TORSO, 20);
        addDamage(Weapon.MAC11, BodyPart.KEVLAR, 16);
        addDamage(Weapon.MAC11, BodyPart.LEFT_ARM, 13);
        addDamage(Weapon.MAC11, BodyPart.RIGHT_ARM, 13);
        addDamage(Weapon.MAC11, BodyPart.GROIN, 16);
        addDamage(Weapon.MAC11, BodyPart.BUTT, 15);
        addDamage(Weapon.MAC11, BodyPart.LEFT_UPPER_LEG, 15);
        addDamage(Weapon.MAC11, BodyPart.RIGHT_UPPER_LEG, 15);
        addDamage(Weapon.MAC11, BodyPart.LEFT_LOWER_LEG, 13);
        addDamage(Weapon.MAC11, BodyPart.RIGHT_LOWER_LEG, 13);
        addDamage(Weapon.MAC11, BodyPart.LEFT_FOOT, 11);
        addDamage(Weapon.MAC11, BodyPart.RIGHT_FOOT, 11);

        // --- P90 ---
        addDamage(Weapon.P90, BodyPart.HEAD, 50);
        addDamage(Weapon.P90, BodyPart.HELMET, 40);
        addDamage(Weapon.P90, BodyPart.TORSO, 33);
        addDamage(Weapon.P90, BodyPart.KEVLAR, 27);
        addDamage(Weapon.P90, BodyPart.LEFT_ARM, 16);
        addDamage(Weapon.P90, BodyPart.RIGHT_ARM, 16);
        addDamage(Weapon.P90, BodyPart.GROIN, 27);
        addDamage(Weapon.P90, BodyPart.BUTT, 25);
        addDamage(Weapon.P90, BodyPart.LEFT_UPPER_LEG, 17);
        addDamage(Weapon.P90, BodyPart.RIGHT_UPPER_LEG, 17);
        addDamage(Weapon.P90, BodyPart.LEFT_LOWER_LEG, 15);
        addDamage(Weapon.P90, BodyPart.RIGHT_LOWER_LEG, 15);
        addDamage(Weapon.P90, BodyPart.LEFT_FOOT, 12);
        addDamage(Weapon.P90, BodyPart.RIGHT_FOOT, 12);

        // --- HK69 (grenade launcher) ---
        addDamage(Weapon.HK69, BodyPart.HEAD, 20);
        addDamage(Weapon.HK69, BodyPart.HELMET, 20);
        addDamage(Weapon.HK69, BodyPart.TORSO, 20);
        addDamage(Weapon.HK69, BodyPart.KEVLAR, 20);
        addDamage(Weapon.HK69, BodyPart.LEFT_ARM, 20);
        addDamage(Weapon.HK69, BodyPart.RIGHT_ARM, 20);
        addDamage(Weapon.HK69, BodyPart.GROIN, 20);
        addDamage(Weapon.HK69, BodyPart.BUTT, 20);
        addDamage(Weapon.HK69, BodyPart.LEFT_UPPER_LEG, 20);
        addDamage(Weapon.HK69, BodyPart.RIGHT_UPPER_LEG, 20);
        addDamage(Weapon.HK69, BodyPart.LEFT_LOWER_LEG, 20);
        addDamage(Weapon.HK69, BodyPart.RIGHT_LOWER_LEG, 20);
        addDamage(Weapon.HK69, BodyPart.LEFT_FOOT, 20);
        addDamage(Weapon.HK69, BodyPart.RIGHT_FOOT, 20);

        // --- LR300 ---
        addDamage(Weapon.LR300, BodyPart.HEAD, 100);
        addDamage(Weapon.LR300, BodyPart.HELMET, 51);
        addDamage(Weapon.LR300, BodyPart.TORSO, 44);
        addDamage(Weapon.LR300, BodyPart.KEVLAR, 29);
        addDamage(Weapon.LR300, BodyPart.LEFT_ARM, 17);
        addDamage(Weapon.LR300, BodyPart.RIGHT_ARM, 17);
        addDamage(Weapon.LR300, BodyPart.GROIN, 31);
        addDamage(Weapon.LR300, BodyPart.BUTT, 28);
        addDamage(Weapon.LR300, BodyPart.LEFT_UPPER_LEG, 20);
        addDamage(Weapon.LR300, BodyPart.RIGHT_UPPER_LEG, 20);
        addDamage(Weapon.LR300, BodyPart.LEFT_LOWER_LEG, 17);
        addDamage(Weapon.LR300, BodyPart.RIGHT_LOWER_LEG, 17);
        addDamage(Weapon.LR300, BodyPart.LEFT_FOOT, 14);
        addDamage(Weapon.LR300, BodyPart.RIGHT_FOOT, 14);

        // --- G36 ---
        addDamage(Weapon.G36, BodyPart.HEAD, 100);
        addDamage(Weapon.G36, BodyPart.HELMET, 51);
        addDamage(Weapon.G36, BodyPart.TORSO, 44);
        addDamage(Weapon.G36, BodyPart.KEVLAR, 29);
        addDamage(Weapon.G36, BodyPart.LEFT_ARM, 17);
        addDamage(Weapon.G36, BodyPart.RIGHT_ARM, 17);
        addDamage(Weapon.G36, BodyPart.GROIN, 31);
        addDamage(Weapon.G36, BodyPart.BUTT, 28);
        addDamage(Weapon.G36, BodyPart.LEFT_UPPER_LEG, 20);
        addDamage(Weapon.G36, BodyPart.RIGHT_UPPER_LEG, 20);
        addDamage(Weapon.G36, BodyPart.LEFT_LOWER_LEG, 17);
        addDamage(Weapon.G36, BodyPart.RIGHT_LOWER_LEG, 17);
        addDamage(Weapon.G36, BodyPart.LEFT_FOOT, 14);
        addDamage(Weapon.G36, BodyPart.RIGHT_FOOT, 14);

        // --- Colt M4A1 ---
        addDamage(Weapon.M4, BodyPart.HEAD, 100);
        addDamage(Weapon.M4, BodyPart.HELMET, 51);
        addDamage(Weapon.M4, BodyPart.TORSO, 44);
        addDamage(Weapon.M4, BodyPart.KEVLAR, 29);
        addDamage(Weapon.M4, BodyPart.LEFT_ARM, 17);
        addDamage(Weapon.M4, BodyPart.RIGHT_ARM, 17);
        addDamage(Weapon.M4, BodyPart.GROIN, 31);
        addDamage(Weapon.M4, BodyPart.BUTT, 28);
        addDamage(Weapon.M4, BodyPart.LEFT_UPPER_LEG, 20);
        addDamage(Weapon.M4, BodyPart.RIGHT_UPPER_LEG, 20);
        addDamage(Weapon.M4, BodyPart.LEFT_LOWER_LEG, 17);
        addDamage(Weapon.M4, BodyPart.RIGHT_LOWER_LEG, 17);
        addDamage(Weapon.M4, BodyPart.LEFT_FOOT, 14);
        addDamage(Weapon.M4, BodyPart.RIGHT_FOOT, 14);

        // --- AK-103 ---
        addDamage(Weapon.AK, BodyPart.HEAD, 100);
        addDamage(Weapon.AK, BodyPart.HELMET, 58);
        addDamage(Weapon.AK, BodyPart.TORSO, 51);
        addDamage(Weapon.AK, BodyPart.KEVLAR, 35);
        addDamage(Weapon.AK, BodyPart.LEFT_ARM, 19);
        addDamage(Weapon.AK, BodyPart.RIGHT_ARM, 19);
        addDamage(Weapon.AK, BodyPart.GROIN, 37);
        addDamage(Weapon.AK, BodyPart.BUTT, 35);
        addDamage(Weapon.AK, BodyPart.LEFT_UPPER_LEG, 22);
        addDamage(Weapon.AK, BodyPart.RIGHT_UPPER_LEG, 22);
        addDamage(Weapon.AK, BodyPart.LEFT_LOWER_LEG, 19);
        addDamage(Weapon.AK, BodyPart.RIGHT_LOWER_LEG, 19);
        addDamage(Weapon.AK, BodyPart.LEFT_FOOT, 15);
        addDamage(Weapon.AK, BodyPart.RIGHT_FOOT, 15);

        // --- Negev LMG ---
        addDamage(Weapon.NEGEV, BodyPart.HEAD, 50);
        addDamage(Weapon.NEGEV, BodyPart.HELMET, 34);
        addDamage(Weapon.NEGEV, BodyPart.TORSO, 30);
        addDamage(Weapon.NEGEV, BodyPart.KEVLAR, 20);
        addDamage(Weapon.NEGEV, BodyPart.LEFT_ARM, 11);
        addDamage(Weapon.NEGEV, BodyPart.RIGHT_ARM, 11);
        addDamage(Weapon.NEGEV, BodyPart.GROIN, 22);
        addDamage(Weapon.NEGEV, BodyPart.BUTT, 20);
        addDamage(Weapon.NEGEV, BodyPart.LEFT_UPPER_LEG, 13);
        addDamage(Weapon.NEGEV, BodyPart.RIGHT_UPPER_LEG, 13);
        addDamage(Weapon.NEGEV, BodyPart.LEFT_LOWER_LEG, 11);
        addDamage(Weapon.NEGEV, BodyPart.RIGHT_LOWER_LEG, 11);
        addDamage(Weapon.NEGEV, BodyPart.LEFT_FOOT, 9);
        addDamage(Weapon.NEGEV, BodyPart.RIGHT_FOOT, 9);

        // --- PSG-1 ---
        addDamage(Weapon.PSG1, BodyPart.HEAD, 100);
        addDamage(Weapon.PSG1, BodyPart.HELMET, 100);
        addDamage(Weapon.PSG1, BodyPart.TORSO, 97);
        addDamage(Weapon.PSG1, BodyPart.KEVLAR, 70);
        addDamage(Weapon.PSG1, BodyPart.LEFT_ARM, 36);
        addDamage(Weapon.PSG1, BodyPart.RIGHT_ARM, 36);
        addDamage(Weapon.PSG1, BodyPart.GROIN, 75);
        addDamage(Weapon.PSG1, BodyPart.BUTT, 70);
        addDamage(Weapon.PSG1, BodyPart.LEFT_UPPER_LEG, 41);
        addDamage(Weapon.PSG1, BodyPart.RIGHT_UPPER_LEG, 41);
        addDamage(Weapon.PSG1, BodyPart.LEFT_LOWER_LEG, 36);
        addDamage(Weapon.PSG1, BodyPart.RIGHT_LOWER_LEG, 36);
        addDamage(Weapon.PSG1, BodyPart.LEFT_FOOT, 29);
        addDamage(Weapon.PSG1, BodyPart.RIGHT_FOOT, 29);

        // --- SR8 ---
        addDamage(Weapon.SR8, BodyPart.HEAD, 100);
        addDamage(Weapon.SR8, BodyPart.HELMET, 100);
        addDamage(Weapon.SR8, BodyPart.TORSO, 100);
        addDamage(Weapon.SR8, BodyPart.KEVLAR, 100);
        addDamage(Weapon.SR8, BodyPart.LEFT_ARM, 50);
        addDamage(Weapon.SR8, BodyPart.RIGHT_ARM, 50);
        addDamage(Weapon.SR8, BodyPart.GROIN, 100);
        addDamage(Weapon.SR8, BodyPart.BUTT, 97);
        addDamage(Weapon.SR8, BodyPart.LEFT_UPPER_LEG, 60);
        addDamage(Weapon.SR8, BodyPart.RIGHT_UPPER_LEG, 60);
        addDamage(Weapon.SR8, BodyPart.LEFT_LOWER_LEG, 50);
        addDamage(Weapon.SR8, BodyPart.RIGHT_LOWER_LEG, 50);
        addDamage(Weapon.SR8, BodyPart.LEFT_FOOT, 40);
        addDamage(Weapon.SR8, BodyPart.RIGHT_FOOT, 40);

        // --- FR-F1 ---
        addDamage(Weapon.FRF1, BodyPart.HEAD, 100);
        addDamage(Weapon.FRF1, BodyPart.HELMET, 100);
        addDamage(Weapon.FRF1, BodyPart.TORSO, 90);
        addDamage(Weapon.FRF1, BodyPart.KEVLAR, 75);
        addDamage(Weapon.FRF1, BodyPart.LEFT_ARM, 40);
        addDamage(Weapon.FRF1, BodyPart.RIGHT_ARM, 40);
        addDamage(Weapon.FRF1, BodyPart.GROIN, 77);
        addDamage(Weapon.FRF1, BodyPart.BUTT, 74);
        addDamage(Weapon.FRF1, BodyPart.LEFT_UPPER_LEG, 50);
        addDamage(Weapon.FRF1, BodyPart.RIGHT_UPPER_LEG, 50);
        addDamage(Weapon.FRF1, BodyPart.LEFT_LOWER_LEG, 40);
        addDamage(Weapon.FRF1, BodyPart.RIGHT_LOWER_LEG, 40);
        addDamage(Weapon.FRF1, BodyPart.LEFT_FOOT, 30);
        addDamage(Weapon.FRF1, BodyPart.RIGHT_FOOT, 30);
    }

    private void addDamage(Weapon weapon, BodyPart bodyPart, int damage) {
        this.damages.put(new Hit(weapon, bodyPart), damage);
    }

    public static DamageManager getInstance() {
        return instance;
    }

    public int getDamage(String weaponId, String bodyPartStr) {
        try {
            Weapon weapon = Weapon.fromValue(weaponId);
            BodyPart bodyPart = BodyPart.fromValue(bodyPartStr);
            Integer damage = this.damages.get(new Hit(weapon, bodyPart));
            return damage != null ? damage : 0;
        } catch (Exception e) {
            return 0;
        }
    }

}