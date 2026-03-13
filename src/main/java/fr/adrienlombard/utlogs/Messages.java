package fr.adrienlombard.utlogs;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for accessing internationalized messages.
 * Uses ResourceBundle to load translations from properties files.
 */
public final class Messages {
    private static final String BASE_NAME = "i18n.messages";
    private static ResourceBundle bundle;

    private Messages() {
        // Utility class, prevent instantiation
    }

    /**
     * Sets the locale for message retrieval.
     * 
     * @param locale the locale to use
     */
    public static void setLocale(final Locale locale) {
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    /**
     * Gets a localized message for the given key.
     * 
     * @param key the message key
     * @return the localized message
     */
    public static String get(final String key) {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BASE_NAME, Locale.getDefault());
        }
        return bundle.getString(key);
    }

    /**
     * Gets a localized message for the given key with parameters.
     * 
     * @param key  the message key
     * @param args the arguments to format into the message
     * @return the formatted localized message
     */
    public static String get(final String key, final Object... args) {
        return MessageFormat.format(get(key), args);
    }

    /**
     * Gets a localized map name for the given map identifier.
     * If no translation exists, returns the original map name.
     * 
     * @param mapName the map identifier (e.g., "ut4_company")
     * @return the localized map name or the original if not found
     */
    public static String getMapName(final String mapName) {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BASE_NAME, Locale.getDefault());
        }
        String key = "map." + mapName;
        try {
            return bundle.getString(key);
        } catch (java.util.MissingResourceException e) {
            // Return original map name if no translation exists
            return mapName;
        }
    }

    /**
     * Gets a localized weapon name for the given weapon identifier.
     * If no translation exists, returns the original weapon name.
     * 
     * @param weaponName the weapon identifier (e.g., "UT_MOD_AK103")
     * @return the localized weapon name or the original if not found
     */
    public static String getWeaponName(final String weaponName) {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BASE_NAME, Locale.getDefault());
        }
        String key = "weapon." + weaponName;
        try {
            return bundle.getString(key);
        } catch (java.util.MissingResourceException e) {
            // Return original weapon name if no translation exists
            return weaponName;
        }
    }
}
