const API_BASE = "http://localhost:8080/api/alerts";
const SIMULATION_API_BASE = "http://localhost:8080/api/simulation";

const RULE_DEFINITIONS = {
	1: "High Amount: amount > 10,000",
	2: "High Velocity: more than 3 transactions in 5 minutes",
	3: "New Payee: first-time payer to payee relationship",
	4: "Daily Limit: payer volume exceeds 50,000 in 24h"
};

const STATUS_FLOW = ["OPEN", "ACKNOWLEDGED", "INVESTIGATING", "DISMISSED", "CLOSED"];

const NEXT_ACTIONS = {
	OPEN: [{ label: "Acknowledge", status: "ACKNOWLEDGED", tone: "primary" }],
	ACKNOWLEDGED: [{ label: "Start Investigation", status: "INVESTIGATING", tone: "primary" }],
	INVESTIGATING: [
		{ label: "Dismiss", status: "DISMISSED", tone: "danger" },
		{ label: "Close", status: "CLOSED", tone: "secondary" }
	],
	DISMISSED: [],
	CLOSED: []
};

const demoAlerts = [
	{
		alertId: 1001,
		status: "OPEN",
		severityScore: 70,
		severityLevel: "HIGH",
		ruleIds: [1, 2],
		alertTimestamp: "2026-08-03T09:35:15",
		txnId: "TXN-889122",
		amount: 17450.75,
		currency: "USD",
		txnTimestamp: "2026-08-03T09:32:20",
		payerAccNum: "PA-2345567",
		payerName: "Nia Traders LLC",
		payerBankName: "BlueLine Bank",
		payeeAccNum: "PY-1117383",
		payeeName: "Global Freight Exchange",
		payeeBankName: "RiverStone Credit",
		auditLogs: [
			{
				status: "OPEN",
				comment: "Automatically opened by rule engine.",
				logTimestamp: "2026-08-03T09:35:15"
			}
		]
	},
	{
		alertId: 1002,
		status: "ACKNOWLEDGED",
		severityScore: 45,
		severityLevel: "MEDIUM",
		ruleIds: [1, 3],
		alertTimestamp: "2026-08-03T10:02:10",
		txnId: "TXN-889248",
		amount: 11600.0,
		currency: "USD",
		txnTimestamp: "2026-08-03T10:00:49",
		payerAccNum: "PA-3981459",
		payerName: "Crestwell Imports",
		payerBankName: "NovaEast Bank",
		payeeAccNum: "PY-7110924",
		payeeName: "Apex Digital Holdings",
		payeeBankName: "Titan National",
		auditLogs: [
			{
				status: "OPEN",
				comment: "Automatically opened by rule engine.",
				logTimestamp: "2026-08-03T10:02:10"
			},
			{
				status: "ACKNOWLEDGED",
				comment: "Analyst picked up the case.",
				logTimestamp: "2026-08-03T10:04:21"
			}
		]
	},
	{
		alertId: 1003,
		status: "INVESTIGATING",
		severityScore: 85,
		severityLevel: "HIGH",
		ruleIds: [1, 2, 4],
		alertTimestamp: "2026-08-03T11:17:04",
		txnId: "TXN-889401",
		amount: 42200.6,
		currency: "USD",
		txnTimestamp: "2026-08-03T11:16:55",
		payerAccNum: "PA-1200441",
		payerName: "SunPeak Energy",
		payerBankName: "Monarch Bank",
		payeeAccNum: "PY-8181032",
		payeeName: "Orchid Strategic Ventures",
		payeeBankName: "Harbor Financial",
		auditLogs: [
			{
				status: "OPEN",
				comment: "Automatically opened by rule engine.",
				logTimestamp: "2026-08-03T11:17:04"
			},
			{
				status: "ACKNOWLEDGED",
				comment: "Escalated to senior analyst.",
				logTimestamp: "2026-08-03T11:19:09"
			},
			{
				status: "INVESTIGATING",
				comment: "Collecting supporting KYC docs and transfer intent.",
				logTimestamp: "2026-08-03T11:24:53"
			}
		]
	}
];

const state = {
	alerts: [],
	selectedAlertId: null,
	filter: "ALL",
	search: "",
	usingDemoData: false,
	loading: false,
	simulationRunning: false,
	simulationStatusTimer: null,
	autoRefreshTimer: null
};

const elements = {
	refreshBtn: document.getElementById("refreshBtn"),
	simStartBtn: document.getElementById("simStartBtn"),
	simStopBtn: document.getElementById("simStopBtn"),
	simMinDelay: document.getElementById("simMinDelay"),
	simMaxDelay: document.getElementById("simMaxDelay"),
	simSuspicious: document.getElementById("simSuspicious"),
	simStatusText: document.getElementById("simStatusText"),
	alertsList: document.getElementById("alertsList"),
	alertCount: document.getElementById("alertCount"),
	metricsRow: document.getElementById("metricsRow"),
	searchInput: document.getElementById("searchInput"),
	statusFilters: document.getElementById("statusFilters"),
	emptyState: document.getElementById("emptyState"),
	alertDetails: document.getElementById("alertDetails"),
	detailAlertId: document.getElementById("detailAlertId"),
	detailTitle: document.getElementById("detailTitle"),
	detailStatus: document.getElementById("detailStatus"),
	detailSeverity: document.getElementById("detailSeverity"),
	summaryGrid: document.getElementById("summaryGrid"),
	transactionGrid: document.getElementById("transactionGrid"),
	rulesList: document.getElementById("rulesList"),
	partiesGrid: document.getElementById("partiesGrid"),
	lifecycleTrack: document.getElementById("lifecycleTrack"),
	auditLogs: document.getElementById("auditLogs"),
	actionComment: document.getElementById("actionComment"),
	actionButtons: document.getElementById("actionButtons"),
	toast: document.getElementById("toast")
};

init();

async function init() {
	wireEvents();
	await loadAlerts();
	await loadSimulationStatus(true);
	startSimulationStatusPolling();
}

function wireEvents() {
	elements.refreshBtn.addEventListener("click", () => loadAlerts(true));
	elements.simStartBtn.addEventListener("click", () => startSimulationFeed());
	elements.simStopBtn.addEventListener("click", () => stopSimulationFeed());

	elements.searchInput.addEventListener("input", (event) => {
		state.search = event.target.value.trim().toLowerCase();
		renderAlertsList();
	});

	elements.statusFilters.addEventListener("click", (event) => {
		const target = event.target.closest("button[data-filter]");
		if (!target) {
			return;
		}
		state.filter = target.dataset.filter;

		Array.from(elements.statusFilters.querySelectorAll(".filter-pill"))
			.forEach((btn) => btn.classList.toggle("active", btn === target));

		renderAlertsList();
	});
}

function startSimulationStatusPolling() {
	if (state.simulationStatusTimer) {
		clearInterval(state.simulationStatusTimer);
	}
	state.simulationStatusTimer = setInterval(() => {
		loadSimulationStatus(true);
	}, 3000);
}

async function loadSimulationStatus(silent) {
	try {
		const response = await fetch(`${SIMULATION_API_BASE}/status`);
		if (!response.ok) {
			throw new Error(`Simulator status unavailable (${response.status})`);
		}
		const status = await response.json();
		renderSimulationStatus(status);
	} catch (error) {
		state.simulationRunning = false;
		updateAutoRefreshLoop();
		elements.simStatusText.textContent = "UNAVAILABLE";
		elements.simStatusText.style.color = "#8f2b16";
		elements.simStartBtn.disabled = true;
		elements.simStopBtn.disabled = true;
		if (!silent) {
			showToast("Simulation service unavailable. Is backend running?");
		}
	}
}

function renderSimulationStatus(status) {
	state.simulationRunning = Boolean(status?.running);

	elements.simStatusText.textContent = state.simulationRunning
		? `ON · tx=${status.generatedTransactions ?? 0} alerts=${status.generatedAlerts ?? 0}`
		: "OFF";
	elements.simStatusText.style.color = state.simulationRunning ? "#2f8f4f" : "#5f687a";

	elements.simStartBtn.disabled = state.simulationRunning;
	elements.simStopBtn.disabled = !state.simulationRunning;

	if (!state.simulationRunning) {
		if (typeof status?.minDelayMs === "number") {
			elements.simMinDelay.value = status.minDelayMs;
		}
		if (typeof status?.maxDelayMs === "number") {
			elements.simMaxDelay.value = status.maxDelayMs;
		}
		if (typeof status?.suspiciousChance === "number") {
			elements.simSuspicious.value = Math.round(status.suspiciousChance * 100);
		}
	}

	updateAutoRefreshLoop();
}

function updateAutoRefreshLoop() {
	if (state.simulationRunning) {
		if (!state.autoRefreshTimer) {
			state.autoRefreshTimer = setInterval(() => {
				if (!state.loading) {
					loadAlerts(false);
				}
			}, 3500);
		}
		return;
	}

	if (state.autoRefreshTimer) {
		clearInterval(state.autoRefreshTimer);
		state.autoRefreshTimer = null;
	}
}

async function startSimulationFeed() {
	const minDelayMs = Number(elements.simMinDelay.value);
	const maxDelayMs = Number(elements.simMaxDelay.value);
	const suspiciousPercent = Number(elements.simSuspicious.value);

	if (!Number.isFinite(minDelayMs) || !Number.isFinite(maxDelayMs) || !Number.isFinite(suspiciousPercent)) {
		showToast("Simulation inputs must be valid numbers.");
		return;
	}

	try {
		const response = await fetch(`${SIMULATION_API_BASE}/start`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				minDelayMs,
				maxDelayMs,
				suspiciousChance: suspiciousPercent / 100
			})
		});

		if (!response.ok) {
			const errorPayload = await safeParseJson(response);
			throw new Error(errorPayload?.error || `Failed to start simulator (${response.status}).`);
		}

		const status = await response.json();
		renderSimulationStatus(status);
		showToast("Live transaction feed started.");
		await loadAlerts(false);
	} catch (error) {
		showToast(error.message);
	}
}

async function stopSimulationFeed() {
	try {
		const response = await fetch(`${SIMULATION_API_BASE}/stop`, { method: "POST" });
		if (!response.ok) {
			const errorPayload = await safeParseJson(response);
			throw new Error(errorPayload?.error || `Failed to stop simulator (${response.status}).`);
		}
		const status = await response.json();
		renderSimulationStatus(status);
		showToast("Live transaction feed stopped.");
	} catch (error) {
		showToast(error.message);
	}
}

async function loadAlerts(showToastOnSuccess = false) {
	state.loading = true;
	elements.refreshBtn.disabled = true;
	elements.refreshBtn.textContent = "Refreshing...";

	try {
		const response = await fetch(API_BASE);
		if (!response.ok) {
			throw new Error(`Failed to fetch alerts (${response.status})`);
		}

		const data = await response.json();
		state.alerts = data;
		state.usingDemoData = false;

		if (!state.alerts.length) {
			state.selectedAlertId = null;
		} else if (!state.selectedAlertId || !state.alerts.some((a) => a.alertId === state.selectedAlertId)) {
			state.selectedAlertId = state.alerts[0].alertId;
		}

		if (showToastOnSuccess) {
			showToast("Alerts refreshed successfully.");
		}
	} catch (error) {
		state.alerts = demoAlerts;
		state.selectedAlertId = demoAlerts[0]?.alertId ?? null;
		state.usingDemoData = true;
		showToast("Backend unavailable. Showing demo alerts.");
	} finally {
		state.loading = false;
		elements.refreshBtn.disabled = false;
		elements.refreshBtn.textContent = "Refresh Alerts";
		renderAll();
	}
}

function renderAll() {
	renderMetrics();
	renderAlertsList();
	renderSelectedAlert();
}

function getVisibleAlerts() {
	return state.alerts.filter((alert) => {
		const statusMatch = state.filter === "ALL" || alert.status === state.filter;
		const haystack = [
			String(alert.alertId),
			alert.txnId,
			alert.payerName,
			alert.payeeName,
			alert.status
		]
			.join(" ")
			.toLowerCase();
		const searchMatch = !state.search || haystack.includes(state.search);
		return statusMatch && searchMatch;
	});
}

function renderMetrics() {
	const counts = {
		total: state.alerts.length,
		high: state.alerts.filter((a) => a.severityLevel === "HIGH").length,
		investigating: state.alerts.filter((a) => a.status === "INVESTIGATING").length,
		acknowledged: state.alerts.filter((a) => a.status === "ACKNOWLEDGED").length
	};

	elements.metricsRow.innerHTML = `
		<article class="metric"><p>Total Active Alerts</p><strong>${counts.total}</strong></article>
		<article class="metric"><p>High Severity</p><strong>${counts.high}</strong></article>
		<article class="metric"><p>Investigating</p><strong>${counts.investigating}</strong></article>
		<article class="metric"><p>Acknowledged</p><strong>${counts.acknowledged}</strong></article>
	`;
}

function renderAlertsList() {
	const visibleAlerts = getVisibleAlerts();
	elements.alertCount.textContent = String(visibleAlerts.length);

	if (!visibleAlerts.length) {
		elements.alertsList.innerHTML = '<p class="empty-list">No alerts match your filters.</p>';
		return;
	}

	elements.alertsList.innerHTML = visibleAlerts
		.sort((a, b) => new Date(b.alertTimestamp) - new Date(a.alertTimestamp))
		.map((alert) => {
			const activeClass = alert.alertId === state.selectedAlertId ? "active" : "";
			return `
				<button class="alert-item ${activeClass}" data-id="${alert.alertId}" type="button">
					<h3>Alert #${alert.alertId} · ${escapeHtml(alert.payerName || "Unknown payer")}</h3>
					<p>Txn ${escapeHtml(alert.txnId || "-")} · ${formatAmount(alert.amount, alert.currency)}</p>
					<div class="alert-meta">
						<span class="status-badge status-${alert.status}">${alert.status}</span>
						<span class="severity-badge severity-${alert.severityLevel}">${alert.severityLevel}</span>
						<span class="mono">${formatDateTime(alert.alertTimestamp)}</span>
					</div>
				</button>
			`;
		})
		.join("");

	Array.from(elements.alertsList.querySelectorAll(".alert-item")).forEach((button) => {
		button.addEventListener("click", async () => {
			const id = Number(button.dataset.id);
			state.selectedAlertId = id;
			renderAlertsList();
			await renderSelectedAlert();
		});
	});
}

async function renderSelectedAlert() {
	if (!state.selectedAlertId) {
		elements.emptyState.classList.remove("hidden");
		elements.alertDetails.classList.add("hidden");
		return;
	}

	let selected = state.alerts.find((alert) => alert.alertId === state.selectedAlertId);

	if (!selected) {
		elements.emptyState.classList.remove("hidden");
		elements.alertDetails.classList.add("hidden");
		return;
	}

	if (!state.usingDemoData) {
		selected = await fetchAlertDetail(selected.alertId, selected);
	}

	elements.emptyState.classList.add("hidden");
	elements.alertDetails.classList.remove("hidden");

	elements.detailAlertId.textContent = `Alert #${selected.alertId}`;
	elements.detailTitle.textContent = selected.txnId ? `Transaction ${selected.txnId}` : "Alert Details";
	elements.detailStatus.textContent = selected.status;
	elements.detailStatus.className = `status-badge status-${selected.status}`;
	elements.detailSeverity.textContent = `${selected.severityLevel} · ${selected.severityScore}`;
	elements.detailSeverity.className = `severity-badge severity-${selected.severityLevel}`;

	renderKeyValueGrid(elements.summaryGrid, [
		["Alert ID", `#${selected.alertId}`],
		["Current Status", selected.status],
		["Severity Score", String(selected.severityScore)],
		["Created", formatDateTime(selected.alertTimestamp)],
		["Rules Triggered", String(selected.ruleIds?.length || 0)],
		["Data Mode", state.usingDemoData ? "Demo" : "Live"]
	]);

	renderRules(selected.ruleIds || []);

	renderKeyValueGrid(elements.transactionGrid, [
		["Transaction ID", selected.txnId || "-"],
		["Amount", formatAmount(selected.amount, selected.currency)],
		["Currency", selected.currency || "-"],
		["Txn Timestamp", formatDateTime(selected.txnTimestamp)]
	]);

	renderParties(selected);
	renderLifecycle(selected.status);
	renderAuditLogs(selected.auditLogs || []);
	renderActionButtons(selected.status, selected.alertId);
}

async function fetchAlertDetail(alertId, fallbackValue) {
	try {
		const response = await fetch(`${API_BASE}/${alertId}`);
		if (!response.ok) {
			throw new Error(`Failed to fetch detail (${response.status})`);
		}
		return await response.json();
	} catch {
		showToast("Could not load full detail. Showing list data.");
		return fallbackValue;
	}
}

function renderRules(ruleIds) {
	if (!ruleIds.length) {
		elements.rulesList.innerHTML = '<span class="rule-chip">No triggered rules</span>';
		return;
	}

	elements.rulesList.innerHTML = ruleIds
		.map((ruleId) => `<span class="rule-chip">Rule ${ruleId}: ${escapeHtml(RULE_DEFINITIONS[ruleId] || "Unknown rule")}</span>`)
		.join("");
}

function renderParties(alert) {
	elements.partiesGrid.innerHTML = `
		<section class="party">
			<h4>Payer</h4>
			<p><strong>Name:</strong> ${escapeHtml(alert.payerName || "-")}</p>
			<p><strong>Account:</strong> ${escapeHtml(alert.payerAccNum || "-")}</p>
			<p><strong>Bank:</strong> ${escapeHtml(alert.payerBankName || "-")}</p>
		</section>
		<section class="party">
			<h4>Payee</h4>
			<p><strong>Name:</strong> ${escapeHtml(alert.payeeName || "-")}</p>
			<p><strong>Account:</strong> ${escapeHtml(alert.payeeAccNum || "-")}</p>
			<p><strong>Bank:</strong> ${escapeHtml(alert.payeeBankName || "-")}</p>
		</section>
	`;
}

function renderLifecycle(currentStatus) {
	const currentIndex = STATUS_FLOW.indexOf(currentStatus);

	elements.lifecycleTrack.innerHTML = STATUS_FLOW.map((status, index) => {
		const stateClass = index < currentIndex ? "done-step" : index === currentIndex ? "active-step" : "";
		return `<li class="${stateClass}">${status}</li>`;
	}).join("");
}

function renderAuditLogs(logs) {
	if (!logs.length) {
		elements.auditLogs.innerHTML = '<p class="empty-list">No audit logs available.</p>';
		return;
	}

	const sortedLogs = [...logs].sort((a, b) => new Date(b.logTimestamp) - new Date(a.logTimestamp));
	elements.auditLogs.innerHTML = sortedLogs
		.map((log) => `
			<article class="audit-log">
				<p><strong>${escapeHtml(log.status || "-")}</strong> · ${formatDateTime(log.logTimestamp)}</p>
				<p>${escapeHtml(log.comment || "No comment")}</p>
			</article>
		`)
		.join("");
}

function renderActionButtons(currentStatus, alertId) {
	const actions = NEXT_ACTIONS[currentStatus] || [];
	elements.actionButtons.innerHTML = "";

	if (!actions.length) {
		elements.actionButtons.innerHTML = '<p class="empty-list">No actions available for this status.</p>';
		return;
	}

	actions.forEach((action) => {
		const button = document.createElement("button");
		button.type = "button";
		button.className = getActionClass(action.tone);
		button.textContent = action.label;
		button.addEventListener("click", () => {
			updateAlertStatus(alertId, action.status);
		});
		elements.actionButtons.appendChild(button);
	});
}

function getActionClass(tone) {
	if (tone === "danger") {
		return "btn btn-danger";
	}
	if (tone === "secondary") {
		return "btn btn-secondary";
	}
	return "btn btn-primary";
}

async function updateAlertStatus(alertId, targetStatus) {
	const comment = elements.actionComment.value.trim();
	const commentRequired = targetStatus === "DISMISSED" || targetStatus === "CLOSED";

	if (commentRequired && !comment) {
		showToast(`Comment is required for ${targetStatus}.`);
		return;
	}

	if (state.usingDemoData) {
		const alert = state.alerts.find((item) => item.alertId === alertId);
		if (!alert) {
			return;
		}
		alert.status = targetStatus;
		alert.auditLogs = alert.auditLogs || [];
		alert.auditLogs.push({
			status: targetStatus,
			comment: comment || "Updated in demo mode.",
			logTimestamp: new Date().toISOString()
		});

		elements.actionComment.value = "";
		showToast(`Alert #${alertId} moved to ${targetStatus} (demo mode).`);
		renderAll();
		return;
	}

	try {
		const response = await fetch(`${API_BASE}/${alertId}/status`, {
			method: "PUT",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ status: targetStatus, comment })
		});

		if (!response.ok) {
			const errorPayload = await safeParseJson(response);
			const message = errorPayload?.error || `Failed to update status (${response.status}).`;
			throw new Error(message);
		}

		const updatedAlert = await response.json();
		state.alerts = state.alerts
			.map((alert) => (alert.alertId === updatedAlert.alertId ? updatedAlert : alert))
			.filter((alert) => ["OPEN", "ACKNOWLEDGED", "INVESTIGATING"].includes(alert.status));

		if (!state.alerts.some((alert) => alert.alertId === alertId)) {
			state.selectedAlertId = state.alerts[0]?.alertId || null;
		}

		elements.actionComment.value = "";
		showToast(`Alert #${alertId} moved to ${targetStatus}.`);
		renderAll();
	} catch (error) {
		showToast(error.message);
	}
}

function renderKeyValueGrid(container, rows) {
	container.innerHTML = rows
		.map(([label, value]) => `<div><dt>${escapeHtml(label)}</dt><dd>${escapeHtml(value || "-")}</dd></div>`)
		.join("");
}

function formatAmount(amount, currency) {
	const numericAmount = Number(amount);
	if (!Number.isFinite(numericAmount)) {
		return "-";
	}

	const code = currency && typeof currency === "string" ? currency : "USD";
	try {
		return new Intl.NumberFormat(undefined, {
			style: "currency",
			currency: code,
			maximumFractionDigits: 2
		}).format(numericAmount);
	} catch {
		return `${numericAmount.toFixed(2)} ${code}`;
	}
}

function formatDateTime(value) {
	if (!value) {
		return "-";
	}
	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return String(value);
	}
	return date.toLocaleString();
}

async function safeParseJson(response) {
	try {
		return await response.json();
	} catch {
		return null;
	}
}

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#039;");
}

let toastTimer;
function showToast(message) {
	elements.toast.textContent = message;
	elements.toast.classList.add("show");
	clearTimeout(toastTimer);
	toastTimer = setTimeout(() => {
		elements.toast.classList.remove("show");
	}, 2700);
}
