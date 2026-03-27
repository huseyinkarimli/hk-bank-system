import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Progress } from '@/components/ui/progress';
import { Switch } from '@/components/ui/switch';
import { Badge } from '@/components/ui/badge';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Moon, Sun, Eye, EyeOff, Lock, AlertTriangle, Trash2, Volume2, VolumeX } from 'lucide-react';
import { useSoundFeedback } from '@/hooks/use-sound-feedback';
import { useAuth } from '@/context/auth-context';
import { useTheme } from '@/context/theme-context';
import { usePrivacy } from '@/context/privacy-context';
import { toast } from 'sonner';
import { DashboardLayout } from '@/components/dashboard/layout';

function ProfileContent() {
  const { user, logout } = useAuth();
  const { isDark, setDark } = useTheme();
  const { isPrivate, togglePrivate } = usePrivacy();
  const { enabled: soundEnabled, toggleSound } = useSoundFeedback();
  const navigate = useNavigate();

  const [firstName, setFirstName] = useState(user?.firstName ?? '');
  const [lastName, setLastName] = useState(user?.lastName ?? '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber ?? '');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [deactivateConfirm, setDeactivateConfirm] = useState('');
  const [showDeactivateDialog, setShowDeactivateDialog] = useState(false);
  const [language, setLanguage] = useState('AZ');
  const [notificationPrefs, setNotificationPrefs] = useState({
    transfer: true,
    payment: true,
    security: true,
  });

  useEffect(() => {
    if (!user) return;
    setFirstName(user.firstName ?? '');
    setLastName(user.lastName ?? '');
    setPhoneNumber(user.phoneNumber ?? '');
  }, [user]);

  const getRoleBadgeColor = (role?: string) => {
    switch (role) {
      case 'ADMIN':
        return 'bg-purple-500/20 text-purple-700 dark:text-purple-300';
      case 'AI_SUPPORT':
        return 'bg-cyan-500/20 text-cyan-700 dark:text-cyan-300';
      default:
        return 'bg-blue-500/20 text-blue-700 dark:text-blue-300';
    }
  };

  const calculatePasswordStrength = (password: string) => {
    let strength = 0;
    if (password.length >= 8) strength += 25;
    if (/[A-Z]/.test(password)) strength += 25;
    if (/[0-9]/.test(password)) strength += 25;
    if (/[^A-Za-z0-9]/.test(password)) strength += 25;
    return strength;
  };

  const handleSaveProfile = () => {
    toast.success('Profil yeniləndi');
  };

  const handleChangePassword = () => {
    if (newPassword !== confirmPassword) {
      toast.error('Parollar uyğun gəlmir');
      return;
    }
    if (calculatePasswordStrength(newPassword) < 50) {
      toast.error('Parol daha çətin olmalıdır');
      return;
    }
    toast.success('Parol dəyişdirildi');
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
  };

  const deactivateValid = deactivateConfirm.trim() === 'TƏSDİQLƏYİRƏM';

  const handleDeactivateAccount = () => {
    if (!deactivateValid) {
      toast.error('Yanlış təsdiq mətni');
      return;
    }
    toast.success('Hesab deaktivləşdirildi');
    logout();
    navigate('/auth/login', { replace: true });
  };

  const passwordStrength = calculatePasswordStrength(newPassword);
  const isAdmin = user?.role === 'ADMIN';

  return (
    <DashboardLayout isAdmin={isAdmin}>
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl md:text-4xl font-bold text-white mb-8">Profil</h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <Card className="lg:col-span-1 p-6 space-y-4 bg-slate-900/50 border-slate-700/50 backdrop-blur-sm">
            <div className="flex flex-col items-center">
              <div className="relative mb-4 flex h-28 w-28 items-center justify-center">
                <div
                  className="absolute inset-0 rounded-full bg-gradient-to-r from-violet-500 via-cyan-500 to-amber-400 opacity-90 blur-[1px] animate-[spin_8s_linear_infinite]"
                  aria-hidden
                />
                <Avatar className="relative h-24 w-24 ring-4 ring-slate-800">
                  <AvatarFallback className="bg-slate-800 text-lg font-bold text-white">
                    {(firstName[0] ?? '?').toUpperCase()}
                    {(lastName[0] ?? '?').toUpperCase()}
                  </AvatarFallback>
                </Avatar>
              </div>
              <h2 className="text-2xl font-bold text-white text-center">
                {firstName} {lastName}
              </h2>
              <p className="text-sm text-slate-400 text-center mt-1">{user?.email}</p>
            </div>

            <div className="border-t border-slate-700 pt-4 space-y-3">
              <div>
                <p className="text-xs font-medium text-slate-500 mb-1">Rol</p>
                <Badge className={getRoleBadgeColor(user?.role)}>
                  {user?.role === 'ADMIN'
                    ? 'İnzibatçı'
                    : user?.role === 'AI_SUPPORT'
                      ? 'AI dəstəyi'
                      : 'İstifadəçi'}
                </Badge>
              </div>
              <div>
                <p className="text-xs font-medium text-slate-500 mb-1">Üzvlük tarixi</p>
                <p className="text-sm font-semibold text-slate-200">2024-01-15</p>
              </div>
              <div>
                <p className="text-xs font-medium text-slate-500 mb-1">Status</p>
                <Badge
                  variant="outline"
                  className="bg-green-500/20 text-green-300 border-green-500/30"
                >
                  Aktiv
                </Badge>
              </div>
            </div>
          </Card>

          <div className="lg:col-span-2">
            <Tabs defaultValue="data" className="w-full">
              <TabsList className="grid w-full grid-cols-3 mb-6 bg-slate-900/80">
                <TabsTrigger value="data">Məlumatlar</TabsTrigger>
                <TabsTrigger value="security">Təhlükəsizlik</TabsTrigger>
                <TabsTrigger value="appearance">Görünüş</TabsTrigger>
              </TabsList>

              <TabsContent value="data">
                <Card className="p-6 space-y-4 bg-slate-900/50 border-slate-700/50">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label>Ad</Label>
                      <Input
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        className="mt-1 bg-slate-950/50"
                      />
                    </div>
                    <div>
                      <Label>Soyad</Label>
                      <Input
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        className="mt-1 bg-slate-950/50"
                      />
                    </div>
                  </div>
                  <div>
                    <Label>E-poçt (dəyişdirilə bilməz)</Label>
                    <Input
                      value={user?.email ?? ''}
                      disabled
                      className="mt-1 bg-slate-950/50"
                    />
                  </div>
                  <div>
                    <Label>Telefon nömrəsi</Label>
                    <div className="flex mt-1">
                      <span className="inline-flex items-center px-3 bg-slate-800 border border-r-0 border-slate-600 rounded-l-md text-slate-400">
                        +994
                      </span>
                      <Input
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                        className="rounded-l-none bg-slate-950/50"
                        placeholder="51 234 56 78"
                      />
                    </div>
                  </div>
                  <Button onClick={handleSaveProfile} className="w-full">
                    Yadda saxla
                  </Button>
                </Card>
              </TabsContent>

              <TabsContent value="security">
                <div className="space-y-4">
                  <Card className="p-6 space-y-4 bg-slate-900/50 border-slate-700/50">
                    <h3 className="font-semibold flex items-center gap-2 text-white">
                      <Lock size={18} />
                      Parolu dəyişdir
                    </h3>
                    <div>
                      <Label>Cari parol</Label>
                      <Input
                        type="password"
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                        className="mt-1 bg-slate-950/50"
                      />
                    </div>
                    <div>
                      <Label>Yeni parol</Label>
                      <Input
                        type="password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        className="mt-1 bg-slate-950/50"
                      />
                      {newPassword ? (
                        <div className="mt-2 space-y-1">
                          <Progress value={passwordStrength} className="h-1" />
                          <p className="text-xs text-slate-400">
                            Parol gücü:{' '}
                            {passwordStrength < 50
                              ? 'Zəif'
                              : passwordStrength < 75
                                ? 'Orta'
                                : 'Güclü'}
                          </p>
                        </div>
                      ) : null}
                    </div>
                    <div>
                      <Label>Parolu təsdiqlə</Label>
                      <Input
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className="mt-1 bg-slate-950/50"
                      />
                    </div>
                    <Button
                      onClick={handleChangePassword}
                      disabled={!currentPassword || !newPassword || !confirmPassword}
                      className="w-full"
                    >
                      Parolu dəyişdir
                    </Button>
                  </Card>

                  <Card className="p-6 space-y-4 border-red-500/40 bg-red-950/20">
                    <h3 className="font-semibold flex items-center gap-2 text-red-400">
                      <AlertTriangle size={18} />
                      Təhlükəli zona
                    </h3>
                    <p className="text-sm text-slate-400">
                      Bu əməliyyat geri alına bilməz. Hesabınız qalıcı olaraq bağlanacaq.
                    </p>
                    <Button
                      variant="destructive"
                      onClick={() => setShowDeactivateDialog(true)}
                      className="w-full"
                    >
                      <Trash2 size={16} className="mr-2" />
                      Hesabı deaktiv et
                    </Button>
                  </Card>
                </div>
              </TabsContent>

              <TabsContent value="appearance">
                <div className="space-y-4">
                  <Card className="p-6 space-y-4 bg-slate-900/50 border-slate-700/50">
                    <h3 className="font-semibold text-white">Tema</h3>
                    <div className="flex items-center justify-between p-3 bg-slate-950/50 rounded-lg">
                      <div className="flex items-center gap-3">
                        {isDark ? <Moon size={20} className="text-slate-300" /> : <Sun size={20} />}
                        <span className="font-medium text-slate-200">
                          {isDark ? 'Tünd' : 'Açıq'} tema
                        </span>
                      </div>
                      <Switch
                        checked={isDark}
                        onCheckedChange={(checked) => setDark(checked)}
                      />
                    </div>
                  </Card>

                  <Card className="p-6 space-y-4 bg-slate-900/50 border-slate-700/50">
                    <h3 className="font-semibold text-white">Məxfilik</h3>
                    <div className="flex items-center justify-between p-3 bg-slate-950/50 rounded-lg">
                      <div className="flex items-center gap-3">
                        {isPrivate ? (
                          <EyeOff size={20} className="text-slate-300" />
                        ) : (
                          <Eye size={20} />
                        )}
                        <span className="font-medium text-slate-200">
                          {isPrivate ? 'Balanslar gizlidir' : 'Balanslar görünür'}
                        </span>
                      </div>
                      <Switch checked={isPrivate} onCheckedChange={() => togglePrivate()} />
                    </div>
                  </Card>

                  <Card className="p-6 space-y-4 bg-slate-900/50 border-slate-700/50">
                    <h3 className="font-semibold text-white">Səs effektləri</h3>
                    <div className="flex items-center justify-between p-3 bg-slate-950/50 rounded-lg">
                      <div className="flex items-center gap-3">
                        {soundEnabled ? (
                          <Volume2 size={20} className="text-slate-300" />
                        ) : (
                          <VolumeX size={20} className="text-slate-500" />
                        )}
                        <span className="font-medium text-slate-200">
                          {soundEnabled ? 'Səs aktivdir' : 'Səs söndürülüb'}
                        </span>
                      </div>
                      <Switch checked={soundEnabled} onCheckedChange={(v) => toggleSound(v)} />
                    </div>
                  </Card>

                  <Card className="p-6 space-y-4 bg-slate-900/50 border-slate-700/50">
                    <h3 className="font-semibold text-white">Dil</h3>
                    <div className="grid grid-cols-3 gap-3">
                      {['AZ', 'EN', 'RU'].map((lang) => (
                        <Card
                          key={lang}
                          onClick={() => setLanguage(lang)}
                          className={`p-4 text-center cursor-pointer transition-all border-slate-700 ${
                            language === lang
                              ? 'ring-2 ring-cyan-500 bg-cyan-500/10'
                              : 'hover:border-cyan-500/40'
                          }`}
                        >
                          <p className="font-semibold text-white">{lang}</p>
                        </Card>
                      ))}
                    </div>
                  </Card>

                  <Card className="p-6 space-y-4 bg-slate-900/50 border-slate-700/50">
                    <h3 className="font-semibold text-white">Bildirişlər</h3>
                    <div className="space-y-3">
                      <div className="flex items-center justify-between p-3 bg-slate-950/50 rounded-lg">
                        <span className="font-medium text-slate-200">Köçürmə bildirişləri</span>
                        <Switch
                          checked={notificationPrefs.transfer}
                          onCheckedChange={(checked) =>
                            setNotificationPrefs({ ...notificationPrefs, transfer: checked })
                          }
                        />
                      </div>
                      <div className="flex items-center justify-between p-3 bg-slate-950/50 rounded-lg">
                        <span className="font-medium text-slate-200">Ödəniş bildirişləri</span>
                        <Switch
                          checked={notificationPrefs.payment}
                          onCheckedChange={(checked) =>
                            setNotificationPrefs({ ...notificationPrefs, payment: checked })
                          }
                        />
                      </div>
                      <div className="flex items-center justify-between p-3 bg-slate-950/50 rounded-lg">
                        <span className="font-medium text-slate-200">Təhlükəsizlik xəbərdarlıqları</span>
                        <Switch
                          checked={notificationPrefs.security}
                          onCheckedChange={(checked) =>
                            setNotificationPrefs({ ...notificationPrefs, security: checked })
                          }
                        />
                      </div>
                    </div>
                  </Card>
                </div>
              </TabsContent>
            </Tabs>
          </div>
        </div>
      </div>

      <AlertDialog open={showDeactivateDialog} onOpenChange={setShowDeactivateDialog}>
        <AlertDialogContent className="bg-slate-900 border-slate-700">
          <AlertDialogHeader>
            <AlertDialogTitle className="text-white">Hesabı deaktiv etmək istəyirsiniz?</AlertDialogTitle>
            <AlertDialogDescription className="text-slate-400">
              Bu əməliyyat geri alına bilməz. Davam etmək üçün aşağıdakı sözü dəqiq yazın.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <p className="text-sm font-medium text-slate-200">Təsdiq: TƏSDİQLƏYİRƏM</p>
          <Input
            value={deactivateConfirm}
            onChange={(e) => setDeactivateConfirm(e.target.value)}
            placeholder="TƏSDİQLƏYİRƏM"
            className="bg-slate-950/50 uppercase"
          />
          <AlertDialogFooter>
            <AlertDialogCancel className="border-slate-600">İmtina</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => handleDeactivateAccount()}
              className="bg-red-600 text-white hover:bg-red-600/90 disabled:opacity-50"
              disabled={!deactivateValid}
            >
              Deaktiv et
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </DashboardLayout>
  );
}

export default function ProfilePage() {
  return <ProfileContent />;
}
