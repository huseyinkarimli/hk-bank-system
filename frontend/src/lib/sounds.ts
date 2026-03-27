/**
 * Web Audio API — lightweight UI feedback. Enabled state from localStorage `soundFeedbackEnabled`.
 */

const STORAGE_KEY = 'soundFeedbackEnabled';

export function isSoundFeedbackEnabled(): boolean {
  if (typeof window === 'undefined') return true;
  const v = localStorage.getItem(STORAGE_KEY);
  if (v === null) return true;
  return v === 'true';
}

export function setSoundFeedbackEnabled(enabled: boolean): void {
  localStorage.setItem(STORAGE_KEY, String(enabled));
  window.dispatchEvent(new Event('hkbank-sound-settings'));
}

type OscillatorKind = 'click' | 'success' | 'error' | 'notification' | 'transfer';

const CONFIG: Record<
  OscillatorKind,
  { frequency: number; duration: number; type: OscillatorType; volume: number }
> = {
  click: { frequency: 420, duration: 0.045, type: 'sine', volume: 0.22 },
  success: { frequency: 620, duration: 0.09, type: 'sine', volume: 0.35 },
  error: { frequency: 280, duration: 0.14, type: 'sine', volume: 0.32 },
  notification: { frequency: 520, duration: 0.07, type: 'sine', volume: 0.28 },
  transfer: { frequency: 580, duration: 0.11, type: 'sine', volume: 0.34 },
};

let audioContext: AudioContext | null = null;

function getContext(): AudioContext | null {
  if (typeof window === 'undefined') return null;
  if (!audioContext) {
    const AC = window.AudioContext || (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
    if (!AC) return null;
    try {
      audioContext = new AC();
    } catch {
      return null;
    }
  }
  return audioContext;
}

function play(kind: OscillatorKind): void {
  if (!isSoundFeedbackEnabled()) return;
  try {
    const ctx = getContext();
    if (!ctx) return;
    const cfg = CONFIG[kind];
    const oscillator = ctx.createOscillator();
    const gain = ctx.createGain();
    oscillator.connect(gain);
    gain.connect(ctx.destination);
    oscillator.frequency.value = cfg.frequency;
    oscillator.type = cfg.type;
    const t = ctx.currentTime;
    gain.gain.setValueAtTime(cfg.volume, t);
    gain.gain.exponentialRampToValueAtTime(0.001, t + cfg.duration);
    oscillator.start(t);
    oscillator.stop(t + cfg.duration);
  } catch {
    /* ignore */
  }
}

export const playClick = () => play('click');
export const playSuccess = () => play('success');
export const playError = () => play('error');
export const playNotification = () => play('notification');
export const playTransfer = () => play('transfer');
