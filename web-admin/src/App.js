import React, { useState } from 'react';
import { Layout, Menu, Table, Tag, Button, Space } from 'antd';
import { DashboardOutlined, UserOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import EmployeeList from './components/EmployeeList';
import AttendanceList from './components/AttendanceList';
import './App.css';

// Dados mockados para garantir que apareçam
const mockEmployees = [
  { id: 1, name: 'João Silva', email: 'joao@pontualiot.com', rfidTag: 'RFID001', active: true },
  { id: 2, name: 'Maria Santos', email: 'maria@pontualiot.com', rfidTag: 'RFID002', active: true },
  { id: 3, name: 'Carlos Lima', email: 'carlos@pontualiot.com', rfidTag: 'RFID003', active: false },
  { id: 4, name: 'Ana Costa', email: 'ana@pontualiot.com', rfidTag: 'RFID004', active: true },
  { id: 5, name: 'Pedro Oliveira', email: 'pedro@pontualiot.com', rfidTag: 'RFID005', active: true }
];

const mockAttendances = [
  { id: 1, employee: { name: 'João Silva' }, date: '2025-10-29', checkIn: '2025-10-29T08:00:00', checkOut: '2025-10-29T17:00:00' },
  { id: 2, employee: { name: 'Maria Santos' }, date: '2025-10-29', checkIn: '2025-10-29T08:15:00', checkOut: '2025-10-29T17:30:00' },
  { id: 3, employee: { name: 'Ana Costa' }, date: '2025-10-29', checkIn: '2025-10-29T07:45:00', checkOut: null }
];

const { Header, Sider, Content } = Layout;

function App() {
  const [selectedKey, setSelectedKey] = useState('1');

  const employeeColumns = [
    { title: 'Nome', dataIndex: 'name', key: 'name' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'RFID Tag', dataIndex: 'rfidTag', key: 'rfidTag' },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      render: (active) => (
        <Tag color={active ? 'green' : 'red'}>
          {active ? 'Ativo' : 'Inativo'}
        </Tag>
      ),
    },
    {
      title: 'Ações',
      key: 'actions',
      render: () => (
        <Space size="middle">
          <Button icon={<EditOutlined />} size="small" />
          <Button icon={<DeleteOutlined />} size="small" danger />
        </Space>
      ),
    },
  ];

  const attendanceColumns = [
    { title: 'Funcionário', dataIndex: ['employee', 'name'], key: 'employee' },
    { title: 'Data', dataIndex: 'date', key: 'date' },
    {
      title: 'Entrada',
      dataIndex: 'checkIn',
      key: 'checkIn',
      render: (checkIn) => checkIn ? new Date(checkIn).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '-'
    },
    {
      title: 'Saída',
      dataIndex: 'checkOut',
      key: 'checkOut',
      render: (checkOut) => checkOut ? new Date(checkOut).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '-'
    }
  ];

  const renderContent = () => {
    switch (selectedKey) {
      case '1':
        return (
          <div>
            <h2>Dashboard - Registros de Ponto</h2>
            <Table
              columns={attendanceColumns}
              dataSource={mockAttendances}
              rowKey="id"
              pagination={{ pageSize: 10 }}
            />
          </div>
        );
      case '2':
        return (
          <div>
            <h2>Gestão de Funcionários</h2>
            <Table
              columns={employeeColumns}
              dataSource={mockEmployees}
              rowKey="id"
              pagination={{ pageSize: 10 }}
            />
          </div>
        );
      default:
        return <div>Bem-vindo ao PontualIoT</div>;
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider>
        <div className="logo">
          <h2 style={{ color: 'white', padding: '16px' }}>PontualIoT</h2>
        </div>
        <Menu 
          theme="dark" 
          mode="inline" 
          selectedKeys={[selectedKey]}
          onClick={({ key }) => setSelectedKey(key)}
        >
          <Menu.Item key="1" icon={<DashboardOutlined />}>
            Dashboard
          </Menu.Item>
          <Menu.Item key="2" icon={<UserOutlined />}>
            Funcionários
          </Menu.Item>
        </Menu>
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 16px' }}>
          <h1>Sistema de Ponto Digital</h1>
        </Header>
        <Content style={{ margin: '16px' }}>
          <div style={{ padding: 24, background: '#fff', minHeight: 360 }}>
            {renderContent()}
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}

export default App;