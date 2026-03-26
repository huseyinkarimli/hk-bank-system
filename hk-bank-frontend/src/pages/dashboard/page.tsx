import { AnimatedBackground } from '@/components/animated-background';
import { ProtectedRoute } from '@/components/protected-route';
import { useAuth } from '@/context/auth-context';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';

function DashboardContent() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/auth/login');
  };

  const accountCards = [
    { label: 'Checking', balance: '$2,450.00', type: 'primary' },
    { label: 'Savings', balance: '$5,890.00', type: 'secondary' },
    { label: 'Credit Card', balance: '$1,200.00', type: 'accent' },
  ];

  const recentTransactions = [
    { id: 1, description: 'Grocery Store', amount: '-$45.50', date: 'Today' },
    { id: 2, description: 'Salary Deposit', amount: '+$3,000.00', date: 'Yesterday' },
    { id: 3, description: 'Netflix Subscription', amount: '-$15.99', date: '2 days ago' },
    { id: 4, description: 'Gas Station', amount: '-$52.30', date: '3 days ago' },
  ];

  return (
    <div className="min-h-screen">
      <AnimatedBackground />

      <div className="relative z-10 container mx-auto px-4 py-8 sm:py-12">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8"
        >
          <div>
            <h1 className="text-3xl sm:text-4xl font-bold text-white">
              Welcome back, {user?.firstName}!
            </h1>
            <p className="text-slate-400 mt-2">{user?.email}</p>
          </div>
          <Button
            onClick={handleLogout}
            variant="outline"
            className="bg-white/5 border-white/20 text-white hover:bg-white/10 hover:text-white"
          >
            Logout
          </Button>
        </motion.div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          {accountCards.map((card, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 * index }}
              whileHover={{ scale: 1.02 }}
            >
              <Card className="bg-white/5 backdrop-blur-md border-white/10 hover:border-blue-500/30 transition-all">
                <CardHeader>
                  <CardTitle className="text-slate-200">{card.label}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-2xl font-bold text-white">{card.balance}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
        >
          <Card className="bg-white/5 backdrop-blur-md border-white/10">
            <CardHeader>
              <CardTitle className="text-white">Recent Transactions</CardTitle>
              <CardDescription className="text-slate-400">
                Your latest account activity
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {recentTransactions.map((transaction) => (
                  <motion.div
                    key={transaction.id}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: 0.05 }}
                    className="flex items-center justify-between p-4 rounded-lg bg-white/5 border border-white/5 hover:border-white/10 transition-all"
                  >
                    <div>
                      <p className="font-semibold text-white">{transaction.description}</p>
                      <p className="text-sm text-slate-400">{transaction.date}</p>
                    </div>
                    <p
                      className={`font-bold ${
                        transaction.amount.startsWith('-') ? 'text-red-400' : 'text-green-400'
                      }`}
                    >
                      {transaction.amount}
                    </p>
                  </motion.div>
                ))}
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <DashboardContent />
    </ProtectedRoute>
  );
}
