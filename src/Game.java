/*
RESTE A FAIRE :

-les impots

OPTIONNEL :

-augmenter le pannel d'action (avoir la pocibilité de faire du marketing, gérer les matière premiere, etc ...)
-changer les images
-faire des animations
*/

import extensions.File;
import extensions.CSVFile;

class Game extends Program {
    final String PATH = "../ressources/";
    final String SAVEPATH = PATH + "saves/";

    final int PRIX_MACHINE  = 8000;                          
    final int PV_INITIAL    = 300;                           
    final double QT_PRODUCT = 40.0;                          
    final double TAUX_USURE = 0.02;                        
    final double[] COEF_TIER = new double[]{1.0, 1.6, 2.7}; 
    final int PRIX_REPARATION = 800;
    final int[] ZONE_MACHINE = new int[]{3, 5, 7, 9, 11, 13, 15};
    final int[] TAILLE_MENU_IG = tailleDesLignes(PATH + "MenuJeu.txt");
    final int PRIX_MIN = 150;
    final int PRIX_MAX = 420;
    final String SEPARATOR = ";";

    void sauvegarde(Societe s){
        sauveBatiments(s.usine);
        sauveJoueur(s);
    }

    void sauveBatiments(Batiment b){
        String[][] tab = new String[length(b.places,1)][length(b.places,2)];
        for(int i=0; i<length(tab,1); i++){
            for(int j=0; j<length(tab,2); j++){
                if(b.places[i][j] != null){
                    tab[i][j] = toString(b.places[i][j]);
                } else {
                    tab[i][j] = "null";
                }
            }
        }
        saveCSV(tab, SAVEPATH + "SBat.csv");
    }

    String toString(Machine m){
        return m.tierMachine + SEPARATOR + 
                m.prixMachine + SEPARATOR +
                m.pointDeVie + SEPARATOR +
                m.quantiteProduction + SEPARATOR +
                m.tauxDUsure;
    }

    void sauveJoueur(Societe s){
        String[][] res = new String[6][1];
        res[0][0] = s.name;
        res[1][0] = s.capital + "";
        res[2][0] = s.prix + "";
        res[3][0] = s.stock + "";
        res[4][0] = s.nbTour + "";
        res[5][0] = s.coefPalierActuel + "";
        saveCSV(res, SAVEPATH + "SJoueur.csv");
    }

    Societe chargerSave(){
        CSVFile f = loadCSV(SAVEPATH + "SJoueur.csv");
        Societe s = newSociete(getCell(f,0,0));
        s.capital = stringToInt(getCell(f,1,0));
        s.prix = stringToInt(getCell(f,2,0));
        s.stock = stringToInt(getCell(f,3,0));
        s.nbTour = stringToInt(getCell(f,4,0));
        s.coefPalierActuel = stringToDouble(getCell(f,5,0));
        s.usine = chargeUsine();
        
        return s;
    }

    Batiment chargeUsine(){
        CSVFile f = loadCSV(SAVEPATH + "SBat.csv");
        Batiment b = newBatiment(rowCount(f));
        for(int i=0; i<rowCount(f); i++){
            for(int j=0; j<columnCount(f); j++){
                if(!equals("null", getCell(f,i,j))){
                    String[] infoMachine = reconstMachine(getCell(f,i,j));
                    b.places[i][j] = new Machine();
                    b.places[i][j].tierMachine = stringToInt(infoMachine[0]);
                    b.places[i][j].prixMachine = stringToInt(infoMachine[1]);
                    b.places[i][j].pointDeVie = stringToInt(infoMachine[2]);
                    b.places[i][j].quantiteProduction = stringToInt(infoMachine[3]);
                    b.places[i][j].tauxDUsure = stringToDouble(infoMachine[4]);
                }
            }
        }

        return b;
    }

    String[] reconstMachine(String info){
        String[] res = new String[5];
        int idx = 0, lastIdx = 0, idxTab = 0;
        while(idx < length(info)){
            if(charAt(info, idx) == charAt(SEPARATOR, 0)){
                res[idxTab] = substring(info, lastIdx, idx);
                idxTab++;
                lastIdx = idx + 1;
            }
            idx++;
        }
        res[idxTab] = substring(info, lastIdx, idx);
        return res;
    }

    int[] tailleDesLignes(String nomFichier) {
        int[] tabValeur = new int[9];
        int i = 0;
        int marqueur = 0;
        int numeroCaseTab = 0;
        String txt = afficherFile(newFile(nomFichier));

        while (i < length(txt) && numeroCaseTab <= 8) {
            if (charAt(txt,i) == '\n') {
                tabValeur[numeroCaseTab] = length(substring(txt,marqueur,i));
                marqueur = i + 1;
                numeroCaseTab = numeroCaseTab + 1;
            }

            i = i + 1;
        }
        return tabValeur;
    }
    
   
    boolean reparerMachines(Societe joueur, int ligne, int col){
        println("je suis la");
        double prixRepairMachine = PRIX_REPARATION * (joueur.usine.places[ligne][col].pointDeVie / (PV_INITIAL * COEF_TIER[joueur.usine.places[ligne][col].tierMachine]));

        if(joueur.usine.places[ligne][col] != null && joueur.capital >= prixRepairMachine){
            joueur.capital = joueur.capital - (int) (prixRepairMachine);
            joueur.usine.places[ligne][col].pointDeVie = (int) (PV_INITIAL * COEF_TIER[joueur.usine.places[ligne][col].tierMachine]);
            println("réparation terminée");
            return true;
        } else {
            println("Impossible de réparer la machine pour le moment ou il n'y a pas de machine a cet emplacement");
            return false;
        }
    }

    boolean changePrix(Societe joueur, int newPrix){
        if(newPrix >= PRIX_MIN && newPrix <= PRIX_MAX){
            joueur.prix = newPrix;
            println("Le prix de vente de vos produits est de " + joueur.prix + " €.");
            return true;
        } else {
            println("Le prix proposer n'est pas acceptable, veuillez indiquez un prix entre 150€ et 420€");
            return false;
        }
    }

    void vendreStock(Societe joueur) {
        int preAlea = (int) (random() * 100) + 1;
        double alea = preAlea/100.0;
        int stockAVendre = (int) (joueur.stock * alea);

        joueur.capital = joueur.capital + (stockAVendre*joueur.prix);
        joueur.stock = joueur.stock - stockAVendre;

        println("vous avez vendu " + stockAVendre + " produits ce mois !\n" +
                "Il vous reste " + joueur.stock + " produits en stocks.");
        delay(2600);
    }
    
    void creerStock(Societe joueur) {
        joueur.stock = joueur.stock + quantiteProductionTotal(joueur);
    }


    int quantiteProductionTotal(Societe joueur) {
        int prodTotal = 0;
       for(int i = 0; i<length(joueur.usine.places, 1); i++) {
            for(int j = 0; j<length(joueur.usine.places, 2); j++) {
                if(joueur.usine.places[i][j] != null){
                    prodTotal = prodTotal + joueur.usine.places[i][j].quantiteProduction;
                }
            }
        }
        return prodTotal;
    }

    boolean vendreMachine(Societe joueur, int ligne, int col){
        if(joueur.usine.places[ligne][col] != null){
            joueur.capital = joueur.capital + (int) (PRIX_MACHINE * COEF_TIER[joueur.usine.places[ligne][col].tierMachine-1]);
            joueur.usine.places[ligne][col] = null;
            joueur.usine.nbMachine--;
            println("Il vous reste " + joueur.usine.nbMachine + " machine(s).");
            return true;
        } else {
            println("il n'y a aucune machine a cet emplacement");
            return false;
        }
    }


    boolean acheterMachine(Societe joueur, int tier){
        if(joueur.capital >= (int) (PRIX_MACHINE * COEF_TIER[tier-1])){
            joueur.capital = joueur.capital - (int) (PRIX_MACHINE * COEF_TIER[tier-1]);
            boolean res = placerMachine(joueur, tier);
            if (res) {
                println("La machine a bien été acheté et installé dans votre usine.");
                return true;
            }
            else {
                println("La machine n'a pu être installé et l'argent vous a été remboursé.");
                joueur.capital = joueur.capital + (int) (PRIX_MACHINE * COEF_TIER[tier-1]);
                return false;
            }
        } 
        else {
            println("Fond insuffisant pour acheter cette machine.");
            return false;
        }
    }

    boolean placerMachine(Societe joueur, int tier) {
        int x = 0;
        int y = 0;
        boolean estPlacee = false;

        while(!estPlacee && x < length(joueur.usine.places, 1)) {
            if(joueur.usine.places[x][y] == null) {
                joueur.usine.places[x][y] = newMachine(tier, COEF_TIER[tier-1]);
                estPlacee = true;
            }
            y++;
            if (y == length(joueur.usine.places, 2)) {
                x++;
                y=0;
            }
        }
        if(estPlacee){
            joueur.usine.nbMachine++;
        }
        
        return estPlacee;
    }

    void palierSuivant(Societe joueur) {
        int pActuel = palierActuel(joueur);
        if(joueur.capital >= joueur.progress.prixPalier[pActuel-1]) {
            if(pActuel<7) {
                joueur.coefPalierActuel = joueur.progress.coefPalier[pActuel];
                reinitSociete(joueur, pActuel);
                println("Félicitation pour l'achat de votre nouvelle usine !!");
            }
            else {
                println("Vous êtes déjà au dernier palier.");
            }
        }
        else {
            println("Vous n'avez pas assez d'argent pour cela.");
        }
    }

    int palierActuel(Societe joueur) {
        int i = 0;
        while(joueur.progress.b[i]) {
            i++;
        }
        return i;
    }

    Machine newMachine(int tiers, double coef){
        Machine m = new Machine();
        m.tierMachine = tiers;
        m.prixMachine = (int) (PRIX_MACHINE * coef);
        m.pointDeVie  = (int) (PV_INITIAL * coef);
        m.quantiteProduction = (int) (QT_PRODUCT * coef);
        m.tauxDUsure = (int) (TAUX_USURE * coef * 10000) / 10000.0;
        return m; 
    }

    Batiment newBatiment(int taille){
        Batiment b = new Batiment();
        b.places = new Machine[taille][taille];
        return b;
    }

    Societe newSociete(String name){
        Societe s = new Societe();
        s.name = name;
        s.progress = new Progression();
        s.usine = newBatiment(ZONE_MACHINE[0]);
        s.usine.places[0][0] = newMachine(1, COEF_TIER[0]);
        s.usine.nbMachine++;
        s.coefPalierActuel = s.progress.coefPalier[0];
        return s;
    }

    void reinitSociete(Societe joueur, int numPalier) {
        joueur.capital = 10000 * (int) joueur.coefPalierActuel; 
        joueur.usine = newBatiment(ZONE_MACHINE[numPalier]);
        joueur.usine.places[0][0] = newMachine(1, COEF_TIER[0]);
        joueur.progress.b[numPalier] = true;
        joueur.stock = 0;
    }

    void afficherUsine(Batiment usine) {
        for(int i = 0; i<length(usine.places, 1); i++) {
            for(int j = 0; j<length(usine.places, 2); j++) {
                print("|");
                if(usine.places[i][j] == null) {
                    print("..|");
                }
                else {
                    print("t" + usine.places[i][j].tierMachine +"|");
                }
            }
            println();
        }
    }
    
    String afficherFile(File f){
        String res = "";
        while(ready(f)){
            res += readLine(f) + "\n";
        }
        return res;
    }
    
    int saisieValide(int entier, int borneInf, int borneSup){
        int value = entier;
        while(value < borneInf || value > borneSup){
            print("choix pas acceptable, reessayez : ");
            value = readInt();
        }
        return value;
    }

    String[] obtenirDonnees(Societe joueur) {
        String[] tab = new String[4];
        tab[0] = " Argent actuel : " + joueur.capital + " €";
        tab[1] = "Nombre de machine : " + joueur.usine.nbMachine;
        tab[2] = "Stock actuel : " + joueur.stock + " marchandises";
        tab[3] = "Quantité de prod par tour : " + quantiteProductionTotal(joueur);
        //possible de rejouter encore 2 info
        return tab;
    }

    int complementADroite(int tailleDepart) {
        final int dis = 80;
        return dis-tailleDepart;
    }

    boolean APerdu(Societe joueur) {
        if (joueur.capital <= (-100000)) {
            return true;
        }
       return false;
    }

    void actionAuto(Societe joueur) {
        if (joueur.nbTour%2 == 0) {
            creerStock(joueur);
        }
        if (joueur.nbTour%4 == 0) {
            impots(joueur);
            vendreStock(joueur);
        }
    }

    void impots(Societe joueur) {
        joueur.capital = (int) ((joueur.capital - 10000) - (joueur.capital*0.1));
    }

    void jeu(Societe joueur){
        boolean end = false;
        while(!end){ 
            boolean roundEnd = false;
            joueur.nbTour++;
            actionAuto(joueur);
            delay(2600);
            while(!roundEnd){ 
                if (APerdu(joueur)) {
                    println("Vous avez perdu !!");
                }
                affichageTour(joueur);
                switch(saisieValide(readInt(), 1, 6)) {
                    case 1: //vente machine
                        affichageC1(joueur);
                        break;
                    case 2: //Achat machine
                        affichageC2(joueur);
                        break;
                    case 3: //reparer machine
                        affichageC3(joueur);
                        break;
                    case 4: //tour suivant
                        roundEnd = true;
                        affichageC4(joueur);
                        break;
                    case 5: //palier suivant
                        affichageC5(joueur);
                        break;
                    case 6: //save
                        sauvegarde(joueur);
                        roundEnd = true;
                        end = true;
                        break; 
                }
            }
        }
    }

    void affichageTour(Societe joueur) {
        clearScreen();
        println(afficherFile(newFile(PATH + "Immeuble.txt")) + "\n\n");
        println(afficherDonnées(afficherFile(newFile(PATH + "MenuJeu.txt")), joueur));

    }

    void affichageC1(Societe joueur) {
        int ligne, col;
        afficherUsine(joueur.usine);
        println("Quelle machine voulez vous vendre ?");
        print("la ligne : ");
        ligne = saisieValide(readInt(), 1, length(joueur.usine.places));
        println();
        print("la colonne : ");
        col = saisieValide(readInt(), 1, length(joueur.usine.places));
        println();
        vendreMachine(joueur, ligne-1, col-1);
        delay(2600);
    }

    void affichageC2(Societe joueur) {
        println("Quelle tier de machine voulez vous acheter ? (1, 2 ou 3)");
        print("votre choix : ");
        acheterMachine(joueur, saisieValide(readInt(), 1, 3));
        delay(2600);
    }

    void affichageC3(Societe joueur) {
        int ligne, col;
        afficherUsine(joueur.usine);
        println("Quelle tier de machine voulez vous réparer ?");
        print("la ligne : ");
        ligne = saisieValide(readInt(), 0, length(joueur.usine.places)-1);
        println();
        print("la colonne : ");
        col = saisieValide(readInt(), 0, length(joueur.usine.places)-1);
        println();
        reparerMachines(joueur, ligne, col);
        delay(2600);
    }

    void affichageC4(Societe joueur) {
        println("Fin du tour " + joueur.nbTour);
        delay(2600);
    }
    
    void affichageC5(Societe joueur) {
        println("Non");
        //palierSuivant(joueur); //TODO
    }

    // donnees joueur , argent, nb machine, taille usine, palier actuel, 
    String afficherDonnées(String menu, Societe joueur) {
        int lig = 1;
        int i = 0;
        int marqueur = 0;
        
        while(i < length(menu)) {
            if(charAt(menu, i) == '\n') {
                String space = "";
                String[] donnees = obtenirDonnees(joueur);
                if(lig>2 && lig<7) {
                    int comp = complementADroite(length(substring(menu, marqueur, i)));
                    for(int j = 0; j < comp; j++) {
                        space = space + ' ';
                    } 

                    menu = substring(menu, 0, i-1) + space + donnees[lig-3] + substring(menu, i, length(menu));
                    i = i + comp + length(donnees[lig-3]);
                }
                marqueur = i +1;
                lig++;
            }
            i++;
        }
        return menu;
    }

    void algorithm(){
        clearScreen();
        println(afficherFile(newFile(PATH + "GameName.txt")) + "\n\n");
        println(afficherFile(newFile(PATH + "MenuPrincipale.txt")));
        
        int choice = saisieValide(readInt(), 1, 5); 
        boolean end = false;
        Societe joueur;
        while(!end) {
            switch(choice) { 
                case 1: // Jouer
                    print("\nChoisissez un nom : ");
                    joueur = newSociete(readString());
                    jeu(joueur);
                    end = true;
                    break;
                case 2: // Charger sauvegarde
                    joueur = chargerSave();
                    jeu(joueur);
                    break;
                case 3: // Règles
                    println();
                    println(afficherFile(newFile(PATH + "Règle.txt")));
                    break;
                case 4: // Stats
                    println("\nstats");
                    break;
                case 5: // Quitter
                    println("\nAu revoir !");
                    end = true; 
                    break;
            }    
        }
    }


    //Test 
        void testNewMachine(){
            for(int i=0; i<3; i++){
                Machine m = newMachine(i+1, COEF_TIER[i]);
                assertEquals(i+1, m.tierMachine);
                assertEquals((int) (PV_INITIAL*COEF_TIER[i]), m.pointDeVie);
                assertEquals((int) (PRIX_MACHINE*COEF_TIER[i]), m.prixMachine);
                assertEquals((int) (QT_PRODUCT*COEF_TIER[i]), m.quantiteProduction);
                assertEquals((int) (TAUX_USURE*COEF_TIER[i]*10000)/10000.0, m.tauxDUsure);
            }
        }

        void testNewBatiment(){
            Batiment b = newBatiment(ZONE_MACHINE[0]);
            b.places[0][0] = newMachine(1, COEF_TIER[0]);
            assertNotEquals(b.places[0][0], null);
        }

        void testAfficherFile(){
            String attemp = "Menu :\n\n1. Jouer\n2. Charger sauvegarde\n3. Règles\n4. Stats\n5. Quitter\n";
            assertEquals(attemp, afficherFile(newFile(PATH + "MenuPrincipale.txt")));
        }

        void testPlacerMachine(){
            Societe s = newSociete("s");
            assertEquals(1, s.usine.nbMachine);
            assertTrue(placerMachine(s, 1));
            assertEquals(2, s.usine.nbMachine);
            for(int i=0; i<7; i++){
                placerMachine(s, 1);
            }
            assertFalse(placerMachine(s, 1));
        }

        void testAcheterMachine(){
            Societe s = newSociete("s");
            assertTrue(acheterMachine(s, 0));
            assertFalse(acheterMachine(s, 2));
        }

        void testVendreMachine(){
            Societe s = newSociete("s");
            assertTrue(vendreMachine(s, 0, 0));
            assertFalse(vendreMachine(s, 0, 0));
        }

        void testCreerStock(){
            Societe s = newSociete("s");
            assertEquals(0, s.stock);
            creerStock(s);
            assertEquals(40, s.stock);
        }

        void testChangePrix(){
            Societe s = newSociete("s");
            assertFalse(changePrix(s, 149));
            assertTrue(changePrix(s, 201));
            assertFalse(changePrix(s, 421));
        }
}