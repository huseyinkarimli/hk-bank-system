import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  role?: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
  isAuthenticated: boolean;
}

export interface RegisterData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber?: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

type AuthPayload = {
  token?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  role?: string;
  user?: {
    id?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    phoneNumber?: string;
    role?: string;
  };
};

async function parseAuthJson(response: Response): Promise<{ token: string; user: User }> {
  const payload = await response.json();
  const body: AuthPayload = payload?.data !== undefined ? payload.data : payload;

  const token = body.token;
  const nested = body.user;
  const user: User = nested
    ? {
        id: String(nested.id ?? nested.email ?? ''),
        email: nested.email ?? '',
        firstName: nested.firstName ?? '',
        lastName: nested.lastName ?? '',
        phoneNumber: nested.phoneNumber,
        role: nested.role,
      }
    : {
        id: body.email ?? '',
        email: body.email ?? '',
        firstName: body.firstName ?? '',
        lastName: body.lastName ?? '',
        phoneNumber: body.phoneNumber,
        role: body.role,
      };

  if (!token || !user.email) {
    throw new Error('Invalid auth response');
  }

  return { token, user };
}

function normalizePhoneForApi(phone: string | undefined): string {
  const digits = (phone ?? '').replace(/\D/g, '').replace(/^994/, '');
  return `+994${digits}`;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('auth_token');
    const storedUser = localStorage.getItem('auth_user');

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }

    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        throw new Error('Login failed');
      }

      const { token: nextToken, user: nextUser } = await parseAuthJson(response);

      setToken(nextToken);
      setUser(nextUser);
      localStorage.setItem('auth_token', nextToken);
      localStorage.setItem('auth_user', JSON.stringify(nextUser));
    } catch (error) {
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterData) => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
          password: data.password,
          phoneNumber: normalizePhoneForApi(data.phoneNumber),
        }),
      });

      if (!response.ok) {
        throw new Error('Registration failed');
      }

      const { token: nextToken, user: nextUser } = await parseAuthJson(response);

      setToken(nextToken);
      setUser(nextUser);
      localStorage.setItem('auth_token', nextToken);
      localStorage.setItem('auth_user', JSON.stringify(nextUser));
    } catch (error) {
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
  };

  const refreshProfile = useCallback(async () => {
    const activeToken = token ?? localStorage.getItem('auth_token');
    if (!activeToken) return;

    try {
      const response = await fetch('/api/users/me', {
        headers: { Authorization: `Bearer ${activeToken}` },
      });
      if (!response.ok) return;

      const payload = await response.json();
      const data = payload?.data ?? payload;
      if (!data?.email) return;

      const next: User = {
        id: String(data.id ?? data.email),
        email: data.email,
        firstName: data.firstName ?? '',
        lastName: data.lastName ?? '',
        phoneNumber: data.phoneNumber,
        role: typeof data.role === 'string' ? data.role : data.role?.name ?? data.role,
      };

      setUser(next);
      localStorage.setItem('auth_user', JSON.stringify(next));
    } catch {
      /* keep cached user */
    }
  }, [token]);

  useEffect(() => {
    if (!token || isLoading) return;
    void refreshProfile();
  }, [token, isLoading, refreshProfile]);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isLoading,
        login,
        register,
        logout,
        refreshProfile,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
