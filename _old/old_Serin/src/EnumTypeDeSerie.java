
/**
 * @author Malicia
 *
 */
public enum EnumTypeDeSerie
{
    // Il faut appeler l'un des constructeurs d�clar�s :
	anime("anime"),
	serie("serie"); 
 
    // Membres :
    private final String nom;

 
    EnumTypeDeSerie(String nom)
    {
        this.nom = nom;
    }
 
    public String getNom(){ return this.nom; }

	public static EnumTypeDeSerie getTypeDeSerie(String typeSerie) {
	    for (EnumTypeDeSerie c : EnumTypeDeSerie.values()) {
	        if (c.name().equals(typeSerie)) {
	            return c;
	        }
	    }
		return null;
	}



};
