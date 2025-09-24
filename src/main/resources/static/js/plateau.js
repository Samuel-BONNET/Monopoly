const boardDiv = document.getElementById("board");
let plateau = [];
let joueurs = [];

async function loadPlateau() {
    const res = await fetch("/api/plateau");
    const data = await res.json();
    plateau = data.plateau; // <-- MAJ du global
    drawPlateau();
}

async function loadJoueurs() {
    const res = await fetch("/api/joueurs");
    joueurs = await res.json();
    drawPions();
}

let caseDivs = {}; // global

function drawPlateau() {
    boardDiv.innerHTML = "";
    caseDivs = {}; // reset

    plateau.forEach((c, i) => {
        const div = document.createElement("div");
        div.className = "case";
        div.textContent = c.nom;

        // i va de 0 à 39 → parfait pour getCasePosition
        const pos = getCasePosition(i);

        div.style.position = "absolute";
        div.style.left = pos.x + "px";
        div.style.top = pos.y + "px";

        boardDiv.appendChild(div);
        caseDivs[c.id] = div;
    });
}

function getCasePosition(numCase) {
    const size = 60;
    const topBottom = 11; // cases en haut et bas
    const sides = 9;      // cases sur les côtés
    const total = topBottom * size;

    let x = 0, y = 0;

    if (numCase === 0) { // Départ / GO (coin bas-droit)
        x = total - size;
        y = total - size;
    } else if (numCase > 0 && numCase < 10) { // ligne du bas, droite → gauche
        x = total - size * (numCase + 1);
        y = total - size;
    } else if (numCase === 10) { // Prison (coin bas-gauche)
        x = 0;
        y = total - size;
    } else if (numCase > 10 && numCase < 20) { // colonne gauche, bas → haut
        const idx = numCase - 11; // 0 à 8
        x = 0;
        y = total - size - size * (idx + 1); // cases juste au-dessus de la Prison
    } else if (numCase === 20) { // coin haut-gauche
        x = 0;
        y = 0;
    } else if (numCase > 20 && numCase < 30) { // ligne du haut, gauche → droite
        const idx = numCase - 21; // 0 à 8
        x = size * (idx + 1);
        y = 0;
    } else if (numCase === 30) { // coin haut-droit
        x = total - size;
        y = 0;
    } else if (numCase > 30 && numCase < 39) { // colonne droite, haut → bas
        const idx = numCase - 31; // 0 à 7
        x = total - size;
        y = size * (idx + 1); // cases 31 → 38
    } else if (numCase === 39) { // Rue de la Paix / Parc Gratuit (juste avant Départ)
        x = total - size;
        y = total - 2 * size;
    }

    return { x, y };
}

function drawPions() {
    document.querySelectorAll(".pion").forEach(e => e.remove());

    joueurs.forEach(j => {
        const div = caseDivs[j.caseActuelle];
        if (div) {
            const pionSpan = document.createElement("span");
            pionSpan.className = "pion";
            pionSpan.textContent = j.pion;
            div.appendChild(pionSpan);
        }
    });
}

// Lancer dés
document.getElementById("rollBtn").addEventListener("click", async () => {
    try {

        const res = await fetch("/api/roll", { method: "POST" });
        const nb = await res.json();

        console.log("Nombre de cases depuis Java :", nb);

        const resTour = await fetch("/api/tourJoueur");
        const tourJoueur = await resTour.json();

        await fetch(`/api/deplacer/${tourJoueur}/${nb}`, { method: 'POST' });

        await loadJoueurs();

    } catch (err) {
        console.error("Erreur lors du lancer de dés :", err);
    }
});

// Fin de tour
document.getElementById("endTurnBtn").addEventListener("click", async() => {
    try{
        const res = await fetch("/api/finTour", { method: "POST" });

        if (!res.ok) {
            throw new Error(`Erreur serveur : ${res.status}`);
        }

        const joueurCourant = await res.json();
        console.log("Nouveau joueur :", joueurCourant.nom);

        await loadJoueurs();

        document.getElementById("currentPlayer").textContent =
            `C'est au tour de ${joueurCourant.nom}`;

    } catch(err){
        console.error("Erreur lors de la fin de tour :", err);
    }
})

// Achat
document.getElementById("achatBtn").addEventListener("click", async () => {
    try {
        // Recupérer le joueur qui doit jouer
        const jRes = await fetch("/api/joueurAJouer", { method: "GET" });
        if (!jRes.ok) throw new Error("Impossible de récupérer le joueur actuel");
        const joueur = await jRes.json()

        // Recupérer nom & case du joueur
        const nomJoueur = joueur.nom || "Joueur inconnu";
        const caseActu = joueur.caseActuelle;
        console.log(`Joueur actuel : ${nomJoueur} (case ${caseActu})`);

        // Verifie si la case est bien une propriété achetable ( Propriété, Gare ou Compagnie )
        const verifProprieteRes = await fetch(`/api/estPropriete/${caseActu}`, { method: "GET" });
        if (!verifProprieteRes.ok) throw new Error("Erreur serveur pour la vérification de la propriété");
        const estPropriete = await verifProprieteRes.json();

        if (!estPropriete) {
            console.warn("La case actuelle n'est pas une propriété achetable");
            return;
        }

        console.info("La case actuelle est une propriété achetable");

        // Recupérer la case & nom de la case actuelle
        const pRes = await fetch(`/api/plateau/${caseActu}`);
        if (!pRes.ok) throw new Error("Impossible de récupérer la propriété actuelle");
        const propriete = await pRes.json();
        const nomPropriete = propriete.nom ?? propriete.name ?? "Propriété inconnue";

        const prixPropriete = propriete.prixAchat;

        if(joueur.capitalTotal < prixPropriete){
            console.warn("Capital insuffisant pour acheter cette propriété");
            document.getElementById("message").textContent = `${nomJoueur} n'a pas assez de capital pour acheter ${nomPropriete} (nécessite ${prixPropriete}, fond du joueur : ${joueur.capitalTotal})`;
            document.getElementById("message").classList.add("error");
            return;
        }

        // Effectue l'achat
        const buyRes = await fetch(`/api/buy/${caseActu}`, { method: "POST" });
        if (!buyRes.ok) throw new Error(`Erreur serveur lors de l'achat : ${buyRes.status}`);

        console.log(`${nomJoueur} a acheté la propriété ${nomPropriete}`);

        document.getElementById("message").textContent = `${nomJoueur} a acheté la propriété : ${nomPropriete}`;
        document.getElementById("message").classList.add("success");

        await loadJoueurs();

    } catch (err) {
        console.error("Erreur lors de l'achat :", err);
    }
});


// todo : menu, affichage cartes cases, & decr capital, afficher sold joueurs


loadPlateau();
loadJoueurs();
