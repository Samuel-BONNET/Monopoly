const boardDiv = document.getElementById("board");
let plateau = [];
let joueurs = [];
let caseDivs = {};
let joueurCourrant = null;

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
        case (numCase === 0): x = total - size; y = total - size; break;
        case (numCase > 0 && numCase < 10): x = total - size * (numCase + 1); y = total - size; break;
        case (numCase === 10): x = 0; y = total - size; break;
        case (numCase > 10 && numCase < 20): x = 0; y = total - size - size * (numCase - 10); break;
        case (numCase === 20): x = 0; y = 0; break;
        case (numCase > 20 && numCase < 30): x = size * (numCase - 20); y = 0; break;
        case (numCase === 30): x = total - size; y = 0; break;
        case (numCase > 30 && numCase < 39): x = total - size; y = size * (numCase - 30); break;
        case (numCase === 39): x = total - size; y = total - 2 * size; break;
        default: console.warn("Numéro de case invalide :", numCase); break;
    }
    return { x, y };
}

// --- Init Game ---
async function initGame() {
    await loadPlateau();
    await loadJoueurs();
    await updateGameState();
}

// --- Lancer dés ---
document.getElementById("rollBtn").addEventListener("click", async () => {

    const resTour = await fetch("/api/joueurAJouer");
    joueurCourrant = await resTour.json();

    // Cas où le Joueur est en prison
    const resEnPrison = await fetch("/api/enPrison");
    const prisonStatus = await resEnPrison.json();

    if (prisonStatus.enPrison) {
        if (prisonStatus.enPrison && prisonStatus.nbToursPrison === 0) {
            // Premier tour en prison → pas de roll possible
            document.getElementById("message").textContent = "Premier tour en prison : vous devez passer votre tour.";
            return;
        } else {
            await showPrisonMenu(joueurCourrant);
            return;
        }
    }

    // --- Verif Nb rolls ---
    const resNbRolls = await fetch("/api/nbRoll");
    let nbRolls = await resNbRolls.json();

    if (nbRolls === 0) {
        document.getElementById("message").textContent = "Vous n'avez plus de rolls restants pour ce tour.";
        return;
    }

    // --- Lancer de dés ---
    const res = await fetch("/api/roll");
    const rollResult = await res.json();

    document.getElementById("resultat_des_1").textContent = rollResult.des[0];
    document.getElementById("resultat_des_2").textContent = rollResult.des[1];

    // --- Gestion Triple ---
    if (rollResult.enPrison){
        document.getElementById("actionCoup").textContent = "Triple double ! Vous allez en prison !";
        await fetch(`/api/envoyerPrison`, { method: "POST" });
        await updateGameState();
        return;
    }

    // --- Gestion double ---
    if (rollResult.des[0] === rollResult.des[1]) {
        const resNbDouble = await fetch(`/api/estTripleDouble`);
        const nbDouble = await resNbDouble.json();
        document.getElementById("nbDouble").textContent = nbDouble;
        if (nbDouble < 3) {
            document.getElementById("double").textContent = "Double ! Vous rejouez.";
            await incrNbRoll();
        } else {
            document.getElementById("double").textContent = "";
        }
    }

    // --- Déplacement joueur ---
    const resDeplacementMessage = await fetch(`/api/deplacer/${rollResult.des[0] + rollResult.des[1]}`, { method: 'POST' });
    const deplacementMessage = await resDeplacementMessage.json();
    document.getElementById("gain-perte").textContent = deplacementMessage[1];
    const caseNom = deplacementMessage[0];
    await loadJoueurs();
    joueurCourrant = joueurs.find(j => j.id === joueurCourrant.id);
    await refreshMoney();


    document.getElementById("message").textContent = `Vous avancez jusqu'à ${caseNom}.`;

    // --- Vérification propriété pour menu achat ---
    const caseActu = joueurCourrant.caseActuelle;
    const resProp = await fetch(`/api/estPropriete/${caseActu}`);
    const estPropriete = await resProp.json();

    if (estPropriete) {
        await showAchatMenu(joueurCourrant, caseActu);
        return;
    }

    if (caseNom === "Chance" || caseNom === "Ccommunauté") {
        document.getElementById("tirerCarte").disabled = false;
    } else {
        await caseEvenement(caseActu);
    }
});

document.getElementById("rollPrisonBtn").addEventListener("click", async () => {
    // Cache le menu prison
    document.getElementById("miniMenuPrison").style.display = "none";

    // Lancer les dés via backend
    const res = await fetch("/api/tenterChancePrison");
    const des = await res.json(); // [dé1, dé2]

    // Affiche les résultats
    document.getElementById("resultat_des_1").textContent = des[0];
    document.getElementById("resultat_des_2").textContent = des[1];

    // Cas double = libéré
    if (des[0] === des[1]) {
        document.getElementById("message").textContent =
            "Vous avez fait un double ! Vous sortez de prison.";
        await fetch("/api/sortiePrison", { method: "POST" });
        await updateGameState();
        return;
    }

    // Cas pas de double → reste en prison
    document.getElementById("message").textContent =
        "Pas de double... Vous restez en prison.";
});


// --- Action Autres Cases ---
async function caseEvenement(caseData){
    const res = await fetch(`/api/actionCase${caseData}`)
    const actionJoueur = await res.text();
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

        btnOui.disabled = false;
        btnNon.disabled = false;

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
                await refreshMoney();
                await refreshPropriete();
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

async function showPrisonMenu(joueur) {
    const overlay = document.getElementById("miniMenuOverlay");
    const menu = document.getElementById("miniMenuPrison");
    const txt = document.getElementById("miniMenuTextPrison");
    const rollBtn = document.getElementById("rollPrisonBtn");
    const payerBtn = document.getElementById("payerBtn");

    if (!overlay || !menu) return;

    // Texte du menu
    txt.textContent = `${joueur.nom}, vous êtes en prison. Voulez-vous tenter un double pour sortir ou payer 50 € ?`;

    // Affichage centré
    overlay.style.display = "block";
    menu.style.display = "block";
    menu.style.top = "50%";
    menu.style.left = "50%";
    menu.style.transform = "translate(-50%, -50%)";

    const resChance = await fetch("/api/peutTenterChance");
    const peutTenter = await resChance.json();

    if(!peutTenter){
        rollBtn.disabled = true;
        txt.textContent = `${joueur.nom}, vous avez déjà tenté 3 fois. Vous devez payer 50 € pour sortir.`;
    } else {
        rollBtn.disabled = false;
    }

    payerBtn.disabled = false;
    rollBtn.focus();

    // Désactivation de tout sauf roll/payer
    const controls = ["achatBtn", "endTurnBtn"];
    controls.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.dataset.wasDisabled = el.disabled ? "true" : "false";
            el.disabled = true;
        }
    });

    // Empêche la fermeture du menu via clic sur overlay
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

        rollBtn.removeEventListener("click", onRoll);
        payerBtn.removeEventListener("click", onPay);

        document.getElementById("rollBtn").disabled = false;
        document.getElementById("endTurnBtn").disabled = false;
    };

    // --- Gestion roll pour tenter un double ---
    async function onRoll() {
        rollBtn.disabled = true;
        payerBtn.disabled = true;
        document.getElementById("message").textContent = `${joueur.nom} tente un double pour sortir...`;

        try {
            const resNbDes = await fetch(`/api/tenterChancePrison`);
            if (!resNbDes.ok) throw new Error("Erreur serveur lors du lancer des dés");

            const text = await resNbDes.text();
            let nbDes;
            try {
                nbDes = JSON.parse(text);
            } catch(err) {
                console.error("Réponse JSON invalide :", text);
                throw new Error("Impossible de lire les dés depuis la réponse serveur");
            }

            if (!Array.isArray(nbDes) || nbDes.length < 2 || (nbDes[0] === 0 && nbDes[1] === 0)) {
                document.getElementById("message").textContent =
                    `${joueur.nom} n'est pas en prison, lancer ignoré.`;
                cleanup();
                return;
            }

            document.getElementById("resultat_des_1").textContent = nbDes[0];
            document.getElementById("resultat_des_2").textContent = nbDes[1];

            if (nbDes[0] === nbDes[1]) {
                document.getElementById("message").textContent = `${joueur.nom} a réussi un double ! Il sort de prison.`;
                await fetch("/api/sortiePrison", { method: "POST" });
                await updateGameState(true);
            } else {
                document.getElementById("message").textContent = `${joueur.nom} a échoué, il reste en prison.`;
                await updateGameState();
            }
        } catch (err) {
            console.error(err);
            document.getElementById("message").textContent = "Erreur lors du lancer.";
        } finally {
            cleanup();
        }
    }


    // --- Gestion paiement ---
    async function onPay() {
        rollBtn.disabled = true;
        payerBtn.disabled = true;
        document.getElementById("message").textContent = `${joueur.nom} paie 50 € pour sortir de prison.`;

        try {
            const resAPaye = await fetch(`/api/payerPrison`, { method: "POST" });
            if(resAPaye){
                await fetch(`/api/sortiePrison`, {method: "POST"});
                document.getElementById("actionCoup").textContent = "Vous sortez de prison !"
            }
            else{
                document.getElementById("actionCoup").textContent = "Vous n'avez pas le solde necessaire !"
            }
            await updateGameState();
        } catch (err) {
            console.error(err);
            document.getElementById("message").textContent = "Erreur lors du paiement.";
        } finally {
            cleanup();
        }
    }

    // Attache les événements
    rollBtn.removeEventListener("click",onRoll);
    payerBtn.removeEventListener("click",onPay);
    rollBtn.addEventListener("click", onRoll);
    payerBtn.addEventListener("click", onPay);

    // Fermeture via Échap
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
    const res = await fetch("api/money");
    if (!res.ok) {
        console.error("Erreur lors de la récupération de l'argent");
    }
    else{
        moneySpan.textContent =  await res.text();
    }
}

async function refreshPropriete() {
    const proprieteDiv = document.getElementById("menuPossessions");
    let listeContainer = proprieteDiv.querySelector("p");

    if (!listeContainer) {
        listeContainer = document.createElement("p");
        proprieteDiv.appendChild(listeContainer);
    }

    listeContainer.innerHTML = "";

    const res = await fetch("/api/joueurAJouer");
    const joueur = await res.json();

    const possessions = joueur.listePossession.filter(p => p != null);


    if (possessions.length === 0) {
        listeContainer.textContent = "Vous n'avez aucune propriété.";
        return;
    }

    possessions.forEach(p => {
        const span = document.createElement("span");
        span.textContent = `${p.nom || p.Nom || "Propriété"} (${p.typeCase || "?"}) - ${p.prixAchat || p.PrixAchat || 0}$`;
        span.classList.add("span-item");

        const btnHypo = document.createElement("button");
        btnHypo.textContent = "Hypothéquer";
        btnHypo.id = p.id;
        btnHypo.addEventListener("click", async () => {
            console.log("Hypothéquer :", p.nom || p.Nom);
            await hypothequer(p.id, p.nom);
            btnHypo.disabled
        });

        const btnRem = document.createElement("button");
        btnRem.textContent = "Rembourser Hypothéque";
        btnRem.id = p.id;
        btnRem.addEventListener("click", async () => {
            console.log("Remboursement d'Hypothéque :", p.nom || p.Nom);
            await rembourserHypothequer(p.id, p.nom);
            btnRem.disabled
        });

        const container = document.createElement("div");
        container.appendChild(span);
        container.appendChild(btnHypo)
        container.appendChild(btnRem);

        listeContainer.appendChild(container);
    });
}

async function hypothequer(id,nom){
    const message = document.getElementById("message");

    const resHypo = await fetch(`api/hypothequer/${id}`, { method: "POST" });
    const hypo = await resHypo.json();

    switch (hypo){
        case 0:
            message.textContent = "Déja hypothéqué !";
            break;
        case -1:
            message.textContent = "Ceci n'est pas hypothequable !";
            break;
        default:
            message.textContent = `Vous venez d'hypothéquer : ${nom}.\nVous gagnez ${hypo} $ !`;
    }
    refreshMoney();
}

async function rembourserHypothequer(id, nom){
    const message = document.getElementById("message");
    const resRemHypo = await fetch(`api/rembourserHypo/${id}`, {method: "POST"} );
    const remHypo = await resRemHypo.json();

    switch (remHypo){
        case 0:
            message.textContent = "Vous n'avez pas les fonds pour cette action !";
            break;
        case -1:
            message.textContent = "Ce bien n'est pas hypothéqué !";
            break;
        case -2:
            message.textContent = "Ceci n'est pas remboursable !";
            break;
        default:
            message.textContent = `Vous venez de rembourser l'hypothèque de ${nom}.\nVous payez ${remHypo} $`;
            break;
    }
    refreshMoney();
}

// --- Fin de tour ---
document.getElementById("endTurnBtn").addEventListener("click", async () => {
    await fetch("/api/finTour", { method: "POST" });
    await updateGameState();
    incrNbRoll();
    document.getElementById("actionCoup").textContent = " ";
});

// --- Tirer Carte ---
document.getElementById("tirerCarte").addEventListener("click", async () => {
    const resActu = await fetch("/api/caseActuelle");
    const caseActu = await resActu.json();
    const caseInfo = plateau[caseActu];
    if (!caseInfo) return;
    const nomCase = caseInfo.nom.toLowerCase();
    if (nomCase.includes("chance")) await caseCarte("CHANCE")
    else if (nomCase.includes("ccommunauté")) await caseCarte("COMMUNAUTE")
});

async function incrNbRoll() {
    const res = await fetch("api/incrNbRoll");
    if (res.ok) document.getElementById("nbRollRestant").textContent = await res.text();
}

async function decrNbRoll() {
    const res = await fetch("api/decrNbRoll");
    if (res.ok) document.getElementById("nbRollRestant").textContent = await res.text();
}

async function caseCarte(caseType) {
    const url = caseType === "CHANCE" ? "/api/chance" : "/api/communaute";
    const res = await fetch(url);
    if(!res.ok) return console.error("Erreur de récupération de Carte")
    const carte = await res.json();
    afficherCarte(carte, caseType);
}

function afficherCarte(carte, type) {
    const modal = document.getElementById("carteModal");
    document.getElementById("carteTitre").textContent = type === "CHANCE" ? "Carte Chance" : "Carte Communauté";
    document.getElementById("carteDescription").textContent = carte.texte;
    modal.style.display = "flex";

    document.getElementById("carteOk").onclick = async () => {
        const cartePayload = { ...carte, typeCarte: type };
        const url = type === "CHANCE" ? "/api/ActionCarteChance" : "/api/ActionCarteCommunaute";
        try { await fetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(cartePayload) }); }
        catch (err) { console.error(err); }
        modal.style.display = "none";
        await updateGameState();
    };
}

// --- updateGameState ---
async function updateGameState() {
    await loadJoueurs();
    await refreshMoney();
    await refreshPropriete();

    const resJoueur = await fetch("/api/joueurAJouer");
    joueurCourrant = await resJoueur.json();
    document.getElementById("currentPlayer").textContent = `C'est au tour de ${joueurCourrant.nom}`;

    document.getElementById("rollBtn").disabled = false;
    document.getElementById("endTurnBtn").disabled = false;

    const resEnPrison = await fetch("/api/enPrison");
    const prisonStatus = await resEnPrison.json();

    if (prisonStatus.enPrison && joueurCourrant) {
        if (prisonStatus.nbToursPrison === 0) {
            document.getElementById("message").textContent = "Vous êtes en prison. Ce tour, vous devez passer votre tour.";
            document.getElementById("rollBtn").disabled = true;
            document.getElementById("endTurnBtn").disabled = false;
        } else {
            await showPrisonMenu(joueurCourrant);
            return;
        }
    }
    drawPions();
    document.getElementById("message").textContent = "État du jeu mis à jour après la carte.";
}

document.addEventListener("DOMContentLoaded", initGame);

document.addEventListener("DOMContentLoaded", () => {
    refreshPropriete(); // ton appel pour remplir le menu
});