import { Search, Eye, EyeOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { usePrivacy } from '@/context/privacy-context';

export function Header() {
  const { isPrivacyMode, togglePrivacyMode } = usePrivacy();

  const openCommandPalette = () => {
    window.dispatchEvent(new CustomEvent('hk:open-command-palette'));
  };

  return (
    <header className="sticky top-0 z-30 w-full bg-slate-900/50 backdrop-blur-md border-b border-slate-700/50">
      <div className="h-16 px-4 md:px-8 flex items-center justify-between gap-4">
        <div className="hidden md:flex flex-1 max-w-md">
          <button
            type="button"
            onClick={openCommandPalette}
            className="w-full flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-800/50 border border-slate-700/50 text-left hover:bg-slate-800/70 transition-colors"
          >
            <Search className="h-4 w-4 text-slate-500 shrink-0" />
            <span className="flex-1 text-sm text-slate-500">Search transactions…</span>
            <kbd className="text-xs font-semibold text-slate-500 bg-slate-700/50 px-2 py-1 rounded">
              Ctrl K
            </kbd>
          </button>
        </div>

        <div className="flex items-center gap-2 md:ml-auto">
          <Button
            variant="ghost"
            size="icon"
            onClick={togglePrivacyMode}
            className="text-slate-400 hover:text-slate-200 hover:bg-slate-800/50"
            title={isPrivacyMode ? 'Disable Privacy Mode' : 'Enable Privacy Mode'}
          >
            {isPrivacyMode ? (
              <EyeOff className="h-5 w-5" />
            ) : (
              <Eye className="h-5 w-5" />
            )}
          </Button>
        </div>
      </div>
    </header>
  );
}
