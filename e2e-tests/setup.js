// Setup global para testes E2E
console.log('🔧 Configurando ambiente de testes E2E...');

// Aguardar serviços estarem prontos antes dos testes
beforeAll(async () => {
  console.log('⏳ Aguardando serviços estarem prontos...');
  
  // Aguardar um pouco para garantir que os serviços estejam rodando
  await new Promise(resolve => setTimeout(resolve, 3000));
  
  console.log('✅ Ambiente de testes E2E configurado');
});

afterAll(async () => {
  console.log('🧹 Limpando ambiente de testes E2E...');
});