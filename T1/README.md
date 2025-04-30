# 🧪 Simulador de Rede de Filas (Java)

Este projeto simula uma **rede de filas com roteamento probabilístico**, utilizando eventos de chegada externa, passagem entre filas e saída do sistema.

---

## 📁 Estrutura do Projeto

```
├── RedeFilas.java        // Classe principal com a simulação completa
├── Fila.java             // Classe que representa cada fila da rede
```

---

## ⚙️ Requisitos

- Java 8 ou superior
- Terminal de comandos ou IDE (ex: VS Code, IntelliJ, Eclipse)

---

## ▶️ Como Rodar

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

## 📊 Saída Esperada

Ao rodar, você verá no console:

- O **tempo total de simulação**
- Para cada fila:
  - Número de **perdas**
  - **Distribuição de tempo** por estado (quantos clientes estavam presentes)
  - **Probabilidade (%)** de cada estado

---

## 🛠️ Configuração das Filas e Roteamento

As filas e o roteamento estão definidos no método `main()` da classe `RedeFilas`. Para alterar a simulação:

### 🔁 Exemplo de configuração:

```java
filas.add(new Fila(1, Integer.MAX_VALUE, 2, 4, 1, 2)); // Fila 1: chegada externa
filas.add(new Fila(2, 5, 0, 0, 4, 8));                 // Fila 2
filas.add(new Fila(2, 10, 0, 0, 5, 15));               // Fila 3

roteamentos.put(0, Arrays.asList(                    // Roteamento a partir da Fila 1
    new Roteamento(1, 0.5),
    new Roteamento(2, 0.5)
));
roteamentos.put(1, List.of(new Roteamento(-1, 1.0))); // Fila 2 → saída
roteamentos.put(2, List.of(new Roteamento(-1, 1.0))); // Fila 3 → saída
```

---

## 📌 Observações

- O simulador roda até **100.000 eventos aleatórios**.
- A **Fila 1** é a única com chegada externa.
- `-1` como destino no roteamento indica **saída do sistema**.

---

## 👩‍💻 Autores

- Carolina Ferreira
- Mateus Caçabuena

Projeto desenvolvido para a disciplina **Simulação e Métodos Analíticos** | PUCRS