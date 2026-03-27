import { cn } from '@/lib/utils'

function Skeleton({ className, ...props }: React.ComponentProps<'div'>) {
  return (
    <div
      data-slot="skeleton"
      className={cn('bg-accent animate-pulse rounded-md', className)}
      {...props}
    />
  )
}

const shimmerBar = 'h-4 rounded-md bg-gradient-to-r from-slate-800 via-slate-600/40 to-slate-800 bg-[length:200%_100%] animate-shimmer'

/** Five-row table loading state for admin data tables (glass / dark theme). */
function AdminTableSkeleton({ rows = 5, columns = 6 }: { rows?: number; columns?: number }) {
  return (
    <div className="w-full space-y-2" aria-hidden>
      {Array.from({ length: rows }).map((_, i) => (
        <div
          key={i}
          className="flex gap-4 p-4 rounded-lg border border-white/5 bg-white/5 backdrop-blur-sm"
        >
          {Array.from({ length: columns }).map((_, j) => (
            <div key={j} className={cn(shimmerBar, 'flex-1 min-w-[4rem]')} />
          ))}
        </div>
      ))}
    </div>
  )
}

function ChartSkeleton({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        'w-full h-80 rounded-xl border border-white/10 bg-white/5 backdrop-blur-md bg-gradient-to-br from-slate-900/80 to-slate-950/80',
        className
      )}
    >
      <div className={cn('h-full w-full rounded-lg m-4', shimmerBar)} />
    </div>
  )
}

function AdminCardSkeleton({ count = 6 }: { count?: number }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {Array.from({ length: count }).map((_, i) => (
        <div
          key={i}
          className="h-32 rounded-xl border border-white/10 bg-white/5 backdrop-blur-md bg-gradient-to-br from-slate-900/60 to-slate-950/60"
        >
          <div className={cn(shimmerBar, 'm-6 h-8 w-2/3')} />
        </div>
      ))}
    </div>
  )
}

export { Skeleton, AdminTableSkeleton, ChartSkeleton, AdminCardSkeleton }
