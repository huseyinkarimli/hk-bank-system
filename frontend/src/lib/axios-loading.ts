let pending = 0;

export function axiosLoadingStart(): void {
  pending += 1;
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('data-axios-loading', 'true');
  }
}

export function axiosLoadingDone(): void {
  pending = Math.max(0, pending - 1);
  if (pending === 0 && typeof document !== 'undefined') {
    document.documentElement.removeAttribute('data-axios-loading');
  }
}
