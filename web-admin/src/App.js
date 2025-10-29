import React from 'react';
import { Layout, Menu } from 'antd';
import { DashboardOutlined, UserOutlined } from '@ant-design/icons';
import './App.css';

const { Header, Sider, Content } = Layout;

function App() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider>
        <div className="logo">
          <h2 style={{ color: 'white', padding: '16px' }}>PontualIoT</h2>
        </div>
        <Menu theme="dark" mode="inline">
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
            Bem-vindo ao PontualIoT
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}

export default App;