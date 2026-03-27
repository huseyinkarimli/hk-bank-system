import { useCallback, useEffect, useState } from 'react';
import {
  isSoundFeedbackEnabled,
  setSoundFeedbackEnabled,
  playClick,
  playSuccess,
  playError,
  playNotification,
  playTransfer,
} from '@/lib/sounds';

export function useSoundFeedback() {
  const [enabled, setEnabled] = useState(() =>
    typeof window !== 'undefined' ? isSoundFeedbackEnabled() : true
  );

  useEffect(() => {
    const sync = () => setEnabled(isSoundFeedbackEnabled());
    window.addEventListener('hkbank-sound-settings', sync);
    window.addEventListener('storage', sync);
    return () => {
      window.removeEventListener('hkbank-sound-settings', sync);
      window.removeEventListener('storage', sync);
    };
  }, []);

  const toggleSound = useCallback((next: boolean) => {
    setSoundFeedbackEnabled(next);
    setEnabled(next);
  }, []);

  const playSound = useCallback((type: 'click' | 'success' | 'error' | 'notification' | 'transfer') => {
    switch (type) {
      case 'click':
        playClick();
        break;
      case 'success':
        playSuccess();
        break;
      case 'error':
        playError();
        break;
      case 'notification':
        playNotification();
        break;
      case 'transfer':
        playTransfer();
        break;
    }
  }, []);

  return {
    enabled,
    toggleSound,
    playSound,
    playClick,
    playSuccess,
    playError,
    playTransfer,
    playNotification,
  };
}
