# Agile Client Interview Output: Transaction Monitoring Dashboard

## 1. Target Persona & Pain Points

### **Persona: The Risk Manager**
The primary user of this system is a Risk Manager (acting as the sole operator) at a large financial institution. Their primary responsibility is to monitor accounts, review flagged activities, and manage the alert lifecycle.

### **User Pain Points**
* **Alert Fatigue:** The user struggles to prioritize which suspicious activities need immediate attention without a clear severity scoring system.
* **Lack of Context:** Time is wasted when the user cannot immediately see transaction specifics (payer, payee, currency, timestamp) upon clicking an alert.
* **Auditability Gaps:** Without mandatory comment fields, there is no historical record of why previous alerts were dismissed or closed.
* **Currency Confusion:** Evaluating global transactions without natively displayed currencies leads to misinterpretation of the actual transaction value.

---

## 2. User Stories

The following user stories define the desired functionality from the perspective of the Risk Manager.

| Story ID | User Story | Acceptance Criteria |
| :--- | :--- | :--- |
| **US-01** | As a Risk Manager, I want to view a queue of active alerts on my dashboard so that I can quickly identify newly flagged activities. | Dashboard displays a list of alerts. Alerts show basic summary info (Rule triggered, Severity). |
| **US-02** | As a Risk Manager, I want to click an alert to view its specific transaction details so that I have the necessary context to investigate. | Detail view displays Payee, Payer (and institutions), Currency, and Timestamp. |
| **US-03** | As a Risk Manager, I want to update an alert's status (e.g., to INVESTIGATING or DISMISSED) so that I can manage the investigation workflow. | Workflow moves from OPEN to ACKNOWLEDGED, then to INVESTIGATING or CLOSED/DISMISSED. |
| **US-04** | As a Risk Manager, I want the system to require a mandatory comment or reason code when I dismiss or close an alert so that there is a clear audit trail of my decision. | Status cannot be changed to DISMISSED or CLOSED without text input. |
| **US-05** | As a Risk Manager, I want incoming transactions evaluated against four specific rules (High Amount, High Velocity, New Payee, Over Daily Limit) so that suspicious behavior is caught automatically. | System implements the Amount Threshold Rule, Velocity Rule, New Payee Rule, and Daily Limit Rule. |
| **US-06** | As a Risk Manager, I want alerts to display a calculated severity score with corresponding color coding (Red, Yellow, Blue) so that I can visually prioritize my queue. | A dynamic severity score is calculated based on weightage applied to the specific rules triggered. Specific score thresholds determine the color-coded severity level (Red=High, Yellow=Medium, Blue=Low) displayed on the UI. |
| **US-07** | As a Risk Manager, I want transaction amounts displayed in their native currency (USD, GBP, INR, EUR) so that I understand the exact monetary value transferred. | UI renders the correct currency symbol/code alongside the numerical amount. |

---

## 3. MoSCoW Prioritization

This framework categorizes the features to ensure our team focuses on the Minimum Viable Product (MVP) first.

| Category | Features Included |
| :--- | :--- |
| **Must Have**<br>*(Required for MVP)* | * Dashboard queue for active alerts.<br>* Transaction detail view (Payer, Payee, Timestamp, Currency).<br>* Alert lifecycle status updates (Investigating, Dismissed).<br>* Core evaluation rules (Amount, Velocity, New Payee, Daily Limit).<br>* Dynamic severity score calculation based on the weighted sum of triggered rules. |
| **Should Have**<br>*(Important, but not critical for day 1)* | * Mandatory comment/reason field for resolving alerts.<br>* Color-coded severity UI indicators (Red, Yellow, Blue).<br>* Multi-currency display support formatting. |
| **Could Have**<br>*(Nice to have if time permits)* | * Real-time dashboard updates without refreshing the page.<br>* Automated base-currency conversion for cross-border transaction rules. |
| **Won't Have**<br>*(Excluded from this sprint)* | * User authentication or login screens.<br>* Support for multiple user roles or operator assignment. |
