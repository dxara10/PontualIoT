# Wireframes e Fluxos de Interface

## 🖥️ Web Admin - Principais Telas

### 1. Dashboard Principal
```
┌─────────────────────────────────────────────────────────┐
│ [Logo] Smart Attendance    [User Menu] [Notifications] │
├─────────────────────────────────────────────────────────┤
│ Dashboard | Devices | Employees | Reports | Settings    │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│ │ Devices     │ │ Events      │ │ Employees   │        │
│ │ Online: 45  │ │ Today: 1.2k │ │ Active: 150 │        │
│ │ Offline: 3  │ │ This Week:  │ │ On Leave: 5 │        │
│ └─────────────┘ └─────────────┘ └─────────────┘        │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Device Status Map                                   │ │
│ │ [Interactive map showing device locations/status]   │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Recent Events                                       │ │
│ │ 08:15 - João Silva - CHECK_IN - Device A1          │ │
│ │ 08:12 - Maria Santos - CHECK_IN - Device B2        │ │
│ │ 08:10 - Pedro Costa - CHECK_OUT - Device A1        │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 2. Gestão de Dispositivos
```
┌─────────────────────────────────────────────────────────┐
│ Devices                                    [+ New Device]│
├─────────────────────────────────────────────────────────┤
│ [Search] [Filter: All/Online/Offline] [Export]          │
├─────────────────────────────────────────────────────────┤
│ Serial    │ Model    │ Location │ Status │ Last Seen    │
├─────────────────────────────────────────────────────────┤
│ DEV001    │ BioMax   │ Entrance │ 🟢     │ 2 min ago    │
│ DEV002    │ CardRead │ Exit     │ 🔴     │ 15 min ago   │
│ DEV003    │ BioMax   │ Floor 2  │ 🟢     │ 1 min ago    │
├─────────────────────────────────────────────────────────┤
│                                        [Prev] [1] [Next]│
└─────────────────────────────────────────────────────────┘

Modal - Device Details:
┌─────────────────────────────────────────┐
│ Device Details                    [X]   │
├─────────────────────────────────────────┤
│ Serial: DEV001                          │
│ Model: BioMax Pro                       │
│ Firmware: v2.1.3                       │
│ IP: 192.168.1.100                      │
│ Location: Main Entrance                 │
│ Status: 🟢 Online                       │
│                                         │
│ [Send Command ▼] [Edit] [Deactivate]    │
│                                         │
│ Recent Commands:                        │
│ • SYNC_TIME - Success (2h ago)          │
│ • REBOOT - Success (1d ago)             │
└─────────────────────────────────────────┘
```

### 3. Relatórios de Ponto
```
┌─────────────────────────────────────────────────────────┐
│ Attendance Reports                                      │
├─────────────────────────────────────────────────────────┤
│ Employee: [All ▼] Period: [Jan 2024 ▼] [Generate]      │
│ Device: [All ▼]   Format: [Table ▼]    [Export CSV]    │
├─────────────────────────────────────────────────────────┤
│ Employee      │ Date     │ Check In │ Check Out │ Hours │
├─────────────────────────────────────────────────────────┤
│ João Silva    │ 15/01    │ 08:00    │ 17:30     │ 8.5h  │
│ João Silva    │ 16/01    │ 08:15    │ 17:45     │ 8.5h  │
│ Maria Santos  │ 15/01    │ 09:00    │ 18:00     │ 8.0h  │
├─────────────────────────────────────────────────────────┤
│ Summary: Total Hours: 156.5h | Avg: 8.2h/day          │
└─────────────────────────────────────────────────────────┘
```

## 📱 Mobile App - Fluxos Principais

### 1. Login
```
┌─────────────────┐
│                 │
│   [App Logo]    │
│                 │
│ ┌─────────────┐ │
│ │ Username    │ │
│ └─────────────┘ │
│ ┌─────────────┐ │
│ │ Password    │ │
│ └─────────────┘ │
│                 │
│   [  LOGIN  ]   │
│                 │
│ Forgot Password?│
└─────────────────┘
```

### 2. Dashboard Funcionário
```
┌─────────────────┐
│ Hello, João!    │
├─────────────────┤
│                 │
│ Today's Status  │
│ ┌─────────────┐ │
│ │ Check In    │ │
│ │ 08:15       │ │
│ └─────────────┘ │
│ ┌─────────────┐ │
│ │ Working     │ │
│ │ 4h 23m      │ │
│ └─────────────┘ │
│                 │
│ [ CHECK OUT ]   │
│                 │
│ Recent History  │
│ • 14/01 - 8.5h  │
│ • 13/01 - 8.0h  │
│ • 12/01 - 8.2h  │
└─────────────────┘
```

## 🔄 Fluxos de Navegação

### Web Admin Flow
```
Login → Dashboard → [Devices|Employees|Reports]
                 ↓
              Device Details → Send Command
                            → Edit Device
                 ↓
              Employee List → Employee Details
                           → Edit Employee
                 ↓
              Reports → Filter → Export
```

### Mobile Flow
```
Login → Dashboard → Check In/Out
              ↓
           History → Details
              ↓
           Profile → Settings
```

## 🎨 Design System

### Cores
- **Primary**: #2563eb (Blue)
- **Success**: #16a34a (Green) 
- **Warning**: #ea580c (Orange)
- **Error**: #dc2626 (Red)
- **Neutral**: #64748b (Gray)

### Componentes
- **Cards**: Bordas arredondadas, sombra sutil
- **Buttons**: Primary/Secondary/Outline variants
- **Status Indicators**: Círculos coloridos (🟢🔴🟡)
- **Tables**: Zebra striping, hover effects
- **Forms**: Floating labels, validation states