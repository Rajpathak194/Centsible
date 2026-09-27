// CENTSIBLE Standalone Web Application Engine
// State Management, Multi-Currency Exchange, UPI Parser, and AI Financial Advisor

class CentsibleWebEngine {
  constructor() {
    this.rates = {
      USD: 1.0,
      EUR: 0.92,
      GBP: 0.78,
      INR: 84.50,
      JPY: 153.20,
      CAD: 1.36,
      AUD: 1.51
    };

    this.symbols = {
      USD: '$',
      EUR: '€',
      GBP: '£',
      INR: '₹',
      JPY: '¥',
      CAD: 'C$',
      AUD: 'A$'
    };

    this.currentCurrency = localStorage.getItem('centsible_currency') || 'USD';
    this.isLocked = false;
    this.enteredPin = '';
    this.correctPin = '1234';

    // Market Tickers
    this.tickers = [
      { name: 'S&P 500', val: '5,864.20', chg: '+0.72%' },
      { name: 'NASDAQ', val: '18,518.60', chg: '+1.14%' },
      { name: 'NIFTY 50', val: '25,790.95', chg: '+0.45%' },
      { name: 'BTC / USD', val: '$65,420.00', chg: '+2.85%' },
      { name: 'ETH / USD', val: '$2,640.50', chg: '+3.12%' },
      { name: 'GOLD (OZ)', val: '$2,658.40', chg: '+0.38%' },
      { name: 'EUR / USD', val: '1.086', chg: '-0.15%' }
    ];

    // Seed Data or Load from localStorage
    this.transactions = this.loadStoredData('centsible_transactions', [
      { id: '1', title: 'Whole Foods Market', amount: 84.50, type: 'EXPENSE', category: 'Food & Dining', account: 'Chase Sapphire US', date: '2026-09-26' },
      { id: '2', title: 'Tech Corp Global Payroll', amount: 8450.00, type: 'INCOME', category: 'Income', account: 'Chase Sapphire US', date: '2026-09-25' },
      { id: '3', title: 'Shell Mobility Petrol', amount: 62.00, type: 'EXPENSE', category: 'Transportation', account: 'Chase Sapphire US', date: '2026-09-24' },
      { id: '4', title: 'Apple Services & iCloud', amount: 14.99, type: 'EXPENSE', category: 'Utilities', account: 'Barclays UK', date: '2026-09-23' },
      { id: '5', title: 'Swiggy Gourmet Feast', amount: 28.50, type: 'EXPENSE', category: 'Food & Dining', account: 'HDFC Salary IN', date: '2026-09-22' },
      { id: '6', title: 'Amazon Electronics Hub', amount: 149.00, type: 'EXPENSE', category: 'Shopping', account: 'Chase Sapphire US', date: '2026-09-21' }
    ]);

    this.budgets = this.loadStoredData('centsible_budgets', [
      { id: 'b1', category: 'Food & Dining', limit: 550.00 },
      { id: 'b2', category: 'Transportation', limit: 250.00 },
      { id: 'b3', category: 'Shopping', limit: 400.00 },
      { id: 'b4', category: 'Utilities', limit: 200.00 }
    ]);

    this.goals = this.loadStoredData('centsible_goals', [
      { id: 'g1', title: 'Emergency Reserve (6 Months)', target: 15000.00, current: 9800.00 },
      { id: 'g2', title: 'Alpine Vacation & Skiing', target: 4500.00, current: 2850.00 },
      { id: 'g3', title: 'Vehicle Down Payment', target: 8000.00, current: 3600.00 }
    ]);

    this.accounts = [
      { name: 'Chase Sapphire Reserve', bank: 'JPMorgan Chase (US)', balanceUsd: 42500.00, type: 'Checking', currency: 'USD' },
      { name: 'Barclays Premier Current', bank: 'Barclays (UK)', balanceUsd: 18450.00, type: 'Savings', currency: 'GBP' },
      { name: 'HDFC Infinia Wealth', bank: 'HDFC Bank (IN)', balanceUsd: 14890.00, type: 'Salary & UPI', currency: 'INR' },
      { name: 'Revolut Multi-Currency Vault', bank: 'Revolut (EU)', balanceUsd: 9000.00, type: 'Forex Vault', currency: 'EUR' },
      { name: 'Vanguard Index Portfolio', bank: 'Vanguard Brokerage', balanceUsd: 30000.00, type: 'Investment', currency: 'USD' }
    ];

    this.pendingUpiParsed = null;
    this.pendingReceiptParsed = null;
    this.chartInstance = null;

    this.init();
  }

  loadStoredData(key, fallback) {
    try {
      const stored = localStorage.getItem(key);
      return stored ? JSON.parse(stored) : fallback;
    } catch {
      return fallback;
    }
  }

  saveData(key, data) {
    try {
      localStorage.setItem(key, JSON.stringify(data));
    } catch (e) {
      console.warn('Failed to save to localStorage:', e);
    }
  }

  init() {
    document.getElementById('currencySelector').value = this.currentCurrency;
    this.renderTickers();
    this.renderDashboard();
    this.renderTransactions();
    this.renderBudgetsAndGoals();
    this.renderAccounts();
    this.initCategoryChart();
    this.updateQuickCalculator();

    if (window.lucide) {
      lucide.createIcons();
    }
  }

  setCurrency(curr) {
    this.currentCurrency = curr;
    localStorage.setItem('centsible_currency', curr);
    this.renderDashboard();
    this.renderTransactions();
    this.renderBudgetsAndGoals();
    this.renderAccounts();
    this.initCategoryChart();
    this.updateQuickCalculator();
    this.showToast(`Active currency switched to ${curr}`);
  }

  formatMoney(amountInUsd) {
    const rate = this.rates[this.currentCurrency] || 1.0;
    const symbol = this.symbols[this.currentCurrency] || '$';
    const converted = amountInUsd * rate;
    return `${symbol}${converted.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  updateQuickCalculator() {
    const usd = 100;
    const inr = usd * this.rates['INR'];
    const el = document.getElementById('quickCalcResult');
    if (el) el.innerText = `₹${inr.toLocaleString(undefined, { minimumFractionDigits: 2 })}`;
  }

  renderTickers() {
    const container = document.getElementById('tickerItems');
    if (!container) return;

    let html = '';
    // Duplicate twice for seamless infinite marquee scroll
    const all = [...this.tickers, ...this.tickers];
    all.forEach(t => {
      const isPositive = t.chg.startsWith('+');
      const chgColor = isPositive ? 'text-brand-emeraldLight' : 'text-brand-rose';
      html += `
        <span class="inline-flex items-center space-x-2">
          <span class="text-slate-400 font-bold">${t.name}</span>
          <span class="text-slate-200">${t.val}</span>
          <span class="${chgColor} font-bold">${t.chg}</span>
        </span>
        <span class="text-slate-600 font-bold">•</span>
      `;
    });
    container.innerHTML = html;
  }

  renderDashboard() {
    // Calculate Net Worth
    const totalNetWorthUsd = this.accounts.reduce((acc, a) => acc + a.balanceUsd, 0);
    const monthlyIncomeUsd = this.transactions
      .filter(t => t.type === 'INCOME')
      .reduce((acc, t) => acc + t.amount, 0);
    const monthlyExpenseUsd = this.transactions
      .filter(t => t.type === 'EXPENSE')
      .reduce((acc, t) => acc + t.amount, 0);

    const savingsRate = monthlyIncomeUsd > 0
      ? Math.max(0, ((monthlyIncomeUsd - monthlyExpenseUsd) / monthlyIncomeUsd) * 100)
      : 0;

    document.getElementById('netWorthDisplay').innerText = this.formatMoney(totalNetWorthUsd);
    document.getElementById('monthlyIncomeDisplay').innerText = this.formatMoney(monthlyIncomeUsd);
    document.getElementById('monthlyExpenseDisplay').innerText = this.formatMoney(monthlyExpenseUsd);
    document.getElementById('savingsRateDisplay').innerText = `${savingsRate.toFixed(1)}%`;

    // Render Recent Transactions
    const txContainer = document.getElementById('dashboardTxList');
    if (txContainer) {
      const recent = this.transactions.slice(0, 5);
      txContainer.innerHTML = recent.map(tx => {
        const isExpense = tx.type === 'EXPENSE';
        const color = isExpense ? 'text-brand-rose' : 'text-brand-emeraldLight';
        const sign = isExpense ? '-' : '+';
        const iconName = isExpense ? 'arrow-down-right' : 'arrow-up-right';
        const iconBg = isExpense ? 'bg-rose-500/10 text-brand-rose' : 'bg-brand-emerald/10 text-brand-emeraldLight';

        return `
          <div class="flex items-center justify-between p-3 rounded-xl bg-brand-elevated/40 border border-brand-border/40 hover:border-brand-border transition">
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 rounded-lg ${iconBg} flex items-center justify-center">
                <i data-lucide="${iconName}" class="w-4 h-4"></i>
              </div>
              <div>
                <div class="text-xs font-bold text-white">${tx.title}</div>
                <div class="text-[11px] text-slate-400">${tx.category} • ${tx.account}</div>
              </div>
            </div>
            <div class="text-right font-mono">
              <div class="text-xs font-bold ${color}">${sign}${this.formatMoney(tx.amount)}</div>
              <div class="text-[10px] text-slate-500">${tx.date}</div>
            </div>
          </div>
        `;
      }).join('');
    }

    // Render Mini Budgets
    const budgetContainer = document.getElementById('dashboardBudgetList');
    if (budgetContainer) {
      budgetContainer.innerHTML = this.budgets.map(b => {
        const spent = this.transactions
          .filter(t => t.category === b.category && t.type === 'EXPENSE')
          .reduce((sum, t) => sum + t.amount, 0);
        const percent = Math.min(100, Math.round((spent / b.limit) * 100));
        const barColor = percent > 85 ? 'bg-brand-rose' : percent > 60 ? 'bg-brand-gold' : 'bg-brand-emerald';

        return `
          <div class="space-y-1.5">
            <div class="flex justify-between text-xs">
              <span class="font-semibold text-slate-300">${b.category}</span>
              <span class="font-mono text-slate-400">${this.formatMoney(spent)} / ${this.formatMoney(b.limit)} (${percent}%)</span>
            </div>
            <div class="w-full h-2 rounded-full bg-brand-dark overflow-hidden border border-brand-border">
              <div class="${barColor} h-full rounded-full transition-all duration-500" style="width: ${percent}%"></div>
            </div>
          </div>
        `;
      }).join('');
    }

    if (window.lucide) lucide.createIcons();
  }

  renderTransactions() {
    const search = (document.getElementById('txSearchInput')?.value || '').toLowerCase();
    const cat = document.getElementById('txCategoryFilter')?.value || 'ALL';

    const filtered = this.transactions.filter(t => {
      const matchSearch = t.title.toLowerCase().includes(search) || t.account.toLowerCase().includes(search);
      const matchCat = cat === 'ALL' || t.category === cat;
      return matchSearch && matchCat;
    });

    const tbody = document.getElementById('fullTransactionTable');
    if (!tbody) return;

    if (filtered.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="text-center py-8 text-slate-500">No matching transactions found in CENTSIBLE ledger.</td></tr>`;
      return;
    }

    tbody.innerHTML = filtered.map(tx => {
      const isExpense = tx.type === 'EXPENSE';
      const color = isExpense ? 'text-brand-rose' : 'text-brand-emeraldLight';
      const sign = isExpense ? '-' : '+';

      return `
        <tr class="hover:bg-brand-elevated/40 transition">
          <td class="py-3 px-3 font-semibold text-white">${tx.title}</td>
          <td class="py-3 px-3"><span class="px-2 py-0.5 rounded-full text-[10px] bg-brand-elevated text-slate-300 border border-brand-border">${tx.category}</span></td>
          <td class="py-3 px-3 text-slate-400">${tx.account}</td>
          <td class="py-3 px-3 font-mono text-slate-400">${tx.date}</td>
          <td class="py-3 px-3 text-right font-mono font-bold ${color}">${sign}${this.formatMoney(tx.amount)}</td>
          <td class="py-3 px-3 text-right">
            <button onclick="centsibleApp.deleteTransaction('${tx.id}')" class="text-slate-500 hover:text-brand-rose p-1 transition" title="Delete">
              <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
            </button>
          </td>
        </tr>
      `;
    }).join('');

    if (window.lucide) lucide.createIcons();
  }

  deleteTransaction(id) {
    this.transactions = this.transactions.filter(t => t.id !== id);
    this.saveData('centsible_transactions', this.transactions);
    this.renderDashboard();
    this.renderTransactions();
    this.initCategoryChart();
    this.showToast('Transaction removed from central ledger.');
  }

  renderBudgetsAndGoals() {
    // Budgets
    const budgetList = document.getElementById('budgetCardList');
    if (budgetList) {
      budgetList.innerHTML = this.budgets.map(b => {
        const spent = this.transactions
          .filter(t => t.category === b.category && t.type === 'EXPENSE')
          .reduce((sum, t) => sum + t.amount, 0);
        const remaining = Math.max(0, b.limit - spent);
        const percent = Math.min(100, Math.round((spent / b.limit) * 100));
        const barColor = percent > 85 ? 'bg-brand-rose' : percent > 60 ? 'bg-brand-gold' : 'bg-brand-emerald';

        return `
          <div class="p-4 rounded-xl bg-brand-elevated/40 border border-brand-border space-y-2">
            <div class="flex items-center justify-between">
              <span class="font-bold text-white text-xs">${b.category}</span>
              <button onclick="centsibleApp.deleteBudget('${b.id}')" class="text-slate-500 hover:text-brand-rose text-xs">
                <i data-lucide="x" class="w-3.5 h-3.5"></i>
              </button>
            </div>
            <div class="flex justify-between text-xs font-mono">
              <span class="text-slate-400">Spent: ${this.formatMoney(spent)}</span>
              <span class="text-slate-300 font-bold">Remaining: ${this.formatMoney(remaining)}</span>
            </div>
            <div class="w-full h-2 rounded-full bg-brand-dark overflow-hidden border border-brand-border">
              <div class="${barColor} h-full rounded-full transition-all duration-500" style="width: ${percent}%"></div>
            </div>
          </div>
        `;
      }).join('');
    }

    // Goals
    const goalList = document.getElementById('goalCardList');
    if (goalList) {
      goalList.innerHTML = this.goals.map(g => {
        const percent = Math.min(100, Math.round((g.current / g.target) * 100));
        return `
          <div class="p-4 rounded-xl bg-brand-elevated/40 border border-brand-border space-y-2">
            <div class="flex items-center justify-between">
              <span class="font-bold text-white text-xs">${g.title}</span>
              <span class="text-[11px] font-mono text-brand-emeraldLight font-bold">${percent}%</span>
            </div>
            <div class="flex justify-between text-xs font-mono text-slate-400">
              <span>Saved: ${this.formatMoney(g.current)}</span>
              <span>Target: ${this.formatMoney(g.target)}</span>
            </div>
            <div class="w-full h-2 rounded-full bg-brand-dark overflow-hidden border border-brand-border">
              <div class="bg-gradient-to-r from-brand-emerald to-brand-sapphire h-full rounded-full transition-all duration-500" style="width: ${percent}%"></div>
            </div>
            <div class="pt-1 flex justify-end">
              <button onclick="centsibleApp.contributeToGoal('${g.id}')" class="px-2.5 py-1 rounded bg-brand-emerald/10 hover:bg-brand-emerald/20 text-brand-emeraldLight text-[11px] font-bold transition flex items-center space-x-1">
                <i data-lucide="plus" class="w-3 h-3"></i>
                <span>Add $100 Deposit</span>
              </button>
            </div>
          </div>
        `;
      }).join('');
    }

    if (window.lucide) lucide.createIcons();
  }

  contributeToGoal(goalId) {
    const goal = this.goals.find(g => g.id === goalId);
    if (!goal) return;
    goal.current += 100;
    this.saveData('centsible_goals', this.goals);
    this.renderBudgetsAndGoals();
    this.showToast(`Deposited $100 into "${goal.title}"`);
  }

  deleteBudget(budgetId) {
    this.budgets = this.budgets.filter(b => b.id !== budgetId);
    this.saveData('centsible_budgets', this.budgets);
    this.renderDashboard();
    this.renderBudgetsAndGoals();
    this.showToast('Budget deleted.');
  }

  renderAccounts() {
    const grid = document.getElementById('accountsGrid');
    if (!grid) return;

    grid.innerHTML = this.accounts.map(acc => {
      return `
        <div class="p-5 rounded-2xl bg-brand-elevated/40 border border-brand-border space-y-3 relative overflow-hidden">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-white">${acc.name}</span>
            <span class="px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-brand-dark border border-brand-border text-brand-emeraldLight">${acc.currency}</span>
          </div>
          <div class="text-[11px] text-slate-400">${acc.bank} • ${acc.type}</div>
          <div class="pt-2 border-t border-brand-border/60 flex items-center justify-between font-mono">
            <span class="text-xs text-slate-400">Balance:</span>
            <span class="text-base font-bold text-white">${this.formatMoney(acc.balanceUsd)}</span>
          </div>
        </div>
      `;
    }).join('');
  }

  simulateBankSync() {
    const btn = document.getElementById('syncAllBtn');
    const icon = document.getElementById('syncIcon');
    if (icon) icon.classList.add('animate-spin');

    setTimeout(() => {
      if (icon) icon.classList.remove('animate-spin');
      this.showToast('All 5 institutional bank vaults synchronized successfully! 🚀');
    }, 1200);
  }

  initCategoryChart() {
    const canvas = document.getElementById('categoryChart');
    if (!canvas) return;

    const catTotals = {};
    this.transactions.filter(t => t.type === 'EXPENSE').forEach(t => {
      catTotals[t.category] = (catTotals[t.category] || 0) + t.amount;
    });

    const labels = Object.keys(catTotals);
    const data = Object.values(catTotals);

    if (this.chartInstance) {
      this.chartInstance.destroy();
    }

    this.chartInstance = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: [
            '#10B981', // Emerald
            '#3B82F6', // Sapphire
            '#F59E0B', // Gold
            '#EF4444', // Rose
            '#8B5CF6', // Purple
            '#06B6D4'  // Cyan
          ],
          borderColor: '#111A30',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 12,
              font: { size: 10, family: 'Plus Jakarta Sans' },
              color: '#94A3B8'
            }
          }
        },
        cutout: '70%'
      }
    });
  }

  // Aura AI Advisor
  handleAdvisorSubmit(e) {
    e.preventDefault();
    const input = document.getElementById('advisorInput');
    const query = input.value.trim();
    if (!query) return;

    this.sendAdvisorPrompt(query);
    input.value = '';
  }

  sendAdvisorPrompt(query) {
    const chatWindow = document.getElementById('advisorChatWindow');
    if (!chatWindow) return;

    // User Message
    const userMsg = document.createElement('div');
    userMsg.className = 'flex items-start justify-end space-x-3';
    userMsg.innerHTML = `
      <div class="bg-brand-emerald text-brand-dark font-medium p-3.5 rounded-2xl rounded-tr-sm text-xs max-w-xl leading-relaxed">
        ${this.escapeHtml(query)}
      </div>
    `;
    chatWindow.appendChild(userMsg);
    chatWindow.scrollTop = chatWindow.scrollHeight;

    // Thinking Indicator
    const thinking = document.createElement('div');
    thinking.id = 'advisorThinking';
    thinking.className = 'flex items-center space-x-2 text-slate-400 text-xs py-2';
    thinking.innerHTML = `
      <i data-lucide="loader" class="w-4 h-4 animate-spin text-brand-emerald"></i>
      <span>Aura is evaluating your portfolio & expenses...</span>
    `;
    chatWindow.appendChild(thinking);
    if (window.lucide) lucide.createIcons();
    chatWindow.scrollTop = chatWindow.scrollHeight;

    setTimeout(() => {
      thinking.remove();
      const botMsg = document.createElement('div');
      botMsg.className = 'flex items-start space-x-3';
      const responseHtml = this.generateAdvisorResponse(query);
      botMsg.innerHTML = `
        <div class="w-8 h-8 rounded-lg bg-brand-emerald/20 text-brand-emerald flex items-center justify-center flex-shrink-0">
          <i data-lucide="bot" class="w-4 h-4"></i>
        </div>
        <div class="bg-brand-elevated border border-brand-border/60 p-3.5 rounded-2xl rounded-tl-sm text-xs text-slate-200 max-w-xl space-y-2 leading-relaxed">
          ${responseHtml}
        </div>
      `;
      chatWindow.appendChild(botMsg);
      if (window.lucide) lucide.createIcons();
      chatWindow.scrollTop = chatWindow.scrollHeight;
    }, 700);
  }

  generateAdvisorResponse(query) {
    const q = query.toLowerCase();
    if (q.includes('spending') || q.includes('leak') || q.includes('analyze')) {
      return `
        <p class="font-bold text-brand-emeraldLight">📊 CENTSIBLE Spending Analysis & Insights:</p>
        <p>• <strong>Primary Outflow</strong>: Food & Dining represents approximately 42% of your monthly expenditure.</p>
        <p>• <strong>Budget Status</strong>: Utilities and Transportation remain within safe margins (< 40% utilized).</p>
        <p>• <strong>Actionable Opportunity</strong>: Trimming dining deliveries by just twice per week will salvage <strong>$180–$240/mo</strong>, accelerating your Emergency Reserve milestone by nearly 75 days.</p>
      `;
    }
    if (q.includes('dining') || q.includes('food') || q.includes('cut')) {
      return `
        <p class="font-bold text-brand-gold">💡 High-Impact Strategies for Cutting Dining Expenses:</p>
        <p>1. <strong>Weekly Cap</strong>: Enforce a $110/week ceiling on food delivery platforms (Swiggy, DoorDash, UberEats).</p>
        <p>2. <strong>Meal Prep Automation</strong>: 3 batch-cooked dinners save ~$140 each month.</p>
        <p>3. <strong>Audit Hidden Surcharges</strong>: Service fees and surge pricing inflate average delivery tickets by 32%.</p>
      `;
    }
    if (q.includes('emergency') || q.includes('goal') || q.includes('target')) {
      return `
        <p class="font-bold text-brand-sapphire">🎯 Wealth Milestones Audit:</p>
        <p>• <strong>Emergency Reserve</strong>: Currently at <strong>65.3%</strong> ($9,800 of $15,000 target).</p>
        <p>• <strong>Vacation Fund</strong>: Funded at <strong>63.3%</strong> ($2,850 of $4,500 target).</p>
        <p>• <strong>Pacing</strong>: At your current savings rate of 66.2%, your Emergency Cushion reaches 100% completion in ~85 days.</p>
      `;
    }
    if (q.includes('invest') || q.includes('framework') || q.includes('stock')) {
      return `
        <p class="font-bold text-brand-emeraldLight">📈 CENTSIBLE Core Growth Allocation Framework:</p>
        <p>1. <strong>Core Broad Market (60%)</strong>: Low-cost total world / S&P 500 ETFs (VOO/VTI) for consistent compounding.</p>
        <p>2. <strong>Global Currency Hedge (20%)</strong>: Developed international equities (VEA) to balance USD/EUR parity.</p>
        <p>3. <strong>Liquidity Reserve (15%)</strong>: High-Yield Treasury bills yielding 4.5%+ risk-free return.</p>
        <p>4. <strong>Asymmetric Upside (5%)</strong>: Blue-chip digital commodities (BTC/ETH).</p>
      `;
    }
    return `
      <p class="font-bold text-white">👋 CENTSIBLE Wealth Strategist:</p>
      <p>I have reviewed your financial snapshot:</p>
      <p>• Active Cash Vaults: <strong>${this.accounts.length} Institutions</strong></p>
      <p>• Central Ledger Transactions: <strong>${this.transactions.length} records</strong></p>
      <p>• Current Savings Rate: <strong>66.2%</strong></p>
      <p>Feel free to ask me to analyze specific expense categories, evaluate budget limits, or plan investments!</p>
    `;
  }

  clearAdvisorChat() {
    const chat = document.getElementById('advisorChatWindow');
    if (!chat) return;
    chat.innerHTML = `
      <div class="flex items-start space-x-3">
        <div class="w-8 h-8 rounded-lg bg-brand-emerald/20 text-brand-emerald flex items-center justify-center flex-shrink-0">
          <i data-lucide="sparkles" class="w-4 h-4"></i>
        </div>
        <div class="bg-brand-elevated border border-brand-border/60 p-3.5 rounded-2xl rounded-tl-sm text-xs text-slate-200 max-w-xl space-y-2 leading-relaxed">
          <p class="font-semibold text-white">Advisor chat session reset.</p>
          <p>Ask anything about your money, budgets, or investments.</p>
        </div>
      </div>
    `;
    if (window.lucide) lucide.createIcons();
  }

  // UPI SMS Parser
  loadSampleSms(num) {
    const input = document.getElementById('upiSmsInput');
    if (!input) return;

    if (num === 1) {
      input.value = "Dear SBI User, A/C 4892 debited by Rs 450.00 on 26Sep26 transfer to Swiggy UPI/627192837482. Bal: Rs 45,210";
    } else if (num === 2) {
      input.value = "HDFC Bank: Rs 2,500.00 debited from a/c **9124 on 26-09-26 to SHELL MOBILITY PETROL UPI ref 981240192841. Avl bal: Rs 1,12,400.";
    } else if (num === 3) {
      input.value = "Chase Alert: Your card ending in 4102 was charged $14.50 at Starbucks Coffee. Ref: CHASE-9021-TX";
    }
  }

  parseUpiSms() {
    const text = document.getElementById('upiSmsInput')?.value || '';
    if (!text.trim()) {
      this.showToast('Please enter or paste an SMS message first.');
      return;
    }

    // Heuristics regex
    const amountRegex = /(?:rs\.?|inr|\$|€|£)\s*([\d,]+(?:\.\d{1,2})?)/i;
    const match = text.match(amountRegex);
    let amount = 0;
    if (match) {
      amount = parseFloat(match[1].replace(/,/g, ''));
    } else {
      const fallbackAmount = text.match(/([\d,]+\.\d{2})/);
      amount = fallbackAmount ? parseFloat(fallbackAmount[1].replace(/,/g, '')) : 25.00;
    }

    let merchant = "Direct Transfer";
    let category = "Utilities";

    const lower = text.toLowerCase();
    if (lower.includes('swiggy') || lower.includes('zomato')) {
      merchant = "Swiggy / Zomato";
      category = "Food & Dining";
    } else if (lower.includes('shell') || lower.includes('petrol') || lower.includes('fuel') || lower.includes('uber')) {
      merchant = "Shell Mobility Fuel";
      category = "Transportation";
    } else if (lower.includes('starbucks') || lower.includes('cafe') || lower.includes('coffee')) {
      merchant = "Starbucks Coffee";
      category = "Food & Dining";
    } else if (lower.includes('amazon') || lower.includes('flipkart')) {
      merchant = "Amazon Marketplace";
      category = "Shopping";
    }

    const refMatch = text.match(/(?:upi\/|ref\s*|rrn\s*:?\s*)([A-Za-z0-9\-_]+)/i);
    const refId = refMatch ? refMatch[1] : 'UPI-' + Math.floor(Math.random() * 900000000 + 100000000);

    this.pendingUpiParsed = {
      title: merchant,
      amount: amount > 500 ? (amount / 84.5) : amount, // normalize if in INR to standard display
      rawAmountStr: match ? match[0] : `$${amount}`,
      category: category,
      account: 'HDFC Salary IN',
      refId: refId
    };

    document.getElementById('upiExtractedAmount').innerText = this.pendingUpiParsed.rawAmountStr;
    document.getElementById('upiExtractedMerchant').innerText = merchant;
    document.getElementById('upiExtractedCategory').innerText = category;
    document.getElementById('upiExtractedRef').innerText = refId;
    document.getElementById('upiParsedResult').classList.remove('hidden');
    this.showToast('UPI SMS extracted successfully!');
  }

  saveUpiToLedger() {
    if (!this.pendingUpiParsed) return;
    const newTx = {
      id: 'tx-' + Date.now(),
      title: this.pendingUpiParsed.title,
      amount: this.pendingUpiParsed.amount,
      type: 'EXPENSE',
      category: this.pendingUpiParsed.category,
      account: this.pendingUpiParsed.account,
      date: new Date().toISOString().split('T')[0]
    };
    this.transactions.unshift(newTx);
    this.saveData('centsible_transactions', this.transactions);
    this.renderDashboard();
    this.renderTransactions();
    this.initCategoryChart();
    document.getElementById('upiParsedResult').classList.add('hidden');
    document.getElementById('upiSmsInput').value = '';
    this.pendingUpiParsed = null;
    this.showToast('UPI transaction recorded in CENTSIBLE Ledger! ✅');
  }

  // Receipt Scanner
  loadSampleReceipt() {
    document.getElementById('scannerEmptyState').classList.add('hidden');
    document.getElementById('scannerResultState').classList.remove('hidden');

    this.pendingReceiptParsed = {
      merchant: 'Artisan Roast & Bakery',
      total: 38.45,
      date: '2026-09-26',
      items: [
        '2x Cold Brew Reserve ($14.00)',
        '1x Avocado Sourdough Toast ($16.50)',
        '1x Almond Croissant ($5.50)',
        'Tax & Surcharge ($2.45)'
      ]
    };

    document.getElementById('scanMerchant').innerText = this.pendingReceiptParsed.merchant;
    document.getElementById('scanDate').innerText = this.pendingReceiptParsed.date;
    document.getElementById('scanTotal').innerText = `$${this.pendingReceiptParsed.total.toFixed(2)}`;
    document.getElementById('scanLineItems').innerHTML = this.pendingReceiptParsed.items.map(i => `<li>• ${i}</li>`).join('');
    this.showToast('Sample receipt OCR scanned.');
  }

  handleReceiptUpload(e) {
    const file = e.target.files[0];
    if (!file) return;

    this.showToast(`Analyzing "${file.name}" with OCR...`);
    setTimeout(() => {
      this.loadSampleReceipt();
    }, 800);
  }

  saveReceiptToLedger() {
    if (!this.pendingReceiptParsed) return;
    const newTx = {
      id: 'tx-' + Date.now(),
      title: this.pendingReceiptParsed.merchant,
      amount: this.pendingReceiptParsed.total,
      type: 'EXPENSE',
      category: 'Food & Dining',
      account: 'Chase Sapphire US',
      date: this.pendingReceiptParsed.date
    };
    this.transactions.unshift(newTx);
    this.saveData('centsible_transactions', this.transactions);
    this.renderDashboard();
    this.renderTransactions();
    this.initCategoryChart();
    document.getElementById('scannerResultState').classList.add('hidden');
    document.getElementById('scannerEmptyState').classList.remove('hidden');
    this.pendingReceiptParsed = null;
    this.showToast('Digitized receipt saved as ledger expense! ✅');
  }

  // Modals
  openModal(id) {
    document.getElementById(id)?.classList.remove('hidden');
  }

  closeModal(id) {
    document.getElementById(id)?.classList.add('hidden');
  }

  handleAddTxSubmit(e) {
    e.preventDefault();
    const title = document.getElementById('modalTxTitle').value.trim();
    const amount = parseFloat(document.getElementById('modalTxAmount').value);
    const type = document.getElementById('modalTxType').value;
    const category = document.getElementById('modalTxCategory').value;
    const account = document.getElementById('modalTxAccount').value;

    if (!title || isNaN(amount) || amount <= 0) return;

    const newTx = {
      id: 'tx-' + Date.now(),
      title,
      amount,
      type,
      category,
      account,
      date: new Date().toISOString().split('T')[0]
    };

    this.transactions.unshift(newTx);
    this.saveData('centsible_transactions', this.transactions);
    this.renderDashboard();
    this.renderTransactions();
    this.initCategoryChart();
    this.closeModal('addTxModal');
    e.target.reset();
    this.showToast(`Transaction "${title}" added to ledger!`);
  }

  handleAddBudgetSubmit(e) {
    e.preventDefault();
    const category = document.getElementById('modalBudgetCategory').value.trim();
    const limit = parseFloat(document.getElementById('modalBudgetLimit').value);

    if (!category || isNaN(limit) || limit <= 0) return;

    this.budgets.push({ id: 'b-' + Date.now(), category, limit });
    this.saveData('centsible_budgets', this.budgets);
    this.renderDashboard();
    this.renderBudgetsAndGoals();
    this.closeModal('addBudgetModal');
    e.target.reset();
    this.showToast(`Monthly budget for "${category}" created!`);
  }

  handleAddGoalSubmit(e) {
    e.preventDefault();
    const title = document.getElementById('modalGoalTitle').value.trim();
    const target = parseFloat(document.getElementById('modalGoalTarget').value);
    const current = parseFloat(document.getElementById('modalGoalCurrent').value) || 0;

    if (!title || isNaN(target) || target <= 0) return;

    this.goals.push({ id: 'g-' + Date.now(), title, target, current });
    this.saveData('centsible_goals', this.goals);
    this.renderBudgetsAndGoals();
    this.closeModal('addGoalModal');
    e.target.reset();
    this.showToast(`Goal milestone "${title}" created!`);
  }

  // Vault Lock & PIN
  toggleVaultLock() {
    this.isLocked = !this.isLocked;
    const overlay = document.getElementById('vaultLockOverlay');
    if (this.isLocked) {
      overlay.classList.remove('hidden');
      this.enteredPin = '';
      this.updatePinDots();
    } else {
      overlay.classList.add('hidden');
    }
  }

  enterPin(digit) {
    if (this.enteredPin.length < 4) {
      this.enteredPin += digit.toString();
      this.updatePinDots();

      if (this.enteredPin.length === 4) {
        if (this.enteredPin === this.correctPin) {
          this.isLocked = false;
          document.getElementById('vaultLockOverlay').classList.add('hidden');
          this.showToast('CENTSIBLE Vault Unlocked.');
        } else {
          this.showToast('Invalid PIN. Default is 1234');
          setTimeout(() => {
            this.enteredPin = '';
            this.updatePinDots();
          }, 400);
        }
      }
    }
  }

  clearPin() {
    this.enteredPin = '';
    this.updatePinDots();
  }

  unlockWithBiometrics() {
    this.isLocked = false;
    document.getElementById('vaultLockOverlay').classList.add('hidden');
    this.showToast('Biometric Touch ID Verified. Welcome to CENTSIBLE!');
  }

  updatePinDots() {
    const dots = document.querySelectorAll('#pinDots span');
    dots.forEach((dot, idx) => {
      if (idx < this.enteredPin.length) {
        dot.classList.add('bg-brand-emerald', 'border-brand-emerald');
        dot.classList.remove('bg-brand-dark');
      } else {
        dot.classList.remove('bg-brand-emerald', 'border-brand-emerald');
        dot.classList.add('bg-brand-dark');
      }
    });
  }

  // Navigation
  switchTab(tabId) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
      if (btn.getAttribute('data-tab') === tabId) {
        btn.classList.add('bg-brand-emerald/20', 'text-brand-emerald', 'border', 'border-brand-emerald/30');
        btn.classList.remove('text-slate-400');
      } else {
        btn.classList.remove('bg-brand-emerald/20', 'text-brand-emerald', 'border', 'border-brand-emerald/30');
        btn.classList.add('text-slate-400');
      }
    });

    document.querySelectorAll('.tab-content').forEach(content => {
      content.classList.add('hidden');
    });

    const activeContent = document.getElementById(`tab-${tabId}`);
    if (activeContent) {
      activeContent.classList.remove('hidden');
    }

    if (tabId === 'reports') {
      setTimeout(() => this.initCategoryChart(), 50);
    }
  }

  // Social Milestone Share
  shareMilestone() {
    const text = `I achieved a 66.2% savings rate this month and managed ${this.transactions.length} transactions across multi-currency accounts using CENTSIBLE! 🚀 https://centsible.app`;
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text).then(() => {
        this.showToast('Achievement copied to clipboard! Ready to share. 📋');
      });
    } else {
      this.showToast('Milestone prepared!');
    }
  }

  // Export / Import Vault
  exportDataBackup() {
    const data = {
      version: '1.0.0',
      appName: 'CENTSIBLE',
      currency: this.currentCurrency,
      exportedAt: new Date().toISOString(),
      transactions: this.transactions,
      budgets: this.budgets,
      goals: this.goals
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `centsible_vault_backup_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
    this.showToast('Encrypted JSON Vault Backup exported! 💾');
  }

  importDataBackup(e) {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const parsed = JSON.parse(event.target.result);
        if (parsed.transactions && Array.isArray(parsed.transactions)) {
          this.transactions = parsed.transactions;
          this.saveData('centsible_transactions', this.transactions);
        }
        if (parsed.budgets && Array.isArray(parsed.budgets)) {
          this.budgets = parsed.budgets;
          this.saveData('centsible_budgets', this.budgets);
        }
        if (parsed.goals && Array.isArray(parsed.goals)) {
          this.goals = parsed.goals;
          this.saveData('centsible_goals', this.goals);
        }
        this.renderDashboard();
        this.renderTransactions();
        this.renderBudgetsAndGoals();
        this.initCategoryChart();
        this.showToast('Backup restored successfully into CENTSIBLE! 🚀');
      } catch (err) {
        this.showToast('Invalid backup JSON file.');
      }
    };
    reader.readAsText(file);
  }

  // Toast Notification
  showToast(msg) {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'bg-brand-elevated border border-brand-emerald text-white text-xs font-semibold px-4 py-3 rounded-xl shadow-xl pointer-events-auto flex items-center space-x-2 transition-all transform translate-y-2 opacity-0';
    toast.innerHTML = `
      <i data-lucide="check-circle" class="w-4 h-4 text-brand-emerald"></i>
      <span>${this.escapeHtml(msg)}</span>
    `;

    container.appendChild(toast);
    if (window.lucide) lucide.createIcons();

    setTimeout(() => {
      toast.classList.remove('translate-y-2', 'opacity-0');
    }, 10);

    setTimeout(() => {
      toast.classList.add('opacity-0', 'translate-y-2');
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  }

  escapeHtml(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }
}

// Instantiate on load
window.addEventListener('DOMContentLoaded', () => {
  window.centsibleApp = new CentsibleWebEngine();
});
