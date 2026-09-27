# CENTSIBLE — Web Application Distribution

This directory contains the standalone web application for **CENTSIBLE**, ready to run directly in any modern browser, host on static web hosting providers, or package as a Progressive Web App (PWA).

## Features
- **Total Net Worth & Live Market Tickers**: Real-time asset tracking across global indices (S&P 500, NASDAQ, NIFTY 50), crypto (BTC, ETH), and commodities.
- **Multi-Currency System**: Instant switching between USD ($), EUR (€), GBP (£), INR (₹), JPY (¥), CAD (C$), and AUD (A$) with live cross-rates.
- **Central Audited Ledger**: Real-time search, category filtering, new expense recording, and item deletion.
- **Monthly Budgets & Savings Goals**: Progress meters with automated 80% usage threshold warnings and interactive deposit tracking.
- **Aura AI Financial Advisor**: Conversational financial strategist with quick suggestion chips.
- **UPI & Bank SMS Parser Simulator**: Instant parsing of debit and UPI SMS alerts with automatic category inference.
- **Multimodal Receipt OCR Digitizer**: Upload receipt images to extract merchant, line items, and totals into the ledger.
- **Institutional Bank Accounts**: Multi-currency international account manager with simulated synchronization.
- **Data Export & Cloud Vault Backup**: Full JSON backup export and restore directly from the browser.
- **Vault Security Overlay**: 4-digit PIN lock and biometric unlock simulation.

## How to Run & Deploy

### Option 1: Double-Click Local Preview
Simply open `web/index.html` in Chrome, Safari, Edge, or Firefox. Everything runs client-side with `localStorage` persistence.

### Option 2: Run with any local HTTP server
```bash
# Python
python3 -m http.server 3000 --directory web

# Node.js
npx serve web
```

### Option 3: Deploy to the Web (Free)
- **Vercel**: Import this folder or run `vercel web`
- **Netlify**: Drag and drop the `web` folder into app.netlify.com/drop
- **Firebase Hosting**: Run `firebase deploy --only hosting` pointing public to `web`
- **GitHub Pages**: Push this directory to your `gh-pages` branch.
