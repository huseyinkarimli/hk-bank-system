/** Spring Data page wrapped in ApiResponse `data` (unwrapped by apiFetch). */
export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  number: number;
  size: number;
}

export interface AdminDashboardStats {
  totalUsers: number;
  bannedUsers: number;
  totalActiveCards: number;
  totalBlockedCards: number;
  todayTransactionCount: number;
  todayTransactionVolume: string | number;
  totalBalanceInAzn: string | number;
  generatedAt?: string;
}

export interface AdminTransactionStats {
  totalTransactions?: number;
  totalVolume?: string | number;
  countByStatus?: Record<string, number>;
  dailyVolume?: Record<string, string | number>;
}

export interface AdminUserRow {
  id: number;
  firstName?: string;
  lastName?: string;
  email: string;
  phoneNumber?: string;
  role?: string;
  createdAt?: string;
}

export interface AdminAccountRow {
  id: number;
  accountNumber?: string;
  iban?: string;
  balance?: string | number;
  currencyType?: string;
  status?: string;
  createdAt?: string;
}

export interface AdminTransactionRow {
  id: number;
  referenceNumber?: string;
  type?: string;
  status?: string;
  amount?: string | number;
  sourceCurrency?: string;
  createdAt?: string;
}

export interface AdminCardRow {
  id: number;
  maskedCardNumber?: string;
  cardHolder?: string;
  cardType?: string;
  status?: string;
  expiryDate?: string;
}

export interface AdminAuditRow {
  id: number;
  userId?: number;
  action?: string;
  description?: string;
  ipAddress?: string;
  entityType?: string;
  entityId?: number;
  createdAt?: string;
}
