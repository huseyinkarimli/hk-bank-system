import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';

const STORAGE_KEY = 'hkbank_theme';

interface ThemeContextType {
  /** True when dark mode is active (Tailwind `dark` class on the root element). */
  isDark: boolean;
  toggle: () => void;
  setDark: (dark: boolean) => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

function applyDarkClass(dark: boolean) {
  const root = document.documentElement;
  document.body.dataset.themeTransition = '1';
  if (dark) {
    root.classList.add('dark');
  } else {
    root.classList.remove('dark');
  }
  window.setTimeout(() => {
    delete document.body.dataset.themeTransition;
  }, 400);
}

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [isDark, setIsDarkState] = useState(true);

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved != null) {
      try {
        const dark = JSON.parse(saved) as boolean;
        setIsDarkState(dark);
        applyDarkClass(dark);
        return;
      } catch {
        /* fall through */
      }
    }
    applyDarkClass(true);
  }, []);

  const setDark = useCallback((dark: boolean) => {
    setIsDarkState(dark);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(dark));
    applyDarkClass(dark);
  }, []);

  const toggle = useCallback(() => {
    setIsDarkState((prev) => {
      const next = !prev;
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      applyDarkClass(next);
      return next;
    });
  }, []);

  const value = useMemo(
    () => ({ isDark, toggle, setDark }),
    [isDark, toggle, setDark]
  );

  return (
    <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return ctx;
}
