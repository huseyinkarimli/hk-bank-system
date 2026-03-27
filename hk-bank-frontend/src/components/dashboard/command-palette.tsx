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
      group: 'Navigation',
      items: [
        {
          icon: LayoutDashboard,
          label: 'Dashboard',
          description: 'Go to your dashboard',
          action: () => navigate('/dashboard'),
        },
      ],
    },
    {
      group: 'Actions',
      items: [
        {
          icon: Send,
          label: 'Transfer Money',
          description: 'Send money to another account',
          action: () => navigate('/dashboard'),
        },
        {
          icon: Plus,
          label: 'New Card',
          description: 'Request a new debit or credit card',
          action: () => navigate('/dashboard'),
        },
        {
          icon: Plus,
          label: 'New Account',
          description: 'Create a new savings account',
          action: () => navigate('/dashboard'),
        },
      ],
    },
    {
      group: 'Tools',
      items: [
        {
          icon: Zap,
          label: 'Quick Pay',
          description: 'Pay from your favorite payees',
          action: () => navigate('/dashboard'),
        },
        {
          icon: CreditCard,
          label: 'Request Money',
          description: 'Request money from friends',
          action: () => navigate('/dashboard'),
        },
      ],
    },
    {
      group: 'Settings',
      items: [
        {
          icon: Settings,
          label: 'Preferences',
          description: 'Manage your account settings',
          action: () => navigate('/dashboard'),
        },
      ],
    },
  ];

  return (
    <CommandDialog open={open} onOpenChange={setOpen}>
      <CommandInput placeholder="Search actions or settings..." />
      <CommandList>
        <CommandEmpty>No results found.</CommandEmpty>
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
              >
                <item.icon className="mr-2 h-4 w-4 text-cyan-400" />
                <div className="flex-1">
                  <p className="font-medium">{item.label}</p>
                  <p className="text-xs text-slate-500">{item.description}</p>
                </div>
                <ArrowRight className="h-4 w-4 text-slate-500" />
              </CommandItem>
            ))}
          </CommandGroup>
        ))}
      </CommandList>
    </CommandDialog>
  );
}
