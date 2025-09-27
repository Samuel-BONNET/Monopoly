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
    const size = 60, total = 11 * size;
    let x = 0, y = 0;
    if (numCase === 0) { x = total - size; y = total - size; }
    else if (numCase > 0 && numCase < 10) { x = total - size * (numCase + 1); y = total - size; }
    else if (numCase === 10) { x = 0; y = total - size; }
    else if (numCase > 10 && numCase < 20) { x = 0; y = total - size - size * (numCase - 10); }
    else if (numCase === 20) { x = 0; y = 0; }
    else if (numCase > 20 && numCase < 30) { x = size * (numCase - 20); y = 0; }
    else if (numCase === 30) { x = total - size; y = 0; }
    else if (numCase > 30 && numCase < 39) { x = total - size; y = size * (numCase - 30); }
    else if (numCase === 39) { x = total - size; y = total - 2 * size; }
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
    const res = await fetch("/api/roll", { method: "POST" });
    const nb = await res.json();

    const resTour = await fetch("/api/tourJoueur");
    const tourJoueur = await resTour.json();

    await fetch(`/api/deplacer/${tourJoueur}/${nb}`, { method: 'POST' });
    await loadJoueurs();

    const joueur = joueurs[tourJoueur];
    document.getElementById("message").innerHTML =
        `Lancé de dés : ${nb}<br>Vous avancez jusqu'à ${joueur.caseActuelle}.`;

    // --- Vérification propriété pour menu achat ---
    const caseActu = joueur.caseActuelle;
    const verifRes = await fetch(`/api/estPropriete/${caseActu}`);
    const estPropriete = await verifRes.json();

    if (estPropriete) {
        showAchatMenu(joueur, caseActu);
    }
    const caseData = plateau[caseActu];
    if (caseData.type === "CaseEvenement" && caseData.nom.toUpperCase().includes("CHANCE")) {
        await caseCarte("CHANCE");
    }
    if (caseData.type === "CaseEvenement" && caseData.nom.toUpperCase().includes("COMMUNAUTE")) {
        await caseCarte("COMMUNAUTE");
    }


});

// --- Mini Menu Achat ---
function showAchatMenu(joueur, caseNum) {
    // Crée le menu
    let menu = document.createElement("div");
    menu.id = "achatMenu";
    menu.style.position = "absolute";
    menu.style.top = "50%";
    menu.style.left = "50%";
    menu.style.transform = "translate(-50%, -50%)";
    menu.style.background = "#fff";
    menu.style.border = "2px solid #000";
    menu.style.padding = "20px";
    menu.style.zIndex = "200";
    menu.style.textAlign = "center";

    menu.innerHTML = `
        <p>${joueur.nom}, voulez-vous acheter cette propriété ?</p>
        <button id="achatOui">Oui</button>
        <button id="achatNon">Non</button>
    `;

    document.body.appendChild(menu);

    document.getElementById("achatOui").addEventListener("click", async () => {
        // Achat côté serveur
        const res = await fetch(`/api/buy/${caseNum}`, { method: "POST" });
        if (res.ok) {
            document.getElementById("message").textContent = `${joueur.nom} a acheté la propriété !`;
            await loadJoueurs();
        } else {
            document.getElementById("message").textContent = `Erreur lors de l'achat`;
        }
        menu.remove();
    });

    document.getElementById("achatNon").addEventListener("click", () => {
        menu.remove();
        document.getElementById("message").textContent = `${joueur.nom} passe son tour.`;
    });
}

// --- Fin de tour ---
document.getElementById("endTurnBtn").addEventListener("click", async () => {
    await fetch("/api/finTour", { method: "POST" });
    await loadJoueurs();
    const joueurRes = await fetch("/api/joueurAJouer");
    const joueur = await joueurRes.json();
    document.getElementById("currentPlayer").textContent = `C'est au tour de ${joueur.nom}`;
});

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
