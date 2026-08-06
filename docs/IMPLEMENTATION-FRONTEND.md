# Frontend Implementation Guide

This guide explains the frontend behavior, UI portability, and user experience design choices.

## Stack

- HTML5
- CSS3
- Vanilla JavaScript (no framework build chain)

## Frontend Purpose

The UI supports two analyst workflows:

- Alert management dashboard for triage and lifecycle actions.
- Live transactions page for real-time monitoring.

## View Structure

Primary views in a single-page layout:

- Dashboard view
- Transactions view

Navigation:
- Bottom navigation toggles active view.

## Data Sources and API Integration

Configured API base paths:
- /api/alerts
- /api/transactions
- /api/simulation

Behavior:
- Alerts view fetches active alerts and details.
- Transactions view polls paginated live transaction feed.
- Simulation controls start/stop live feed and show status.

## Portable UI Characteristics

Portable here means the UI can run consistently across environments with minimal setup:

- Static assets only (index.html, styles.css, script.js).
- No frontend build step required for local browser usage.
- Dockerized with Nginx for predictable deployment.
- Works with backend in containerized and non-containerized setups.

## Responsiveness

The UI includes responsive breakpoints for desktop and mobile:

- Grid layout collapses for narrow screens.
- Navigation adapts into compact bottom control.
- Metric cards and detail panels reflow for readability.

## Accessibility Notes

The current implementation includes practical accessibility support:

- Semantic labels and region labeling (aria-label).
- Dynamic content announcements via aria-live.
- Filter controls use aria-pressed state.
- Buttons and inputs are keyboard-usable.
- Focus styling is visible for form elements.

## Data Handling and Safety

- Uses safe HTML escaping before rendering dynamic values.
- Handles malformed/unavailable API responses gracefully.
- Provides user feedback through toast messages.

## Alert Lifecycle UX

The detail panel supports workflow actions based on current status:

- Contextual action buttons per current state
- Visual lifecycle track
- Audit history display

When backend is available, status updates are sent to API.

## Demo and Failure Tolerance

If backend is unavailable:
- UI switches to built-in demo alerts for continuity.
- This keeps SME demos and UI walkthroughs usable without full backend availability.

## Simulation UX

Users can:
- Start and stop simulation
- Tune min/max delay
- Monitor generated traffic counters

Status polling keeps the dashboard synchronized with backend simulation state.

## Files to Know

- frontend/index.html
- frontend/styles.css
- frontend/script.js

## Run Options

Option A: Open frontend/index.html directly.

Option B: Run through Docker Compose (recommended for end-to-end behavior).
