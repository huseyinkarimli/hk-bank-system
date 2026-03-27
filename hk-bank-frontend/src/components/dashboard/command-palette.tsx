import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import {
  ArrowRight,
  CreditCard,
  LayoutDashboard,
  Plus,
  Send,
  Settings,
  Zap,
  Wallet,
  Bell,
  UserRound,
} from 'lucide-react';

export function CommandPalette() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setOpen((v) => !v);
      }
    };

    const onOpen = () => setOpen(true);

    document.addEventListener('keydown', down);
    window.addEventListener('hk:open-command-palette', onOpen);
    return () => {
      document.removeEventListener('keydown', down);
      window.removeEventListener('hk:open-command-palette', onOpen);
    };
  }, []);

  const commands = [
    {
      group: 'Naviqasiya',
      items: [
        {
          icon: LayoutDashboard,
          label: 'İdarə paneli',
          description: 'Əsas ekrana keç',
          action: () => navigate('/dashboard'),
        },
        {
          icon: Wallet,
          label: 'Hesablar',
          description: 'Hesablarımı aç',
          action: () => navigate('/accounts'),
        },
        {
          icon: Bell,
          label: 'Bildirişlər',
          description: 'Bildirişlər siyahısı',
          action: () => navigate('/notifications'),
        },
        {
          icon: UserRound,
          label: 'Profil',
          description: 'Profil və parametrlər',
          action: () => navigate('/profile'),
        },
      ],
    },
    {
      group: 'Əməliyyatlar',
      items: [
        {
          icon: Send,
          label: 'Köçürmə',
          description: 'Başqa hesaba köçürmə',
          action: () => navigate('/transfer'),
        },
        {
          icon: Plus,
          label: 'Yeni kart',
          description: 'Debet və ya kredit kartı',
          action: () => navigate('/cards'),
        },
        {
          icon: Plus,
          label: 'Yeni hesab',
          description: 'Yeni hesab aç',
          action: () => navigate('/accounts'),
        },
      ],
    },
    {
      group: 'Alətlər',
      items: [
        {
          icon: Zap,
          label: 'Sürətli ödəniş',
          description: 'Ödənişlər səhifəsi',
          action: () => navigate('/payments'),
        },
        {
          icon: CreditCard,
          label: 'Kartlarım',
          description: 'Kart idarəetməsi',
          action: () => navigate('/cards'),
        },
      ],
    },
    {
      group: 'Parametrlər',
      items: [
        {
          icon: Settings,
          label: 'Tənzimləmələr',
          description: 'Profil və görünüş',
          action: () => navigate('/profile'),
        },
      ],
    },
  ];

  return (
    <CommandDialog
      open={open}
      onOpenChange={setOpen}
      title="Əmrlər paneli"
      description="Əməliyyat axtarın və seçin"
      className="pb-[max(12px,env(safe-area-inset-bottom))]"
    >
      <CommandInput placeholder="Axtarış…" className="min-h-12 text-base" />
      <CommandList className="max-h-[min(70vh,28rem)]">
        <CommandEmpty>Nəticə tapılmadı.</CommandEmpty>
        {commands.map((group) => (
          <CommandGroup key={group.group} heading={group.group}>
            {group.items.map((item) => (
              <CommandItem
                key={item.label}
                value={item.label}
                onSelect={() => {
                  item.action();
                  setOpen(false);
                }}
                className="min-h-12 py-3 cursor-pointer touch-manipulation"
              >
                <item.icon className="mr-2 h-5 w-5 text-cyan-400 shrink-0" />
                <div className="flex-1 min-w-0 text-left">
                  <p className="font-medium">{item.label}</p>
                  <p className="text-xs text-slate-500">{item.description}</p>
                </div>
                <ArrowRight className="h-4 w-4 text-slate-500 shrink-0" />
              </CommandItem>
            ))}
          </CommandGroup>
        ))}
      </CommandList>
    </CommandDialog>
  );
}
