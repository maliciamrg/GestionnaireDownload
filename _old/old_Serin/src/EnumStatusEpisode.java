
/**
 * @author Malicia
 *
 */
public enum EnumStatusEpisode
{
	// Il faut appeler l'un des constructeurs d�clar�s :
	avenir("avenir",10),
	absent("absent",20),
	encours("encours",30),
	present("present",40);
	
	// Membres :
	private final String nom;
	private final Integer numOrdre;
	

	EnumStatusEpisode(String nom, Integer numOrdre)
	{
		this.nom = nom;
		this.numOrdre=numOrdre;
	}

	public String getNom(){ return this.nom; }
	public Integer getNumOrdre()
	{
		return numOrdre;
	};
	
	public static EnumTypeDeSerie getStatusEpisode(String statusEpisode) {
		for (EnumTypeDeSerie c : EnumTypeDeSerie.values()) {
			if (c.name().equals(statusEpisode)) {
				return c;
			}
		}
		return null;
	}
}


