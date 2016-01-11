package zpi.squad.app.grouploc;

public enum POISpecies {
    RESTERAUNT, KFC, McDonald, BAR, COFFEE, SHOPPING_MALL, MARKET, NIGHT_CLUB, PARK, STORE;

    public static POISpecies getRightSpecies(String species) {
        if (species.equals("RESTERAUNT"))
            return RESTERAUNT;
        else if (species.equals("KFC"))
            return KFC;
        else if (species.equals("McDonald"))
            return McDonald;
        else if (species.equals("BAR"))
            return BAR;
        else if (species.equals("COFFEE"))
            return COFFEE;
        else if (species.equals("SHOPPING_MALL"))
            return SHOPPING_MALL;
        else if (species.equals("MARKET"))
            return MARKET;
        else if (species.equals("NIGHT_CLUB"))
            return NIGHT_CLUB;
        else if (species.equals("PARK"))
            return PARK;
        else if (species.equals("STORE"))
            return STORE;
        else
            return null;


    }
}
