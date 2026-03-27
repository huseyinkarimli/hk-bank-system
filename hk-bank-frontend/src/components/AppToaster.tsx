import { Toaster } from 'sonner';
import { useTheme } from '@/context/theme-context';

export function AppToaster() {
  const { isDark } = useTheme();
  return <Toaster richColors position="top-center" theme={isDark ? 'dark' : 'light'} />;
}
