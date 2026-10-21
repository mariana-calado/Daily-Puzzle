# Daily Puzzle

Hub de mini-jogos diários de raciocínio e palavras, no estilo Wordle / NYT Games. Todo dia há um desafio novo em cada jogo, igual para todos os jogadores, com sequência (streak), estatísticas e histórico. O app funciona 100% offline.

## Jogos

| Jogo | Como funciona |
|---|---|
| Termo | Descubra a palavra de 5 letras em 6 tentativas (verde, amarelo e cinza) |
| Caça-palavras | Encontre as palavras escondidas na grade arrastando o dedo (horizontal, vertical e diagonal) |
| Hashtag | Reordene as letras arrastando até formar as palavras, com número limitado de movimentos |
| Sudoku | Grade 9x9 gerada para o dia, com contagem de erros |
| Cruzadinha Mini | Cruzadinha pequena com dicas horizontais e verticais |

Funcionalidades comuns a todos:

- **Desafio do dia:** o conteúdo é escolhido pela data, então é o mesmo para todo mundo no mesmo dia.
- **Um resultado por jogo por dia:** a regra é garantida no banco (índice único em jogo + data).
- **Jogos anteriores:** nas Estatísticas, tocar num dia vazio abre o desafio daquela data.
- **Já jogou?** Ao abrir um jogo já concluído, o app mostra se o jogador venceu ou perdeu, com **Jogar novamente** (modo treino, que não altera a sequência) e acesso ao resultado completo.
- **Apagar histórico:** nas Estatísticas, cada jogo tem uma lixeira (com confirmação) que remove os resultados e a estatística daquele jogo.
- **Sequência de vitórias:** recalculada a partir do histórico, então preencher dias antigos não a distorce.

## Arquitetura

O app segue o padrão **MVVM** em camadas, com fluxo de dados unidirecional (o estado desce, os eventos sobem):

```
View (Composables)  ──observa──▶  ViewModel (StateFlow)  ──▶  Repository  ──▶  Room (DAO + Database)
      ▲                                  │
      └────────── eventos (onXxx) ───────┘
```

- **View:** telas em Jetpack Compose com Material 3. Cada tela recebe o estado pronto e funções de evento; não conhece banco nem navegação.
- **ViewModel:** expõe o estado como `StateFlow` imutável. Só o ViewModel altera o estado.
- **Repository:** única porta de entrada para os dados. `GameRepository` fala com o Room; `WordSearchRepository` entrega o conteúdo do Caça-palavras.
- **Model (Room):** `GameProgressEntity` (um registro por jogo e dia) e `GameStatsEntity` (um registro por jogo), acessados por `ProgressDao` e `StatsDao`.
- **Injeção de dependência:** Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, módulo `DatabaseModule`).
- **Navegação:** Navigation Compose, com as rotas centralizadas em `Routes.kt` e argumentos de jogo e data.

### Estrutura de pastas

```
br.pucpr.dailypuzzle
├── DailyPuzzleApp.kt, MainActivity.kt
├── data
│   ├── local          AppDatabase, dao/, entity/        (Room)
│   ├── repository     GameRepository, WordSearchRepository
│   └── content        carregador e gerador do Caça-palavras
├── di                 DatabaseModule                    (Hilt)
├── model              Game, DailySeed, estados dos jogos
├── navigation         NavGraph, Routes
├── ui
│   ├── home           Home e seu ViewModel
│   ├── games          termo/, cacapalavras/, hashtag/, sudoku/, cruzadinha/
│   ├── result         tela de resultado compartilhada
│   ├── stats          estatísticas e histórico
│   ├── common         componentes reutilizáveis
│   └── theme          cores, tipografia, formas
└── util               DateUtils e bancos de conteúdo diário
```

## Persistência (Room): CRUD

| Operação | Onde acontece na interface | Código |
|---|---|---|
| Criar | Ao terminar um jogo, o resultado é gravado | `GameRepository.saveResult` -> `ProgressDao.insert` |
| Ler | Home (status do dia), Estatísticas, Resultado e "já jogou" | `observeTodayProgress`, `observeStats`, `getProgress` (consultas `@Query` com `Flow`) |
| Atualizar | Estatísticas e sequência recalculadas a cada resultado | `StatsDao.upsert` |
| Apagar | Lixeira de cada jogo nas Estatísticas | `GameRepository.clearHistory` -> `deleteByGame` nos dois DAOs |

## Decisões de design

- **Room como fonte da verdade (SSOT):** a Home e as Estatísticas observam o banco por `Flow` e se atualizam sozinhas quando um jogo grava um resultado.
- **Hilt em vez de fábrica manual:** menos código repetido nos ViewModels e dependências validadas em tempo de compilação.
- **Sequência calculada pelo histórico:** permite jogar dias anteriores fora de ordem sem corromper o contador.
- **Visual:** Material 3 com paleta própria, fonte Poppins embutida, ícones Material Symbols em vetor (sem biblioteca extra) e cor própria para cada jogo.
- **Telas sem lógica de navegação:** recebem lambdas (`onBack`, `onShowResult`) e quem decide o destino é o `NavGraph`.

## Divisão do trabalho

| Pessoa | Responsabilidade |
|---|---|
| Mariana | Home, navegação, Room (entidades, DAOs, repositório), estatísticas, tela de resultado, tema e identidade visual, Caça-palavras |
| Mateus | Jogos Termo, Sudoku, Hashtag e Cruzadinha Mini e seus bancos de conteúdo |

## Tecnologias

Kotlin 2.2, Jetpack Compose (BOM 2026.02.01) com Material 3, Navigation Compose 2.10, Room 2.8 (KSP), Hilt 2.60, Coroutines e Flow. `minSdk` 26, `targetSdk` 37.

## Como rodar

1. Abra o projeto no Android Studio e aguarde a sincronização do Gradle.
2. Escolha um emulador ou aparelho com Android 8.0 (API 26) ou superior.
3. Execute o módulo `app`.

Pela linha de comando:

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Testes

- `WordSearchGeneratorTest`: garante que as 6 palavras sempre cabem na grade, em 500 combinações de tema e data.
- `GameRepositoryStreakTest`: cobre as regras da sequência (dias seguidos, dia antigo isolado, derrota e resultado duplicado) e a exclusão do histórico.
- `ModoTreinoTest`: usa os ViewModels reais com banco falso para conferir que vencer salva o resultado, que um jogo já jogado mostra o resultado anterior, que "Jogar novamente" não altera o que foi salvo e que dias passados usam a data da rota.
