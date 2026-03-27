import { ActionModal } from '@/components/admin/modals';

interface BanModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (reason: string) => Promise<void>;
  isLoading?: boolean;
  userLabel?: string;
}

export function BanModal({ isOpen, onClose, onSubmit, isLoading, userLabel }: BanModalProps) {
  return (
    <ActionModal
      isOpen={isOpen}
      onClose={onClose}
      isLoading={isLoading}
      title={`Ban user${userLabel ? ` — ${userLabel}` : ''}`}
      fields={[
        {
          name: 'reason',
          label: 'Reason',
          type: 'textarea',
          placeholder: 'Enter reason for banning this user…',
        },
      ]}
      onSubmit={async (data) => {
        await onSubmit(data.reason);
      }}
    />
  );
}
