<div class="entete" >
<?php
  include('interfaceweb/entete.htm');  // Nous appelons l'entete du site
?>
</div>
<div class="colonneGauche" >
<?php
  include('interfaceweb/menu.htm');   // Nous appelons notre menu
?>
</div>
<div id="contenu">
<div class="colonneDroite">
<?php

  // On définit le tableau contenant les pages autorisées
  // ----------------------------------------------------
  $pageOK = array('info' => 'log4j_info.html',
				  'debug' => 'log4j_debug.html',
				  'warn' => 'log4j_warn.html',
				  'error' => 'log4j_error.html',
				  'addserie' => 'interfaceweb/MajSeries.php',
				  'listseries' => 'interfaceweb/ListSerie.php',
				  'listeepisodes' => 'interfaceweb/ListEpisodes.php',
				  'listhash' => 'interfaceweb/ListHash.php',
				  'status' => 'interfaceweb/StatusAutres.php',
				  'purge' => 'interfaceweb/ForcePurgeFilm.php',
				  'log' => 'Log/GestionnaireDownload'.date ("Y-m-d").'.html',
                  );

  // On teste que le paramètre d'url existe et qu'il est bien autorisé
  // -----------------------------------------------------------------
  if ( (isset($_GET['page'])) && (isset($pageOK[$_GET['page']])) ) {
    include($pageOK[$_GET['page']]);   // Nous appelons le contenu central de la page
  } else {
    include('interfaceweb/acceuil.php');   // Page par défaut quant elle n'existe pas dans le tableau
  }

?>
</div> 
</div> 
<div class="pied">
<?php
  include('interfaceweb/pied.htm');   // Nous appelons le pied de page
?>
</div> 