package fr.adrienlombard.utlogs;

import java.util.Map;
import java.util.HashMap;

public class Player {
    private String name;
    private int kills;
    private int deaths;
    private int score;
    private int suicides;
    private int killedByWorld;
    private int flagCaptures;
    private int flagReturns;
    private int teamKills;
    private int killedByTeammates;
    private int hitsGiven;
    private int hitsReceived;
    private int damageGiven;
    private int damageReceived;
    private int currentKillStreak;
    private int maxKillStreak;
    private int currentDeathStreak;
    private int maxDeathStreak;
    private Team team;

    private final Map<String, Integer> killsByVictim;
    private final Map<String, Integer> deathsByKiller;
    private final Map<String, Integer> killsByWeapon;
    private final Map<String, Integer> hitsByBodyPart;

    public Player(String name) {
        this.name = name;
        this.kills = 0;
        this.deaths = 0;
        this.score = 0;
        this.suicides = 0;
        this.killedByWorld = 0;
        this.flagCaptures = 0;
        this.flagReturns = 0;
        this.teamKills = 0;
        this.killedByTeammates = 0;
        this.hitsGiven = 0;
        this.hitsReceived = 0;
        this.damageGiven = 0;
        this.damageReceived = 0;
        this.currentKillStreak = 0;
        this.maxKillStreak = 0;
        this.currentDeathStreak = 0;
        this.maxDeathStreak = 0;
        this.team = Team.UNKNOWN;
        this.killsByVictim = new HashMap<>();
        this.deathsByKiller = new HashMap<>();
        this.killsByWeapon = new HashMap<>();
        this.hitsByBodyPart = new HashMap<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
        this.currentKillStreak++;
        if (this.currentKillStreak > this.maxKillStreak) {
            this.maxKillStreak = this.currentKillStreak;
        }
        this.currentDeathStreak = 0;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
        this.currentDeathStreak++;
        if (this.currentDeathStreak > this.maxDeathStreak) {
            this.maxDeathStreak = this.currentDeathStreak;
        }
        this.currentKillStreak = 0;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getSuicides() {
        return suicides;
    }

    public void addSuicide() {
        this.suicides++;
        this.deaths++; // Suicide counts as death
        this.currentDeathStreak++;
        if (this.currentDeathStreak > this.maxDeathStreak) {
            this.maxDeathStreak = this.currentDeathStreak;
        }
        this.currentKillStreak = 0;
    }

    public int getKilledByWorld() {
        return killedByWorld;
    }

    public void addKilledByWorld() {
        this.killedByWorld++;
        this.deaths++; // World kill counts as death
        this.currentDeathStreak++;
        if (this.currentDeathStreak > this.maxDeathStreak) {
            this.maxDeathStreak = this.currentDeathStreak;
        }
        this.currentKillStreak = 0;
    }

    public int getFlagCaptures() {
        return flagCaptures;
    }

    public void addFlagCapture() {
        this.flagCaptures++;
    }

    public int getFlagReturns() {
        return flagReturns;
    }

    public void addFlagReturn() {
        this.flagReturns++;
    }

    public int getTeamKills() {
        return teamKills;
    }

    public void addTeamKill() {
        this.teamKills++;
    }

    public int getKilledByTeammates() {
        return killedByTeammates;
    }

    public void addKilledByTeammates() {
        this.killedByTeammates++;
    }

    public int getHitsGiven() {
        return hitsGiven;
    }

    public void addHitGiven() {
        this.hitsGiven++;
    }

    public int getHitsReceived() {
        return hitsReceived;
    }

    public void addHitReceived() {
        this.hitsReceived++;
    }

    public int getDamageGiven() {
        return damageGiven;
    }

    public void addDamageGiven(int damage) {
        this.damageGiven += damage;
    }

    public int getDamageReceived() {
        return damageReceived;
    }

    public void addDamageReceived(int damage) {
        this.damageReceived += damage;
    }

    public int getMaxKillStreak() {
        return maxKillStreak;
    }

    public int getMaxDeathStreak() {
        return maxDeathStreak;
    }

    public int getCurrentDeathStreak() {
        return currentDeathStreak;
    }

    public void addKillAgainst(String victimName) {
        killsByVictim.merge(victimName, 1, Integer::sum);
    }

    public void addDeathBy(String killerName) {
        deathsByKiller.merge(killerName, 1, Integer::sum);
    }

    public Map<String, Integer> getKillsByVictim() {
        return java.util.Collections.unmodifiableMap(killsByVictim);
    }

    public Map<String, Integer> getDeathsByKiller() {
        return java.util.Collections.unmodifiableMap(deathsByKiller);
    }

    public void addKillWithWeapon(String weaponName) {
        killsByWeapon.merge(weaponName, 1, Integer::sum);
    }

    public Map<String, Integer> getKillsByWeapon() {
        return java.util.Collections.unmodifiableMap(killsByWeapon);
    }

    public void addHitToBodyPart(String bodyPart) {
        hitsByBodyPart.merge(bodyPart, 1, Integer::sum);
    }

    public Map<String, Integer> getHitsByBodyPart() {
        return java.util.Collections.unmodifiableMap(hitsByBodyPart);
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", score=" + score +
                ", team=" + team +
                '}';
    }
}
