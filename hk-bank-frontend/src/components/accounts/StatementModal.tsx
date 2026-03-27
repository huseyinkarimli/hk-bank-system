import { useMemo, useState } from 'react';
import { toast } from 'sonner';
import { Download, FileJson } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { api } from '@/lib/axios';

interface StatementModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  accountId: string;
  currency: string;
}

/** Spring `LocalDateTime` query param without timezone drift from `datetime-local`. */
function toLocalDateTimeParam(s: string): string {
  if (!s) return '';
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(s)) return `${s}:00`;
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(s)) return s;
  return s;
}

export function StatementModal({
  open,
  onOpenChange,
  accountId,
  currency,
}: StatementModalProps) {
  const defaults = useMemo(() => {
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - 30);
    const pad = (n: number) => String(n).padStart(2, '0');
    const fmt = (d: Date) =>
      `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    return { from: fmt(from), to: fmt(to) };
  }, []);

  const [from, setFrom] = useState(defaults.from);
  const [to, setTo] = useState(defaults.to);
  const [busy, setBusy] = useState(false);

  const fromParam = toLocalDateTimeParam(from);
  const toParam = toLocalDateTimeParam(to);

  const handleJson = async () => {
    if (!fromParam || !toParam) {
      toast.error('Tarix aralığını seçin');
      return;
    }
    setBusy(true);
    try {
      const { data } = await api.get<{ data?: unknown }>(
        `/api/accounts/${accountId}/statement`,
        { params: { from: fromParam, to: toParam } }
      );
      const payload = data?.data ?? data;
      const blob = new Blob([JSON.stringify(payload, null, 2)], {
        type: 'application/json',
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `statement_${accountId}_${from.slice(0, 10)}_${to.slice(0, 10)}.json`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('JSON hesabat yükləndi');
    } catch {
      /* interceptor */
    } finally {
      setBusy(false);
    }
  };

  const handlePdf = async () => {
    if (!fromParam || !toParam) {
      toast.error('Tarix aralığını seçin');
      return;
    }
    setBusy(true);
    try {
      const res = await api.get(`/api/accounts/${accountId}/statement/pdf`, {
        params: { from: fromParam, to: toParam },
        responseType: 'blob',
      });
      const blob = res.data as Blob;
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `statement_${accountId}_${from.slice(0, 10)}_${to.slice(0, 10)}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('PDF hesabat yükləndi');
    } catch {
      /* interceptor */
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Hesabat — {currency}</DialogTitle>
          <DialogDescription>
            Tarix aralığını seçin və JSON və ya PDF formatında yükləyin.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-2">
          <div className="grid gap-2">
            <Label htmlFor="stmt-from">Başlanğıc</Label>
            <Input
              id="stmt-from"
              type="datetime-local"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="stmt-to">Son</Label>
            <Input
              id="stmt-to"
              type="datetime-local"
              value={to}
              onChange={(e) => setTo(e.target.value)}
            />
          </div>
        </div>
        <DialogFooter className="flex-col sm:flex-row gap-2">
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Bağla
          </Button>
          <Button
            type="button"
            variant="secondary"
            className="gap-2"
            disabled={busy}
            onClick={() => void handleJson()}
          >
            <FileJson className="h-4 w-4" />
            JSON yüklə
          </Button>
          <Button
            type="button"
            className="gap-2"
            disabled={busy}
            onClick={() => void handlePdf()}
          >
            <Download className="h-4 w-4" />
            PDF yüklə
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
