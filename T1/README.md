## 👩‍💻 Autores

- Carolina Ferreira
- Mateus Caçabuena

Projeto desenvolvido para a disciplina Simulação e Métodos Analíticos | PUCRS

Este projeto simula uma rede de filas com roteamento probabilístico, utilizando eventos de chegada externa, passagem entre filas e saída do sistema.

---

## Requisitos

- Java 8 ou superior
- Terminal de comandos ou IDE (ex: VS Code, IntelliJ, Eclipse)

---

## Como Rodar

### 1. **Compile os arquivos**

No terminal (dentro da pasta do projeto):

```bash
javac Fila.java RedeFilas.java
```

### 2. **Execute a simulação**

```bash
java RedeFilas
```

---

## Saída Esperada

Ao rodar, você verá no console:

- O tempo total de simulação
- Para cada fila:
  - Número de perdas
  - Distribuição de tempo por estado (quantos clientes estavam presentes)
  - Probabilidade (%) de cada estado

---

## Observações

- O simulador roda até 100.000 eventos aleatórios.
- A Fila 1 é a única com chegada externa.
- `-1` como destino no roteamento indica saída do sistema.
