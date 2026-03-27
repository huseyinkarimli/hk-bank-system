import { useState } from 'react';
import { motion } from 'framer-motion';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { AdminTableSkeleton } from '@/components/ui/skeleton';

export interface Column<T> {
  key: keyof T;
  label: string;
  render?: (value: unknown, row: T) => React.ReactNode;
  width?: string;
}

export interface RowAction<T> {
  label: string;
  onClick: (row: T) => void;
  variant?: 'default' | 'destructive' | 'outline';
  icon?: React.ReactNode;
}

interface DataTableProps<T extends Record<string, unknown>> {
  columns: Column<T>[];
  data: T[];
  actions?: RowAction<T>[];
  pageSize?: number;
  isLoading?: boolean;
  emptyMessage?: string;
}

export function DataTable<T extends Record<string, unknown>>({
  columns,
  data,
  actions,
  pageSize = 10,
  isLoading = false,
  emptyMessage = 'No data available',
}: DataTableProps<T>) {
  const [currentPage, setCurrentPage] = useState(1);

  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const paginatedData = data.slice(startIndex, endIndex);
  const totalPages = Math.max(1, Math.ceil(data.length / pageSize));

  if (isLoading) {
    return <AdminTableSkeleton rows={5} columns={columns.length + (actions?.length ? 1 : 0)} />;
  }

  return (
    <div className="space-y-4">
      <div className="overflow-x-auto rounded-lg border border-white/5">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/10 bg-white/[0.03]">
              {columns.map((col) => (
                <th key={String(col.key)} className="px-4 py-3 text-left font-semibold text-slate-300">
                  {col.label}
                </th>
              ))}
              {actions?.length ? (
                <th className="px-4 py-3 text-left font-semibold text-slate-300">Actions</th>
              ) : null}
            </tr>
          </thead>
          <tbody>
            {paginatedData.length === 0 ? (
              <tr>
                <td
                  colSpan={columns.length + (actions?.length ? 1 : 0)}
                  className="px-4 py-10 text-center text-slate-400"
                >
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              paginatedData.map((row, idx) => (
                <motion.tr
                  key={startIndex + idx}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: Math.min(idx * 0.03, 0.3) }}
                  className="border-b border-white/5 transition hover:bg-white/[0.04]"
                >
                  {columns.map((col) => (
                    <td key={String(col.key)} className="px-4 py-3 text-slate-300">
                      {col.render ? col.render(row[col.key], row) : String(row[col.key] ?? '')}
                    </td>
                  ))}
                  {actions?.length ? (
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        {actions.map((action, i) => (
                          <button
                            key={i}
                            type="button"
                            onClick={() => action.onClick(row)}
                            className="inline-flex items-center gap-1 rounded-md bg-cyan-600/90 px-2 py-1 text-xs font-medium text-white transition hover:bg-cyan-500"
                          >
                            {action.icon}
                            <span>{action.label}</span>
                          </button>
                        ))}
                      </div>
                    </td>
                  ) : null}
                </motion.tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center justify-between">
          <p className="text-sm text-slate-400">
            Page {currentPage} of {totalPages} ({data.length} total)
          </p>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              className="border-white/15 bg-transparent"
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              disabled={currentPage === 1}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="border-white/15 bg-transparent"
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              disabled={currentPage === totalPages}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
