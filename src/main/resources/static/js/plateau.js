const boardDiv = document.getElementById("board");
let plateau = [];
let joueurs = [];
let caseDivs = {};

// --- Plateau & Joueurs ---
async function loadPlateau() {
    const res = await fetch("/api/plateau");
    plateau = await res.json();
    drawPlateau();
}

async function loadJoueurs() {
    const res = await fetch("/api/joueurs");
    joueurs = await res.json();
    drawPions();
}

// --- Dessin plateau et pions ---
function drawPlateau() {
    boardDiv.innerHTML = "";
    caseDivs = {};
    plateau.forEach((c, i) => {
        const div = document.createElement("div");
        div.className = "case";
        div.textContent = c.nom;

        const pos = getCasePosition(i);
        div.style.left = pos.x + "px";
        div.style.top = pos.y + "px";

        boardDiv.appendChild(div);
        caseDivs[i] = div;
    });
}

function drawPions() {
    document.querySelectorAll(".pion").forEach(e => e.remove());
    joueurs.forEach(j => {
        const div = caseDivs[j.caseActuelle];
        if (!div) return;
        const pionSpan = document.createElement("span");
        pionSpan.className = "pion";
        pionSpan.textContent = j.pion;
        div.appendChild(pionSpan);
    });
}

function getCasePosition(numCase) {
    const size = 60;
    const total = 11 * size;
    let x = 0, y = 0;

    switch (true) {
        case (numCase === 0):
            x = total - size;
            y = total - size;
            break;

        case (numCase > 0 && numCase < 10):
            x = total - size * (numCase + 1);
            y = total - size;
            break;

        case (numCase === 10):
            x = 0;
            y = total - size;
            break;

        case (numCase > 10 && numCase < 20):
            x = 0;
            y = total - size - size * (numCase - 10);
            break;

        case (numCase === 20):
            x = 0;
            y = 0;
            break;

        case (numCase > 20 && numCase < 30):
            x = size * (numCase - 20);
            y = 0;
            break;

        case (numCase === 30):
            x = total - size;
            y = 0;
            break;

        case (numCase > 30 && numCase < 39):
            x = total - size;
            y = size * (numCase - 30);
            break;

        case (numCase === 39):
            x = total - size;
            y = total - 2 * size;
            break;

        default:
            console.warn("Numéro de case invalide :", numCase);
            break;
    }

    return { x, y };
}


// --- Init Game ---
async function initGame() {
    await loadPlateau();
    await loadJoueurs();
    const res = await fetch("/api/joueurAJouer");
    if (res.ok) {
        const joueur = await res.json();
        document.getElementById("currentPlayer").textContent = `C'est au tour de ${joueur.nom}`;
    }
}

// --- Lancer dés ---
document.getElementById("rollBtn").addEventListener("click", async () => {
    const resNbRolls = await fetch("/api/nbRoll");
    const nbRolls = await resNbRolls.json();

    if(nbRolls <= 0){
        document.getElementById("message").textContent = "Vous n'avez plus de rolls restants pour ce tour.";
        return;
    }
    else {
        const res = await fetch("/api/roll", {method: "POST"});
        const nb = await res.json();

        document.getElementById("resultat_des_1").textContent = nb[0];
        document.getElementById("resultat_des_2").textContent = nb[1];

        decrNbRoll();

        const resTour = await fetch("/api/tourJoueur");
        const tourJoueur = await resTour.json();

        await fetch(`/api/deplacer/${tourJoueur}/${nb[0] + nb[1]}`, {method: 'POST'});
        await loadJoueurs();

        const joueur = joueurs[tourJoueur];
        const caseNom = plateau[joueur.caseActuelle]?.nom || "une case inconnue";
        document.getElementById("message").textContent = `Vous avancez jusqu'à ${caseNom}.`;




        // --- Vérification propriété pour menu achat ---
        const caseActu = joueur.caseActuelle;
        const verifRes = await fetch(`/api/estPropriete/${caseActu}`);
        const estPropriete = await verifRes.json();

        if (estPropriete) {
            showAchatMenu(joueur, caseActu);
            return;
        }
        const caseData = plateau[caseActu];
        if (caseData.type === "CaseEvenement" && caseData.nom.toUpperCase().includes("CHANCE")) {
            await caseCarte("CHANCE");
            return;
        }
        if (caseData.type === "CaseEvenement" && caseData.nom.toUpperCase().includes("COMMUNAUTE")) {
            await caseCarte("COMMUNAUTE");
            return;
        }
        else{
            await caseEvenement(caseActu);
        }
    }
});

// --- Action Autres Cases ---
async function caseEvenement(caseData){
    const res = await fetch(`/api/actionCase${caseData}`)
    const actionJoueur = res.text();
    document.getElementById("actionCoup").textContent = actionJoueur;
}
async function showAchatMenu(joueur, caseNum) {
    const overlay = document.getElementById("miniMenuOverlay");
    const menu = document.getElementById("miniMenuAchat");
    const txt = document.getElementById("miniMenuText");
    const btnOui = document.getElementById("achatOuiBtn");
    const btnNon = document.getElementById("achatNonBtn");

    if (!overlay || !menu || menu.style.display === "block") return;

    const nomCase = plateau[caseNum]?.nom ?? "cette propriété";
    txt.textContent = `${joueur.nom}, voulez-vous acheter ${nomCase} ?`;

    overlay.style.display = "block";
    menu.style.display = "block";
    menu.style.top = "50%";
    menu.style.left = "50%";
    menu.style.transform = "translate(-50%, -50%)";

    // Désactivation des contrôles
    const controls = ["rollBtn", "endTurnBtn", "achatBtn"];
    controls.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.dataset.wasDisabled = el.disabled ? "true" : "false";
            el.disabled = true;
        }
    });

    btnOui.focus();

    // Empêche la fermeture du menu sur clic overlay
    const onOverlayClick = e => {
        e.stopPropagation();
        e.preventDefault();
    };
    overlay.addEventListener("click", onOverlayClick, { passive: false });

    // Nettoyage global
    const cleanup = () => {
        overlay.style.display = "none";
        menu.style.display = "none";
        overlay.removeEventListener("click", onOverlayClick);
        document.removeEventListener("keydown", keyHandler);

        controls.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.disabled = el.dataset.wasDisabled === "true";
                delete el.dataset.wasDisabled;
            }
        });

        btnOui.removeEventListener("click", onOui);
        btnNon.removeEventListener("click", onNon);
    };

    // Gestion achat
    async function onOui() {
        btnOui.disabled = true;
        btnNon.disabled = true;

        try {
            const res = await fetch(`/api/buy/${caseNum}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ joueurId: joueur.id })
            });

            const txtRes = await res.text();

            if (res.ok) {
                document.getElementById("message").textContent = txtRes;
                await loadJoueurs();
                refreshMoney();
            } else {
                document.getElementById("message").textContent = `Erreur lors de l'achat : ${txtRes}`;
            }
        } catch (err) {
            console.error(err);
            document.getElementById("message").textContent = "Erreur réseau lors de l'achat.";
        } finally {
            cleanup();
        }
    }

    function onNon() {
        document.getElementById("message").textContent = `${joueur.nom} passe son tour.`;
        cleanup();
    }

    // Événements
    btnOui.addEventListener("click", onOui);
    btnNon.addEventListener("click", onNon);

    const keyHandler = e => {
        if (e.key === "Escape") {
            e.preventDefault();
            cleanup();
        }
    };
    document.addEventListener("keydown", keyHandler);
}

// Refresh Money
async function refreshMoney() {
    const moneySpan = document.getElementById("valeurBanque");
    const res = await fetch("api/money")
    if (!res.ok) {
        console.error("Erreur lors de la récupération de l'argent");
        return;
    }
    else{
        moneySpan.textContent =  await res.text();
    }

}

// --- Fin de tour ---
document.getElementById("endTurnBtn").addEventListener("click", async () => {
    await fetch("/api/finTour", { method: "POST" });
    await loadJoueurs();
    incrNbRoll();
    const joueurRes = await fetch("/api/joueurAJouer");
    const joueur = await joueurRes.json();
    document.getElementById("currentPlayer").textContent = `C'est au tour de ${joueur.nom}`;

});

async function incrNbRoll() {
    const nbRollsP = document.getElementById("nbRollRestant");
    const res = await fetch("api/incrNbRoll");
    if (!res.ok) {
        console.error("Erreur lors de l'incrémentation du nombre de rolls");
        return;
    } else {
        nbRollsP.textContent = await res.text();
    }
}

async function decrNbRoll() {
    const nbRollsP = document.getElementById("nbRollRestant");
    const res = await fetch("api/decrNbRoll");
    if (!res.ok) {
        console.error("Erreur lors de la décrémentation du nombre de rolls");
        return;
    } else {
        nbRollsP.textContent = await res.text();
    }
}

async function caseCarte(caseType) {
    if (caseType === "CHANCE") {
        const response = await fetch("/api/chance");
        if (!response.ok) {
            console.error("Erreur lors de la récupération d'une carte Chance");
            return;
        }
        const carte = await response.json();
        afficherCarte(carte, "chance");
    }

    if (caseType === "COMMUNAUTE") {
        const response = await fetch("/api/communaute");
        if (!response.ok) {
            console.error("Erreur lors de la récupération d'une carte Caisse de Communauté");
            return;
        }
        const carte = await response.json();
        afficherCarte(carte, "communaute");
    }
}

function afficherCarte(carte, type) {
    const modal = document.getElementById("carteModal");
    const titre = document.getElementById("carteTitre");
    const description = document.getElementById("carteDescription");
    const boutonOk = document.getElementById("carteOk");

    titre.textContent = (type === "chance" ? "Carte Chance" : "Carte Communauté");
    description.textContent = carte.description;

    modal.style.display = "flex";

    boutonOk.onclick = async () => {
        await appliquerActionCarte(carte, type);
        modal.style.display = "none";
    };
}

async function appliquerActionCarte(carte, type) {
    const url = type === "chance" ? "/api/ActionCarteChance" : "/api/ActionCarteCommunaute";
    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(carte)
    });

    if (!response.ok) {
        console.error("Erreur lors de l'application de la carte", await response.text());
    } else {
        console.log("Carte appliquée avec succès !");
        await refreshGameState();
    }
}


document.addEventListener("DOMContentLoaded", initGame);
