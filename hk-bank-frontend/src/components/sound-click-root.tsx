import { useEffect } from 'react';
import { playClick } from '@/lib/sounds';

/**
 * Capturing listener: click feedback on buttons, links, and role="button" (excludes form fields).
 */
export function SoundClickRoot() {
  useEffect(() => {
    const onPointerDown = (e: PointerEvent) => {
      if (e.button !== 0) return;
      const target = e.target as HTMLElement | null;
      if (!target) return;
      if (target.closest('input, textarea, select, [contenteditable="true"], [data-sound-ignore]')) {
        return;
      }
      const interactive = target.closest(
        'button, a[href], [role="button"], input[type="submit"], input[type="button"], [data-sound-click]'
      );
      if (!interactive) return;
      playClick();
    };
    document.addEventListener('pointerdown', onPointerDown, true);
    return () => document.removeEventListener('pointerdown', onPointerDown, true);
  }, []);

  return null;
}
