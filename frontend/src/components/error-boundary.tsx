import React, { type ReactNode } from 'react';
import { AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('ErrorBoundary:', error, errorInfo);
  }

  resetError = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      return (
        this.props.fallback || (
          <div className="min-h-[50vh] flex items-center justify-center px-4 py-12">
            <div className="max-w-md w-full space-y-4 text-center rounded-2xl border border-slate-700/60 bg-slate-900/60 p-8">
              <div className="flex justify-center">
                <div className="rounded-full bg-red-500/15 p-3">
                  <AlertCircle className="w-8 h-8 text-red-400" />
                </div>
              </div>
              <h2 className="text-xl font-bold text-white">Xəta baş verdi</h2>
              <p className="text-sm text-slate-400">
                {this.state.error?.message || 'Gözlənilməz xəta baş verdi. Səhifəni yeniləməyi sınayın.'}
              </p>
              <Button type="button" onClick={this.resetError} className="w-full">
                Yenidən cəhd et
              </Button>
            </div>
          </div>
        )
      );
    }

    return this.props.children;
  }
}
