
// Check server status
async function checkServerStatus() {

    const statusElement = document.querySelector(".status");

    try {

        const response = await fetch("/api/health");

        if (!response.ok) {
            throw new Error("Server error");
        }

        const data = await response.json();

        statusElement.innerHTML =
            '<span class="status-dot"></span>' + data.message;

    } catch (error) {

        statusElement.innerHTML =
            '<span style="color:#f87171">● Server Disconnected</span>';
    }
}


// Analyze grammar
async function analyzeGrammar() {

    const grammar = document.getElementById("grammarInput").value;
    const startSymbol = document.getElementById("startSymbol").value;

    const button = document.getElementById("analyzeBtn");

    if (!grammar.trim() || !startSymbol.trim()) {
        alert("Please enter grammar and start symbol.");
        return;
    }

    button.disabled = true;
    button.textContent = "Analyzing...";

    try {

        const response = await fetch("/api/analyze", {

            method: "POST",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

            body: new URLSearchParams({
                grammar: grammar,
                startSymbol: startSymbol
            })
        });

        const data = await response.json();

        if (data.status === "error") {
            alert(data.message);
            return;
        }

        document.getElementById("firstFollowResult").textContent =
            "FIRST:\n" + data.first +
            "\n\nFOLLOW:\n" + data.follow;

        document.getElementById("lr0Result").textContent =
            data.lr0;

        document.getElementById("slrResult").textContent =
            data.slr +
            "\nSLR Conflict: " + data.conflict;

    } catch (error) {

        alert("Unable to analyze grammar. Check server connection.");

        console.error(error);

    } finally {

        button.disabled = false;
        button.textContent = "Analyze Grammar";
    }
}


// Parse input string
async function parseInput() {

    const grammar = document.getElementById("grammarInput").value;
    const startSymbol = document.getElementById("startSymbol").value;
    const inputString = document.getElementById("inputString").value;

    const button = document.getElementById("parseBtn");
    const resultElement = document.getElementById("parseResult");

    if (!grammar.trim() || !startSymbol.trim()) {
        alert("Please enter grammar and start symbol first.");
        return;
    }

    button.disabled = true;
    button.textContent = "Parsing...";

    try {

        const response = await fetch("/api/parse", {

            method: "POST",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

            body: new URLSearchParams({
                grammar: grammar,
                startSymbol: startSymbol,
                inputString: inputString
            })
        });

        const data = await response.json();

        if (data.status === "error") {

            resultElement.textContent = data.message;
            return;
        }

        resultElement.textContent = data.parse;

    } catch (error) {

        resultElement.textContent =
            "Unable to perform parsing. Check server connection.";

        console.error(error);

    } finally {

        button.disabled = false;
        button.textContent = "Parse Input";
    }
}


// Initialize website
document.addEventListener("DOMContentLoaded", () => {

    checkServerStatus();

    document.getElementById("analyzeBtn")
        .addEventListener("click", analyzeGrammar);

    document.getElementById("parseBtn")
        .addEventListener("click", parseInput);

});
