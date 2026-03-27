import { createContext, useContext, useEffect, useState } from 'react';

interface PrivacyContextType {
  isPrivacyMode: boolean;
  togglePrivacyMode: () => void;
  blurAmount: number;
  setBlurAmount: (amount: number) => void;
}

const PrivacyContext = createContext<PrivacyContextType | undefined>(undefined);

export function PrivacyProvider({ children }: { children: React.ReactNode }) {
  const [isPrivacyMode, setIsPrivacyMode] = useState(false);
  const [blurAmount, setBlurAmount] = useState(10);
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem('privacyMode');
    const savedBlur = localStorage.getItem('blurAmount');
    if (saved) setIsPrivacyMode(JSON.parse(saved));
    if (savedBlur) setBlurAmount(JSON.parse(savedBlur));
    setIsHydrated(true);
  }, []);

  useEffect(() => {
    if (!isHydrated) return;
    localStorage.setItem('privacyMode', JSON.stringify(isPrivacyMode));
  }, [isPrivacyMode, isHydrated]);

  useEffect(() => {
    if (!isHydrated) return;
    localStorage.setItem('blurAmount', JSON.stringify(blurAmount));
  }, [blurAmount, isHydrated]);

  const togglePrivacyMode = () => {
    setIsPrivacyMode((prev) => !prev);
  };

  return (
    <PrivacyContext.Provider
      value={{
        isPrivacyMode,
        togglePrivacyMode,
        blurAmount,
        setBlurAmount,
      }}
    >
      {children}
    </PrivacyContext.Provider>
  );
}

export function usePrivacy() {
  const context = useContext(PrivacyContext);
  if (context === undefined) {
    throw new Error('usePrivacy must be used within PrivacyProvider');
  }
  return context;
}
