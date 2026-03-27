const EXCHANGE_RATES: Record<string, number> = {
  'AZN/USD': 0.588,
  'USD/AZN': 1.7,
  'AZN/EUR': 0.535,
  'EUR/AZN': 1.87,
  'USD/EUR': 0.91,
  'EUR/USD': 1.1,
};

export function convertCurrency(amount: number, fromCurrency: string, toCurrency: string): number {
  if (fromCurrency === toCurrency) return amount;
  const rateKey = `${fromCurrency}/${toCurrency}`;
  const rate = EXCHANGE_RATES[rateKey];
  if (!rate) {
    return 0;
  }
  return parseFloat((amount * rate).toFixed(2));
}

export function getAvailableCurrencies(): string[] {
  return ['AZN', 'USD', 'EUR'];
}

export function formatCurrency(amount: number, currency: string): string {
  try {
    return new Intl.NumberFormat('az-AZ', {
      style: 'currency',
      currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `${amount.toFixed(2)} ${currency}`;
  }
}
